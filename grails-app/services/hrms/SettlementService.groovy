package hrms

import grails.gorm.transactions.Transactional

import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Day 9: চূড়ান্ত নিষ্পত্তি (full & final)।
 *
 * হিসাবের নিয়ম (শেষ কর্মদিবসের তারিখ ধরে; দৈনিক হার = gross / ৩০):
 *  - বকেয়া বেতন: শেষ কর্মদিবসের মাসের বেতন চূড়ান্ত পে-রোলে না থাকলে, মাসের শুরু থেকে শেষ দিন পর্যন্ত দিন × (gross / মাসের দিন)।
 *  - ছুটি নগদায়ন: carry-forward যোগ্য ছুটির ধরনের (যেমন বার্ষিক) বাকি ব্যালেন্স × দৈনিক হার।
 *  - গ্র্যাচুইটি: পূর্ণ চাকরি-বছর ≥ পে-রোলের যোগ্যতার বছর (PayrollService) হলে বেসিক × পূর্ণ বছর।
 *  - অনুমোদিত কিন্তু অপরিশোধিত খরচের দাবি যোগ; চলমান ঋণের বাকি কিস্তি বাদ।
 *  - কম নোটিস (মওকুফ না হলে, কর্মীর নিজের পদত্যাগে): ঘাটতি দিন × দৈনিক হার বাদ।
 *  - ধাপ: DRAFT (HR হিসাব/সমন্বয়) -> APPROVED (হিসাব বিভাগ/Admin) -> PAID (হিসাব বিভাগ/Admin)।
 */
@Transactional
class SettlementService {

    static final BigDecimal DAYS_IN_MONTH_FOR_RATE = 30.0

    def accessService
    def salaryService
    def loanService
    def leaveService
    def notificationService
    def auditService

    private static LocalDate today() { LocalDate.now(AttendanceService.ZONE) }

    private static LocalDateTime now() { LocalDateTime.now(AttendanceService.ZONE) }

    private static Map fail(String m) { [ok: false, message: m] }

    private static BigDecimal r0(BigDecimal v) { (v ?: 0.0).setScale(0, RoundingMode.HALF_UP) }

    private String actor() { accessService.currentUser()?.username ?: 'system' }

    private boolean isAccounts() { AccessService.has('ROLE_ADMIN,ROLE_ACCOUNTANT') }

    private boolean isOwn(FinalSettlement fs) { accessService.currentEmployee()?.id == fs.employee.id }

    // ------------------------------------------------------------------ তথ্য
    FinalSettlement settlementOf(Separation s) {
        s == null ? null : FinalSettlement.findBySeparation(s)
    }

    /** হিসাব বিভাগ/Admin এর কাজের অপেক্ষায় (অনুমোদন বা পরিশোধ) */
    List<FinalSettlement> pendingForAccounts() {
        if (!isAccounts()) return []
        FinalSettlement.createCriteria().list {
            inList('status', ['DRAFT', 'APPROVED'])
            separation {
                eq('status', 'ACCEPTED')
            }
            order('id', 'desc')
        } as List<FinalSettlement>
    }

    // ------------------------------------------------------------------ হিসাব তৈরি (HR)
    Map prepare(Separation s) {
        if (!accessService.isHrAdmin() || !accessService.canManage(s.employee)) return fail('হিসাব তৈরির অনুমতি আপনার নেই।')
        if (s.status != 'ACCEPTED') return fail('শুধু গৃহীত কেসের নিষ্পত্তি হিসাব করা যায়।')
        FinalSettlement fs = FinalSettlement.findBySeparation(s)
        if (fs != null && fs.status != 'DRAFT') return fail('অনুমোদিত নিষ্পত্তি আর বদলানো যায় না।')

        Employee emp = s.employee
        LocalDate lwd = s.lastWorkingDay ?: s.requestedLastDay
        BigDecimal daily = null

        SalaryStructure st = salaryService.structureFor(emp, lwd)
        BigDecimal basic = st.basic
        BigDecimal gross = st.gross
        daily = gross.divide(DAYS_IN_MONTH_FOR_RATE, 6, RoundingMode.HALF_UP)

        // বকেয়া বেতন
        boolean monthPaid = PayrollLine.executeQuery(
                "select count(l) from PayrollLine l where l.employee = :e and l.payrollRun.payYear = :y and l.payrollRun.payMonth = :m and l.payrollRun.status = 'FINALIZED'",
                [e: emp, y: lwd.year, m: lwd.monthValue])[0] > 0
        int salaryDays = monthPaid ? 0 : lwd.dayOfMonth
        BigDecimal unpaidSalary = r0(gross.divide(lwd.lengthOfMonth() as BigDecimal, 6, RoundingMode.HALF_UP) * salaryDays)

        // ছুটি নগদায়ন
        BigDecimal leaveDays = 0.0
        leaveService.ensureBalances(emp, lwd.year).each { LeaveBalance b ->
            if (b.leaveType.carryForward && b.leaveType.hasQuota()) {
                BigDecimal avail = b.availableAsOf(lwd)
                if (avail > 0) leaveDays += avail
            }
        }
        leaveDays = leaveDays.setScale(1, RoundingMode.HALF_UP)
        BigDecimal leaveEncashment = r0(daily * leaveDays)

        // গ্র্যাচুইটি
        int serviceYears = ChronoUnit.YEARS.between(emp.joinDate, lwd) as int
        BigDecimal gratuity = serviceYears >= PayrollService.GRATUITY_ELIGIBLE_YEARS ? r0(basic * serviceYears) : 0.0

        // অনুমোদিত খরচের দাবি ও ঋণ
        BigDecimal pendingClaims = (ExpenseClaim.executeQuery("select sum(c.amount) from ExpenseClaim c where c.employee = :e and c.status = 'APPROVED'", [e: emp])[0] ?: 0.0) as BigDecimal
        BigDecimal loanDeduction = 0.0
        Loan.findAllByEmployeeAndStatus(emp, 'ACTIVE').each { Loan l -> loanDeduction += loanService.outstanding(l) }

        // কম নোটিসের কর্তন
        int shortDays = 0
        if (!s.noticeWaived && s.initiatedBy == 'employee') {
            LocalDate minDay = s.noticeDate.plusDays(s.noticeDays)
            if (lwd.isBefore(minDay)) shortDays = ChronoUnit.DAYS.between(lwd, minDay) as int
        }
        BigDecimal noticeDeduction = r0(daily * shortDays)

        if (fs == null) fs = new FinalSettlement(separation: s, employee: emp)
        fs.basic = basic
        fs.gross = gross
        fs.salaryDays = salaryDays
        fs.unpaidSalary = unpaidSalary
        fs.leaveDays = leaveDays
        fs.leaveEncashment = leaveEncashment
        fs.serviceYears = serviceYears
        fs.gratuity = gratuity
        fs.pendingClaims = pendingClaims
        fs.loanDeduction = loanDeduction
        fs.noticeShortDays = shortDays
        fs.noticeDeduction = noticeDeduction
        fs.status = 'DRAFT'
        fs.preparedBy = actor()
        fs.preparedAt = now()
        fs.notes = "স্বয়ংক্রিয় হিসাব (শেষ কর্মদিবস: ${lwd}, দৈনিক হার = gross/৩০)".toString()
        recalc(fs)
        if (!fs.save(flush: true)) return fail('নিষ্পত্তির হিসাব সংরক্ষণ করা যায়নি।')

        auditService.record('SETTLEMENT_PREPARED', 'FinalSettlement', fs.id, "${emp.name}: নিট ${fs.netPayable}".toString())
        [ok: true, message: 'নিষ্পত্তির হিসাব তৈরি হয়েছে।', settlement: fs]
    }

    /** নিট প্রদেয় = পাওনা - কর্তন + হাতে সমন্বয় */
    private static void recalc(FinalSettlement fs) {
        fs.netPayable = (fs.unpaidSalary ?: 0.0) + (fs.leaveEncashment ?: 0.0) + (fs.gratuity ?: 0.0) + (fs.pendingClaims ?: 0.0) -
                (fs.loanDeduction ?: 0.0) - (fs.noticeDeduction ?: 0.0) + (fs.otherAdjustment ?: 0.0)
    }

    // ------------------------------------------------------------------ সমন্বয় (HR)
    Map saveAdjustment(FinalSettlement fs, BigDecimal adjustment, String note) {
        if (!accessService.isHrAdmin() || !accessService.canManage(fs.employee)) return fail('সমন্বয়ের অনুমতি আপনার নেই।')
        if (fs.status != 'DRAFT') return fail('শুধু খসড়া নিষ্পত্তিতে সমন্বয় করা যায়।')
        BigDecimal adj = (adjustment ?: 0.0).setScale(2, RoundingMode.HALF_UP)
        if (adj != 0 && !note?.trim()) return fail('সমন্বয়ের কারণ লিখুন।')
        fs.otherAdjustment = adj
        fs.adjustmentNote = adj == 0 ? null : note.trim().take(255)
        recalc(fs)
        fs.save(flush: true)
        auditService.record('SETTLEMENT_ADJUSTED', 'FinalSettlement', fs.id, "${fs.employee.name}: সমন্বয় ${adj}".toString())
        [ok: true, message: 'সমন্বয় সংরক্ষণ হয়েছে।']
    }

    // ------------------------------------------------------------------ অনুমোদন ও পরিশোধ (হিসাব বিভাগ / Admin)
    Map approve(FinalSettlement fs) {
        if (!isAccounts() || isOwn(fs)) return fail('নিষ্পত্তি অনুমোদনের অনুমতি আপনার নেই।')
        if (fs.status != 'DRAFT') return fail('শুধু খসড়া নিষ্পত্তি অনুমোদন করা যায়।')
        fs.status = 'APPROVED'
        fs.approvedBy = actor()
        fs.approvedAt = now()
        fs.save(flush: true)
        notificationService.notifyRoles(['ROLE_HR', 'ROLE_ADMIN'], 'নিষ্পত্তি অনুমোদিত', "${fs.employee.name} - নিট ${fs.netPayable}".toString(),
                "/offboarding/show/${fs.separation.id}".toString(), 'INFO')
        auditService.record('SETTLEMENT_APPROVED', 'FinalSettlement', fs.id, "${fs.employee.name}: নিট ${fs.netPayable}".toString())
        [ok: true, message: 'নিষ্পত্তি অনুমোদিত হয়েছে।']
    }

    /**
     * পরিশোধ রেকর্ড। একই সাথে: অনুমোদিত খরচের দাবিগুলো PAID হয় এবং চলমান ঋণ নিষ্পত্তি (SETTLED) হয়।
     * ঋণ নিষ্পত্তি ব্যর্থ হলে (যেমন খসড়া পে-রোলে কিস্তি ধরা) পরিশোধ হয়ে যায়, তবে বার্তায় সতর্কতা থাকে।
     */
    Map markPaid(FinalSettlement fs, LocalDate paidDate, String paymentRef) {
        if (!isAccounts() || isOwn(fs)) return fail('পরিশোধ রেকর্ডের অনুমতি আপনার নেই।')
        if (fs.status != 'APPROVED') return fail('অনুমোদনের পর পরিশোধ রেকর্ড করা যায়।')
        if (paidDate == null || paidDate.isAfter(today())) return fail('পরিশোধের তারিখ আজ বা তার আগের হতে হবে।')
        if (!paymentRef?.trim()) return fail('পেমেন্ট রেফারেন্স দিন।')

        fs.status = 'PAID'
        fs.paidBy = actor()
        fs.paidDate = paidDate
        fs.paymentRef = paymentRef.trim().take(80)
        fs.save(flush: true)

        ExpenseClaim.findAllByEmployeeAndStatus(fs.employee, 'APPROVED').each { ExpenseClaim c ->
            c.status = 'PAID'
            c.paidBy = fs.paidBy
            c.paidDate = paidDate
            c.paymentRef = fs.paymentRef
            c.save(flush: true)
        }
        List<String> warnings = []
        Loan.findAllByEmployeeAndStatus(fs.employee, 'ACTIVE').each { Loan l ->
            Map r = loanService.settle(l, 'চূড়ান্ত নিষ্পত্তিতে কর্তন')
            if (!r.ok) warnings << "${l}: ${r.message}".toString()
        }

        notificationService.notifyRoles(['ROLE_HR', 'ROLE_ADMIN'], 'নিষ্পত্তি পরিশোধিত', "${fs.employee.name} - রেফ: ${fs.paymentRef}".toString(),
                "/offboarding/show/${fs.separation.id}".toString(), 'INFO')
        auditService.record('SETTLEMENT_PAID', 'FinalSettlement', fs.id, "${fs.employee.name}: ${fs.paymentRef}".toString())
        String msg = 'পরিশোধ রেকর্ড হয়েছে।'
        if (warnings) msg += ' সতর্কতা: ' + warnings.join('; ')
        [ok: true, message: msg]
    }
}

package hrms

import grails.gorm.transactions.Transactional

import hrms.payroll.TaxCalculator

import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * Day 5: মাসিক পে-রোল - উপস্থিতি, ছুটি ও ওভারটাইম থেকে বেতন শিট।
 *
 * নিয়ম (সহজ রাখা হয়েছে; ট্যাক্স/PF/বোনাস Day 6 এ):
 *  - শুধু শেষ হয়ে যাওয়া মাসের পে-রোল চালানো যায়। INACTIVE কর্মী বাদ।
 *  - কর্মদিবস = সাপ্তাহিক ছুটি (শুক্রবার) বাদে দিন, কর্মীর যোগদানের তারিখ থেকে।
 *  - মাসের মাঝে যোগ দিলে আয়ের অংশগুলো (যোগদানের পর দিন / মাসের মোট দিন) অনুপাতে।
 *  - প্রতিটি কর্মদিবসে: অনুমোদিত ছুটি (বেতনসহ/বিনা বেতন) -> উপস্থিতি রেকর্ড (PRESENT/LATE/HOLIDAY) -> না থাকলে অনুপস্থিত।
 *  - অনুপস্থিত ও বিনা বেতনের ছুটির প্রতিদিনে কাটা হয় gross / 30।
 *  - প্রতি ৩ দিন দেরিতে ১ দিনের বেতন কাটা (LATE_DAYS_PER_DEDUCTION)।
 *  - ওভারটাইম = অনুমোদিত ঘণ্টা × (বেসিক / 208) × 2। হিসাব বিভাগ ঘণ্টা হাতে বদলাতে পারে।
 *  - সব টাকা পূর্ণ টাকায় গোল করা।
 *
 * Day 6 এর যোগ:
 *  - বোনাস: ওই পে-মাসের PLANNED/PAID বোনাস আয়ে যোগ (gross এর অংশ), TDS এ ধরা হয় না।
 *  - PF: শুধু FULL_TIME কর্মী। কর্মীর অংশ = উপার্জিত বেসিকের ১০% (কাটা হয়); কোম্পানির অংশ সমান (শুধু তথ্য)।
 *  - TDS: নিয়মিত মাসিক gross (কাঠামো + ভাতা) ×১২ থেকে বার্ষিক কর ({@link TaxCalculator}), ÷১২; মাঝ-মাসে যোগ দিলে অনুপাতে।
 *  - ঋণ/অগ্রিম: ACTIVE ঋণের এই মাস পর্যন্ত বকেয়া কিস্তি কাটা হয়। চূড়ান্ত করলে কিস্তি PAID, পুনরায় খুললে আবার PENDING।
 *  - গ্র্যাচুইটি প্রভিশন: ১ বছর পূর্ণ FULL_TIME কর্মীর জন্য মাসে বেসিক ÷ ১২ (শুধু তথ্য; পাওনা হয় ৫ বছর পর)।
 */
@Transactional
class PayrollService {

    static final int DAYS_PER_MONTH = 30
    static final int LATE_DAYS_PER_DEDUCTION = 3
    static final BigDecimal OT_HOURS_DIVISOR = 208.0
    static final BigDecimal OT_MULTIPLIER = 2.0
    static final BigDecimal PF_EMPLOYEE_PCT = 10.0
    static final BigDecimal PF_EMPLOYER_PCT = 10.0
    static final List<String> PF_TYPES = ['FULL_TIME']
    static final int GRATUITY_MIN_MONTHS_PROVISION = 12
    static final int GRATUITY_ELIGIBLE_YEARS = 5

    def accessService
    def leaveService
    def salaryService

    private static LocalDate today() { LocalDate.now(AttendanceService.ZONE) }

    // ------------------------------------------------------------------ চালানো
    /** মাসের পে-রোল তৈরি (বা DRAFT থাকলে নতুন করে হিসাব)। হাতে দেওয়া OT ঘণ্টা ও সমন্বয় ধরে রাখা হয়। */
    Map generate(int year, int month) {
        YearMonth ym
        try {
            ym = YearMonth.of(year, month)
        } catch (Exception ignored) {
            return fail('মাস বা বছর সঠিক নয়।')
        }
        if (ym.atEndOfMonth().isAfter(today())) return fail('মাস শেষ হওয়ার পর পে-রোল চালানো যাবে।')

        PayrollRun run = PayrollRun.findByPayYearAndPayMonth(year, month)
        if (run != null && run.status == 'FINALIZED') return fail('এই মাসের পে-রোল চূড়ান্ত হয়ে গেছে। আবার চালাতে আগে "পুনরায় খুলুন" (Admin)।')

        Map<Long, Map> carry = [:]
        if (run != null) {
            PayrollLine.findAllByPayrollRun(run).each { PayrollLine l ->
                carry[l.employee.id] = [ot: l.overtimeHoursManual, adj: l.adjustment, note: l.adjustmentNote]
                l.discard()   // নিচে HQL দিয়ে মোছা হবে, তাই সেশন থেকে সরিয়ে রাখি
            }
            deleteLines(run)
        } else {
            run = new PayrollRun(payYear: year, payMonth: month)
        }

        run.workingDays = leaveService.workDays(ym.atDay(1), ym.atEndOfMonth()).size()
        run.generatedBy = accessService.currentUser()?.username
        run.generatedAt = LocalDateTime.now(AttendanceService.ZONE)
        run.save(failOnError: true, flush: true)

        List<Employee> emps = Employee.findAll("from Employee e where e.status <> 'INACTIVE' and e.joinDate <= :end order by e.name",
                [end: ym.atEndOfMonth()])
        Map ctx = loadContext(ym, run)
        emps.each { Employee e -> buildLine(run, e, ym, ctx, carry[e.id]) }
        updateTotals(run)

        [ok: true, run: run, message: "${year}-${String.format('%02d', month)} মাসের বেতন শিট তৈরি হয়েছে (${emps.size()} জন কর্মী)।".toString()]
    }

    /** একটি লাইনের OT ঘণ্টা (খালি = অনুমোদিত OT থেকে) ও সমন্বয় বদলে সেই কর্মীর হিসাব নতুন করে */
    Map updateLine(PayrollLine line, BigDecimal otHours, BigDecimal adjustment, String note) {
        PayrollRun run = line.payrollRun
        if (run.status != 'DRAFT') return fail('চূড়ান্ত পে-রোল বদলানো যায় না।')
        if (otHours != null && (otHours < 0 || otHours > 300)) return fail('OT ঘণ্টা ০ থেকে ৩০০ এর মধ্যে হতে হবে।')
        BigDecimal adj = (adjustment ?: 0.0).setScale(0, RoundingMode.HALF_UP)
        if (adj != 0 && !note?.trim()) return fail('সমন্বয় দিলে কারণ লিখুন।')

        Employee emp = line.employee
        YearMonth ym = run.period
        Map carry = [ot: otHours, adj: adj, note: note?.trim()]
        deleteLine(line)
        PayrollLine fresh = buildLine(run, emp, ym, loadContext(ym, run), carry)
        updateTotals(run)
        [ok: true, line: fresh, message: 'লাইন হালনাগাদ হয়েছে।']
    }

    Map finalizeRun(PayrollRun run) {
        if (run.status != 'DRAFT') return fail('পে-রোল আগেই চূড়ান্ত হয়েছে।')
        if (PayrollLine.countByPayrollRun(run) == 0) return fail('কোনো লাইন নেই - চূড়ান্ত করা যাবে না।')
        run.status = 'FINALIZED'
        run.finalizedBy = accessService.currentUser()?.username
        run.finalizedAt = LocalDateTime.now(AttendanceService.ZONE)
        run.save(flush: true)
        markPaid(run)
        [ok: true, message: 'পে-রোল চূড়ান্ত করা হয়েছে। কিস্তি ও বোনাস "পরিশোধিত" চিহ্নিত হয়েছে; এখন আর বদলানো যাবে না।']
    }

    Map reopen(PayrollRun run) {
        if (run.status != 'FINALIZED') return fail('পে-রোল চূড়ান্ত অবস্থায় নেই।')
        run.status = 'DRAFT'
        run.finalizedBy = null
        run.finalizedAt = null
        run.save(flush: true)
        markUnpaid(run)
        [ok: true, message: 'পে-রোল আবার খোলা হয়েছে (DRAFT)। কিস্তি ও বোনাস আবার "বাকি" অবস্থায়।']
    }

    Map deleteRun(PayrollRun run) {
        if (run.status != 'DRAFT') return fail('চূড়ান্ত পে-রোল মোছা যায় না।')
        deleteLines(run)
        run.delete(flush: true)
        [ok: true, message: 'পে-রোল মুছে ফেলা হয়েছে।']
    }

    // ------------------------------------------------------------------ মাসের তথ্য একবারে লোড
    private Map loadContext(YearMonth ym, PayrollRun run) {
        LocalDate first = ym.atDay(1)
        LocalDate last = ym.atEndOfMonth()

        Map<Long, Map<LocalDate, Attendance>> att = [:]
        Attendance.findAllByAttDateBetween(first, last).each { Attendance a ->
            Map<LocalDate, Attendance> m = att[a.employee.id]
            if (m == null) { m = [:]; att[a.employee.id] = m }
            m[a.attDate] = a
        }

        Map<Long, List<LeaveRequest>> leaves = [:].withDefault { [] as List<LeaveRequest> }
        LeaveRequest.executeQuery(
                "from LeaveRequest r where r.status = 'APPROVED' and r.fromDate <= :last and r.toDate >= :first",
                [first: first, last: last]).each { LeaveRequest r -> leaves[r.employee.id] << r }

        Map<Long, BigDecimal> ot = [:]
        OvertimeRequest.findAllByStatusAndOtDateBetween('APPROVED', first, last).each { OvertimeRequest o ->
            ot[o.employee.id] = (ot[o.employee.id] ?: 0.0) + (o.hours ?: 0.0)
        }

        Map<Long, List<EmployeeSalaryHead>> heads = [:].withDefault { [] as List<EmployeeSalaryHead> }
        EmployeeSalaryHead.list().each { EmployeeSalaryHead h ->
            if (h.head.active) heads[h.employee.id] << h
        }

        // Day 6: বোনাস (ওই পে-মাসের, বাতিল বাদে)
        Map<Long, List<Bonus>> bonuses = [:].withDefault { [] as List<Bonus> }
        Bonus.findAllByPayYearAndPayMonthAndStatusNotEqual(ym.year, ym.monthValue, 'CANCELLED').each { Bonus b -> bonuses[b.employee.id] << b }

        // Day 6: ACTIVE ঋণের বকেয়া কিস্তি (এই মাস বা আগের) - এই পে-রোলে ধরা বা কোনো পে-রোলেই ধরা নেই এমন
        int ymKey = ym.year * 12 + ym.monthValue
        Map<Long, List<LoanInstallment>> dueLoans = [:].withDefault { [] as List<LoanInstallment> }
        LoanInstallment.executeQuery(
                "from LoanInstallment i where i.status = 'PENDING' and i.loan.status = 'ACTIVE' " +
                "and (i.dueYear * 12 + i.dueMonth) <= :key and (i.payrollRun is null or i.payrollRun = :run) order by i.dueYear, i.dueMonth, i.seqNo",
                [key: ymKey, run: run]).each { LoanInstallment i -> dueLoans[i.loan.employee.id] << i }

        [att: att, leaves: leaves, ot: ot, heads: heads, bonuses: bonuses, loans: dueLoans]
    }

    // ------------------------------------------------------------------ একজনের হিসাব
    private PayrollLine buildLine(PayrollRun run, Employee e, YearMonth ym, Map ctx, Map carry) {
        LocalDate first = ym.atDay(1)
        LocalDate last = ym.atEndOfMonth()
        LocalDate start = e.joinDate.isAfter(first) ? e.joinDate : first
        List<String> warnings = []

        SalaryStructure s = salaryService.structureFor(e, last)
        BigDecimal structureGross = s.gross
        BigDecimal currentGross = salaryService.currentStructure(e).gross
        if (e.salary != null && e.salary.compareTo(currentGross) != 0) {
            warnings << "প্রোফাইলের বেতন (${e.salary.setScale(0, RoundingMode.HALF_UP)}) কাঠামোর gross (${currentGross}) এর সাথে মেলে না - কাঠামো ধরা হয়েছে".toString()
        }

        // ---- দিন গণনা
        List<LocalDate> days = leaveService.workDays(start, last)
        List<LeaveRequest> leaves = (ctx.leaves as Map)[e.id] as List<LeaveRequest>
        Map<LocalDate, Attendance> att = ((ctx.att as Map)[e.id] ?: [:]) as Map<LocalDate, Attendance>

        int present = 0, late = 0, holiday = 0, paidLeave = 0, unpaidLeave = 0, absent = 0
        for (LocalDate d : days) {
            LeaveRequest lr = leaves.find { LeaveRequest r -> !d.isBefore(r.fromDate) && !d.isAfter(r.toDate) }
            if (lr != null) {
                if (lr.leaveType.paid) paidLeave++ else unpaidLeave++
                continue
            }
            Attendance a = att[d]
            if (a == null || a.status == 'ABSENT') {
                absent++
            } else if (a.status == 'LATE') {
                present++; late++
            } else if (a.status == 'PRESENT') {
                present++
            } else if (a.status == 'HOLIDAY') {
                holiday++
            } else if (a.status == 'ON_LEAVE') {
                paidLeave++
            }
        }
        if (!days.isEmpty() && att.isEmpty() && leaves.isEmpty()) {
            warnings << 'এই মাসে কোনো উপস্থিতি/ছুটির রেকর্ড নেই - সব কর্মদিবস অনুপস্থিত ধরা হয়েছে'
        }

        // ---- আয়
        BigDecimal factor = new BigDecimal(last.toEpochDay() - start.toEpochDay() + 1)
                .divide(new BigDecimal(ym.lengthOfMonth()), 8, RoundingMode.HALF_UP)
        BigDecimal basic = r0(s.basic * factor)
        BigDecimal rent = r0(s.houseRent * factor)
        BigDecimal med = r0(s.medical * factor)
        BigDecimal conv = r0(s.conveyance * factor)

        BigDecimal otherAllowance = 0.0
        BigDecimal otherAllowanceFull = 0.0     // পূর্ণ মাসের ভাতা (TDS এর নিয়মিত আয় বের করতে)
        BigDecimal otherDeduction = 0.0
        List<Map> items = []
        ((ctx.heads as Map)[e.id] as List<EmployeeSalaryHead>).each { EmployeeSalaryHead h ->
            BigDecimal amt = r0(h.head.amountFor(s.basic, h.amount) * factor)
            items << [code: h.head.code, name: h.head.name, type: h.head.headType, amount: amt]
            if (h.head.headType == 'EARNING') {
                otherAllowance += amt
                otherAllowanceFull += r0(h.head.amountFor(s.basic, h.amount))
            } else {
                otherDeduction += amt
            }
        }

        // ---- Day 6: বোনাস
        BigDecimal bonusTotal = 0.0
        ((ctx.bonuses as Map)[e.id] as List<Bonus>).each { Bonus b ->
            items << [code: b.bonusType == 'FESTIVAL' ? 'BONUS-F' : (b.bonusType == 'PERFORMANCE' ? 'BONUS-P' : 'BONUS'),
                      name: "বোনাস: ${b.title}".toString().take(80), type: 'EARNING', amount: r0(b.amount)]
            bonusTotal += r0(b.amount)
        }

        // ---- ওভারটাইম
        BigDecimal otAuto = ((ctx.ot as Map)[e.id] ?: 0.0) as BigDecimal
        BigDecimal otManual = carry?.ot as BigDecimal
        BigDecimal otHours = otManual != null ? otManual : otAuto
        BigDecimal otAmount = r0(s.basic.divide(OT_HOURS_DIVISOR, 10, RoundingMode.HALF_UP) * OT_MULTIPLIER * otHours)

        BigDecimal gross = basic + rent + med + conv + otherAllowance + otAmount + bonusTotal

        // ---- কর্তন (অনুপস্থিতি/বিনা বেতন ছুটি/দেরি)
        int lateDedDays = late.intdiv(LATE_DAYS_PER_DEDUCTION)
        BigDecimal baseEarned = basic + rent + med + conv
        BigDecimal absentDed = r0(structureGross * (absent + unpaidLeave) / DAYS_PER_MONTH)
        if (absentDed > baseEarned) absentDed = baseEarned
        BigDecimal lateDed = r0(structureGross * lateDedDays / DAYS_PER_MONTH)
        if (lateDed > baseEarned - absentDed) lateDed = baseEarned - absentDed

        // ---- Day 6: PF, TDS, ঋণের কিস্তি, গ্র্যাচুইটি প্রভিশন
        boolean pfEligible = PF_TYPES.contains(e.employeeType)
        BigDecimal pfEmp = pfEligible ? r0(basic * PF_EMPLOYEE_PCT / 100) : 0.0
        BigDecimal pfEmpr = pfEligible ? r0(basic * PF_EMPLOYER_PCT / 100) : 0.0

        BigDecimal regularMonthly = structureGross + otherAllowanceFull
        BigDecimal tdsAmt = r0(TaxCalculator.monthlyTds(regularMonthly, e.gender == 'FEMALE') * factor)

        BigDecimal loanDed = 0.0
        List<LoanInstallment> dueInst = (ctx.loans as Map)[e.id] as List<LoanInstallment>
        dueInst.each { LoanInstallment i ->
            loanDed += i.amount
            i.payrollRun = run
            i.save(failOnError: true)
        }

        long serviceMonths = ChronoUnit.MONTHS.between(e.joinDate, last.plusDays(1))
        BigDecimal gratuityProv = (pfEligible && serviceMonths >= GRATUITY_MIN_MONTHS_PROVISION) ? r0(basic / 12) : 0.0

        if (pfEmp > 0) items << [code: 'PF', name: "প্রভিডেন্ট ফান্ড (${PF_EMPLOYEE_PCT.intValue()}%)".toString(), type: 'DEDUCTION', amount: pfEmp]
        if (tdsAmt > 0) items << [code: 'TDS', name: 'আয়কর (TDS)', type: 'DEDUCTION', amount: tdsAmt]
        if (loanDed > 0) items << [code: 'LOAN', name: 'ঋণ/অগ্রিমের কিস্তি', type: 'DEDUCTION', amount: loanDed]

        BigDecimal totalDed = absentDed + lateDed + otherDeduction + pfEmp + tdsAmt + loanDed

        BigDecimal adj = ((carry?.adj ?: 0.0) as BigDecimal).setScale(0, RoundingMode.HALF_UP)
        BigDecimal net = gross - totalDed + adj
        if (net < 0) warnings << 'নেট বেতন ঋণাত্মক - কর্তন/সমন্বয় যাচাই করুন'

        PayrollLine line = new PayrollLine(
                payrollRun: run, employee: e, employeeName: e.name,
                departmentName: e.department?.name, designationTitle: e.designation?.title,
                workingDays: days.size(), presentDays: present, lateDays: late, holidayDays: holiday,
                paidLeaveDays: paidLeave, unpaidLeaveDays: unpaidLeave, absentDays: absent, lateDeductionDays: lateDedDays,
                basic: basic, houseRent: rent, medical: med, conveyance: conv, otherAllowance: otherAllowance,
                overtimeHours: otHours, overtimeHoursManual: otManual, overtimeAmount: otAmount, bonus: bonusTotal, grossEarnings: gross,
                absentDeduction: absentDed, lateDeduction: lateDed, otherDeduction: otherDeduction,
                pfEmployee: pfEmp, tds: tdsAmt, loanDeduction: loanDed, pfEmployer: pfEmpr, gratuityProvision: gratuityProv, totalDeduction: totalDed,
                adjustment: adj, adjustmentNote: carry?.note as String, structureGross: structureGross, netPay: net,
                remarks: warnings ? warnings.join('; ') : null)
        line.save(failOnError: true, flush: true)

        items.each { Map item ->
            new PayrollLineItem(line: line, headCode: item.code, headName: item.name, headType: item.type, amount: item.amount as BigDecimal)
                    .save(failOnError: true)
        }
        line
    }

    // ------------------------------------------------------------------ মোছা ও মোট
    private void deleteLine(PayrollLine line) {
        LoanInstallment.executeUpdate("update LoanInstallment i set i.payrollRun = null where i.payrollRun = :r and i.status = 'PENDING' and i.loan in (select l from Loan l where l.employee = :e)",
                [r: line.payrollRun, e: line.employee])
        PayrollLineItem.executeUpdate('delete from PayrollLineItem i where i.line = :l', [l: line])
        line.delete(flush: true)
    }

    private void deleteLines(PayrollRun run) {
        LoanInstallment.executeUpdate("update LoanInstallment i set i.payrollRun = null where i.payrollRun = :r and i.status = 'PENDING'", [r: run])
        PayrollLineItem.executeUpdate('delete from PayrollLineItem i where i.line in (select l from PayrollLine l where l.payrollRun = :r)', [r: run])
        PayrollLine.executeUpdate('delete from PayrollLine l where l.payrollRun = :r', [r: run])
    }

    // ------------------------------------------------------------------ Day 6: চূড়ান্ত / পুনরায় খোলা
    /** চূড়ান্ত করলে: এই পে-রোলে কাটা কিস্তি PAID (সব কিস্তি শোধ হলে ঋণ CLOSED) ও এ মাসের বোনাস PAID */
    private void markPaid(PayrollRun run) {
        List<LoanInstallment> insts = LoanInstallment.findAllByPayrollRunAndStatus(run, 'PENDING')
        Set<Loan> loans = [] as Set<Loan>
        insts.each { LoanInstallment i ->
            i.status = 'PAID'
            i.save(failOnError: true, flush: true)
            loans << i.loan
        }
        loans.each { Loan l ->
            if (LoanInstallment.countByLoanAndStatus(l, 'PENDING') == 0) {
                l.status = 'CLOSED'
                l.save(failOnError: true)
            }
        }
        Bonus.executeUpdate("update Bonus b set b.status = 'PAID' where b.payYear = :y and b.payMonth = :m and b.status = 'PLANNED'",
                [y: run.payYear, m: run.payMonth])
    }

    private void markUnpaid(PayrollRun run) {
        List<LoanInstallment> insts = LoanInstallment.findAllByPayrollRunAndStatus(run, 'PAID')
        Set<Loan> loans = [] as Set<Loan>
        insts.each { LoanInstallment i ->
            i.status = 'PENDING'
            i.save(failOnError: true)
            loans << i.loan
        }
        loans.each { Loan l ->
            if (l.status == 'CLOSED') {
                l.status = 'ACTIVE'
                l.save(failOnError: true)
            }
        }
        Bonus.executeUpdate("update Bonus b set b.status = 'PLANNED' where b.payYear = :y and b.payMonth = :m and b.status = 'PAID'",
                [y: run.payYear, m: run.payMonth])
    }

    // ------------------------------------------------------------------ Day 6: PF ও গ্র্যাচুইটি রিপোর্ট
    /** কর্মীওয়ারি PF জমা (চূড়ান্ত পে-রোল থেকে) */
    List<Map> pfLedger() {
        Map<Long, Map> byEmp = [:]
        PayrollLine.executeQuery(
                "select l.employee.id, l.employeeName, sum(l.pfEmployee), sum(l.pfEmployer), count(l) from PayrollLine l " +
                "where l.payrollRun.status = 'FINALIZED' and (l.pfEmployee > 0 or l.pfEmployer > 0) group by l.employee.id, l.employeeName order by l.employeeName").each { row ->
            BigDecimal emp = row[2] as BigDecimal
            BigDecimal empr = row[3] as BigDecimal
            byEmp[row[0] as Long] = [name: row[1], employee: emp, employer: empr, total: emp + empr, months: row[4]]
        }
        byEmp.values() as List<Map>
    }

    /** গ্র্যাচুইটি: ১ বছর পূর্ণ হলে প্রভিশন চলে; ৫ বছর পূর্ণ হলে পাওনা = শেষ বেসিক × পূর্ণ বছর (বছরে ৩০ দিনের মজুরি) */
    List<Map> gratuityReport() {
        LocalDate today = today()
        Employee.findAll("from Employee e where e.status <> 'INACTIVE' order by e.name").collect { Employee e ->
            long months = ChronoUnit.MONTHS.between(e.joinDate, today)
            int years = (int) (months / 12)
            SalaryStructure s = salaryService.currentStructure(e)
            boolean eligible = years >= GRATUITY_ELIGIBLE_YEARS
            [employee: e, years: years, months: months % 12, basic: s.basic, fullTime: PF_TYPES.contains(e.employeeType),
             eligible: eligible, payable: eligible ? r0(s.basic * years) : 0.0,
             provisionSoFar: (PF_TYPES.contains(e.employeeType) && years >= 1) ? r0(s.basic * years) : 0.0]
        }
    }

    void updateTotals(PayrollRun run) {
        List<PayrollLine> lines = PayrollLine.findAllByPayrollRun(run)
        run.employeeCount = lines.size()
        run.totalGross = lines.sum(0.0) { PayrollLine l -> l.grossEarnings } as BigDecimal
        run.totalDeduction = lines.sum(0.0) { PayrollLine l -> l.totalDeduction } as BigDecimal
        run.totalNet = lines.sum(0.0) { PayrollLine l -> l.netPay } as BigDecimal
        run.save(failOnError: true, flush: true)
    }

    // ------------------------------------------------------------------ তালিকা ও CSV
    List<PayrollLine> lines(PayrollRun run) {
        PayrollLine.findAllByPayrollRun(run, [sort: 'employeeName', order: 'asc'])
    }

    List<PayrollLineItem> items(PayrollLine line) {
        PayrollLineItem.findAllByLine(line, [sort: 'id'])
    }

    static final List<String> CSV_HEADER = ['Employee', 'Department', 'Designation', 'Working days', 'Present', 'Late', 'Paid leave',
                                            'Unpaid leave', 'Absent', 'Basic', 'House rent', 'Medical', 'Conveyance', 'Other allowance',
                                            'OT hours', 'OT amount', 'Gross', 'Absent deduction', 'Late deduction', 'Other deduction',
                                            'Adjustment', 'Bonus', 'PF (employee)', 'TDS', 'Loan installment', 'Total deduction', 'Net pay', 'PF (employer)', 'Gratuity provision']

    List<List> csvRows(PayrollRun run) {
        lines(run).collect { PayrollLine l ->
            [l.employeeName, l.departmentName, l.designationTitle, l.workingDays, l.presentDays, l.lateDays, l.paidLeaveDays,
             l.unpaidLeaveDays, l.absentDays, l.basic, l.houseRent, l.medical, l.conveyance, l.otherAllowance,
             l.overtimeHours, l.overtimeAmount, l.grossEarnings, l.absentDeduction, l.lateDeduction, l.otherDeduction,
             l.adjustment, l.bonus, l.pfEmployee, l.tds, l.loanDeduction, l.totalDeduction, l.netPay, l.pfEmployer, l.gratuityProvision] as List
        }
    }

    // ------------------------------------------------------------------ helpers
    private static BigDecimal r0(BigDecimal v) { v.setScale(0, RoundingMode.HALF_UP) }

    private static Map fail(String message) { [ok: false, message: message] }
}

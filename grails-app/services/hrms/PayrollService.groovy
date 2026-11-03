package hrms

import grails.gorm.transactions.Transactional

import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

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
 */
@Transactional
class PayrollService {

    static final int DAYS_PER_MONTH = 30
    static final int LATE_DAYS_PER_DEDUCTION = 3
    static final BigDecimal OT_HOURS_DIVISOR = 208.0
    static final BigDecimal OT_MULTIPLIER = 2.0

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
        Map ctx = loadContext(ym)
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
        PayrollLine fresh = buildLine(run, emp, ym, loadContext(ym), carry)
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
        [ok: true, message: 'পে-রোল চূড়ান্ত করা হয়েছে। এখন আর বদলানো যাবে না।']
    }

    Map reopen(PayrollRun run) {
        if (run.status != 'FINALIZED') return fail('পে-রোল চূড়ান্ত অবস্থায় নেই।')
        run.status = 'DRAFT'
        run.finalizedBy = null
        run.finalizedAt = null
        run.save(flush: true)
        [ok: true, message: 'পে-রোল আবার খোলা হয়েছে (DRAFT)।']
    }

    Map deleteRun(PayrollRun run) {
        if (run.status != 'DRAFT') return fail('চূড়ান্ত পে-রোল মোছা যায় না।')
        deleteLines(run)
        run.delete(flush: true)
        [ok: true, message: 'পে-রোল মুছে ফেলা হয়েছে।']
    }

    // ------------------------------------------------------------------ মাসের তথ্য একবারে লোড
    private Map loadContext(YearMonth ym) {
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

        [att: att, leaves: leaves, ot: ot, heads: heads]
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
        BigDecimal otherDeduction = 0.0
        List<Map> items = []
        ((ctx.heads as Map)[e.id] as List<EmployeeSalaryHead>).each { EmployeeSalaryHead h ->
            BigDecimal amt = r0(h.head.amountFor(s.basic, h.amount) * factor)
            items << [code: h.head.code, name: h.head.name, type: h.head.headType, amount: amt]
            if (h.head.headType == 'EARNING') otherAllowance += amt else otherDeduction += amt
        }

        // ---- ওভারটাইম
        BigDecimal otAuto = ((ctx.ot as Map)[e.id] ?: 0.0) as BigDecimal
        BigDecimal otManual = carry?.ot as BigDecimal
        BigDecimal otHours = otManual != null ? otManual : otAuto
        BigDecimal otAmount = r0(s.basic.divide(OT_HOURS_DIVISOR, 10, RoundingMode.HALF_UP) * OT_MULTIPLIER * otHours)

        BigDecimal gross = basic + rent + med + conv + otherAllowance + otAmount

        // ---- কর্তন (অনুপস্থিতি/বিনা বেতন ছুটি/দেরি)
        int lateDedDays = late.intdiv(LATE_DAYS_PER_DEDUCTION)
        BigDecimal baseEarned = basic + rent + med + conv
        BigDecimal absentDed = r0(structureGross * (absent + unpaidLeave) / DAYS_PER_MONTH)
        if (absentDed > baseEarned) absentDed = baseEarned
        BigDecimal lateDed = r0(structureGross * lateDedDays / DAYS_PER_MONTH)
        if (lateDed > baseEarned - absentDed) lateDed = baseEarned - absentDed
        BigDecimal totalDed = absentDed + lateDed + otherDeduction

        BigDecimal adj = ((carry?.adj ?: 0.0) as BigDecimal).setScale(0, RoundingMode.HALF_UP)
        BigDecimal net = gross - totalDed + adj
        if (net < 0) warnings << 'নেট বেতন ঋণাত্মক - কর্তন/সমন্বয় যাচাই করুন'

        PayrollLine line = new PayrollLine(
                payrollRun: run, employee: e, employeeName: e.name,
                departmentName: e.department?.name, designationTitle: e.designation?.title,
                workingDays: days.size(), presentDays: present, lateDays: late, holidayDays: holiday,
                paidLeaveDays: paidLeave, unpaidLeaveDays: unpaidLeave, absentDays: absent, lateDeductionDays: lateDedDays,
                basic: basic, houseRent: rent, medical: med, conveyance: conv, otherAllowance: otherAllowance,
                overtimeHours: otHours, overtimeHoursManual: otManual, overtimeAmount: otAmount, grossEarnings: gross,
                absentDeduction: absentDed, lateDeduction: lateDed, otherDeduction: otherDeduction, totalDeduction: totalDed,
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
        PayrollLineItem.executeUpdate('delete from PayrollLineItem i where i.line = :l', [l: line])
        line.delete(flush: true)
    }

    private void deleteLines(PayrollRun run) {
        PayrollLineItem.executeUpdate('delete from PayrollLineItem i where i.line in (select l from PayrollLine l where l.payrollRun = :r)', [r: run])
        PayrollLine.executeUpdate('delete from PayrollLine l where l.payrollRun = :r', [r: run])
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
                                            'Adjustment', 'Total deduction', 'Net pay']

    List<List> csvRows(PayrollRun run) {
        lines(run).collect { PayrollLine l ->
            [l.employeeName, l.departmentName, l.designationTitle, l.workingDays, l.presentDays, l.lateDays, l.paidLeaveDays,
             l.unpaidLeaveDays, l.absentDays, l.basic, l.houseRent, l.medical, l.conveyance, l.otherAllowance,
             l.overtimeHours, l.overtimeAmount, l.grossEarnings, l.absentDeduction, l.lateDeduction, l.otherDeduction,
             l.adjustment, l.totalDeduction, l.netPay] as List
        }
    }

    // ------------------------------------------------------------------ helpers
    private static BigDecimal r0(BigDecimal v) { v.setScale(0, RoundingMode.HALF_UP) }

    private static Map fail(String message) { [ok: false, message: message] }
}

package hrms

import grails.plugin.springsecurity.annotation.Secured

import java.time.YearMonth

/** Day 5: মাসিক পে-রোল - চালানো, বেতন শিট, লাইন সংশোধন, চূড়ান্ত করা, CSV */
@Secured(['ROLE_ADMIN', 'ROLE_ACCOUNTANT'])
class PayrollController {

    static allowedMethods = [generate: 'POST', updateLine: 'POST', finalizeRun: 'POST', reopen: 'POST', delete: 'POST']

    def payrollService

    /** সব পে-রোল চালানোর তালিকা + নতুন মাস চালানোর ফর্ম */
    def index() {
        YearMonth last = YearMonth.now(AttendanceService.ZONE).minusMonths(1)
        [
            runs       : PayrollRun.createCriteria().list { order('payYear', 'desc'); order('payMonth', 'desc') },
            defaultYear: last.year,
            defaultMonth: last.monthValue,
            lateRule   : PayrollService.LATE_DAYS_PER_DEDUCTION,
            otMultiplier: PayrollService.OT_MULTIPLIER,
            dayDivisor : PayrollService.DAYS_PER_MONTH
        ]
    }

    def generate() {
        Integer year = params.int('payYear')
        Integer month = params.int('payMonth')
        if (year == null || month == null) {
            flash.error = 'বছর ও মাস দিন।'
            redirect(action: 'index')
            return
        }
        Map r = payrollService.generate(year, month)
        flash[r.ok ? 'success' : 'error'] = r.message
        if (r.ok) redirect(action: 'show', id: (r.run as PayrollRun).id)
        else redirect(action: 'index')
    }

    /** বেতন শিট */
    def show(Long id) {
        PayrollRun run = id ? PayrollRun.get(id) : null
        if (!run) { notFound(); return }
        List<PayrollLine> lines = payrollService.lines(run)
        [
            run     : run,
            lines   : lines,
            warnings: lines.count { it.remarks },
            totals  : [
                basic     : lines.sum(0.0) { it.basic },
                houseRent : lines.sum(0.0) { it.houseRent },
                medical   : lines.sum(0.0) { it.medical },
                conveyance: lines.sum(0.0) { it.conveyance },
                allowance : lines.sum(0.0) { it.otherAllowance },
                overtime  : lines.sum(0.0) { it.overtimeAmount },
                adjustment: lines.sum(0.0) { it.adjustment }
            ]
        ]
    }

    /** একজনের হিসাবের বিস্তারিত ও (DRAFT এ) OT/সমন্বয় ফর্ম */
    def line(Long id) {
        PayrollLine line = id ? PayrollLine.get(id) : null
        if (!line) { notFound(); return }
        [line: line, run: line.payrollRun, items: payrollService.items(line),
         earnings: PayrollLineItem.findAllByLineAndHeadType(line, 'EARNING', [sort: 'id']),
         deductions: PayrollLineItem.findAllByLineAndHeadType(line, 'DEDUCTION', [sort: 'id']),
         otMultiplier: PayrollService.OT_MULTIPLIER, dayDivisor: PayrollService.DAYS_PER_MONTH,
         lateRule: PayrollService.LATE_DAYS_PER_DEDUCTION]
    }

    def updateLine(Long id) {
        PayrollLine line = id ? PayrollLine.get(id) : null
        if (!line) { notFound(); return }
        BigDecimal ot = null
        BigDecimal adj = null
        try {
            if (params.overtimeHours?.toString()?.trim()) ot = new BigDecimal(params.overtimeHours.toString().trim())
            if (params.adjustment?.toString()?.trim()) adj = new BigDecimal(params.adjustment.toString().trim())
        } catch (NumberFormatException ignored) {
            flash.error = 'ঘণ্টা বা সমন্বয়ের মান সঠিক সংখ্যা নয়।'
            redirect(action: 'line', id: id)
            return
        }
        Map r = payrollService.updateLine(line, ot, adj, params.adjustmentNote as String)
        flash[r.ok ? 'success' : 'error'] = r.message
        if (r.ok) redirect(action: 'line', id: (r.line as PayrollLine).id)
        else redirect(action: 'line', id: id)
    }

    def finalizeRun(Long id) {
        PayrollRun run = id ? PayrollRun.get(id) : null
        if (!run) { notFound(); return }
        Map r = payrollService.finalizeRun(run)
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'show', id: id)
    }

    @Secured(['ROLE_ADMIN'])
    def reopen(Long id) {
        PayrollRun run = id ? PayrollRun.get(id) : null
        if (!run) { notFound(); return }
        Map r = payrollService.reopen(run)
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'show', id: id)
    }

    def delete(Long id) {
        PayrollRun run = id ? PayrollRun.get(id) : null
        if (!run) { notFound(); return }
        Map r = payrollService.deleteRun(run)
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: r.ok ? 'index' : 'show', id: r.ok ? null : id)
    }

    /** বেতন শিট CSV (Excel এ খোলে; বাংলা নামের জন্য BOM) */
    def exportCsv(Long id) {
        PayrollRun run = id ? PayrollRun.get(id) : null
        if (!run) { notFound(); return }
        response.characterEncoding = 'UTF-8'
        response.contentType = 'text/csv; charset=UTF-8'
        response.setHeader('Content-Disposition', "attachment; filename=\"salary-sheet-${run}.csv\"".toString())
        Writer w = response.writer
        w << CsvUtil.BOM
        w << CsvUtil.line(PayrollService.CSV_HEADER) << '\r\n'
        payrollService.csvRows(run).each { List row -> w << CsvUtil.line(row) << '\r\n' }
        w.flush()
    }

    /** Day 6 (ডেমো): পে-স্লিপ - প্রিন্ট/PDF সেভের উপযোগী পাতা */
    def payslip(Long id) {
        PayrollLine line = id ? PayrollLine.get(id) : null
        if (!line) { notFound(); return }
        [line: line, run: line.payrollRun,
         earnings: PayrollLineItem.findAllByLineAndHeadType(line, 'EARNING', [sort: 'id']),
         deductions: PayrollLineItem.findAllByLineAndHeadType(line, 'DEDUCTION', [sort: 'id'])]
    }

    private void notFound() {
        flash.error = 'পে-রোল পাওয়া যায়নি।'
        redirect(action: 'index')
    }
}

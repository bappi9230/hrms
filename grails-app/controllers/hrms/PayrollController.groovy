package hrms

import grails.plugin.springsecurity.annotation.Secured

import java.time.YearMonth

/** Day 5: মাসিক পে-রোল - চালানো, বেতন শিট, লাইন সংশোধন, চূড়ান্ত করা, CSV।  Day 6: পে-স্লিপ PDF/ইমেইল, ব্যাংক ও ওয়ালেট শিট, PF/গ্র্যাচুইটি */
@Secured(['ROLE_ADMIN', 'ROLE_ACCOUNTANT'])
class PayrollController {

    static allowedMethods = [generate: 'POST', updateLine: 'POST', finalizeRun: 'POST', reopen: 'POST', delete: 'POST', emailPayslip: 'POST', emailAll: 'POST']

    def payrollService
    def payslipService
    def bankSheetService

    /** সব পে-রোল চালানোর তালিকা + নতুন মাস চালানোর ফর্ম */
    def index() {
        YearMonth last = YearMonth.now(AttendanceService.ZONE).minusMonths(1)
        [
            runs       : PayrollRun.createCriteria().list { order('payYear', 'desc'); order('payMonth', 'desc') },
            defaultYear: last.year,
            defaultMonth: last.monthValue,
            lateRule   : PayrollService.LATE_DAYS_PER_DEDUCTION,
            otMultiplier: PayrollService.OT_MULTIPLIER,
            dayDivisor : PayrollService.DAYS_PER_MONTH,
            pfEmployeePct: PayrollService.PF_EMPLOYEE_PCT
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
            mailConfigured: payslipService.mailConfigured,
            unsentCount: lines.count { it.emailedAt == null },
            totals  : [
                basic     : lines.sum(0.0) { it.basic },
                houseRent : lines.sum(0.0) { it.houseRent },
                medical   : lines.sum(0.0) { it.medical },
                conveyance: lines.sum(0.0) { it.conveyance },
                allowance : lines.sum(0.0) { it.otherAllowance },
                bonus     : lines.sum(0.0) { it.bonus },
                pf        : lines.sum(0.0) { it.pfEmployee },
                tds       : lines.sum(0.0) { it.tds },
                loan      : lines.sum(0.0) { it.loanDeduction },
                pfEmployer: lines.sum(0.0) { it.pfEmployer },
                gratuity  : lines.sum(0.0) { it.gratuityProvision },
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

    /** পে-স্লিপ - প্রিন্টের উপযোগী পাতা (PDF: payslipPdf) */
    def payslip(Long id) {
        PayrollLine line = id ? PayrollLine.get(id) : null
        if (!line) { notFound(); return }
        [line: line, run: line.payrollRun, self: false,
         earnings: PayrollLineItem.findAllByLineAndHeadType(line, 'EARNING', [sort: 'id']),
         deductions: PayrollLineItem.findAllByLineAndHeadType(line, 'DEDUCTION', [sort: 'id'])]
    }

    // ------------------------------------------------------------------ Day 6: পে-স্লিপ PDF ও ইমেইল
    def payslipPdf(Long id) {
        PayrollLine line = id ? PayrollLine.get(id) : null
        if (!line) { notFound(); return }
        sendPdf(payslipService.pdf(line), PayslipService.fileName(line))
    }

    def emailPayslip(Long id) {
        PayrollLine line = id ? PayrollLine.get(id) : null
        if (!line) { notFound(); return }
        Map r = payslipService.email(line)
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'show', id: line.payrollRun.id)
    }

    def emailAll(Long id) {
        PayrollRun run = id ? PayrollRun.get(id) : null
        if (!run) { notFound(); return }
        Map r = payslipService.emailAll(run, params.onlyUnsent != 'false')
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'show', id: id)
    }

    private void sendPdf(byte[] bytes, String fileName) {
        response.contentType = 'application/pdf'
        response.setHeader('Content-Disposition', "inline; filename=\"${fileName}\"".toString())
        response.setContentLength(bytes.length)
        response.outputStream.write(bytes)
        response.outputStream.flush()
    }

    // ------------------------------------------------------------------ Day 6: ব্যাংক অ্যাডভাইস / ওয়ালেট শিট
    /** চূড়ান্ত পে-রোলের পেমেন্ট শিট (ব্যাংক / bKash-Nagad / নগদ / তথ্য নেই) - প্রিন্টের উপযোগী */
    def bankAdvice(Long id) {
        PayrollRun run = id ? PayrollRun.get(id) : null
        if (!run) { notFound(); return }
        if (run.status != 'FINALIZED') {
            flash.error = 'ব্যাংক অ্যাডভাইস শুধু চূড়ান্ত পে-রোলের জন্য।'
            redirect(action: 'show', id: id)
            return
        }
        [run: run, sheet: bankSheetService.build(run), company: bankSheetService.companyName,
         companyBank: bankSheetService.companyBank, companyAccount: bankSheetService.companyAccount]
    }

    def bankCsv(Long id) {
        PayrollRun run = id ? PayrollRun.get(id) : null
        if (!run || run.status != 'FINALIZED') { notFound(); return }
        Map sheet = bankSheetService.build(run)
        writeCsv("bank-advice-${run}.csv", BankSheetService.BANK_HEADER, bankSheetService.bankRows(run, sheet))
    }

    def walletCsv(Long id) {
        PayrollRun run = id ? PayrollRun.get(id) : null
        if (!run || run.status != 'FINALIZED') { notFound(); return }
        Map sheet = bankSheetService.build(run)
        writeCsv("mobile-wallet-${run}.csv", BankSheetService.WALLET_HEADER, bankSheetService.walletRows(run, sheet))
    }

    private void writeCsv(String fileName, List<String> header, List<List> rows) {
        response.characterEncoding = 'UTF-8'
        response.contentType = 'text/csv; charset=UTF-8'
        response.setHeader('Content-Disposition', "attachment; filename=\"${fileName}\"".toString())
        Writer w = response.writer
        w << CsvUtil.BOM
        w << CsvUtil.line(header) << '\r\n'
        rows.each { List row -> w << CsvUtil.line(row) << '\r\n' }
        w.flush()
    }

    // ------------------------------------------------------------------ Day 6: PF ও গ্র্যাচুইটি
    @Secured(['ROLE_ADMIN', 'ROLE_ACCOUNTANT'])
    def pfGratuity() {
        [pf: payrollService.pfLedger(), gratuity: payrollService.gratuityReport(),
         pfEmployeePct: PayrollService.PF_EMPLOYEE_PCT, pfEmployerPct: PayrollService.PF_EMPLOYER_PCT,
         eligibleYears: PayrollService.GRATUITY_ELIGIBLE_YEARS]
    }

    private void notFound() {
        flash.error = 'পে-রোল পাওয়া যায়নি।'
        redirect(action: 'index')
    }
}

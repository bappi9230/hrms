package hrms

import grails.plugin.springsecurity.annotation.Secured

/** Day 6: কর্মীর নিজের পে-স্লিপ (শুধু চূড়ান্ত পে-রোলের, শুধু নিজের) - ESS এর শুরু */
@Secured(['ROLE_EMPLOYEE'])
class PayslipController {

    def accessService
    def payslipService

    def index() {
        Employee me = accessService.currentEmployee()
        List<PayrollLine> lines = me == null ? [] : PayrollLine.executeQuery(
                "from PayrollLine l where l.employee = :e and l.payrollRun.status = 'FINALIZED' order by l.payrollRun.payYear desc, l.payrollRun.payMonth desc", [e: me])
        [lines: lines, me: me]
    }

    def show(Long id) {
        PayrollLine line = own(id)
        if (!line) return
        render(view: '/payroll/payslip', model: [line: line, run: line.payrollRun, self: true,
                earnings: PayrollLineItem.findAllByLineAndHeadType(line, 'EARNING', [sort: 'id']),
                deductions: PayrollLineItem.findAllByLineAndHeadType(line, 'DEDUCTION', [sort: 'id'])])
    }

    def pdf(Long id) {
        PayrollLine line = own(id)
        if (!line) return
        byte[] bytes = payslipService.pdf(line)
        response.contentType = 'application/pdf'
        response.setHeader('Content-Disposition', "inline; filename=\"${PayslipService.fileName(line)}\"".toString())
        response.setContentLength(bytes.length)
        response.outputStream.write(bytes)
        response.outputStream.flush()
    }

    /** শুধু নিজের ও চূড়ান্ত পে-স্লিপ; নইলে তালিকায় ফেরত */
    private PayrollLine own(Long id) {
        Employee me = accessService.currentEmployee()
        PayrollLine line = id ? PayrollLine.get(id) : null
        if (line == null || me == null || line.employee.id != me.id || line.payrollRun.status != 'FINALIZED') {
            flash.error = 'পে-স্লিপ পাওয়া যায়নি।'
            redirect(action: 'index')
            return null
        }
        line
    }
}

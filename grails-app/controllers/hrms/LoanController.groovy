package hrms

import grails.plugin.springsecurity.annotation.Secured

import java.time.YearMonth

/** Day 6: ঋণ ও বেতন অগ্রিম - আবেদন (হিসাব বিভাগ/Admin), অনুমোদন (Admin), কিস্তির তালিকা */
@Secured(['ROLE_ADMIN', 'ROLE_ACCOUNTANT'])
class LoanController {

    static allowedMethods = [save: 'POST', approve: 'POST', reject: 'POST', cancel: 'POST', settle: 'POST']

    def loanService

    def index() {
        String status = Loan.STATUSES.contains(params.status) ? params.status : null
        List<Loan> loans = status ? Loan.findAllByStatus(status, [sort: 'id', order: 'desc']) : Loan.list(sort: 'id', order: 'desc')
        [loans: loans, status: status, outstanding: loans.collectEntries { Loan l -> [(l.id): l.status == 'ACTIVE' ? loanService.outstanding(l) : 0.0] }]
    }

    def create() {
        YearMonth next = YearMonth.now(AttendanceService.ZONE)
        // এ মাসের পে-রোল চূড়ান্ত হয়ে গেলে পরের মাস
        PayrollRun run = PayrollRun.findByPayYearAndPayMonth(next.year, next.monthValue)
        if (run?.status == 'FINALIZED') next = next.plusMonths(1)
        [employees: Employee.findAllByStatusNotEqual('INACTIVE', [sort: 'name']), defaultYear: next.year, defaultMonth: next.monthValue,
         maxLoan: LoanService.MAX_LOAN_GROSS_MULTIPLE, maxAdvance: LoanService.MAX_ADVANCE_GROSS_MULTIPLE,
         maxInstallmentPct: LoanService.MAX_INSTALLMENT_PCT_OF_GROSS, form: [:]]
    }

    def save() {
        Employee emp = params.long('employee') ? Employee.get(params.long('employee')) : null
        BigDecimal amount = decimal(params.amount)
        Map r = loanService.create(emp, params.loanType as String, amount, params.int('installments'),
                params.int('startYear'), params.int('startMonth'), params.reason as String)
        if (r.ok) {
            flash.success = r.message
            redirect(action: 'show', id: (r.loan as Loan).id)
        } else {
            flash.error = r.message
            YearMonth ym = YearMonth.now(AttendanceService.ZONE)
            render(view: 'create', model: [employees: Employee.findAllByStatusNotEqual('INACTIVE', [sort: 'name']),
                    defaultYear: params.int('startYear') ?: ym.year, defaultMonth: params.int('startMonth') ?: ym.monthValue,
                    maxLoan: LoanService.MAX_LOAN_GROSS_MULTIPLE, maxAdvance: LoanService.MAX_ADVANCE_GROSS_MULTIPLE,
                    maxInstallmentPct: LoanService.MAX_INSTALLMENT_PCT_OF_GROSS, form: params])
        }
    }

    def show(Long id) {
        Loan loan = id ? Loan.get(id) : null
        if (!loan) { notFound(); return }
        [loan: loan, installments: loanService.installmentsOf(loan), outstanding: loanService.outstanding(loan)]
    }

    @Secured(['ROLE_ADMIN'])
    def approve(Long id) {
        act(id) { Loan l -> loanService.approve(l, params.note as String) }
    }

    @Secured(['ROLE_ADMIN'])
    def reject(Long id) {
        act(id) { Loan l -> loanService.reject(l, params.note as String) }
    }

    def cancel(Long id) {
        act(id) { Loan l -> loanService.cancel(l) }
    }

    @Secured(['ROLE_ADMIN'])
    def settle(Long id) {
        act(id) { Loan l -> loanService.settle(l, params.note as String) }
    }

    private void act(Long id, Closure<Map> action) {
        Loan loan = id ? Loan.get(id) : null
        if (!loan) { notFound(); return }
        Map r = action.call(loan)
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'show', id: id)
    }

    private static BigDecimal decimal(def v) {
        try {
            v?.toString()?.trim() ? new BigDecimal(v.toString().trim()) : null
        } catch (NumberFormatException ignored) {
            null
        }
    }

    private void notFound() {
        flash.error = 'ঋণ পাওয়া যায়নি।'
        redirect(action: 'index')
    }
}

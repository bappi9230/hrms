package hrms

import grails.plugin.springsecurity.annotation.Secured
import org.springframework.web.multipart.MultipartFile

import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Day 8: খরচের দাবি - জমা, ম্যানেজার ও হিসাব বিভাগের অনুমোদন, পরিশোধ, রসিদ ডাউনলোড */
@Secured(['isAuthenticated()'])
class ExpenseController {

    static allowedMethods = [save: 'POST', approve: 'POST', reject: 'POST', pay: 'POST', cancel: 'POST']

    def accessService
    def expenseService

    private static LocalDate today() { LocalDate.now(AttendanceService.ZONE) }

    def index() {
        Employee me = accessService.currentEmployee()
        boolean seesAll = AccessService.has('ROLE_ADMIN,ROLE_HR,ROLE_ACCOUNTANT')
        boolean approver = accessService.isApprover() || AccessService.has('ROLE_ACCOUNTANT')
        // scope: mine | approvals | all
        String scope = params.scope in ['mine', 'approvals', 'all'] ? params.scope : (me ? 'mine' : 'approvals')
        if (scope == 'all' && !seesAll) scope = 'mine'
        if (scope == 'approvals' && !approver) scope = 'mine'
        if (scope == 'mine' && me == null) scope = 'approvals'
        String status = ExpenseClaim.STATUSES.contains(params.status) ? params.status : null

        List<ExpenseClaim> claims
        if (scope == 'approvals') {
            claims = expenseService.pendingClaimsForMe() + expenseService.payableClaims()
        } else {
            claims = ExpenseClaim.createCriteria().list {
                if (scope == 'mine') eq('employee', me)
                if (status) eq('status', status)
                order('id', 'desc')
            } as List<ExpenseClaim>
        }
        BigDecimal total = claims.findAll { it.status in ['APPROVED', 'PAID', 'PENDING_MANAGER', 'PENDING_ACCOUNTS'] }.sum { it.amount } as BigDecimal ?: 0.0
        [claims: claims, scope: scope, status: status, seesAll: seesAll, approver: approver, total: total, hasEmployee: me != null,
         pendingCount: approver ? expenseService.pendingClaimsForMe().size() : 0]
    }

    def create() {
        if (!accessService.currentEmployee()) { noProfile(); return }
        render(view: 'create', model: formModel(new ExpenseClaim(expenseDate: today(), category: params.category ?: 'OTHER'), [:]))
    }

    def save() {
        Employee me = accessService.currentEmployee()
        if (!me) { noProfile(); return }
        ExpenseClaim c = new ExpenseClaim(employee: me, category: params.category ?: 'OTHER', title: params.title as String,
                expenseDate: parseDate(params.expenseDate as String), amount: decimal(params.amount),
                description: params.description as String,
                travel: params.long('travel') ? TravelRequest.get(params.long('travel')) : null)
        MultipartFile file = request.getFile('receipt')
        Map r = expenseService.submitClaim(c, file)
        if (r.ok) {
            flash.success = r.message
            redirect(action: 'show', id: c.id)
        } else {
            flash.error = r.message
            render(view: 'create', model: formModel(c, params))
        }
    }

    def show(Long id) {
        ExpenseClaim c = id ? ExpenseClaim.get(id) : null
        if (!c || !expenseService.canSee(c.employee)) { notFound(); return }
        boolean own = accessService.currentEmployee()?.id == c.employee.id
        boolean accounts = AccessService.has('ROLE_ADMIN,ROLE_ACCOUNTANT')
        List<Map> steps = [[label: 'জমা', done: true],
                           [label: 'ম্যানেজার', done: c.status in ['PENDING_ACCOUNTS', 'APPROVED', 'PAID']],
                           [label: 'হিসাব বিভাগ', done: c.status in ['APPROVED', 'PAID']],
                           [label: 'পরিশোধ', done: c.status == 'PAID']]
        [claim: c, own: own, steps: steps,
         canManagerAct: c.status == 'PENDING_MANAGER' && accessService.canManage(c.employee),
         canAccountsAct: c.status == 'PENDING_ACCOUNTS' && accounts && !own,
         canPay: c.status == 'APPROVED' && accounts && !own,
         canCancel: (own || accessService.isHrAdmin()) && c.status in ['PENDING_MANAGER', 'PENDING_ACCOUNTS'],
         today: today()]
    }

    /** ম্যানেজার-ধাপ বা হিসাব-ধাপ, দাবির বর্তমান অবস্থা অনুযায়ী */
    def approve(Long id) { decide(id, true) }

    def reject(Long id) { decide(id, false) }

    private void decide(Long id, boolean approve) {
        ExpenseClaim c = id ? ExpenseClaim.get(id) : null
        if (!c || !expenseService.canSee(c.employee)) { notFound(); return }
        Map r = c.status == 'PENDING_ACCOUNTS' ? expenseService.accountsDecide(c, approve, params.note as String)
                : expenseService.managerDecide(c, approve, params.note as String)
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'show', id: id)
    }

    def pay(Long id) {
        ExpenseClaim c = id ? ExpenseClaim.get(id) : null
        if (!c || !expenseService.canSee(c.employee)) { notFound(); return }
        Map r = expenseService.markPaid(c, parseDate(params.paidDate as String) ?: today(), params.paymentRef as String)
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'show', id: id)
    }

    def cancel(Long id) {
        ExpenseClaim c = id ? ExpenseClaim.get(id) : null
        if (!c || !expenseService.canSee(c.employee)) { notFound(); return }
        Map r = expenseService.cancelClaim(c)
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'show', id: id)
    }

    def receipt(Long id) {
        ExpenseClaim c = id ? ExpenseClaim.get(id) : null
        if (!c || !c.hasReceipt || !expenseService.canSee(c.employee)) { response.sendError(404); return }
        response.contentType = c.receiptType ?: 'application/octet-stream'
        response.setHeader('Content-Disposition', "attachment; filename*=UTF-8''" + URLEncoder.encode(c.receiptName ?: 'receipt', 'UTF-8').replace('+', '%20'))
        response.setHeader('X-Content-Type-Options', 'nosniff')
        response.outputStream << c.receiptData
        response.outputStream.flush()
    }

    // ------------------------------------------------------------------ helpers
    private Map formModel(ExpenseClaim c, def form) {
        Employee me = accessService.currentEmployee()
        [claim: c, categories: ExpenseClaim.CATEGORIES, form: form,
         travels: me ? TravelRequest.findAllByEmployeeAndStatus(me, 'APPROVED', [sort: 'fromDate', order: 'desc']) : [],
         receiptAbove: ExpenseClaim.RECEIPT_REQUIRED_ABOVE, today: today(), minDate: today().minusDays(ExpenseClaim.MAX_AGE_DAYS)]
    }

    private void noProfile() {
        flash.error = 'আপনার লগইনের সাথে কোনো কর্মী প্রোফাইল যুক্ত নেই, তাই দাবি করা যাবে না।'
        redirect(action: 'index')
    }

    private static BigDecimal decimal(def v) {
        try { v?.toString()?.trim() ? new BigDecimal(v.toString().trim()) : null } catch (NumberFormatException ignored) { null }
    }

    private static LocalDate parseDate(String s) {
        try { s ? LocalDate.parse(s) : null } catch (DateTimeParseException ignored) { null }
    }

    private void notFound() {
        flash.error = 'দাবি পাওয়া যায়নি বা দেখার অনুমতি নেই।'
        redirect(action: 'index')
    }
}

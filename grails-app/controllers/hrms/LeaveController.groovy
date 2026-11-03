package hrms

import grails.plugin.springsecurity.annotation.Secured

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeParseException

/** Day 4: ছুটি - ব্যালেন্স, আবেদন, অনুমোদন, দলের ক্যালেন্ডার */
@Secured(['isAuthenticated()'])
class LeaveController {

    static allowedMethods = [save: 'POST', cancel: 'POST', approve: 'POST', reject: 'POST']

    def accessService
    def leaveService

    /** আমার ব্যালেন্স কার্ড ও আমার আবেদনের তালিকা */
    def index() {
        Employee me = accessService.currentEmployee()
        int year = params.int('year') ?: LocalDate.now(AttendanceService.ZONE).year
        [
            me       : me,
            year     : year,
            balances : me ? leaveService.ensureBalances(me, year).collect { leaveService.view(it) } : [],
            requests : me ? leaveService.myRequests(me) : []
        ]
    }

    def apply() {
        Employee me = accessService.currentEmployee()
        if (!me) { noEmployee(); return }
        formModel(me)
    }

    def save() {
        Employee me = accessService.currentEmployee()
        if (!me) { noEmployee(); return }

        LocalDate from = parseDate(params.fromDate as String)
        LocalDate to = parseDate(params.toDate as String)
        LeaveType type = params.long('leaveType') ? LeaveType.get(params.long('leaveType')) : null

        Map r = leaveService.apply(me, type, from, to, params.reason as String)
        if (!r.ok) {
            flash.error = r.message
            render(view: 'apply', model: formModel(me) + [form: params])
            return
        }
        flash.success = r.message
        redirect(action: 'show', id: (r.request as LeaveRequest).id)
    }

    def show(Long id) {
        LeaveRequest r = id ? LeaveRequest.get(id) : null
        if (!r) { notFound(); return }
        if (!canSee(r)) { redirect(controller: 'auth', action: 'denied'); return }
        [
            leave     : r,
            canDecide : leaveService.canDecide(r),
            canCancel : leaveService.canCancel(r),
            balance   : leaveService.view(leaveService.ensureBalance(r.employee, r.leaveType, r.fromDate.year))
        ]
    }

    def cancel(Long id) {
        LeaveRequest r = id ? LeaveRequest.get(id) : null
        if (!r) { notFound(); return }
        if (!canSee(r)) { redirect(controller: 'auth', action: 'denied'); return }
        Map res = leaveService.cancel(r)
        flash[res.ok ? 'success' : 'error'] = res.message
        redirect(action: params.back == 'show' ? 'show' : 'index', id: params.back == 'show' ? id : null)
    }

    /** আমার অনুমোদনের অপেক্ষায় থাকা আবেদন */
    def approvals() {
        User u = accessService.currentUser()
        [
            pending: leaveService.pendingForMe(),
            recent : u ? LeaveApproval.findAllByActedBy(u.username, [sort: 'id', order: 'desc', max: 15]) : []
        ]
    }

    def approve(Long id) {
        LeaveRequest r = id ? LeaveRequest.get(id) : null
        if (!r) { notFound(); return }
        Map res = leaveService.approve(r, params.remarks as String)
        flash[res.ok ? 'success' : 'error'] = res.message
        backAfterDecision(id)
    }

    def reject(Long id) {
        LeaveRequest r = id ? LeaveRequest.get(id) : null
        if (!r) { notFound(); return }
        Map res = leaveService.reject(r, params.remarks as String)
        flash[res.ok ? 'success' : 'error'] = res.message
        backAfterDecision(id)
    }

    /** দলের ছুটির ক্যালেন্ডার (মাস অনুযায়ী) */
    def calendar() {
        YearMonth month
        try {
            month = params.month ? YearMonth.parse(params.month as String) : YearMonth.now(AttendanceService.ZONE)
        } catch (DateTimeParseException ignored) {
            month = YearMonth.now(AttendanceService.ZONE)
        }
        Map data = leaveService.calendar(month)
        [month: month, prev: month.minusMonths(1), next: month.plusMonths(1), days: data.days, rows: data.rows,
         seeDetails: accessService.isApprover()]
    }

    /** HR/Admin: সব আবেদন (স্ট্যাটাস ফিল্টারসহ) */
    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def all() {
        String status = params.status in LeaveRequest.STATUSES ? params.status : null
        List items = LeaveRequest.createCriteria().list(max: 100) {
            if (status) eq('status', status)
            order('appliedAt', 'desc')
        }
        [items: items, status: status, statuses: LeaveRequest.STATUSES]
    }

    /** HR/Admin: সব কর্মীর ছুটির ব্যালেন্স */
    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def balances() {
        int year = params.int('year') ?: LocalDate.now(AttendanceService.ZONE).year
        List<LeaveType> types = LeaveType.findAllByActive(true, [sort: 'id'])
        List rows = Employee.list(sort: 'name').collect { Employee e ->
            Map<Long, Map> byType = [:]
            leaveService.ensureBalances(e, year).each { LeaveBalance b -> byType[b.leaveType.id] = leaveService.view(b) }
            [employee: e, byType: byType]
        }
        [year: year, types: types, rows: rows]
    }

    // ------------------------------------------------------------------ helpers
    private Map formModel(Employee me) {
        int year = LocalDate.now(AttendanceService.ZONE).year
        [
            me      : me,
            types   : LeaveType.findAllByActive(true, [sort: 'id']).findAll { LeaveType t -> t.eligible(me) },
            balances: leaveService.ensureBalances(me, year).collect { leaveService.view(it) },
            form    : [:]
        ]
    }

    private boolean canSee(LeaveRequest r) {
        accessService.canView(r.employee) || leaveService.canDecide(r)
    }

    private void backAfterDecision(Long id) {
        if (params.back == 'show') redirect(action: 'show', id: id)
        else redirect(action: 'approvals')
    }

    private static LocalDate parseDate(String s) {
        try { s ? LocalDate.parse(s) : null } catch (DateTimeParseException ignored) { null }
    }

    private void noEmployee() {
        flash.warning = 'এই অ্যাকাউন্টের সাথে কোনো কর্মীর প্রোফাইল যুক্ত নেই, তাই ছুটির আবেদন করা যাবে না।'
        redirect(controller: 'dashboard', action: 'index')
    }

    private void notFound() {
        flash.error = 'ছুটির আবেদন পাওয়া যায়নি।'
        redirect(action: 'index')
    }
}

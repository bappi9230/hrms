package hrms

import grails.plugin.springsecurity.annotation.Secured
import org.springframework.beans.factory.annotation.Value

import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Day 9: চাকরি ছাড়া - পদত্যাগ, ম্যানেজার/HR সিদ্ধান্ত, ক্লিয়ারেন্স, চূড়ান্ত নিষ্পত্তি, চিঠি */
@Secured(['isAuthenticated()'])
class OffboardingController {

    static allowedMethods = [save: 'POST', start: 'POST', managerDecision: 'POST', hrDecision: 'POST', withdraw: 'POST', cancelExit: 'POST',
                             tick: 'POST', reopenItem: 'POST', prepare: 'POST', adjust: 'POST', approveSettlement: 'POST', pay: 'POST', complete: 'POST']

    @Value('${hrms.company.name:HRMS Demo Ltd.}')
    String companyName

    def accessService
    def offboardingService
    def settlementService

    private static LocalDate today() { LocalDate.now(AttendanceService.ZONE) }

    // ------------------------------------------------------------------ তালিকা
    def index() {
        Employee me = accessService.currentEmployee()
        boolean seesAll = AccessService.has('ROLE_ADMIN,ROLE_HR,ROLE_ACCOUNTANT')
        boolean isManager = AccessService.has('ROLE_MANAGER')
        boolean approver = accessService.isApprover() || AccessService.has('ROLE_ACCOUNTANT')
        String scope = params.scope in ['mine', 'approvals', 'team', 'all'] ? params.scope : (me ? 'mine' : 'approvals')
        if (scope == 'all' && !seesAll) scope = 'mine'
        if (scope == 'approvals' && !approver) scope = 'mine'
        if (scope == 'team' && !isManager) scope = 'mine'
        if (scope == 'mine' && me == null) scope = 'approvals'
        String status = Separation.STATUSES.contains(params.status) ? params.status : null

        List<Separation> list
        if (scope == 'approvals') {
            list = (offboardingService.pendingForMe() + settlementService.pendingForAccounts().collect { FinalSettlement f -> f.separation }).unique()
        } else {
            Set<Long> teamIds = scope == 'team' ? (accessService.approvableIds() ?: ([] as Set<Long>)) : null
            list = Separation.createCriteria().list {
                if (scope == 'mine') eq('employee', me)
                if (scope == 'team') inList('employee.id', teamIds ? (teamIds as List) : [-1L])
                if (status) eq('status', status)
                order('id', 'desc')
            } as List<Separation>
        }
        [separations: list, scope: scope, status: status, seesAll: seesAll, isManager: isManager, approver: approver, hasEmployee: me != null,
         canStart: accessService.isHrAdmin(), pendingCount: approver ? (offboardingService.pendingForMe().size() + settlementService.pendingForAccounts().size()) : 0]
    }

    // ------------------------------------------------------------------ নতুন
    def apply() {
        Employee me = accessService.currentEmployee()
        if (!me) { noProfile(); return }
        int notice = OffboardingService.noticeDaysFor(me)
        [me: me, notice: notice, minDay: today().plusDays(notice), reasons: Separation.REASONS, form: [:],
         live: Separation.countByEmployeeAndStatusInList(me, Separation.LIVE_STATUSES)]
    }

    def save() {
        Employee me = accessService.currentEmployee()
        if (!me) { noProfile(); return }
        Map r = offboardingService.submit(me, params.reasonCategory as String, params.reason as String, parseDate(params.lastDay as String))
        flash[r.ok ? 'success' : 'error'] = r.message
        if (r.ok) {
            redirect(action: 'show', id: r.separation.id)
        } else {
            int notice = OffboardingService.noticeDaysFor(me)
            render(view: 'apply', model: [me: me, notice: notice, minDay: today().plusDays(notice), reasons: Separation.REASONS, form: params,
                                          live: Separation.countByEmployeeAndStatusInList(me, Separation.LIVE_STATUSES)])
        }
    }

    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def startForm() {
        [employees: startableEmployees(), types: Separation.TYPES, reasons: Separation.REASONS, form: params, tomorrow: today()]
    }

    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def start() {
        Employee emp = params.long('employee') ? Employee.get(params.long('employee')) : null
        Map r = offboardingService.hrInitiate(emp, params.exitType as String, params.reasonCategory as String, params.reason as String, parseDate(params.lastDay as String))
        flash[r.ok ? 'success' : 'error'] = r.message
        if (r.ok) {
            redirect(action: 'show', id: r.separation.id)
        } else {
            render(view: 'startForm', model: [employees: startableEmployees(), types: Separation.TYPES, reasons: Separation.REASONS, form: params, tomorrow: today()])
        }
    }

    // ------------------------------------------------------------------ কেসের পাতা
    def show(Long id) {
        Separation s = id ? Separation.get(id) : null
        if (!s || !offboardingService.canSee(s)) { notFound(); return }
        boolean own = accessService.currentEmployee()?.id == s.employee.id
        boolean hr = accessService.isHrAdmin() && !own
        boolean accounts = AccessService.has('ROLE_ADMIN,ROLE_ACCOUNTANT') && !own
        List<ClearanceItem> items = s.status in ['ACCEPTED', 'COMPLETED'] ? offboardingService.clearanceOf(s) : []
        FinalSettlement fs = settlementService.settlementOf(s)
        Map progress = offboardingService.clearanceProgress(s)
        boolean accepted = s.status == 'ACCEPTED'
        [separation: s, own: own, items: items, progress: progress, settlement: fs, today: today(),
         canTick: items.collectEntries { ClearanceItem i -> [(i.id): offboardingService.canTick(i)] },
         canManagerAct: s.status == 'PENDING_MANAGER' && accessService.canManage(s.employee),
         canHrAct: s.status in ['PENDING_MANAGER', 'PENDING_HR'] && hr,
         canWithdraw: own && s.status in ['PENDING_MANAGER', 'PENDING_HR'],
         canCancel: hr && s.status in Separation.LIVE_STATUSES && (fs == null || fs.status == 'DRAFT'),
         canPrepare: hr && accepted && (fs == null || fs.status == 'DRAFT'),
         canAdjust: hr && accepted && fs != null && fs.status == 'DRAFT',
         canApproveSettlement: accounts && accepted && fs != null && fs.status == 'DRAFT',
         canPay: accounts && accepted && fs != null && fs.status == 'APPROVED',
         canComplete: hr && accepted,
         canLetter: own || accessService.isHrAdmin(),
         minLastDay: s.noticeDate.plusDays(s.noticeDays)]
    }

    def managerDecision(Long id) {
        act(id) { Separation s -> offboardingService.managerDecide(s, params.decision == 'approve', params.note as String) }
    }

    def hrDecision(Long id) {
        act(id) { Separation s ->
            offboardingService.hrDecide(s, params.decision == 'approve', params.note as String, parseDate(params.lastDay as String), params.waive != null)
        }
    }

    def withdraw(Long id) { act(id) { Separation s -> offboardingService.withdraw(s) } }

    def cancelExit(Long id) { act(id) { Separation s -> offboardingService.cancelExit(s, params.note as String) } }

    // ------------------------------------------------------------------ ক্লিয়ারেন্স
    /** id = ClearanceItem */
    def tick(Long id) {
        ClearanceItem item = id ? ClearanceItem.get(id) : null
        if (!item || !offboardingService.canSee(item.separation)) { notFound(); return }
        Map r = offboardingService.tick(item, params.remarks as String)
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'show', id: item.separation.id, fragment: 'clearance')
    }

    def reopenItem(Long id) {
        ClearanceItem item = id ? ClearanceItem.get(id) : null
        if (!item || !offboardingService.canSee(item.separation)) { notFound(); return }
        Map r = offboardingService.reopen(item)
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'show', id: item.separation.id, fragment: 'clearance')
    }

    // ------------------------------------------------------------------ চূড়ান্ত নিষ্পত্তি (id = Separation)
    def prepare(Long id) { act(id, 'settlement') { Separation s -> settlementService.prepare(s) } }

    def adjust(Long id) {
        act(id, 'settlement') { Separation s ->
            FinalSettlement fs = settlementService.settlementOf(s)
            fs ? settlementService.saveAdjustment(fs, decimal(params.adjustment), params.note as String) : [ok: false, message: 'আগে নিষ্পত্তির হিসাব তৈরি করুন।']
        }
    }

    def approveSettlement(Long id) {
        act(id, 'settlement') { Separation s ->
            FinalSettlement fs = settlementService.settlementOf(s)
            fs ? settlementService.approve(fs) : [ok: false, message: 'নিষ্পত্তির হিসাব নেই।']
        }
    }

    def pay(Long id) {
        act(id, 'settlement') { Separation s ->
            FinalSettlement fs = settlementService.settlementOf(s)
            fs ? settlementService.markPaid(fs, parseDate(params.paidDate as String) ?: today(), params.paymentRef as String) : [ok: false, message: 'নিষ্পত্তির হিসাব নেই।']
        }
    }

    def complete(Long id) { act(id) { Separation s -> offboardingService.complete(s, params.exitNote as String) } }

    // ------------------------------------------------------------------ চিঠি (প্রিন্টযোগ্য)
    /** kind = acceptance | relieving | experience */
    def letter(Long id, String kind) {
        Separation s = id ? Separation.get(id) : null
        boolean own = s != null && accessService.currentEmployee()?.id == s.employee.id
        if (!s || !(own || accessService.isHrAdmin())) { notFound(); return }
        boolean ok = kind == 'acceptance' ? s.status in ['ACCEPTED', 'COMPLETED'] : (kind in ['relieving', 'experience'] && s.status == 'COMPLETED')
        if (!ok) {
            flash.error = 'এই চিঠি এখনো তৈরি করা যাবে না (পদত্যাগ গৃহীত বা কেস সম্পন্ন হতে হবে)।'
            redirect(action: 'show', id: id)
            return
        }
        [separation: s, kind: kind, company: companyName, today: today()]
    }

    // ------------------------------------------------------------------ helpers
    private void act(Long id, String fragment = null, Closure<Map> action) {
        Separation s = id ? Separation.get(id) : null
        if (!s || !offboardingService.canSee(s)) { notFound(); return }
        Map r = action.call(s)
        flash[r.ok ? 'success' : 'error'] = r.message
        if (fragment) redirect(action: 'show', id: id, fragment: fragment) else redirect(action: 'show', id: id)
    }

    private List<Employee> startableEmployees() {
        Employee.findAllByStatusNotEqual('INACTIVE', [sort: 'name']).findAll { Employee e ->
            accessService.canManage(e) && Separation.countByEmployeeAndStatusInList(e, Separation.LIVE_STATUSES) == 0
        }
    }

    private void noProfile() {
        flash.error = 'আপনার লগইনের সাথে কোনো কর্মী প্রোফাইল যুক্ত নেই।'
        redirect(action: 'index')
    }

    private void notFound() {
        flash.error = 'কেস পাওয়া যায়নি বা দেখার অনুমতি নেই।'
        redirect(action: 'index')
    }

    private static BigDecimal decimal(def v) {
        try { v?.toString()?.trim() ? new BigDecimal(v.toString().trim()) : null } catch (NumberFormatException ignored) { null }
    }

    private static LocalDate parseDate(String s) {
        try { s ? LocalDate.parse(s) : null } catch (DateTimeParseException ignored) { null }
    }
}

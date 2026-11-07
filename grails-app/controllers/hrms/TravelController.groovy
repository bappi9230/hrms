package hrms

import grails.plugin.springsecurity.annotation.Secured

import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Day 8: ভ্রমণের অনুরোধ - জমা, ম্যানেজার/HR এর অনুমোদন, প্রত্যাহার */
@Secured(['isAuthenticated()'])
class TravelController {

    static allowedMethods = [save: 'POST', approve: 'POST', reject: 'POST', cancel: 'POST']

    def accessService
    def expenseService

    private static LocalDate today() { LocalDate.now(AttendanceService.ZONE) }

    def index() {
        Employee me = accessService.currentEmployee()
        boolean seesAll = AccessService.has('ROLE_ADMIN,ROLE_HR,ROLE_ACCOUNTANT')
        boolean approver = accessService.isApprover()
        String scope = params.scope in ['mine', 'approvals', 'all'] ? params.scope : (me ? 'mine' : 'approvals')
        if (scope == 'all' && !seesAll) scope = 'mine'
        if (scope == 'approvals' && !approver) scope = 'mine'
        if (scope == 'mine' && me == null) scope = 'approvals'

        List<TravelRequest> list
        if (scope == 'approvals') {
            list = expenseService.pendingTravelForMe()
        } else {
            list = TravelRequest.createCriteria().list {
                if (scope == 'mine') eq('employee', me)
                order('fromDate', 'desc')
            } as List<TravelRequest>
        }
        [travels: list, scope: scope, seesAll: seesAll, approver: approver, me: me, today: today(), modes: TravelRequest.MODES, form: [:],
         canAct: list.collectEntries { TravelRequest t -> [(t.id): t.status == 'PENDING' && accessService.canManage(t.employee)] },
         canCancel: list.collectEntries { TravelRequest t -> [(t.id): (accessService.currentEmployee()?.id == t.employee.id || accessService.isHrAdmin()) && t.status in ['PENDING', 'APPROVED']] },
         claimCount: list.collectEntries { TravelRequest t -> [(t.id): ExpenseClaim.countByTravelAndStatusInList(t, ExpenseClaim.LIVE_STATUSES)] }]
    }

    def save() {
        Employee me = accessService.currentEmployee()
        TravelRequest t = new TravelRequest(employee: me, destination: params.destination as String, purpose: params.purpose as String,
                fromDate: parseDate(params.fromDate as String), toDate: parseDate(params.toDate as String),
                travelMode: params.travelMode ?: 'BUS', estimatedCost: decimal(params.estimatedCost))
        Map r = expenseService.submitTravel(t)
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'index', params: [scope: 'mine'])
    }

    def approve(Long id) { act(id) { TravelRequest t -> expenseService.decideTravel(t, true, params.note as String) } }

    def reject(Long id) { act(id) { TravelRequest t -> expenseService.decideTravel(t, false, params.note as String) } }

    def cancel(Long id) { act(id) { TravelRequest t -> expenseService.cancelTravel(t) } }

    private void act(Long id, Closure<Map> action) {
        TravelRequest t = id ? TravelRequest.get(id) : null
        if (!t || !expenseService.canSee(t.employee)) {
            flash.error = 'অনুরোধ পাওয়া যায়নি বা দেখার অনুমতি নেই।'
            redirect(action: 'index')
            return
        }
        Map r = action.call(t)
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'index', params: [scope: params.scope ?: 'mine'])
    }

    private static BigDecimal decimal(def v) {
        try { v?.toString()?.trim() ? new BigDecimal(v.toString().trim()) : null } catch (NumberFormatException ignored) { null }
    }

    private static LocalDate parseDate(String s) {
        try { s ? LocalDate.parse(s) : null } catch (DateTimeParseException ignored) { null }
    }
}

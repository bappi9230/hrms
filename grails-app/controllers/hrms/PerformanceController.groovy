package hrms

import grails.plugin.springsecurity.annotation.Secured

import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Day 8: লক্ষ্য/KPI, মূল্যায়ন চক্র ও মূল্যায়ন ফর্ম (self + manager) */
@Secured(['isAuthenticated()'])
class PerformanceController {

    static allowedMethods = [saveGoal: 'POST', updateGoal: 'POST', cancelGoal: 'POST', submitSelf: 'POST', submitManager: 'POST',
                             saveCycle: 'POST', openCycle: 'POST', syncCycle: 'POST', closeCycle: 'POST', deleteCycle: 'POST']

    def accessService
    def performanceService

    // ------------------------------------------------------------------ লক্ষ্য
    def index() {
        Employee me = accessService.currentEmployee()
        boolean wide = accessService.isHrAdmin() || accessService.approvableIds()
        // scope: mine (ডিফল্ট) | team (অধীনের / HR-Admin হলে সবার)
        String scope = (params.scope == 'team' && wide) || me == null ? 'team' : 'mine'
        String status = Goal.STATUSES.contains(params.status) ? params.status : null
        Set<Long> ids = performanceService.visibleGoalOwnerIds()
        List<Goal> goals = Goal.createCriteria().list {
            if (scope == 'mine') {
                eq('employee', me)
            } else if (ids != null) {
                inList('employee.id', ids ?: [-1L])
            }
            if (status) eq('status', status)
            order('dueDate', 'asc')
        } as List<Goal>
        [goals: goals, scope: scope, status: status, wide: wide, me: me,
         usedWeight: me ? performanceService.usedWeight(me, LocalDate.now(AttendanceService.ZONE).year) : 0,
         assignable: assignableEmployees(me), today: LocalDate.now(AttendanceService.ZONE),
         canEdit: goals.collectEntries { Goal g -> [(g.id): performanceService.canEditGoalsOf(g.employee)] },
         form: [:]]
    }

    def saveGoal() {
        Employee emp = params.long('employee') ? Employee.get(params.long('employee')) : accessService.currentEmployee()
        Map r = performanceService.createGoal(emp, params.title as String, params.description as String, decimal(params.targetValue),
                params.unit as String, params.int('weight'), parseDate(params.dueDate as String))
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'index', params: [scope: (emp?.id != accessService.currentEmployee()?.id) ? 'team' : 'mine'])
    }

    def updateGoal(Long id) {
        Goal g = id ? Goal.get(id) : null
        if (!g) { notFound(); return }
        Map r = performanceService.updateProgress(g, decimal(params.currentValue))
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'index', params: [scope: params.scope ?: 'mine'])
    }

    def cancelGoal(Long id) {
        Goal g = id ? Goal.get(id) : null
        if (!g) { notFound(); return }
        Map r = performanceService.cancelGoal(g)
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'index', params: [scope: params.scope ?: 'mine'])
    }

    // ------------------------------------------------------------------ মূল্যায়ন
    def appraisals() {
        List<AppraisalCycle> cycles = AppraisalCycle.findAllByStatusNotEqual('DRAFT', [sort: 'startDate', order: 'desc'])
        AppraisalCycle cycle = params.long('cycle') ? AppraisalCycle.get(params.long('cycle')) : (cycles.find { it.status == 'OPEN' } ?: (cycles ? cycles[0] : null))
        if (cycle != null && cycle.status == 'DRAFT') cycle = null
        List<Appraisal> list = performanceService.visibleAppraisals(cycle)
        [cycles: cycles, cycle: cycle, appraisals: list, today: LocalDate.now(AttendanceService.ZONE),
         canReview: list.collectEntries { Appraisal a -> [(a.id): performanceService.canReview(a)] },
         myId: accessService.currentEmployee()?.id]
    }

    def appraisal(Long id) {
        Appraisal a = id ? Appraisal.get(id) : null
        if (!a || !performanceService.canSeeAppraisal(a)) { notFound(true); return }
        boolean own = performanceService.isOwn(a)
        [appraisal: a, own: own, goals: performanceService.goalsFor(a.employee, a.cycle),
         liveScore: performanceService.goalScore(a.employee, a.cycle),
         canSelf: own && a.cycle.status == 'OPEN' && a.status == 'PENDING_SELF',
         canReview: performanceService.canReview(a), ratingLabels: Appraisal.RATING_LABELS,
         today: LocalDate.now(AttendanceService.ZONE)]
    }

    def submitSelf(Long id) {
        Appraisal a = id ? Appraisal.get(id) : null
        if (!a || !performanceService.canSeeAppraisal(a)) { notFound(true); return }
        Map r = performanceService.submitSelf(a, params.int('rating'), params.comment as String)
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'appraisal', id: id)
    }

    def submitManager(Long id) {
        Appraisal a = id ? Appraisal.get(id) : null
        if (!a || !performanceService.canSeeAppraisal(a)) { notFound(true); return }
        Map r = performanceService.submitManager(a, params.int('rating'), params.comment as String)
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'appraisal', id: id)
    }

    // ------------------------------------------------------------------ চক্র (HR/Admin)
    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def cycles() {
        List<AppraisalCycle> list = AppraisalCycle.list(sort: 'startDate', order: 'desc')
        Map<Long, Map> stats = [:]
        list.each { AppraisalCycle c ->
            stats[c.id] = [total: Appraisal.countByCycle(c), done: Appraisal.countByCycleAndStatus(c, 'COMPLETED'),
                           self : Appraisal.countByCycleAndStatus(c, 'PENDING_MANAGER')]
        }
        int year = LocalDate.now(AttendanceService.ZONE).year
        [cycles: list, stats: stats, defaultYear: year]
    }

    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def saveCycle() {
        Map r = performanceService.createCycle(params.name as String, parseDate(params.startDate as String), parseDate(params.endDate as String), parseDate(params.dueDate as String))
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'cycles')
    }

    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def openCycle(Long id) { cycleAction(id) { AppraisalCycle c -> performanceService.openCycle(c) } }

    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def syncCycle(Long id) { cycleAction(id) { AppraisalCycle c -> performanceService.syncCycle(c) } }

    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def closeCycle(Long id) { cycleAction(id) { AppraisalCycle c -> performanceService.closeCycle(c) } }

    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def deleteCycle(Long id) { cycleAction(id) { AppraisalCycle c -> performanceService.deleteCycle(c) } }

    private void cycleAction(Long id, Closure<Map> action) {
        AppraisalCycle c = id ? AppraisalCycle.get(id) : null
        if (!c) { flash.error = 'চক্র পাওয়া যায়নি।'; redirect(action: 'cycles'); return }
        Map r = action.call(c)
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'cycles')
    }

    // ------------------------------------------------------------------ helpers
    /** লক্ষ্য দেওয়া যাবে এমন কর্মী: HR/Admin সবাই, ম্যানেজার নিজের অধীনের + নিজে, কর্মী শুধু নিজে */
    private List<Employee> assignableEmployees(Employee me) {
        Set<Long> ids = performanceService.visibleGoalOwnerIds()
        List<Employee> all = ids == null ? Employee.findAllByStatusNotEqual('INACTIVE', [sort: 'name'])
                : (ids ? Employee.createCriteria().list { inList('id', ids as List); ne('status', 'INACTIVE'); order('name') } as List<Employee> : [])
        all.findAll { Employee e -> performanceService.canEditGoalsOf(e) }
    }

    private static BigDecimal decimal(def v) {
        try { v?.toString()?.trim() ? new BigDecimal(v.toString().trim()) : null } catch (NumberFormatException ignored) { null }
    }

    private static LocalDate parseDate(String s) {
        try { s ? LocalDate.parse(s) : null } catch (DateTimeParseException ignored) { null }
    }

    private void notFound(boolean toAppraisals = false) {
        flash.error = 'তথ্য পাওয়া যায়নি বা দেখার অনুমতি নেই।'
        redirect(action: toAppraisals ? 'appraisals' : 'index')
    }
}

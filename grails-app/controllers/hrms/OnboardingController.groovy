package hrms

import grails.plugin.springsecurity.annotation.Secured

import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Day 7: অনবোর্ডিং চেকলিস্ট, সম্পদ বরাদ্দ ও পরীক্ষাকাল (probation) */
@Secured(['ROLE_ADMIN', 'ROLE_HR'])
class OnboardingController {

    static allowedMethods = [start: 'POST', complete: 'POST', reopen: 'POST', addTask: 'POST', deleteTask: 'POST',
                             issueAsset: 'POST', returnAsset: 'POST', startProbation: 'POST',
                             confirmProbation: 'POST', extendProbation: 'POST', notConfirmProbation: 'POST']

    def onboardingService

    def index() {
        Map<Long, Map> progress = onboardingService.progressByEmployee()
        List<Employee> employees = progress ? Employee.getAll(progress.keySet() as List).findAll { it != null }.sort { Employee e -> e.joinDate }.reverse() : []
        [employees: employees, progress: progress, probations: onboardingService.ongoingProbations(),
         noChecklist: Employee.findAllByJoinDateGreaterThanEquals(LocalDate.now(AttendanceService.ZONE).minusDays(30)).findAll { !progress.containsKey(it.id) }]
    }

    def show(Long id) {
        Employee emp = id ? Employee.get(id) : null
        if (!emp) { notFound(); return }
        List<OnboardingTask> tasks = OnboardingTask.findAllByEmployee(emp, [sort: 'sortOrder'])
        [employee: emp, tasks: tasks, progress: onboardingService.progress(emp),
         assets: AssetAllocation.findAllByEmployee(emp, [sort: 'issuedDate', order: 'desc']),
         probation: ProbationRecord.findByEmployee(emp),
         maxExtensions: ProbationRecord.MAX_EXTENSIONS, categories: OnboardingTemplate.CATEGORIES, owners: OnboardingTemplate.OWNERS, assetTypes: AssetAllocation.TYPES,
         today: LocalDate.now(AttendanceService.ZONE)]
    }

    // ------------------------------------------------------------------ চেকলিস্ট
    def start(Long id) {
        Employee emp = id ? Employee.get(id) : null
        if (!emp) { notFound(); return }
        int n = onboardingService.startOnboarding(emp)
        flash[n ? 'success' : 'warning'] = n ? "${n}টি কাজ নিয়ে চেকলিস্ট তৈরি হয়েছে।".toString() : 'চেকলিস্ট আগেই আছে (বা কোনো সক্রিয় টেমপ্লেট নেই)।'
        redirect(action: 'show', id: id)
    }

    def complete(Long id) { taskAction(id) { OnboardingTask t -> onboardingService.completeTask(t, params.remarks as String) } }

    def reopen(Long id) { taskAction(id) { OnboardingTask t -> onboardingService.reopenTask(t) } }

    def deleteTask(Long id) { taskAction(id) { OnboardingTask t -> onboardingService.deleteTask(t) } }

    private void taskAction(Long id, Closure<Map> action) {
        OnboardingTask t = id ? OnboardingTask.get(id) : null
        if (!t) { notFound(); return }
        Long empId = t.employee.id
        Map res = action.call(t)
        flash[res.ok ? 'success' : 'error'] = res.message
        redirect(action: 'show', id: empId, fragment: 'checklist')
    }

    def addTask(Long id) {
        Employee emp = id ? Employee.get(id) : null
        if (!emp) { notFound(); return }
        Map res = onboardingService.addTask(emp, params.title as String, params.category as String, params.ownerRole as String, parseDate(params.dueDate as String))
        flash[res.ok ? 'success' : 'error'] = res.message
        redirect(action: 'show', id: id, fragment: 'checklist')
    }

    // ------------------------------------------------------------------ সম্পদ
    def issueAsset(Long id) {
        Employee emp = id ? Employee.get(id) : null
        if (!emp) { notFound(); return }
        AssetAllocation a = new AssetAllocation(employee: emp, assetType: params.assetType ?: 'OTHER', assetName: params.assetName?.toString()?.trim(),
                serialNo: params.serialNo?.toString()?.trim() ?: null, issuedDate: parseDate(params.issuedDate as String) ?: LocalDate.now(AttendanceService.ZONE),
                remarks: params.remarks?.toString()?.trim() ?: null)
        Map res = onboardingService.issueAsset(a)
        flash[res.ok ? 'success' : 'error'] = res.message
        redirect(action: 'show', id: id, fragment: 'assets')
    }

    def returnAsset(Long id) {
        AssetAllocation a = id ? AssetAllocation.get(id) : null
        if (!a) { notFound(); return }
        Long empId = a.employee.id
        Map res = onboardingService.returnAsset(a, parseDate(params.returnedDate as String), params.remarks as String)
        flash[res.ok ? 'success' : 'error'] = res.message
        redirect(action: 'show', id: empId, fragment: 'assets')
    }

    // ------------------------------------------------------------------ পরীক্ষাকাল
    def startProbation(Long id) {
        Employee emp = id ? Employee.get(id) : null
        if (!emp) { notFound(); return }
        Map res = onboardingService.startProbationManually(emp, params.int('months'))
        flash[res.ok ? 'success' : 'error'] = res.message
        redirect(action: 'show', id: id, fragment: 'probation')
    }

    def confirmProbation(Long id) { probationAction(id) { ProbationRecord r -> onboardingService.confirmProbation(r, params.note as String) } }

    def extendProbation(Long id) { probationAction(id) { ProbationRecord r -> onboardingService.extendProbation(r, params.int('months'), params.note as String) } }

    def notConfirmProbation(Long id) { probationAction(id) { ProbationRecord r -> onboardingService.notConfirmProbation(r, params.note as String) } }

    private void probationAction(Long id, Closure<Map> action) {
        ProbationRecord r = id ? ProbationRecord.get(id) : null
        if (!r) { notFound(); return }
        Long empId = r.employee.id
        Map res = action.call(r)
        flash[res.ok ? 'success' : 'error'] = res.message
        redirect(action: 'show', id: empId, fragment: 'probation')
    }

    // ------------------------------------------------------------------ helpers
    private static LocalDate parseDate(String s) {
        try { s ? LocalDate.parse(s) : null } catch (DateTimeParseException ignored) { null }
    }

    private void notFound() {
        flash.error = 'তথ্য পাওয়া যায়নি।'
        redirect(action: 'index')
    }
}

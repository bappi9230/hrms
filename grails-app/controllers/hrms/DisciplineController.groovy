package hrms

import grails.plugin.springsecurity.annotation.Secured

import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Day 9: শৃঙ্খলা কেস - খোলা, কর্মীর জবাব, HR এর সিদ্ধান্ত (গোপনীয়) */
@Secured(['isAuthenticated()'])
class DisciplineController {

    static allowedMethods = [save: 'POST', reply: 'POST', close: 'POST']

    def accessService
    def relationsService

    private static LocalDate today() { LocalDate.now(AttendanceService.ZONE) }

    def index() {
        String status = DisciplinaryCase.STATUSES.contains(params.status) ? params.status : null
        [cases: relationsService.visibleCases(status), status: status, canRaise: accessService.isApprover(), myId: accessService.currentEmployee()?.id]
    }

    def create() {
        if (!accessService.isApprover()) { denied(); return }
        [employees: raisable(), types: DisciplinaryCase.TYPES, form: [incidentDate: today().toString(), employee: params.employee], today: today(),
         isHr: accessService.isHrAdmin()]
    }

    def save() {
        if (!accessService.isApprover()) { denied(); return }
        Employee emp = params.long('employee') ? Employee.get(params.long('employee')) : null
        Map r = relationsService.raiseCase(emp, params.caseType as String, parseDate(params.incidentDate as String), params.subject as String, params.description as String)
        flash[r.ok ? 'success' : 'error'] = r.message
        if (r.ok) {
            redirect(action: 'show', id: (r.discipline as DisciplinaryCase).id)
        } else {
            render(view: 'create', model: [employees: raisable(), types: DisciplinaryCase.TYPES, form: params, today: today(), isHr: accessService.isHrAdmin()])
        }
    }

    def show(Long id) {
        DisciplinaryCase c = id ? DisciplinaryCase.get(id) : null
        if (!c || !relationsService.canSeeCase(c)) { notFound(); return }
        boolean own = accessService.currentEmployee()?.id == c.employee.id
        boolean hr = accessService.isHrAdmin() && !own
        [c: c, own: own,
         canRespond: own && c.status == 'OPEN',
         canClose: hr && c.status != 'CLOSED' && !(c.caseType == 'SHOW_CAUSE' && c.status == 'OPEN'),
         waitingResponse: c.caseType == 'SHOW_CAUSE' && c.status == 'OPEN',
         outcomes: DisciplinaryCase.OUTCOMES]
    }

    def reply(Long id) { act(id) { DisciplinaryCase c -> relationsService.respondCase(c, params.response as String) } }

    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def close(Long id) { act(id) { DisciplinaryCase c -> relationsService.closeCase(c, params.outcome as String, params.note as String) } }

    private void act(Long id, Closure<Map> action) {
        DisciplinaryCase c = id ? DisciplinaryCase.get(id) : null
        if (!c || !relationsService.canSeeCase(c)) { notFound(); return }
        Map r = action.call(c)
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'show', id: id)
    }

    private List<Employee> raisable() {
        Employee.findAllByStatusNotEqual('INACTIVE', [sort: 'name']).findAll { Employee e -> accessService.canManage(e) }
    }

    private void denied() {
        flash.error = 'কেস খোলার অনুমতি শুধু HR/Admin ও ম্যানেজারদের।'
        redirect(action: 'index')
    }

    private void notFound() {
        flash.error = 'কেস পাওয়া যায়নি বা দেখার অনুমতি নেই।'
        redirect(action: 'index')
    }

    private static LocalDate parseDate(String s) {
        try { s ? LocalDate.parse(s) : null } catch (DateTimeParseException ignored) { null }
    }
}

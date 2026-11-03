package hrms

import grails.plugin.springsecurity.annotation.Secured

import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Day 7: জনবল চাহিদা - ম্যানেজার/HR তোলে, HR/Admin অনুমোদন করে */
@Secured(['ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER'])
class RequisitionController {

    static allowedMethods = [save: 'POST', approve: 'POST', reject: 'POST', cancel: 'POST', close: 'POST']

    def accessService
    def recruitmentService

    def index() {
        String status = ManpowerRequisition.STATUSES.contains(params.status) ? params.status : null
        String me = accessService.currentUser()?.username
        boolean all = accessService.isHrAdmin()
        List<ManpowerRequisition> list = ManpowerRequisition.createCriteria().list {
            if (!all) eq('requestedBy', me)
            if (status) eq('status', status)
            order('id', 'desc')
        } as List<ManpowerRequisition>
        [requisitions: list, status: status, canDecide: all,
         hired: list.collectEntries { ManpowerRequisition r -> [(r.id): r.hiredCount()] }]
    }

    def create() {
        Employee me = accessService.currentEmployee()
        render(view: 'create', model: formModel(new ManpowerRequisition(department: me?.department, vacancies: 1)))
    }

    def save() {
        ManpowerRequisition r = new ManpowerRequisition(
                title: params.title?.toString()?.trim(),
                department: params.long('department') ? Department.get(params.long('department')) : null,
                designation: params.long('designation') ? Designation.get(params.long('designation')) : null,
                branch: params.long('branch') ? Branch.get(params.long('branch')) : null,
                vacancies: params.int('vacancies'),
                employeeType: params.employeeType ?: 'FULL_TIME',
                minSalary: decimal(params.minSalary), maxSalary: decimal(params.maxSalary),
                expectedJoinDate: parseDate(params.expectedJoinDate as String),
                justification: params.justification?.toString()?.trim())
        Map res = recruitmentService.saveRequisition(r)
        if (res.ok) {
            flash.success = res.message
            redirect(action: 'show', id: r.id)
        } else {
            flash.error = res.message
            render(view: 'create', model: formModel(r))
        }
    }

    def show(Long id) {
        ManpowerRequisition r = id ? ManpowerRequisition.get(id) : null
        if (!r || !canSee(r)) { notFound(); return }
        [requisition: r, hired: r.hiredCount(), postings: JobPosting.findAllByRequisition(r, [sort: 'id', order: 'desc']),
         canDecide: accessService.isHrAdmin(), mine: r.requestedBy == accessService.currentUser()?.username]
    }

    def approve(Long id) { decide(id) { ManpowerRequisition r -> recruitmentService.approveRequisition(r, params.note as String) } }

    def reject(Long id) { decide(id) { ManpowerRequisition r -> recruitmentService.rejectRequisition(r, params.note as String) } }

    def close(Long id) { decide(id) { ManpowerRequisition r -> recruitmentService.closeRequisition(r) } }

    def cancel(Long id) {
        ManpowerRequisition r = id ? ManpowerRequisition.get(id) : null
        if (!r || !canSee(r)) { notFound(); return }
        Map res = recruitmentService.cancelRequisition(r)
        flash[res.ok ? 'success' : 'error'] = res.message
        redirect(action: 'show', id: id)
    }

    /** approve / reject / close শুধু HR ও Admin এর */
    private void decide(Long id, Closure<Map> action) {
        if (!accessService.isHrAdmin()) { redirect(controller: 'auth', action: 'denied'); return }
        ManpowerRequisition r = id ? ManpowerRequisition.get(id) : null
        if (!r) { notFound(); return }
        Map res = action.call(r)
        flash[res.ok ? 'success' : 'error'] = res.message
        redirect(action: 'show', id: id)
    }

    private boolean canSee(ManpowerRequisition r) {
        accessService.isHrAdmin() || r.requestedBy == accessService.currentUser()?.username
    }

    private Map formModel(ManpowerRequisition r) {
        [requisition: r, departments: Department.list(sort: 'name'), designations: Designation.list(sort: 'title'),
         branches: Branch.list(sort: 'name'), types: Employee.TYPES]
    }

    private static BigDecimal decimal(def v) {
        try { v?.toString()?.trim() ? new BigDecimal(v.toString().trim()) : null } catch (NumberFormatException ignored) { null }
    }

    private static LocalDate parseDate(String s) {
        try { s ? LocalDate.parse(s) : null } catch (DateTimeParseException ignored) { null }
    }

    private void notFound() {
        flash.error = 'জনবল চাহিদা পাওয়া যায়নি।'
        redirect(action: 'index')
    }
}

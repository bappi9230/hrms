package hrms

import grails.plugin.springsecurity.annotation.Secured

import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Day 5: বেতন কাঠামো, ভাতা/কর্তন বরাদ্দ, ইনক্রিমেন্ট ও সংশোধনের লগ */
@Secured(['ROLE_ADMIN', 'ROLE_HR', 'ROLE_ACCOUNTANT'])
class SalaryController {

    static allowedMethods = [saveStructure: 'POST', assignHead: 'POST', removeHead: 'POST', saveRevision: 'POST', syncToProfile: 'POST']

    def salaryService

    /** সব কর্মীর বর্তমান বেতন কাঠামো (নাম দিয়ে খোঁজা যায়) */
    def index() {
        String q = params.q?.toString()?.trim()
        List<Employee> emps = Employee.createCriteria().list {
            if (q) ilike('name', ('%' + q + '%').toString())
            order('name', 'asc')
        }
        List rows = emps.collect { Employee e ->
            SalaryStructure s = salaryService.currentStructure(e)
            [employee: e, structure: s, mismatch: e.salary?.compareTo(s.gross) != 0,
             headCount: EmployeeSalaryHead.countByEmployee(e)]
        }
        [rows: rows, q: q]
    }

    /** একজন কর্মীর কাঠামো, খাত, সংশোধনের ইতিহাস */
    def show(Long id) {
        Employee emp = id ? Employee.get(id) : null
        if (!emp) { notFound(); return }
        SalaryStructure cur = salaryService.currentStructure(emp)
        List<EmployeeSalaryHead> assigned = salaryService.headsOf(emp)
        Set<Long> assignedIds = assigned.collect { it.head.id } as Set<Long>
        [
            employee    : emp,
            current     : cur,
            history     : salaryService.history(emp),
            assigned    : assigned,
            freeHeads   : SalaryHead.findAllByActive(true, [sort: 'headType']).findAll { !assignedIds.contains(it.id) },
            revisions   : salaryService.revisions(emp),
            mismatch    : emp.salary?.compareTo(cur.gross) != 0,
            canRevise   : accessAllowsRevise(),
            today       : LocalDate.now(AttendanceService.ZONE),
            revisionTypes: SalaryRevision.TYPES
        ]
    }

    def saveStructure(Long id) {
        Employee emp = id ? Employee.get(id) : null
        if (!emp) { notFound(); return }
        LocalDate eff = parseDate(params.effectiveFrom as String)
        Map r = salaryService.saveStructure(emp, eff, dec('basic'), dec('houseRent'), dec('medical'), dec('conveyance'), params.remarks as String)
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'show', id: id)
    }

    /** প্রোফাইলের বেতনের সাথে মিলিয়ে ডিফল্ট ভাগে নতুন কাঠামো (আজ থেকে) */
    def syncToProfile(Long id) {
        Employee emp = id ? Employee.get(id) : null
        if (!emp) { notFound(); return }
        Map p = salaryService.splitGross(emp.salary)
        Map r = salaryService.saveStructure(emp, LocalDate.now(AttendanceService.ZONE), p.basic as BigDecimal,
                p.houseRent as BigDecimal, p.medical as BigDecimal, p.conveyance as BigDecimal, 'প্রোফাইলের বেতন অনুযায়ী')
        flash[r.ok ? 'success' : 'error'] = r.ok ? 'কাঠামো প্রোফাইলের বেতনের সাথে মেলানো হয়েছে।' : r.message
        redirect(action: 'show', id: id)
    }

    def assignHead(Long id) {
        Employee emp = id ? Employee.get(id) : null
        if (!emp) { notFound(); return }
        SalaryHead head = params.long('headId') ? SalaryHead.get(params.long('headId')) : null
        Map r = salaryService.assignHead(emp, head, dec('amount'))
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'show', id: id)
    }

    def removeHead(Long id) {
        Employee emp = id ? Employee.get(id) : null
        if (!emp) { notFound(); return }
        Map r = salaryService.removeHead(emp, params.long('assignmentId'))
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'show', id: id)
    }

    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def saveRevision(Long id) {
        Employee emp = id ? Employee.get(id) : null
        if (!emp) { notFound(); return }
        Map r = salaryService.revise(emp, params.revisionType as String, parseDate(params.effectiveDate as String),
                dec('newGross'), params.remarks as String)
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'show', id: id)
    }

    /** সব কর্মীর সংশোধন/ইনক্রিমেন্টের লগ */
    def revisions() {
        [items: salaryService.revisions(null, 200)]
    }

    // ------------------------------------------------------------------ helpers
    private boolean accessAllowsRevise() {
        AccessService.has('ROLE_ADMIN,ROLE_HR')
    }

    private BigDecimal dec(String name) {
        String v = params[name]?.toString()?.trim()
        if (!v) return null
        try { new BigDecimal(v) } catch (NumberFormatException ignored) { null }
    }

    private static LocalDate parseDate(String s) {
        try { s ? LocalDate.parse(s) : null } catch (DateTimeParseException ignored) { null }
    }

    private void notFound() {
        flash.error = 'কর্মী পাওয়া যায়নি।'
        redirect(action: 'index')
    }
}

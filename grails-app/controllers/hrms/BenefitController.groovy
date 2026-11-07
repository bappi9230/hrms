package hrms

import grails.plugin.springsecurity.annotation.Secured

import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Day 8: কর্মীর সুবিধা (benefits)। প্ল্যান বানানো: সেটিংস -> সুবিধার প্ল্যান। নথিভুক্তি/শেষ: Admin/HR; সবাই নিজের সুবিধা দেখে। */
@Secured(['isAuthenticated()'])
class BenefitController {

    static allowedMethods = [enroll: 'POST', endBenefit: 'POST']

    def accessService
    def assetService

    def index() {
        Employee me = accessService.currentEmployee()
        boolean seesAll = AccessService.has('ROLE_ADMIN,ROLE_HR,ROLE_ACCOUNTANT')
        String scope = params.scope == 'all' && seesAll ? 'all' : (me ? 'mine' : (seesAll ? 'all' : 'mine'))
        String status = BenefitEnrollment.STATUSES.contains(params.status) ? params.status : null
        List<BenefitEnrollment> list = BenefitEnrollment.createCriteria().list {
            if (scope == 'mine') { if (me != null) eq('employee', me) else eq('id', -1L) }
            if (status) eq('status', status)
            order('startDate', 'desc')
        } as List<BenefitEnrollment>
        boolean canManage = accessService.isHrAdmin()
        [enrollments: list, scope: scope, status: status, seesAll: seesAll, canManage: canManage, hasEmployee: me != null,
         plans: BenefitPlan.findAllByActive(true, [sort: 'name']),
         employees: canManage ? Employee.findAllByStatusNotEqual('INACTIVE', [sort: 'name']) : [],
         today: LocalDate.now(AttendanceService.ZONE)]
    }

    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def enroll() {
        BenefitPlan plan = params.long('plan') ? BenefitPlan.get(params.long('plan')) : null
        Employee emp = params.long('employee') ? Employee.get(params.long('employee')) : null
        Map r = assetService.enrollBenefit(plan, emp, parseDate(params.startDate as String), params.remarks as String)
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'index', params: [scope: 'all'])
    }

    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def endBenefit(Long id) {
        BenefitEnrollment b = id ? BenefitEnrollment.get(id) : null
        if (!b) {
            flash.error = 'তথ্য পাওয়া যায়নি।'
            redirect(action: 'index')
            return
        }
        Map r = assetService.endBenefit(b, parseDate(params.endDate as String))
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'index', params: [scope: 'all'])
    }

    private static LocalDate parseDate(String s) {
        try { s ? LocalDate.parse(s) : null } catch (DateTimeParseException ignored) { null }
    }
}

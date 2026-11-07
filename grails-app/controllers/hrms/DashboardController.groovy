package hrms

import grails.plugin.springsecurity.annotation.Secured

import java.time.LocalDate

@Secured(['isAuthenticated()'])
class DashboardController {

    def accessService
    def leaveService
    def expenseService
    def performanceService
    def offboardingService
    def settlementService

    def index() {
        // Day 9: HR/Admin/হিসাব বিভাগ ছাড়া সবার হোম = আমার পোর্টাল
        if (!AccessService.has('ROLE_ADMIN,ROLE_HR,ROLE_ACCOUNTANT')) {
            redirect(controller: 'portal', action: 'index')
            return
        }
        leaveService.syncStatus()   // আজকের ON_LEAVE অবস্থা হালনাগাদ
        List byDepartment = Employee.createCriteria().list {
            projections {
                groupProperty('department')
                count()
            }
        }.collect { [department: it[0], count: it[1]] }
        .sort { -it.count }

        // approvals waiting for this user (overtime + regularization)
        int pending = 0
        if (accessService.isApprover()) {
            Set<Long> ids = accessService.approvableIds()
            Long myId = accessService.currentEmployee()?.id
            pending += leaveService.pendingForMe().size()
            [OvertimeRequest, RegularizationRequest].each { Class c ->
                pending += c.createCriteria().list {
                    eq('status', 'PENDING')
                    if (ids != null) inList('employee.id', ids ?: [-1L])
                }.count { it.employee.id != myId }
            }
        }

        // Day 8: খরচের দাবি, ভ্রমণ ও মূল্যায়ন যেগুলো আমার সিদ্ধান্তের অপেক্ষায়
        if (accessService.isApprover() || AccessService.has('ROLE_ACCOUNTANT')) {
            pending += expenseService.pendingCountForMe() + expenseService.payableClaims().size()
        }
        if (accessService.isApprover()) pending += performanceService.pendingReviewCount()
        // Day 9: চাকরি-ছাড়ার সিদ্ধান্ত/নিষ্পত্তি, শৃঙ্খলা জবাব, নতুন অভিযোগ
        pending += offboardingService.pendingForMe().size() + settlementService.pendingForAccounts().size()
        if (accessService.isHrAdmin()) pending += DisciplinaryCase.countByStatus('RESPONDED') + Grievance.countByStatus('OPEN')

        // documents that expired or expire within 30 days (HR / Admin only)
        List expiring = []
        if (accessService.isHrAdmin()) {
            expiring = EmployeeDocument.createCriteria().list {
                projections {
                    property('id'); property('title'); property('expiryDate'); property('employee')
                }
                isNotNull('expiryDate')
                le('expiryDate', LocalDate.now().plusDays(EmployeeDocument.EXPIRING_DAYS))
                order('expiryDate', 'asc')
                maxResults(8)
            }.collect { r ->
                [id: r[0], title: r[1], expiryDate: r[2], employee: r[3], expiryStatus: EmployeeDocument.expiryStatusOf(r[2] as LocalDate)]
            }
        }

        [
            totalEmployees: Employee.count(),
            presentToday  : Attendance.countByAttDateAndStatusInList(LocalDate.now(), ['PRESENT', 'LATE']),
            onLeaveCount  : Employee.countByStatus('ON_LEAVE'),
            pendingCount  : pending,
            byDepartment  : byDepartment,
            recentJoins   : Employee.list(max: 5, sort: 'joinDate', order: 'desc'),
            expiring      : expiring
        ]
    }
}
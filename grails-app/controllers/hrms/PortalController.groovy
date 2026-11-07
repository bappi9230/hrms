package hrms

import grails.plugin.springsecurity.annotation.Secured

import java.time.LocalDate
import java.time.YearMonth

/** Day 9: কর্মী ও ম্যানেজারের নিজস্ব হোম পাতা (Self-Service) */
@Secured(['isAuthenticated()'])
class PortalController {

    def accessService
    def leaveService
    def attendanceService
    def relationsService
    def approvalService

    def index() {
        Employee me = accessService.currentEmployee()
        LocalDate today = LocalDate.now(AttendanceService.ZONE)
        Map m = [me: me, today: today, announcements: relationsService.current(4)]

        List<Map> items = approvalService.pendingItems()
        m.approvals = items.take(5)
        m.approvalTotal = items.size()

        if (me != null) {
            leaveService.syncStatus()
            m.balances = leaveService.ensureBalances(me, today.year).collect { LeaveBalance b -> leaveService.view(b) }.findAll { Map v -> v.limited }
            m.todayRecord = Attendance.findByEmployeeAndAttDate(me, today)
            m.summary = attendanceService.monthSummary(me, YearMonth.from(today))
            m.payslip = PayrollLine.executeQuery(
                    "from PayrollLine l where l.employee = :e and l.payrollRun.status = 'FINALIZED' order by l.payrollRun.payYear desc, l.payrollRun.payMonth desc", [e: me], [max: 1])[0]
            m.myPendingLeave = LeaveRequest.countByEmployeeAndStatus(me, 'PENDING')
            m.myClaims = ExpenseClaim.countByEmployeeAndStatusInList(me, ['PENDING_MANAGER', 'PENDING_ACCOUNTS', 'APPROVED'])
            m.myGoals = Goal.countByEmployeeAndStatus(me, 'ACTIVE')
            m.myCourses = TrainingEnrollment.countByEmployeeAndStatus(me, 'ENROLLED')
            m.myAssets = AssetAllocation.countByEmployeeAndStatus(me, 'ISSUED')
            m.caseWaiting = DisciplinaryCase.findAllByEmployeeAndStatus(me, 'OPEN')
            m.myGrievances = Grievance.countByEmployeeAndStatusInList(me, ['OPEN', 'IN_REVIEW'])
            m.mySeparation = Separation.findByEmployeeAndStatusInList(me, Separation.LIVE_STATUSES)
        }

        // ম্যানেজারের টিম
        if (AccessService.has('ROLE_MANAGER') && me != null) {
            Set<Long> ids = accessService.approvableIds() ?: ([] as Set<Long>)
            ids = ids - [me.id]
            List<Long> idList = ids ? (ids as List<Long>) : [-1L]
            m.teamSize = ids.size()
            m.teamPresent = ids ? Attendance.createCriteria().count {
                eq('attDate', today)
                inList('status', ['PRESENT', 'LATE'])
                inList('employee.id', idList)
            } : 0
            m.teamOnLeave = ids ? LeaveRequest.createCriteria().count {
                eq('status', 'APPROVED')
                le('fromDate', today)
                ge('toDate', today)
                inList('employee.id', idList)
            } : 0
        }
        m
    }
}

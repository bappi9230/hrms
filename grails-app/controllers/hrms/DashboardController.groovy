package hrms

import grails.plugin.springsecurity.annotation.Secured

@Secured(['isAuthenticated()'])
class DashboardController {

    def springSecurityService

    def index() {
        User user = springSecurityService.currentUser as User

        List byDepartment = Employee.createCriteria().list {
            projections {
                groupProperty('department')
                count()
            }
        }.collect { [department: it[0], count: it[1]] }
        .sort { -it.count }

        [
            currentUser   : user,
            totalEmployees: Employee.count(),
            activeCount   : Employee.countByStatus('ACTIVE'),
            onLeaveCount  : Employee.countByStatus('ON_LEAVE'),
            pendingCount  : 0,   // approvals arrive with Leave (Day 4)
            byDepartment  : byDepartment,
            recentJoins   : Employee.list(max: 5, sort: 'joinDate', order: 'desc')
        ]
    }
}
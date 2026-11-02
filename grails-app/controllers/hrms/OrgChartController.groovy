package hrms

import grails.plugin.springsecurity.annotation.Secured

/** রিপোর্টিং হায়ারার্কি অনুযায়ী অর্গ চার্ট (গাছের মতো তালিকা) */
@Secured(['isAuthenticated()'])
class OrgChartController {

    def index() {
        List<Employee> all = Employee.list(sort: 'name')
        Set<Long> allIds = all*.id as Set<Long>

        // ম্যানেজারের id -> অধীনের কর্মীদের তালিকা
        Map<Long, List<Employee>> children = [:].withDefault { [] as List<Employee> }
        List<Employee> roots = []
        all.each { Employee e ->
            Long managerId = e.reportingManager?.id
            if (managerId != null && allIds.contains(managerId)) children[managerId] << e
            else roots << e
        }

        [roots: roots, children: children, total: all.size()]
    }
}

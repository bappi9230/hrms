package hrms

import grails.plugin.springsecurity.SpringSecurityUtils

/**
 * কে কোন কর্মীর তথ্য দেখতে/অনুমোদন করতে পারবে তার নিয়ম এক জায়গায়।
 *  - ADMIN / HR / ACCOUNTANT : সবাইকে দেখতে পারে
 *  - MANAGER                 : নিজে + নিজের অধীনের (সরাসরি ও পরোক্ষ) সবাই
 *  - EMPLOYEE                : শুধু নিজে
 * null মানে "সবাই" (কোনো সীমা নেই)।
 */
class AccessService {

    def springSecurityService

    // ------------------------------------------------------------------ বর্তমান ব্যবহারকারী
    User currentUser() {
        springSecurityService.isLoggedIn() ? (User) springSecurityService.currentUser : null
    }

    Employee currentEmployee() {
        currentUser()?.employee
    }

    static boolean has(String roles) {
        SpringSecurityUtils.ifAnyGranted(roles)
    }

    boolean isHrAdmin() { has('ROLE_ADMIN,ROLE_HR') }

    boolean isApprover() { has('ROLE_ADMIN,ROLE_HR,ROLE_MANAGER') }

    // ------------------------------------------------------------------ দৃশ্যমানতা
    /** null = সবাইকে দেখতে পারে */
    Set<Long> visibleEmployeeIds() {
        if (has('ROLE_ADMIN,ROLE_HR,ROLE_ACCOUNTANT')) return null
        Long me = currentEmployee()?.id
        if (me == null) return [] as Set<Long>
        Set<Long> ids = [me] as Set<Long>
        if (has('ROLE_MANAGER')) ids.addAll(subordinateIds(me))
        ids
    }

    /** অনুমোদনের জন্য যাদের আবেদন এই ব্যবহারকারী দেখবে; null = সবাই */
    Set<Long> approvableIds() {
        if (has('ROLE_ADMIN,ROLE_HR')) return null
        Long me = currentEmployee()?.id
        if (me == null) return [] as Set<Long>
        subordinateIds(me)
    }

    /**
     * Day 8: এই কর্মীর ওপর "ম্যানেজার-সিদ্ধান্ত" (অনুমোদন, মূল্যায়ন, লক্ষ্য দেওয়া) দেওয়া যাবে কি?
     * নিজের ক্ষেত্রে কখনোই নয়; HR/Admin অন্য সবার ক্ষেত্রে; ম্যানেজার শুধু নিজের (সরাসরি/পরোক্ষ) অধীনের।
     */
    boolean canManage(Employee employee) {
        if (employee == null) return false
        Long me = currentEmployee()?.id
        if (me != null && me == employee.id) return false
        if (isHrAdmin()) return true
        me != null && subordinateIds(me).contains(employee.id)
    }

    boolean canView(Employee employee) {
        if (employee == null) return false
        Set<Long> ids = visibleEmployeeIds()
        ids == null || ids.contains(employee.id)
    }

    boolean canSeeSalary(Employee employee) {
        if (employee == null) return false
        has('ROLE_ADMIN,ROLE_HR,ROLE_ACCOUNTANT') || currentEmployee()?.id == employee.id
    }

    // ------------------------------------------------------------------ হায়ারার্কি
    /** একজন ম্যানেজারের অধীনের সবাই (সরাসরি + পরোক্ষ), ম্যানেজার নিজে বাদ */
    Set<Long> subordinateIds(Long bossId) {
        Map<Long, List<Long>> children = [:].withDefault { [] as List<Long> }
        Employee.executeQuery('select e.id, e.reportingManager.id from Employee e where e.reportingManager is not null').each { row ->
            children[row[1] as Long] << (row[0] as Long)
        }
        Set<Long> result = [] as Set<Long>
        List<Long> queue = [bossId]
        while (queue) {
            Long current = queue.remove(0)
            children[current].each { Long child ->
                if (result.add(child)) queue << child   // add() false হলে আগেই দেখা - চক্রে আটকাবে না
            }
        }
        result.remove(bossId)
        result
    }
}
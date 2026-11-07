package hrms

import grails.gorm.transactions.Transactional

import java.time.LocalDate

/** চাকরির ইতিহাস (যোগদান, পদোন্নতি, বদলি) এবং কর্মী মুছে ফেলার কাজ */
@Transactional
class EmploymentService {

    /** নতুন কর্মী তৈরির পর ইতিহাসে "যোগদান" লাইন */
    EmploymentHistory logJoined(Employee employee, String by) {
        new EmploymentHistory(
                employee: employee,
                eventType: 'JOINED',
                effectiveDate: employee.joinDate,
                toDepartment: employee.department?.name,
                toDesignation: employee.designation?.title,
                toBranch: employee.branch?.name,
                newSalary: employee.salary,
                remarks: 'যোগদান',
                recordedBy: by
        ).save(failOnError: true)
    }

    /**
     * পদোন্নতি / বদলি রেকর্ড করে কর্মীর প্রোফাইলও হালনাগাদ করে।
     * changes: department, designation, branch, salary (null মানে অপরিবর্তিত)।
     * আসলে কিছু বদলালে ইতিহাসের লাইন ফেরত দেয়, না বদলালে null।
     */
    EmploymentHistory recordChange(Employee employee, String eventType, LocalDate effective,
                                   Map changes, String remarks, String by) {
        EmploymentHistory h = new EmploymentHistory(
                employee: employee, eventType: eventType, effectiveDate: effective,
                remarks: remarks, recordedBy: by)
        boolean changed = false

        Department dept = changes.department as Department
        if (dept && dept.id != employee.department?.id) {
            h.fromDepartment = employee.department?.name
            h.toDepartment = dept.name
            employee.department = dept
            changed = true
        }

        Designation desig = changes.designation as Designation
        if (desig && desig.id != employee.designation?.id) {
            h.fromDesignation = employee.designation?.title
            h.toDesignation = desig.title
            employee.designation = desig
            changed = true
        }

        Branch branch = changes.branch as Branch
        if (branch && branch.id != employee.branch?.id) {
            h.fromBranch = employee.branch?.name
            h.toBranch = branch.name
            employee.branch = branch
            changed = true
        }

        BigDecimal newSalary = changes.salary as BigDecimal
        if (newSalary != null && newSalary >= 0 && newSalary.compareTo(employee.salary) != 0) {
            h.oldSalary = employee.salary
            h.newSalary = newSalary
            employee.salary = newSalary
            changed = true
        }

        if (!changed) return null
        employee.save(failOnError: true)
        h.save(failOnError: true)
    }

    /** কর্মী ও তার সব সংশ্লিষ্ট তথ্য একসাথে মোছে (এক ট্রানজ্যাকশনে) */
    void deleteEmployee(Employee employee) {
        LeaveApproval.executeUpdate('delete from LeaveApproval a where a.leaveRequest in (select r from LeaveRequest r where r.employee = :e)', [e: employee])
        // Day 8: খরচের দাবি আগে (ভ্রমণের ওপর নির্ভরশীল), তারপর ভ্রমণ; মূল্যায়নে reviewer থাকলে ফাঁকা করা
        Appraisal.executeUpdate('update Appraisal a set a.reviewer = null where a.reviewer = :e', [e: employee])
        AssetAllocation.findAllByEmployeeAndStatus(employee, 'ISSUED').each { AssetAllocation al ->
            if (al.asset != null && al.asset.status == 'ASSIGNED') { al.asset.status = 'AVAILABLE'; al.asset.save(failOnError: true) }
        }
        [ExpenseClaim, TravelRequest, Goal, Appraisal, TrainingEnrollment, BenefitEnrollment].each { Class c ->
            c.executeUpdate("delete from ${c.simpleName} x where x.employee = :e".toString(), [e: employee])
        }
        [LeaveRequest, LeaveBalance, EmployeeDocument, EmploymentHistory, Attendance, OvertimeRequest, RegularizationRequest, Roster,
         SalaryStructure, EmployeeSalaryHead, SalaryRevision, PaymentInfo,
         OnboardingTask, AssetAllocation, ProbationRecord].each { Class c ->
            c.executeUpdate("delete from ${c.simpleName} x where x.employee = :e".toString(), [e: employee])
        }
        employee.delete(flush: true)
    }
}

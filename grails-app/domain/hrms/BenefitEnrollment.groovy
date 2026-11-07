package hrms

import java.time.LocalDate

/** Day 8: কর্মী কোন সুবিধা পাচ্ছেন। ACTIVE -> ENDED */
class BenefitEnrollment {

    static final List<String> STATUSES = ['ACTIVE', 'ENDED']

    BenefitPlan plan
    Employee employee
    LocalDate startDate
    LocalDate endDate
    String status = 'ACTIVE'
    String remarks
    String enrolledBy
    Date dateCreated

    static constraints = {
        plan nullable: false
        employee nullable: false
        startDate nullable: false
        endDate nullable: true
        status inList: STATUSES
        remarks nullable: true, maxSize: 255
        enrolledBy nullable: true, maxSize: 50
    }

    static mapping = {
        table 'hrms_benefit_enrollment'
    }

    String toString() { "${employee} - ${plan}".toString() }
}

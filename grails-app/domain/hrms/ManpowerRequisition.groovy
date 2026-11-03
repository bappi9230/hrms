package hrms

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Day 7: জনবল চাহিদা (Manpower Requisition)। ম্যানেজার/HR চাহিদা তোলে (PENDING), HR/Admin অনুমোদন করলে (APPROVED)
 * তবেই এর বিপরীতে চাকরির বিজ্ঞপ্তি (JobPosting) দেওয়া যায়। সব পদ পূরণ হলে CLOSED হয়ে যায়।
 */
class ManpowerRequisition {

    static final List<String> STATUSES = ['PENDING', 'APPROVED', 'REJECTED', 'CLOSED', 'CANCELLED']

    String title
    Department department
    Designation designation
    Branch branch
    Integer vacancies = 1
    String employeeType = 'FULL_TIME'
    BigDecimal minSalary
    BigDecimal maxSalary
    LocalDate expectedJoinDate
    String justification
    String status = 'PENDING'
    String requestedBy
    String decidedBy
    LocalDateTime decidedAt
    String decisionNote
    Date dateCreated

    static constraints = {
        title blank: false, maxSize: 120
        department nullable: false
        designation nullable: false
        branch nullable: true
        vacancies nullable: false, min: 1, max: 50
        employeeType inList: Employee.TYPES
        minSalary nullable: true, min: 0.0
        maxSalary nullable: true, min: 0.0, validator: { BigDecimal max, ManpowerRequisition r ->
            if (max != null && r.minSalary != null && max < r.minSalary) return ['manpowerRequisition.maxSalary.belowMin']
            true
        }
        expectedJoinDate nullable: true
        justification blank: false, maxSize: 500
        status inList: STATUSES
        requestedBy nullable: true, maxSize: 50
        decidedBy nullable: true, maxSize: 50
        decidedAt nullable: true
        decisionNote nullable: true, maxSize: 255
    }

    static mapping = {
        table 'hrms_requisition'
    }

    /** এই চাহিদার বিপরীতে কতজন নিয়োগ হয়ে গেছে */
    int hiredCount() {
        if (id == null) return 0
        (Candidate.executeQuery("select count(c) from Candidate c where c.jobPosting.requisition = :r and c.currentStage = 'HIRED'", [r: this])[0] as Number).intValue()
    }

    int remaining() { Math.max(0, vacancies - hiredCount()) }

    String toString() { "${title} (#${id})".toString() }
}

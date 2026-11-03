package hrms

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Day 7: অফার লেটার। SENT -> ACCEPTED / DECLINED / WITHDRAWN। মেয়াদ (validUntil) পার হলে SENT অফার "মেয়াদোত্তীর্ণ" ধরা হয়।
 * ACCEPTED অফার থেকে প্রার্থীকে কর্মীতে রূপান্তর করা যায়।
 */
class JobOffer {

    static final List<String> STATUSES = ['SENT', 'ACCEPTED', 'DECLINED', 'WITHDRAWN']

    Candidate candidate
    Department department
    Designation designation
    Branch branch
    String employeeType = 'FULL_TIME'      // পরীক্ষাকাল শেষে যে ধরনে যাবে
    BigDecimal salary                      // মাসিক gross
    LocalDate joinDate
    Integer probationMonths = 3
    LocalDate validUntil
    String terms
    String status = 'SENT'
    String offeredBy
    LocalDateTime decidedAt
    String decisionNote
    Date dateCreated

    static constraints = {
        candidate nullable: false
        department nullable: false
        designation nullable: false
        branch nullable: true
        employeeType inList: Employee.TYPES
        salary nullable: false, min: 1.0
        joinDate nullable: false
        probationMonths nullable: false, min: 0, max: 12
        validUntil nullable: false
        terms nullable: true, maxSize: 1500
        status inList: STATUSES
        offeredBy nullable: true, maxSize: 50
        decidedAt nullable: true
        decisionNote nullable: true, maxSize: 255
    }

    static mapping = {
        table 'hrms_job_offer'
    }

    boolean isExpired() {
        status == 'SENT' && validUntil.isBefore(LocalDate.now(AttendanceService.ZONE))
    }

    /** SENT / ACCEPTED অফার "চলমান"; নতুন অফার দিতে হলে এগুলো থাকা চলবে না */
    boolean isLive() { status in ['SENT', 'ACCEPTED'] }

    String toString() { "Offer #${id}".toString() }
}

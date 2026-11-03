package hrms

import java.time.LocalDate

/** Day 7: চাকরির বিজ্ঞপ্তি - অনুমোদিত জনবল চাহিদা থেকে তৈরি; প্রার্থীরা এর বিপরীতে আবেদন করে */
class JobPosting {

    static final List<String> STATUSES = ['OPEN', 'CLOSED']

    ManpowerRequisition requisition
    String title
    String description
    String requirements
    String location
    LocalDate deadline
    String status = 'OPEN'
    String postedBy
    Date dateCreated

    static constraints = {
        requisition nullable: false
        title blank: false, maxSize: 120
        description blank: false, maxSize: 3000
        requirements nullable: true, maxSize: 2000
        location nullable: true, maxSize: 120
        deadline nullable: true
        status inList: STATUSES
        postedBy nullable: true, maxSize: 50
    }

    static mapping = {
        table 'hrms_job_posting'
    }

    /** OPEN এবং শেষ তারিখ পার হয়নি */
    boolean isAcceptingApplications() {
        status == 'OPEN' && (deadline == null || !deadline.isBefore(LocalDate.now(AttendanceService.ZONE)))
    }

    String toString() { title }
}

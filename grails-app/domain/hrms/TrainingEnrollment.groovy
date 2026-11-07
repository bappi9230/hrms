package hrms

import java.time.LocalDate

/** Day 8: কোর্সে নথিভুক্তি। COMPLETED হলে সার্টিফিকেট নম্বর পায়। */
class TrainingEnrollment {

    static final List<String> STATUSES = ['ENROLLED', 'COMPLETED', 'FAILED', 'CANCELLED']

    Course course
    Employee employee
    String status = 'ENROLLED'
    Integer score
    LocalDate completedDate
    String certificateNo
    String enrolledBy
    Date dateCreated

    static constraints = {
        course nullable: false
        employee nullable: false, unique: 'course'
        status inList: STATUSES
        score nullable: true, min: 0, max: 100
        completedDate nullable: true
        certificateNo nullable: true, unique: true, maxSize: 30
        enrolledBy nullable: true, maxSize: 50
    }

    static mapping = {
        table 'hrms_training_enrollment'
    }

    String toString() { "${employee} @ ${course}".toString() }
}

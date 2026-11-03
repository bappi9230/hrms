package hrms

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Day 7: পরীক্ষাকাল (probation) ট্র্যাকিং। ONGOING -> CONFIRMED (স্থায়ী) বা NOT_CONFIRMED।
 * সর্বোচ্চ ২ বার বাড়ানো যায় (extensions), প্রতিবার ১-৩ মাস।
 */
class ProbationRecord {

    static final List<String> STATUSES = ['ONGOING', 'CONFIRMED', 'NOT_CONFIRMED']
    static final int MAX_EXTENSIONS = 2
    static final int EXPIRING_DAYS = 30

    Employee employee
    LocalDate startDate
    LocalDate endDate
    String confirmType = 'FULL_TIME'       // স্থায়ী হলে কর্মীর ধরন যা হবে
    Integer extensions = 0
    String status = 'ONGOING'
    String reviewNote
    String decidedBy
    LocalDateTime decidedAt

    static constraints = {
        employee nullable: false, unique: true
        startDate nullable: false
        endDate nullable: false, validator: { LocalDate end, ProbationRecord r ->
            if (end != null && r.startDate != null && !end.isAfter(r.startDate)) return ['probationRecord.endDate.beforeStart']
            true
        }
        confirmType inList: Employee.TYPES
        extensions nullable: false, min: 0
        status inList: STATUSES
        reviewNote nullable: true, maxSize: 500
        decidedBy nullable: true, maxSize: 50
        decidedAt nullable: true
    }

    static mapping = {
        table 'hrms_probation'
    }

    /** ONGOING থাকা অবস্থায়: OVERDUE (শেষ তারিখ পার) | EXPIRING (৩০ দিনের মধ্যে) | RUNNING; অন্যথায় status */
    String phase() {
        if (status != 'ONGOING') return status
        LocalDate today = LocalDate.now(AttendanceService.ZONE)
        if (endDate.isBefore(today)) return 'OVERDUE'
        if (!endDate.isAfter(today.plusDays(EXPIRING_DAYS))) return 'EXPIRING'
        'RUNNING'
    }

    String toString() { "${employee?.name} probation".toString() }
}

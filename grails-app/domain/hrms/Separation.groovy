package hrms

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Day 9: চাকরি ছাড়ার কেস (পদত্যাগ, চাকরিচ্যুতি, অবসর, চুক্তি শেষ)।
 * পদত্যাগ: PENDING_MANAGER -> PENDING_HR -> ACCEPTED (ক্লিয়ারেন্স + নিষ্পত্তি) -> COMPLETED।
 * বাতিল: REJECTED / WITHDRAWN। HR এর পক্ষ থেকে শুরু হলে সরাসরি ACCEPTED।
 */
class Separation {

    static final List<String> TYPES    = ['RESIGNATION', 'TERMINATION', 'RETIREMENT', 'CONTRACT_END']
    static final List<String> REASONS  = ['BETTER_OPPORTUNITY', 'HIGHER_STUDIES', 'PERSONAL', 'HEALTH', 'RELOCATION',
                                          'MISCONDUCT', 'PERFORMANCE', 'CONTRACT_END', 'RETIREMENT', 'OTHER']
    static final List<String> STATUSES = ['PENDING_MANAGER', 'PENDING_HR', 'ACCEPTED', 'COMPLETED', 'REJECTED', 'WITHDRAWN']
    static final List<String> LIVE_STATUSES = ['PENDING_MANAGER', 'PENDING_HR', 'ACCEPTED']

    Employee employee
    String exitType = 'RESIGNATION'
    String reasonCategory = 'OTHER'
    String reason

    LocalDate noticeDate                  // নোটিস জমার তারিখ
    LocalDate requestedLastDay            // কর্মীর চাওয়া শেষ দিন
    LocalDate lastWorkingDay              // HR এর চূড়ান্ত শেষ কর্মদিবস
    Integer noticeDays = 30
    Boolean noticeWaived = false

    String status = 'PENDING_MANAGER'
    String initiatedBy = 'employee'       // employee | hr

    String managerBy
    LocalDateTime managerAt
    String managerNote
    String hrBy
    LocalDateTime hrAt
    String hrNote

    String exitInterviewNote
    String letterNo
    String completedBy
    LocalDateTime completedAt
    Date dateCreated

    static constraints = {
        employee nullable: false
        exitType inList: TYPES
        reasonCategory inList: REASONS
        reason blank: false, maxSize: 1000
        noticeDate nullable: false
        requestedLastDay nullable: false
        lastWorkingDay nullable: true
        noticeDays nullable: false, min: 0, max: 365
        noticeWaived nullable: false
        status inList: STATUSES
        initiatedBy nullable: true, maxSize: 20
        managerBy nullable: true, maxSize: 50
        managerAt nullable: true
        managerNote nullable: true, maxSize: 255
        hrBy nullable: true, maxSize: 50
        hrAt nullable: true
        hrNote nullable: true, maxSize: 255
        exitInterviewNote nullable: true, maxSize: 1000
        letterNo nullable: true, maxSize: 30
        completedBy nullable: true, maxSize: 50
        completedAt nullable: true
    }

    static mapping = {
        table 'hrms_separation'
    }

    String toString() { "${exitType} #${id}".toString() }
}

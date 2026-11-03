package hrms

import java.time.LocalDate
import java.time.LocalDateTime

/** Day 4: ছুটির আবেদন (multi-level অনুমোদন: stages = "MANAGER,HR" এর মতো) */
class LeaveRequest {

    static final List<String> STATUSES = ['PENDING', 'APPROVED', 'REJECTED', 'CANCELLED']

    Employee employee
    LeaveType leaveType
    LocalDate fromDate
    LocalDate toDate
    Integer days                          // সাপ্তাহিক ছুটি বাদে কর্মদিবস
    String reason
    String status = 'PENDING'
    String stages = 'HR'                  // কমা দিয়ে ধাপ: MANAGER, HR
    Integer currentLevel = 1              // এখন কোন ধাপে (১ থেকে শুরু)
    LocalDateTime appliedAt = LocalDateTime.now()
    String cancelledBy

    static hasMany = [approvals: LeaveApproval]

    static constraints = {
        employee nullable: false
        leaveType nullable: false
        fromDate nullable: false
        toDate nullable: false, validator: { LocalDate to, LeaveRequest r ->
            if (to != null && r.fromDate != null && to.isBefore(r.fromDate)) return ['leaveRequest.toDate.beforeFrom']
            true
        }
        days nullable: false, min: 1
        reason blank: false, maxSize: 500
        status inList: STATUSES
        stages blank: false, maxSize: 30
        currentLevel nullable: false, min: 1
        cancelledBy nullable: true, maxSize: 100
    }

    static mapping = {
        table 'hrms_leave_request'
        approvals sort: 'id'
    }

    List<String> stageList() { stages.split(',').toList() }

    /** এই মুহূর্তে কোন ধাপের অনুমোদন বাকি (MANAGER / HR) */
    String currentStage() {
        List<String> s = stageList()
        s[Math.min(currentLevel, s.size()) - 1]
    }

    String toString() { "${employee?.name}: ${fromDate} – ${toDate}".toString() }
}

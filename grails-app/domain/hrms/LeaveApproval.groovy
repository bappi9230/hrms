package hrms

import java.time.LocalDateTime

/** Day 4: ছুটির আবেদনের প্রতিটি অনুমোদন/বাতিলের লাইন (অডিট ট্রেইল) */
class LeaveApproval {

    Integer stepLevel
    String stage                          // MANAGER / HR
    String decision                       // APPROVED / REJECTED
    String actedBy                        // ইউজারনেম
    String actedByName
    String remarks
    LocalDateTime actedAt = LocalDateTime.now()

    static belongsTo = [leaveRequest: LeaveRequest]

    static constraints = {
        stepLevel nullable: false
        stage maxSize: 20
        decision inList: ['APPROVED', 'REJECTED']
        actedBy maxSize: 100
        actedByName nullable: true, maxSize: 100
        remarks nullable: true, maxSize: 500
    }

    static mapping = {
        table 'hrms_leave_approval'
    }
}

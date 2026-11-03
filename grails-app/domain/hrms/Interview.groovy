package hrms

import java.time.LocalDateTime

/** Day 7: ইন্টারভিউয়ের সময়সূচি ও ফিডব্যাক (প্রতি রাউন্ডে একটি) */
class Interview {

    static final List<String> STATUSES = ['SCHEDULED', 'COMPLETED', 'CANCELLED']
    static final List<String> MODES = ['ONSITE', 'ONLINE', 'PHONE']
    static final List<String> RECOMMENDATIONS = ['HIRE', 'HOLD', 'REJECT']

    Candidate candidate
    Integer roundNo = 1
    String roundName = 'HR / Screening'
    LocalDateTime scheduledAt
    Integer durationMinutes = 45
    String mode = 'ONSITE'
    String location
    Employee interviewer
    String status = 'SCHEDULED'
    Integer rating
    String recommendation
    String feedback
    LocalDateTime feedbackAt
    String feedbackBy
    String scheduledBy
    Date dateCreated

    static constraints = {
        candidate nullable: false
        roundNo nullable: false, min: 1
        roundName blank: false, maxSize: 60
        scheduledAt nullable: false
        durationMinutes nullable: false, min: 15, max: 240
        mode inList: MODES
        location nullable: true, maxSize: 150
        interviewer nullable: false
        status inList: STATUSES
        rating nullable: true, min: 1, max: 5
        recommendation nullable: true, inList: RECOMMENDATIONS
        feedback nullable: true, maxSize: 1500
        feedbackAt nullable: true
        feedbackBy nullable: true, maxSize: 50
        scheduledBy nullable: true, maxSize: 50
    }

    static mapping = {
        table 'hrms_interview'
    }

    LocalDateTime endsAt() { scheduledAt.plusMinutes(durationMinutes) }

    String toString() { "${candidate?.name} - ${roundName}".toString() }
}

package hrms

import java.time.LocalDateTime

/**
 * Day 7: প্রার্থী। Kanban বোর্ডের ধাপ: APPLIED -> SCREENING -> INTERVIEW -> OFFER -> HIRED; যেকোনো সময় REJECTED।
 * HIRED শুধু "কর্মীতে রূপান্তর" এর মাধ্যমে হয় (employee ফিল্ডে তৈরি হওয়া কর্মী বসে)।
 */
class Candidate {

    static final List<String> STAGES = ['APPLIED', 'SCREENING', 'INTERVIEW', 'OFFER', 'HIRED', 'REJECTED']
    static final List<String> BOARD_STAGES = ['APPLIED', 'SCREENING', 'INTERVIEW', 'OFFER', 'HIRED']
    static final List<String> MOVABLE_STAGES = ['APPLIED', 'SCREENING', 'INTERVIEW', 'OFFER']
    static final List<String> SOURCES = ['WEBSITE', 'REFERRAL', 'LINKEDIN', 'JOB_PORTAL', 'WALK_IN', 'OTHER']
    static final List<String> RESUME_EXTENSIONS = ['pdf', 'doc', 'docx']
    static final int RESUME_MAX_BYTES = 2 * 1024 * 1024

    String name
    String email
    String phone
    JobPosting jobPosting
    String source = 'WEBSITE'
    String currentStage = 'APPLIED'
    BigDecimal expectedSalary
    BigDecimal experienceYears
    String note
    String rejectReason
    boolean hasResume = false              // তালিকা দেখাতে ফাইলের বাইট লোড করতে হয় না
    LocalDateTime stageChangedAt = LocalDateTime.now()
    Employee employee                      // কর্মীতে রূপান্তরের পর
    String createdBy
    Date dateCreated

    static constraints = {
        name blank: false, maxSize: 100
        email blank: false, email: true, maxSize: 120, unique: 'jobPosting'
        phone nullable: true, maxSize: 20
        jobPosting nullable: false
        source inList: SOURCES
        currentStage inList: STAGES
        expectedSalary nullable: true, min: 0.0
        experienceYears nullable: true, min: 0.0, max: 60.0
        note nullable: true, maxSize: 500
        rejectReason nullable: true, maxSize: 255
        employee nullable: true
        createdBy nullable: true, maxSize: 50
    }

    static mapping = {
        table 'hrms_candidate'
    }

    String toString() { name }
}

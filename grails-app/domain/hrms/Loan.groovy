package hrms

import java.time.LocalDateTime

/**
 * Day 6: কর্মীর ঋণ (LOAN) বা বেতন অগ্রিম (ADVANCE)। অনুমোদনের পর কিস্তির তালিকা (LoanInstallment) তৈরি হয়
 * এবং প্রতি মাসের পে-রোলে কিস্তি আপনা-আপনি কাটা হয়।
 * PENDING -> ACTIVE (Admin অনুমোদন) -> CLOSED (সব কিস্তি শোধ)। বাতিল: REJECTED / CANCELLED।
 */
class Loan {

    static final List<String> TYPES    = ['LOAN', 'ADVANCE']
    static final List<String> STATUSES = ['PENDING', 'ACTIVE', 'CLOSED', 'REJECTED', 'CANCELLED']

    Employee employee
    String loanType = 'LOAN'
    BigDecimal amount
    Integer installments = 1
    BigDecimal installmentAmount          // সাধারণ কিস্তি (শেষটায় ভগ্নাংশ মিলিয়ে নেওয়া হয়)
    Integer startYear                     // প্রথম কিস্তি কাটার মাস
    Integer startMonth
    String reason
    String status = 'PENDING'
    String requestedBy
    String decidedBy
    LocalDateTime decidedAt
    String decisionNote
    Date dateCreated

    static constraints = {
        employee nullable: false
        loanType inList: TYPES
        amount nullable: false, min: 1.0
        installments nullable: false, min: 1, max: 60
        installmentAmount nullable: false, min: 1.0
        startYear nullable: false
        startMonth nullable: false, min: 1, max: 12
        reason nullable: true, maxSize: 255
        status inList: STATUSES
        requestedBy nullable: true, maxSize: 50
        decidedBy nullable: true, maxSize: 50
        decidedAt nullable: true
        decisionNote nullable: true, maxSize: 255
    }

    static mapping = {
        table 'hrms_loan'
    }

    String toString() { "${loanType} #${id}".toString() }
}

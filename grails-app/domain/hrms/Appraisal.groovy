package hrms

import java.time.LocalDateTime

/**
 * Day 8: একজন কর্মীর একটি চক্রের মূল্যায়ন। ধাপ: PENDING_SELF (নিজের মূল্যায়ন) -> PENDING_MANAGER -> COMPLETED।
 * রেটিং ১-৫। goalScore = ম্যানেজার জমা দেওয়ার সময়ের লক্ষ্য-অর্জনের (weighted) শতাংশ।
 */
class Appraisal {

    static final List<String> STATUSES = ['PENDING_SELF', 'PENDING_MANAGER', 'COMPLETED']
    static final Map<Integer, String> RATING_LABELS = [5: 'অসাধারণ', 4: 'প্রত্যাশার চেয়ে ভালো', 3: 'প্রত্যাশা অনুযায়ী', 2: 'উন্নতি দরকার', 1: 'সন্তোষজনক নয়']

    AppraisalCycle cycle
    Employee employee
    Employee reviewer                 // তৈরির সময়ের সরাসরি ম্যানেজার (না থাকলে HR/Admin)
    Integer selfRating
    String selfComment
    Integer managerRating
    String managerComment
    BigDecimal goalScore
    String status = 'PENDING_SELF'
    LocalDateTime selfSubmittedAt
    LocalDateTime managerSubmittedAt
    String reviewedBy
    Date dateCreated

    static constraints = {
        cycle nullable: false
        employee nullable: false, unique: 'cycle'
        reviewer nullable: true
        selfRating nullable: true, min: 1, max: 5
        selfComment nullable: true, maxSize: 1000
        managerRating nullable: true, min: 1, max: 5
        managerComment nullable: true, maxSize: 1000
        goalScore nullable: true
        status inList: STATUSES
        selfSubmittedAt nullable: true
        managerSubmittedAt nullable: true
        reviewedBy nullable: true, maxSize: 50
    }

    static mapping = {
        table 'hrms_appraisal'
    }

    String toString() { "${employee} - ${cycle}".toString() }
}

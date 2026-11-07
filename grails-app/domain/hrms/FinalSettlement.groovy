package hrms

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Day 9: চূড়ান্ত নিষ্পত্তি (full & final)। DRAFT (HR হিসাব করে) -> APPROVED (হিসাব বিভাগ) -> PAID।
 * netPayable = বকেয়া বেতন + ছুটি নগদায়ন + গ্র্যাচুইটি + অনুমোদিত খরচ - ঋণ কর্তন - কম নোটিস কর্তন + হাতে সমন্বয়
 */
class FinalSettlement {

    static final List<String> STATUSES = ['DRAFT', 'APPROVED', 'PAID']

    Separation separation
    Employee employee

    BigDecimal basic = 0
    BigDecimal gross = 0

    Integer salaryDays = 0
    BigDecimal unpaidSalary = 0
    BigDecimal leaveDays = 0
    BigDecimal leaveEncashment = 0
    Integer serviceYears = 0
    BigDecimal gratuity = 0
    BigDecimal pendingClaims = 0
    BigDecimal loanDeduction = 0
    Integer noticeShortDays = 0
    BigDecimal noticeDeduction = 0
    BigDecimal otherAdjustment = 0
    String adjustmentNote
    BigDecimal netPayable = 0

    String status = 'DRAFT'
    String preparedBy
    LocalDateTime preparedAt
    String approvedBy
    LocalDateTime approvedAt
    String paidBy
    LocalDate paidDate
    String paymentRef
    String notes
    Date dateCreated

    static constraints = {
        separation nullable: false, unique: true
        employee nullable: false
        status inList: STATUSES
        adjustmentNote nullable: true, maxSize: 255
        preparedBy nullable: true, maxSize: 50
        preparedAt nullable: true
        approvedBy nullable: true, maxSize: 50
        approvedAt nullable: true
        paidBy nullable: true, maxSize: 50
        paidDate nullable: true
        paymentRef nullable: true, maxSize: 80
        notes nullable: true, maxSize: 500
    }

    static mapping = {
        table 'hrms_final_settlement'
    }

    String toString() { "Settlement #${id}".toString() }
}

package hrms

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Day 8: খরচের দাবি (reimbursement)। ধাপ: PENDING_MANAGER -> PENDING_ACCOUNTS -> APPROVED -> PAID।
 * ম্যানেজার না থাকলে সরাসরি PENDING_ACCOUNTS। বাতিল: REJECTED / CANCELLED।
 * ৫০০ ৳ এর বেশি হলে রসিদ বাধ্যতামূলক।
 */
class ExpenseClaim {

    static final List<String> CATEGORIES = ['TRAVEL', 'FOOD', 'ACCOMMODATION', 'TRANSPORT', 'OFFICE_SUPPLIES', 'TRAINING', 'COMMUNICATION', 'OTHER']
    static final List<String> STATUSES = ['PENDING_MANAGER', 'PENDING_ACCOUNTS', 'APPROVED', 'PAID', 'REJECTED', 'CANCELLED']
    static final List<String> LIVE_STATUSES = ['PENDING_MANAGER', 'PENDING_ACCOUNTS', 'APPROVED', 'PAID']
    static final List<String> RECEIPT_EXTENSIONS = ['pdf', 'jpg', 'jpeg', 'png']
    static final int RECEIPT_MAX_BYTES = 2 * 1024 * 1024
    static final BigDecimal RECEIPT_REQUIRED_ABOVE = 500.0
    static final BigDecimal MAX_AMOUNT = 500000.0
    static final int MAX_AGE_DAYS = 90

    Employee employee
    TravelRequest travel
    String category = 'OTHER'
    String title
    LocalDate expenseDate
    BigDecimal amount
    String description

    byte[] receiptData
    String receiptName
    String receiptType
    Long receiptSize
    boolean hasReceipt = false        // তালিকায় blob লোড এড়াতে

    String status = 'PENDING_MANAGER'
    String managerBy
    LocalDateTime managerAt
    String managerNote
    String accountsBy
    LocalDateTime accountsAt
    String accountsNote
    String paidBy
    LocalDate paidDate
    String paymentRef
    Date dateCreated

    static constraints = {
        employee nullable: false
        travel nullable: true
        category inList: CATEGORIES
        title blank: false, maxSize: 150
        expenseDate nullable: false
        amount nullable: false, min: 1.0, max: MAX_AMOUNT
        description nullable: true, maxSize: 500
        receiptData nullable: true, maxSize: RECEIPT_MAX_BYTES
        receiptName nullable: true, maxSize: 255
        receiptType nullable: true, maxSize: 100
        receiptSize nullable: true
        status inList: STATUSES
        managerBy nullable: true, maxSize: 50
        managerAt nullable: true
        managerNote nullable: true, maxSize: 255
        accountsBy nullable: true, maxSize: 50
        accountsAt nullable: true
        accountsNote nullable: true, maxSize: 255
        paidBy nullable: true, maxSize: 50
        paidDate nullable: true
        paymentRef nullable: true, maxSize: 80
    }

    static mapping = {
        table 'hrms_expense_claim'
        receiptData sqlType: 'blob'
    }

    String toString() { "${title} (#${id})".toString() }
}

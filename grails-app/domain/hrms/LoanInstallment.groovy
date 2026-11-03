package hrms

/**
 * Day 6: ঋণের একটি কিস্তি। PENDING = এখনো কাটা হয়নি; PAID = চূড়ান্ত পে-রোলে কাটা হয়েছে;
 * SETTLED = পে-রোলের বাইরে শোধ/মওকুফ (আগাম নিষ্পত্তি)।
 * payrollRun: যে পে-রোলের খসড়ায় এখন ধরা আছে (খসড়া আবার হিসাব করলে ফাঁকা হয়ে আবার বসে)।
 */
class LoanInstallment {

    static final List<String> STATUSES = ['PENDING', 'PAID', 'SETTLED']

    Loan loan
    Integer seqNo
    Integer dueYear
    Integer dueMonth
    BigDecimal amount
    String status = 'PENDING'
    PayrollRun payrollRun

    static constraints = {
        loan nullable: false
        seqNo nullable: false
        dueYear nullable: false
        dueMonth nullable: false, min: 1, max: 12
        amount nullable: false, min: 0.0
        status inList: STATUSES
        payrollRun nullable: true
    }

    static mapping = {
        table 'hrms_loan_installment'
    }
}

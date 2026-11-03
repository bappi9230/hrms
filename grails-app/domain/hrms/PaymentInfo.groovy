package hrms

/**
 * Day 6: কর্মীর বেতন পাওয়ার মাধ্যম - ব্যাংক অ্যাকাউন্ট, bKash/Nagad নম্বর বা নগদ।
 * ব্যাংক অ্যাডভাইস ও মোবাইল ওয়ালেট শিট এই তথ্য থেকে তৈরি হয়।
 */
class PaymentInfo {

    static final List<String> METHODS = ['BANK', 'BKASH', 'NAGAD', 'CASH']

    Employee employee
    String method = 'BANK'
    String bankName
    String bankBranch
    String accountName
    String accountNo
    String routingNo
    String walletNo

    static constraints = {
        employee nullable: false, unique: true
        method inList: METHODS
        bankName nullable: true, maxSize: 80
        bankBranch nullable: true, maxSize: 80
        accountName nullable: true, maxSize: 100
        accountNo nullable: true, maxSize: 30
        routingNo nullable: true, maxSize: 20
        walletNo nullable: true, maxSize: 20
    }

    static mapping = {
        table 'hrms_payment_info'
    }

    /** তথ্য সম্পূর্ণ কিনা (নির্বাচিত মাধ্যমের জন্য দরকারি ঘরগুলো ভরা আছে) */
    boolean isComplete() {
        if (method == 'BANK') return bankName?.trim() && accountNo?.trim()
        if (method == 'BKASH' || method == 'NAGAD') return walletNo?.trim()
        true
    }

    static transients = ['complete']
}

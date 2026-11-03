package hrms

/** Day 5: পে-রোল লাইনের অতিরিক্ত ভাতা/কর্তনের খাতওয়ারি তালিকা (পে-স্লিপে দেখানোর জন্য) */
class PayrollLineItem {

    PayrollLine line
    String headCode
    String headName
    String headType          // EARNING / DEDUCTION
    BigDecimal amount = 0

    static constraints = {
        line nullable: false
        headCode maxSize: 20
        headName maxSize: 80
        headType inList: SalaryHead.TYPES
    }

    static mapping = {
        table 'hrms_payroll_line_item'
    }
}

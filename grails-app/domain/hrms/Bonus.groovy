package hrms

/**
 * Day 6: উৎসব বা পারফরম্যান্স বোনাস। নির্দিষ্ট পে-মাসের পে-রোলে আয় হিসেবে যোগ হয়।
 * PLANNED = পে-রোলে ধরা হবে; PAID = চূড়ান্ত পে-রোলে দেওয়া হয়েছে; CANCELLED = বাতিল।
 */
class Bonus {

    static final List<String> TYPES    = ['FESTIVAL', 'PERFORMANCE', 'OTHER']
    static final List<String> STATUSES = ['PLANNED', 'PAID', 'CANCELLED']

    Employee employee
    String bonusType = 'FESTIVAL'
    String title                          // যেমন "Eid-ul-Fitr 2026"
    Integer payYear
    Integer payMonth
    BigDecimal amount
    String note
    String status = 'PLANNED'
    String createdBy
    Date dateCreated

    static constraints = {
        employee nullable: false
        bonusType inList: TYPES
        title blank: false, maxSize: 80
        payYear nullable: false
        payMonth nullable: false, min: 1, max: 12
        amount nullable: false, min: 1.0
        note nullable: true, maxSize: 255
        status inList: STATUSES
        createdBy nullable: true, maxSize: 50
    }

    static mapping = {
        table 'hrms_bonus'
    }
}

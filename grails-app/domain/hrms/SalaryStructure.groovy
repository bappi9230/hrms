package hrms

import java.time.LocalDate

/**
 * Day 5: কর্মীর বেতন কাঠামো (মাসিক)। gross = বেসিক + বাড়ি ভাড়া + চিকিৎসা + যাতায়াত।
 * প্রতিটি বেতন সংশোধনে নতুন effectiveFrom সহ নতুন রেকর্ড হয়; পে-রোল মাসের শেষ তারিখে যেটা চালু সেটা ধরে।
 */
class SalaryStructure {

    Employee employee
    LocalDate effectiveFrom
    BigDecimal basic = 0
    BigDecimal houseRent = 0
    BigDecimal medical = 0
    BigDecimal conveyance = 0
    String remarks
    String createdBy
    Date dateCreated

    static transients = ['gross']

    static constraints = {
        employee nullable: false, unique: 'effectiveFrom'
        effectiveFrom nullable: false
        basic nullable: false, min: 0.01
        houseRent nullable: false, min: 0.0
        medical nullable: false, min: 0.0
        conveyance nullable: false, min: 0.0
        remarks nullable: true, maxSize: 255
        createdBy nullable: true, maxSize: 50
    }

    static mapping = {
        table 'hrms_salary_structure'
    }

    BigDecimal getGross() {
        (basic ?: 0.0) + (houseRent ?: 0.0) + (medical ?: 0.0) + (conveyance ?: 0.0)
    }
}

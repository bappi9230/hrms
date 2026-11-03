package hrms

/**
 * Day 5: বেতনের অতিরিক্ত খাত - ভাতা (EARNING) বা কর্তন (DEDUCTION)।
 * মূল কাঠামো (বেসিক, বাড়ি ভাড়া, চিকিৎসা, যাতায়াত) আলাদা - SalaryStructure এ।
 * FIXED = নির্দিষ্ট টাকা, PERCENT_BASIC = বেসিকের শতাংশ।
 */
class SalaryHead {

    static final List<String> TYPES      = ['EARNING', 'DEDUCTION']
    static final List<String> CALC_TYPES = ['FIXED', 'PERCENT_BASIC']

    String code
    String name
    String headType = 'EARNING'
    String calcType = 'FIXED'
    BigDecimal defaultValue = 0      // FIXED হলে টাকা, PERCENT_BASIC হলে শতাংশ
    boolean active = true

    static constraints = {
        code blank: false, unique: true, maxSize: 20
        name blank: false, unique: true, maxSize: 80
        headType inList: TYPES
        calcType inList: CALC_TYPES
        defaultValue nullable: false, min: 0.0, validator: { BigDecimal v, SalaryHead h ->
            if (v != null && h.calcType == 'PERCENT_BASIC' && v > 100) return ['salaryHead.defaultValue.percentTooBig']
            true
        }
    }

    static mapping = {
        table 'hrms_salary_head'
    }

    /** কর্মীর জন্য মাসিক পরিমাণ: কর্মী-ভিত্তিক নির্দিষ্ট টাকা থাকলে সেটাই, না হলে নীতি অনুযায়ী */
    BigDecimal amountFor(BigDecimal basic, BigDecimal override) {
        if (override != null) return override
        if (calcType == 'PERCENT_BASIC') return (basic ?: 0.0) * (defaultValue ?: 0.0) / 100
        defaultValue ?: 0.0
    }

    String toString() { name }
}

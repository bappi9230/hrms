package hrms

/** Day 8: সুবিধার প্ল্যান (স্বাস্থ্য বীমা, যাতায়াত, মোবাইল বিল ...)। মাস্টার স্ক্রিন থেকে পরিচালিত। */
class BenefitPlan {

    static final List<String> TYPES = ['HEALTH_INSURANCE', 'LIFE_INSURANCE', 'TRANSPORT', 'MOBILE', 'MEAL', 'EDUCATION', 'OTHER']

    String name
    String benefitType = 'OTHER'
    String provider
    String description
    BigDecimal monthlyValue            // কোম্পানি মাসে কত খরচ করে (তথ্যের জন্য)
    Integer minServiceMonths = 0       // যোগদানের কত মাস পর থেকে পাবে
    boolean active = true

    static constraints = {
        name blank: false, unique: true, maxSize: 100
        benefitType inList: TYPES
        provider nullable: true, maxSize: 100
        description nullable: true, maxSize: 500
        monthlyValue nullable: true, min: 0.0
        minServiceMonths nullable: false, min: 0, max: 120
    }

    static mapping = {
        table 'hrms_benefit_plan'
    }

    String toString() { name }
}

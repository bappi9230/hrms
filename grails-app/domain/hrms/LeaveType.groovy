package hrms

/**
 * Day 4: ছুটির ধরন ও নীতি (কোটা, accrual, carry forward, অনুমোদনের ধাপ)।
 * annualDays = 0 মানে সীমাহীন (যেমন বিনা বেতনের ছুটি) - ব্যালেন্স যাচাই হয় না।
 */
class LeaveType {

    static final List<String> ACCRUALS = ['YEARLY', 'MONTHLY']
    static final List<String> GENDERS  = ['MALE', 'FEMALE']

    String code
    String name
    BigDecimal annualDays = 0
    String accrual = 'YEARLY'            // YEARLY = বছরের শুরুতেই পুরো কোটা, MONTHLY = প্রতি মাসে ১/১২ অংশ
    boolean paid = true
    boolean carryForward = false
    BigDecimal maxCarryForward = 0       // পরের বছরে সর্বোচ্চ কত দিন যাবে
    Integer approvalLevels = 1           // 1 = ম্যানেজার, 2 = ম্যানেজার + HR
    String gender                        // null = সবার জন্য, FEMALE = শুধু নারী (যেমন মাতৃত্বকালীন)
    boolean active = true

    static constraints = {
        code blank: false, unique: true, maxSize: 10
        name blank: false, unique: true, maxSize: 60
        annualDays nullable: false, min: 0.0, max: 365.0
        accrual inList: ACCRUALS
        maxCarryForward nullable: false, min: 0.0, max: 365.0
        approvalLevels nullable: false, inList: [1, 2]
        gender nullable: true, inList: GENDERS
    }

    static mapping = {
        table 'hrms_leave_type'
    }

    boolean hasQuota() { annualDays != null && annualDays > 0 }

    boolean eligible(Employee e) { gender == null || gender == e?.gender }

    String toString() { name }
}

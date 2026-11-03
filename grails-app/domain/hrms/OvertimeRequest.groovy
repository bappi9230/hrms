package hrms

import java.time.LocalDate

/**
 * ওভারটাইম আবেদন। Day 5 এ পে-রোল এর জন্য প্রয়োজনীয় ফিল্ড যোগ হয়েছে (তারিখ, ঘণ্টা)।
 * আবেদন/অনুমোদনের স্ক্রিন Day 3 এর বাকি অংশ - পে-রোল শুধু APPROVED রেকর্ড পড়ে।
 */
class OvertimeRequest {

    static final List<String> STATUSES = ['PENDING', 'APPROVED', 'REJECTED']

    Employee employee
    LocalDate otDate
    BigDecimal hours
    String reason
    String status = 'PENDING'
    String decidedBy

    static constraints = {
        employee nullable: false
        otDate nullable: true
        hours nullable: true, min: 0.0, max: 24.0
        reason nullable: true, maxSize: 255
        status inList: STATUSES
        decidedBy nullable: true, maxSize: 50
    }
}

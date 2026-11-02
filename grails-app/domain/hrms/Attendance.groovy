package hrms

import java.time.LocalDate
import java.time.LocalTime

/** Day 3 এ সম্পূর্ণ হবে - এখন শুধু কম্পাইল ও ড্যাশবোর্ডের জন্য ন্যূনতম রূপ */
class Attendance {

    static final List<String> STATUSES = ['PRESENT', 'LATE', 'ABSENT', 'ON_LEAVE', 'HOLIDAY']

    Employee employee
    LocalDate attDate
    String status = 'PRESENT'
    LocalTime checkIn
    LocalTime checkOut

    static constraints = {
        employee nullable: false, unique: 'attDate'
        attDate nullable: false
        status inList: STATUSES
        checkIn nullable: true
        checkOut nullable: true
    }
}

package hrms

import java.time.LocalDate
import java.time.LocalTime

/** দৈনিক উপস্থিতি - প্রতি কর্মীর প্রতি দিনে একটি রেকর্ড */
class Attendance {

    static final List<String> STATUSES = ['PRESENT', 'LATE', 'ABSENT', 'ON_LEAVE', 'HOLIDAY']
    static final List<String> SOURCES  = ['SELF', 'MANUAL']

    Employee employee
    LocalDate attDate
    String status = 'PRESENT'
    LocalTime checkIn
    LocalTime checkOut

    Shift shift                       // যে শিফট ধরে হিসাব হয়েছে
    Integer lateMinutes = 0           // শিফট শুরুর পর কত মিনিট দেরি
    boolean earlyOut = false          // নির্ধারিত সময়ের আগে বের হয়েছে কিনা
    String source = 'SELF'            // SELF = নিজে চেক-ইন, MANUAL = HR এন্ট্রি
    String remarks

    static constraints = {
        employee nullable: false, unique: 'attDate'
        attDate nullable: false
        status inList: STATUSES
        checkIn nullable: true
        checkOut nullable: true
        shift nullable: true
        lateMinutes nullable: false, min: 0
        source inList: SOURCES
        remarks nullable: true, maxSize: 255
    }
}

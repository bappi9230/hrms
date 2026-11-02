package hrms

import java.time.LocalDate
import java.time.YearMonth

/** Day 3 এ সম্পূর্ণ হবে - এখন শুধু প্রোফাইলের জন্য মাসিক সারসংক্ষেপ */
class AttendanceService {

    Map monthSummary(Employee employee, YearMonth month) {
        LocalDate from = month.atDay(1)
        LocalDate to = month.atEndOfMonth()
        List<Attendance> rows = Attendance.findAllByEmployeeAndAttDateBetween(employee, from, to)
        [
            month  : month,
            total  : rows.size(),
            present: rows.count { it.status == 'PRESENT' },
            late   : rows.count { it.status == 'LATE' },
            absent : rows.count { it.status == 'ABSENT' }
        ]
    }
}

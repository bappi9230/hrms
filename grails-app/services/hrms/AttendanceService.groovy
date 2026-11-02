package hrms

import grails.gorm.transactions.Transactional

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/** উপস্থিতি: চেক-ইন / চেক-আউট (গ্রেস টাইম ধরে) ও মাসিক সারসংক্ষেপ */
class AttendanceService {

    static final ZoneId ZONE = ZoneId.of('Asia/Dhaka')

    /**
     * কর্মীর জন্য ওই দিনের শিফট। রোস্টার যোগ হওয়ার আগে পর্যন্ত প্রথম শিফটই ধরা হয়।
     */
    Shift shiftFor(Employee employee, LocalDate date) {
        Shift.list(sort: 'id', max: 1)[0]
    }

    @Transactional
    Map checkIn(Employee employee) {
        if (employee == null) return fail('এই অ্যাকাউন্টের সাথে কোনো কর্মী যুক্ত নেই।')

        LocalDateTime now = LocalDateTime.now(ZONE)
        LocalDate today = now.toLocalDate()
        LocalTime nowTime = now.toLocalTime().truncatedTo(ChronoUnit.MINUTES)

        Shift shift = shiftFor(employee, today)
        if (shift == null) return fail('কোনো শিফট নির্ধারণ করা নেই। HR-কে জানান।')

        Attendance att = Attendance.findByEmployeeAndAttDate(employee, today)
        if (att?.checkIn != null) return fail('আজ ইতিমধ্যে চেক-ইন করা হয়েছে।')
        if (att == null) att = new Attendance(employee: employee, attDate: today)

        long lateBy = Duration.between(shift.startTime, nowTime).toMinutes()
        boolean late = lateBy > shift.graceMinutes

        att.checkIn = nowTime
        att.shift = shift
        att.source = 'SELF'
        att.status = late ? 'LATE' : 'PRESENT'
        att.lateMinutes = late ? (int) lateBy : 0

        if (!att.save(flush: true)) return fail('উপস্থিতি সংরক্ষণ করা যায়নি।')
        [ok: true, message: late ? "চেক-ইন হয়েছে (${att.lateMinutes} মিনিট দেরি)।".toString() : 'চেক-ইন হয়েছে।']
    }

    @Transactional
    Map checkOut(Employee employee) {
        if (employee == null) return fail('এই অ্যাকাউন্টের সাথে কোনো কর্মী যুক্ত নেই।')

        LocalDateTime now = LocalDateTime.now(ZONE)
        LocalDate today = now.toLocalDate()
        LocalTime nowTime = now.toLocalTime().truncatedTo(ChronoUnit.MINUTES)

        Attendance att = Attendance.findByEmployeeAndAttDate(employee, today)
        if (att?.checkIn == null) return fail('আগে চেক-ইন করুন।')
        if (att.checkOut != null) return fail('আজ ইতিমধ্যে চেক-আউট করা হয়েছে।')

        Shift shift = att.shift ?: shiftFor(employee, today)
        att.checkOut = nowTime
        att.earlyOut = shift != null && nowTime.isBefore(shift.endTime.minusMinutes(shift.earlyOutMinutes))

        if (!att.save(flush: true)) return fail('উপস্থিতি সংরক্ষণ করা যায়নি।')
        [ok: true, message: att.earlyOut ? 'চেক-আউট হয়েছে (সময়ের আগে)।' : 'চেক-আউট হয়েছে।']
    }

    /** একজন কর্মীর একটি মাসের সব রেকর্ড (নতুন তারিখ আগে) */
    List<Attendance> monthRecords(Employee employee, YearMonth month) {
        Attendance.findAllByEmployeeAndAttDateBetween(employee, month.atDay(1), month.atEndOfMonth(), [sort: 'attDate', order: 'desc'])
    }

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

    private static Map fail(String message) {
        [ok: false, message: message]
    }
}

package hrms

import grails.plugin.springsecurity.annotation.Secured

import java.time.LocalDate
import java.time.YearMonth

@Secured(['isAuthenticated()'])
class AttendanceController {

    static allowedMethods = [checkIn: 'POST', checkOut: 'POST']

    def accessService
    def attendanceService

    /** আমার আজকের অবস্থা + এই মাসের তালিকা; অনুমোদনকারীদের জন্য আজকের টিম তালিকা */
    def index() {
        Employee me = accessService.currentEmployee()
        LocalDate today = LocalDate.now(AttendanceService.ZONE)
        YearMonth month = YearMonth.from(today)

        List teamToday = []
        if (accessService.isApprover()) {
            Set<Long> ids = accessService.visibleEmployeeIds()
            teamToday = Attendance.createCriteria().list {
                eq('attDate', today)
                if (ids != null) inList('employee.id', ids ?: [-1L])
                employee { order('name', 'asc') }
            }
        }

        [
            me         : me,
            today      : today,
            todayRecord: me ? Attendance.findByEmployeeAndAttDate(me, today) : null,
            shift      : me ? attendanceService.shiftFor(me, today) : null,
            records    : me ? attendanceService.monthRecords(me, month) : [],
            summary    : me ? attendanceService.monthSummary(me, month) : null,
            month      : month,
            teamToday  : teamToday
        ]
    }

    def checkIn() {
        Map r = attendanceService.checkIn(accessService.currentEmployee())
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'index')
    }

    def checkOut() {
        Map r = attendanceService.checkOut(accessService.currentEmployee())
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'index')
    }
}

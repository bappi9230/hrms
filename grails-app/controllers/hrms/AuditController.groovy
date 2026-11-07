package hrms

import grails.plugin.springsecurity.annotation.Secured

import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Day 9: অডিট ট্রেইল ও ইমেইল লগ (HR/Admin, শুধু দেখা) */
@Secured(['ROLE_ADMIN', 'ROLE_HR'])
class AuditController {

    static final int PAGE = 30

    def auditService
    def notificationService

    def index() {
        int offset = Math.max(0, params.int('offset') ?: 0)
        Date from = toDate(parseDate(params.from as String), false)
        Date to = toDate(parseDate(params.to as String), true)
        List<AuditLog> rows = auditService.search(params.actor?.toString()?.trim(), params.act?.toString()?.trim(), from, to, PAGE, offset)
        [rows: rows, total: rows.hasProperty('totalCount') ? rows.totalCount : rows.size(), offset: offset, page: PAGE, f: params,
         actors: AuditLog.executeQuery('select distinct a.actor from AuditLog a order by a.actor')]
    }

    def emails() {
        int offset = Math.max(0, params.int('offset') ?: 0)
        List<EmailLog> rows = EmailLog.list(max: PAGE, offset: offset, sort: 'id', order: 'desc')
        [rows: rows, total: EmailLog.count(), offset: offset, page: PAGE, configured: notificationService.mailConfigured]
    }

    private static Date toDate(LocalDate d, boolean endOfDay) {
        if (d == null) return null
        Date.from((endOfDay ? d.plusDays(1).atStartOfDay().minusSeconds(1) : d.atStartOfDay()).atZone(AttendanceService.ZONE).toInstant())
    }

    private static LocalDate parseDate(String s) {
        try { s ? LocalDate.parse(s) : null } catch (DateTimeParseException ignored) { null }
    }
}

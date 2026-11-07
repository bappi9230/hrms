package hrms

import grails.plugin.springsecurity.annotation.Secured

import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Day 9: কোম্পানির ঘোষণা - সবাই দেখে, HR/Admin দেয় */
@Secured(['isAuthenticated()'])
class AnnouncementController {

    static allowedMethods = [save: 'POST', toggle: 'POST', remove: 'POST']

    def accessService
    def relationsService

    def index() {
        boolean hr = accessService.isHrAdmin()
        List<Announcement> current = relationsService.current(50)
        List<Announcement> others = hr ? Announcement.list(sort: 'id', order: 'desc', max: 100).findAll { Announcement a -> !current.contains(a) } : []
        [current: current, others: others, hr: hr, today: LocalDate.now(AttendanceService.ZONE)]
    }

    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def create() {
        [categories: Announcement.CATEGORIES, form: [publishDate: LocalDate.now(AttendanceService.ZONE).toString(), notify: 'on'], today: LocalDate.now(AttendanceService.ZONE)]
    }

    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def save() {
        Map r = relationsService.postAnnouncement(params.title as String, params.body as String, params.category as String, params.pinned != null,
                parseDate(params.publishDate as String), parseDate(params.expiryDate as String), params.notify != null)
        flash[r.ok ? 'success' : 'error'] = r.message
        if (r.ok) {
            redirect(action: 'index')
        } else {
            render(view: 'create', model: [categories: Announcement.CATEGORIES, form: params, today: LocalDate.now(AttendanceService.ZONE)])
        }
    }

    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def toggle(Long id) { act(id) { Announcement a -> relationsService.toggleAnnouncement(a) } }

    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def remove(Long id) { act(id) { Announcement a -> relationsService.deleteAnnouncement(a) } }

    private void act(Long id, Closure<Map> action) {
        Announcement a = id ? Announcement.get(id) : null
        if (!a) {
            flash.error = 'ঘোষণা পাওয়া যায়নি।'
        } else {
            Map r = action.call(a)
            flash[r.ok ? 'success' : 'error'] = r.message
        }
        redirect(action: 'index')
    }

    private static LocalDate parseDate(String s) {
        try { s ? LocalDate.parse(s) : null } catch (DateTimeParseException ignored) { null }
    }
}

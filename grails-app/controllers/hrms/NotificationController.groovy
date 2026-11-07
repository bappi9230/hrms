package hrms

import grails.plugin.springsecurity.annotation.Secured

/** Day 9: ইন-অ্যাপ নোটিফিকেশন - তালিকা, পড়া, সব পড়া */
@Secured(['isAuthenticated()'])
class NotificationController {

    static allowedMethods = [markAll: 'POST']

    def accessService
    def notificationService

    def index() {
        User u = accessService.currentUser()
        boolean unreadOnly = params.filter == 'unread'
        List<Notification> list = u == null ? [] : Notification.createCriteria().list(max: 100) {
            eq('user', u)
            if (unreadOnly) eq('seen', false)
            order('id', 'desc')
        } as List<Notification>
        [notifications: list, unreadOnly: unreadOnly, unread: notificationService.unreadCount(u)]
    }

    /** পড়া হিসেবে চিহ্নিত করে নোটিফিকেশনের লিংকে নিয়ে যায় */
    def open(Long id) {
        Notification n = id ? Notification.get(id) : null
        if (!n || n.user.id != accessService.currentUser()?.id) {
            redirect(action: 'index')
            return
        }
        notificationService.markSeen(n)
        String link = n.link
        if (link && link.startsWith('/') && !link.startsWith('//')) {
            redirect(uri: link)
        } else {
            redirect(action: 'index')
        }
    }

    def markAll() {
        int n = notificationService.markAllSeen(accessService.currentUser())
        flash.success = n > 0 ? "${n}টি নোটিফিকেশন পড়া হয়েছে।".toString() : 'নতুন কোনো নোটিফিকেশন নেই।'
        redirect(action: 'index')
    }
}

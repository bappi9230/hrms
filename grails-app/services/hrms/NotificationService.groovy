package hrms

import grails.gorm.transactions.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.MimeMessageHelper

/**
 * Day 9: ইন-অ্যাপ নোটিফিকেশন ও ইমেইল।
 *  - নোটিফিকেশন: টপবারের ঘণ্টায় দেখা যায়; কর্মী/রোল/সবাইকে পাঠানো যায়।
 *  - ইমেইল: SPRING_MAIL_HOST সেট না থাকলে পাঠানো হয় না, কিন্তু EmailLog এ SKIPPED হিসেবে জমা থাকে।
 */
@Transactional
class NotificationService {

    @Value('${hrms.mail.from:no-reply@hrms.local}')
    String mailFrom

    /** spring.mail.host সেট থাকলে Spring এই বিন বানায় (নাম অনুযায়ী ইনজেক্ট); না থাকলে null */
    def mailSender

    boolean isMailConfigured() { mailSender != null }

    // ------------------------------------------------------------------ পড়া
    int unreadCount(User u) {
        u == null ? 0 : Notification.countByUserAndSeen(u, false)
    }

    List<Notification> latest(User u, int max) {
        u == null ? [] : Notification.findAllByUser(u, [sort: 'id', order: 'desc', max: max])
    }

    void markSeen(Notification n) {
        if (n != null && !n.seen) {
            n.seen = true
            n.save(flush: true)
        }
    }

    /** সব অপঠিত পড়া হিসেবে চিহ্নিত করে; কয়টি বদলাল তা ফেরত দেয় */
    int markAllSeen(User u) {
        if (u == null) return 0
        Notification.executeUpdate('update Notification n set n.seen = true where n.user = :u and n.seen = false', [u: u])
    }

    // ------------------------------------------------------------------ পাঠানো
    Notification notifyUser(User u, String title, String message, String link, String type = 'INFO') {
        if (u == null || !title) return null
        Notification n = new Notification(user: u, title: title.take(150), message: message?.take(500), link: link, type: type)
        n.save(flush: true)
    }

    /** কর্মীর লগইন (এক বা একাধিক) যাকে নোটিফিকেশন যাবে */
    void notifyEmployee(Employee e, String title, String message, String link, String type = 'INFO') {
        if (e == null) return
        User.findAllByEmployee(e).each { User u -> notifyUser(u, title, message, link, type) }
    }

    /** নির্দিষ্ট রোলের সবাইকে (exceptUserId বাদে) */
    void notifyRoles(List<String> roles, String title, String message, String link, String type = 'INFO', Long exceptUserId = null) {
        usersWithRoles(roles).each { User u ->
            if (exceptUserId == null || u.id != exceptUserId) notifyUser(u, title, message, link, type)
        }
    }

    /** সব চালু ব্যবহারকারীকে */
    int notifyAll(String title, String message, String link, String type = 'INFO') {
        List<User> users = User.findAllByEnabled(true)
        users.each { User u -> notifyUser(u, title, message, link, type) }
        users.size()
    }

    List<User> usersWithRoles(List<String> roles) {
        User.executeQuery('select distinct ur.user from UserRole ur where ur.role.authority in (:a) and ur.user.enabled = true', [a: roles]) as List<User>
    }

    // ------------------------------------------------------------------ ইমেইল
    Map sendEmail(String to, String subject, String body) {
        if (!to?.trim()) return [ok: false, message: 'ইমেইল ঠিকানা নেই।']
        if (!subject?.trim()) return [ok: false, message: 'ইমেইলের বিষয় নেই।']
        EmailLog entry = new EmailLog(toAddress: to.trim(), subject: subject.trim().take(200), body: body?.take(4000))
        if (!mailConfigured) {
            entry.status = 'SKIPPED'
            entry.save(flush: true)
            return [ok: false, message: 'ইমেইল সার্ভার ঠিক করা নেই (SPRING_MAIL_HOST সেট করুন)।']
        }
        try {
            def msg = mailSender.createMimeMessage()
            MimeMessageHelper helper = new MimeMessageHelper(msg, false, 'UTF-8')
            helper.setFrom(mailFrom)
            helper.setTo(to.trim())
            helper.setSubject(entry.subject)
            helper.setText(body ?: '', false)
            mailSender.send(msg)
            entry.status = 'SENT'
        } catch (Exception ex) {
            log.warn("Email to ${to} failed: ${ex.message}")
            entry.status = 'FAILED'
            entry.error = ex.message?.take(500)
        }
        entry.save(flush: true)
        [ok: entry.status == 'SENT', message: entry.status == 'SENT' ? "ইমেইল পাঠানো হয়েছে: ${to}".toString() : "ইমেইল পাঠানো যায়নি (${to})".toString()]
    }
}

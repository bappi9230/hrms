package hrms

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** Day 9: চাকরি ছাড়া, কর্মী সম্পর্ক, নোটিফিকেশনের ব্যাজ/নাম এবং টপবারের ঘণ্টা */
class Day9TagLib {

    static namespace = 'hrms'

    def notificationService
    def accessService

    private static final Map<String, Map<String, List<String>>> BADGES = [
        separation : [PENDING_MANAGER: ['warning text-dark', 'ম্যানেজারের অপেক্ষায়'], PENDING_HR: ['warning text-dark', 'HR এর অপেক্ষায়'],
                      ACCEPTED: ['primary', 'গৃহীত (ক্লিয়ারেন্স চলছে)'], COMPLETED: ['success', 'সম্পন্ন'],
                      REJECTED: ['danger', 'প্রত্যাখ্যাত'], WITHDRAWN: ['secondary', 'প্রত্যাহার']],
        settlement : [DRAFT: ['warning text-dark', 'খসড়া'], APPROVED: ['primary', 'অনুমোদিত'], PAID: ['success', 'পরিশোধিত']],
        discipline : [OPEN: ['warning text-dark', 'খোলা'], RESPONDED: ['info text-dark', 'জবাব এসেছে'], CLOSED: ['secondary', 'বন্ধ']],
        grievance  : [OPEN: ['warning text-dark', 'নতুন'], IN_REVIEW: ['primary', 'পর্যালোচনায়'], RESOLVED: ['success', 'সমাধান হয়েছে'], REJECTED: ['danger', 'প্রত্যাখ্যাত']],
        announcement: [GENERAL: ['secondary', 'সাধারণ'], POLICY: ['primary', 'নীতিমালা'], HOLIDAY: ['success', 'ছুটি'], EVENT: ['info text-dark', 'অনুষ্ঠান'], URGENT: ['danger', 'জরুরি']],
        email      : [SENT: ['success', 'পাঠানো হয়েছে'], SKIPPED: ['secondary', 'বাদ (মেইল সার্ভার নেই)'], FAILED: ['danger', 'ব্যর্থ']]
    ]

    /** kind = separation | settlement | discipline | grievance | announcement | email */
    def d9Badge = { attrs ->
        List<String> v = BADGES[attrs.kind as String]?.get(attrs.status as String) ?: ['light text-dark', (attrs.status ?: '') as String]
        out << "<span class=\"badge text-bg-${v[0]}\">${v[1].encodeAsHTML()}</span>"
    }

    private static final Map<String, String> NAMES = [
        RESIGNATION: 'পদত্যাগ', TERMINATION: 'চাকরিচ্যুতি', RETIREMENT: 'অবসর', CONTRACT_END: 'চুক্তি শেষ',
        BETTER_OPPORTUNITY: 'ভালো সুযোগ', HIGHER_STUDIES: 'উচ্চশিক্ষা', PERSONAL: 'ব্যক্তিগত', HEALTH: 'স্বাস্থ্য', RELOCATION: 'স্থানান্তর',
        MISCONDUCT: 'অসদাচরণ', PERFORMANCE: 'কর্মদক্ষতা', OTHER: 'অন্যান্য',
        IT: 'আইটি', ADMIN: 'অ্যাডমিন', ACCOUNTS: 'হিসাব বিভাগ', MANAGER: 'ম্যানেজার', HR: 'HR',
        VERBAL_WARNING: 'মৌখিক সতর্কতা', WRITTEN_WARNING: 'লিখিত সতর্কতা', SHOW_CAUSE: 'কারণ দর্শানো', FINAL_WARNING: 'চূড়ান্ত সতর্কতা', SUSPENSION: 'সাসপেনশন',
        NO_ACTION: 'কোনো ব্যবস্থা নয়', WARNING_ISSUED: 'সতর্কতা জারি', SUSPENDED: 'সাসপেন্ড', DISMISSAL_RECOMMENDED: 'চাকরিচ্যুতির সুপারিশ',
        WORKPLACE: 'কর্মপরিবেশ', HARASSMENT: 'হয়রানি', PAYROLL: 'বেতন', FACILITY: 'সুযোগ-সুবিধা',
        GENERAL: 'সাধারণ', POLICY: 'নীতিমালা', HOLIDAY: 'ছুটি', EVENT: 'অনুষ্ঠান', URGENT: 'জরুরি']

    /** enum কোডের বাংলা নাম (RETIREMENT কারণ হিসেবেও একই শব্দ) */
    def d9Name = { attrs ->
        String v = attrs.value as String
        out << (NAMES[v] ?: (v ?: '—')).encodeAsHTML()
    }

    /** LocalDateTime বা Date: dd-MM-yyyy HH:mm */
    def dt = { attrs ->
        def v = attrs.value
        if (v instanceof LocalDateTime) out << v.format(DateTimeFormatter.ofPattern('dd-MM-yyyy HH:mm'))
        else if (v instanceof Date) out << new java.text.SimpleDateFormat('dd-MM-yyyy HH:mm').format(v)
        else out << '—'
    }

    /** টপবারের নোটিফিকেশন ঘণ্টা */
    def bell = { attrs ->
        User u = accessService.currentUser()
        if (u == null) return
        int unread = notificationService.unreadCount(u)
        List<Notification> items = notificationService.latest(u, 6)
        String base = g.createLink(controller: 'notification', action: 'index')
        StringBuilder sb = new StringBuilder()
        sb << '<div class="dropdown me-2"><a class="btn btn-sm btn-outline-light position-relative" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false" aria-label="নোটিফিকেশন">'
        sb << '<i class="bi bi-bell"></i>'
        if (unread > 0) sb << "<span class=\"position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger\">${unread > 99 ? '99+' : unread}</span>"
        sb << '</a><div class="dropdown-menu dropdown-menu-end p-0" style="width:320px;max-width:92vw;">'
        sb << '<div class="px-3 py-2 border-bottom fw-semibold d-flex justify-content-between"><span>নোটিফিকেশন</span>'
        sb << "<a href=\"${base}\" class=\"small\">সব দেখুন</a></div>"
        if (!items) {
            sb << '<div class="px-3 py-3 text-muted small">কোনো নোটিফিকেশন নেই।</div>'
        } else {
            items.each { Notification n ->
                String href = g.createLink(controller: 'notification', action: 'open', id: n.id)
                sb << "<a href=\"${href}\" class=\"dropdown-item border-bottom py-2 ${n.seen ? '' : 'bg-light fw-semibold'}\" style=\"white-space:normal;\">"
                sb << "<div class=\"small\">${n.title.encodeAsHTML()}</div>"
                if (n.message) sb << "<div class=\"small text-muted fw-normal\">${n.message.take(80).encodeAsHTML()}</div>"
                sb << '</a>'
            }
        }
        sb << '</div></div>'
        out << sb.toString()
    }
}

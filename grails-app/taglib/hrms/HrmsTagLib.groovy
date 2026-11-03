package hrms

import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HrmsTagLib {

    static namespace = 'hrms'

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern('dd-MM-yyyy')

    def date = { attrs ->
        def v = attrs.value
        if (v == null) {
            out << '—'
        } else if (v instanceof LocalDate) {
            out << v.format(DATE_FMT)
        } else if (v instanceof Date) {
            out << new java.text.SimpleDateFormat('dd-MM-yyyy').format(v)
        } else {
            out << v.toString().encodeAsHTML()
        }
    }

    def money = { attrs ->
        def v = attrs.value
        if (v == null) {
            out << '—'
        } else {
            out << new DecimalFormat('#,##0').format(v as BigDecimal) << ' ৳'
        }
    }

    /** ফাইলের সাইজ: KB / MB */
    def fileSize = { attrs ->
        Long bytes = attrs.value as Long
        if (bytes == null) {
            out << '—'
        } else if (bytes < 1024 * 1024) {
            out << Math.max(1L, (long) (bytes / 1024)) << ' KB'
        } else {
            out << String.format('%.1f MB', bytes / (1024.0d * 1024.0d))
        }
    }

    def statusBadge = { attrs ->
        String status = attrs.status as String
        String css = [ACTIVE: 'success', ON_LEAVE: 'warning text-dark', INACTIVE: 'secondary'].get(status, 'light text-dark')
        out << "<span class=\"badge text-bg-${css}\">${(status ?: '').replace('_', ' ').encodeAsHTML()}</span>"
    }

    /** ছুটির আবেদনের স্ট্যাটাস ব্যাজ */
    def leaveStatus = { attrs ->
        String status = attrs.status as String
        Map m = [PENDING: ['warning text-dark', 'অপেক্ষমাণ'], APPROVED: ['success', 'অনুমোদিত'],
                 REJECTED: ['danger', 'বাতিল (প্রত্যাখ্যাত)'], CANCELLED: ['secondary', 'প্রত্যাহার']]
        List v = m[status] ?: ['light text-dark', status ?: '']
        out << "<span class=\"badge text-bg-${v[0]}\">${v[1]}</span>"
    }

    /** অনুমোদনের ধাপের নাম */
    def stageName = { attrs ->
        out << ([MANAGER: 'ম্যানেজার', HR: 'HR'][attrs.stage as String] ?: (attrs.stage ?: '')).toString().encodeAsHTML()
    }

    def initials = { attrs ->
        String name = (attrs.name ?: '') as String
        def parts = name.trim().split(/\s+/).findAll { it }
        String text = parts.size() >= 2 ? "${parts[0][0]}${parts[-1][0]}" : (parts ? parts[0].take(2) : '?')
        out << text.toUpperCase().encodeAsHTML()
    }

    def label = { attrs ->
        String v = (attrs.value ?: '') as String
        out << v.toLowerCase().split('_').collect { it.capitalize() }.join(' ').encodeAsHTML()
    }

    private static final List<String> MONTHS_BN = ['জানুয়ারি', 'ফেব্রুয়ারি', 'মার্চ', 'এপ্রিল', 'মে', 'জুন', 'জুলাই',
                                                   'আগস্ট', 'সেপ্টেম্বর', 'অক্টোবর', 'নভেম্বর', 'ডিসেম্বর']

    /** মাসের বাংলা নাম (১-১২) */
    def monthName = { attrs ->
        Integer m = attrs.value as Integer
        out << ((m != null && m >= 1 && m <= 12) ? MONTHS_BN[m - 1] : '—')
    }

    /** পে-রোলের অবস্থা ব্যাজ */
    def payrollStatus = { attrs ->
        String status = attrs.status as String
        out << (status == 'FINALIZED' ? '<span class="badge text-bg-success">চূড়ান্ত</span>' : '<span class="badge text-bg-warning text-dark">খসড়া (DRAFT)</span>')
    }

    /** ঋণ/অগ্রিমের অবস্থা ব্যাজ */
    def loanStatus = { attrs ->
        String status = attrs.status as String
        Map m = [PENDING: ['warning text-dark', 'অপেক্ষমাণ'], ACTIVE: ['primary', 'চলমান'], CLOSED: ['success', 'শোধ হয়েছে'],
                 REJECTED: ['danger', 'প্রত্যাখ্যাত'], CANCELLED: ['secondary', 'প্রত্যাহার']]
        List v = m[status] ?: ['light text-dark', status ?: '']
        out << "<span class=\"badge text-bg-${v[0]}\">${v[1]}</span>"
    }

    /** টাকা, ঋণাত্মক হলে লাল ও চিহ্নসহ */
    def signedMoney = { attrs ->
        BigDecimal v = (attrs.value ?: 0) as BigDecimal
        String text = new DecimalFormat('#,##0').format(v.abs()) + ' ৳'
        if (v < 0) out << '<span class="text-danger">- ' << text << '</span>'
        else if (v > 0) out << '+ ' << text
        else out << '—'
    }

    // ------------------------------------------------------------------ Day 7: নিয়োগ ও অনবোর্ডিং
    private static final Map<String, Map<String, List<String>>> RECRUIT_BADGES = [
        requisition: [PENDING: ['warning text-dark', 'অপেক্ষমাণ'], APPROVED: ['success', 'অনুমোদিত'], REJECTED: ['danger', 'প্রত্যাখ্যাত'],
                      CLOSED: ['secondary', 'বন্ধ'], CANCELLED: ['secondary', 'প্রত্যাহার']],
        posting    : [OPEN: ['success', 'খোলা'], CLOSED: ['secondary', 'বন্ধ']],
        stage      : [APPLIED: ['secondary', 'আবেদন'], SCREENING: ['info text-dark', 'বাছাই'], INTERVIEW: ['primary', 'ইন্টারভিউ'],
                      OFFER: ['warning text-dark', 'অফার'], HIRED: ['success', 'নিয়োগ'], REJECTED: ['danger', 'প্রত্যাখ্যাত']],
        interview  : [SCHEDULED: ['primary', 'নির্ধারিত'], COMPLETED: ['success', 'সম্পন্ন'], CANCELLED: ['secondary', 'বাতিল']],
        offer      : [SENT: ['warning text-dark', 'পাঠানো'], ACCEPTED: ['success', 'গৃহীত'], DECLINED: ['danger', 'প্রত্যাখ্যাত'],
                      WITHDRAWN: ['secondary', 'প্রত্যাহার']],
        recommend  : [HIRE: ['success', 'HIRE'], HOLD: ['warning text-dark', 'HOLD'], REJECT: ['danger', 'REJECT']],
        probation  : [ONGOING: ['primary', 'চলছে'], RUNNING: ['primary', 'চলছে'], EXPIRING: ['warning text-dark', 'শিগগির শেষ'],
                      OVERDUE: ['danger', 'মেয়াদ পার - সিদ্ধান্ত বাকি'], CONFIRMED: ['success', 'স্থায়ী'], NOT_CONFIRMED: ['dark', 'স্থায়ী হয়নি']],
        asset      : [ISSUED: ['primary', 'বরাদ্দ'], RETURNED: ['secondary', 'ফেরত']]
    ]

    /** kind = requisition | posting | stage | interview | offer | recommend | probation | asset */
    def recruitBadge = { attrs ->
        List<String> v = RECRUIT_BADGES[attrs.kind as String]?.get(attrs.status as String) ?: ['light text-dark', (attrs.status ?: '') as String]
        out << "<span class=\"badge text-bg-${v[0]}\">${v[1]}</span>"
    }

    private static final Map<String, String> SOURCE_BN = [WEBSITE: 'ওয়েবসাইট', REFERRAL: 'রেফারেল', LINKEDIN: 'LinkedIn', JOB_PORTAL: 'জব পোর্টাল',
                                                           WALK_IN: 'সরাসরি এসেছেন', OTHER: 'অন্যান্য']

    def sourceName = { attrs ->
        out << (SOURCE_BN[attrs.value as String] ?: (attrs.value ?: '—')).toString().encodeAsHTML()
    }

    /** তারিখ+সময়: dd-MM-yyyy HH:mm */
    def dateTime = { attrs ->
        def v = attrs.value
        out << (v instanceof java.time.LocalDateTime ? v.format(java.time.format.DateTimeFormatter.ofPattern('dd-MM-yyyy HH:mm')) : '—')
    }
}

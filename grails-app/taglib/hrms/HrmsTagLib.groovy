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
}
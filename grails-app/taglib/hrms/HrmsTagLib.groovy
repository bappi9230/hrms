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
}
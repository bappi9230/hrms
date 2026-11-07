package hrms

/**
 * Day 8: পারফরম্যান্স, প্রশিক্ষণ, খরচ/ভ্রমণ, সম্পদ ও সুবিধা মডিউলের ভিউ-ট্যাগ।
 *   <hrms:d8Badge kind="claim" status="PAID"/>   - স্ট্যাটাস ব্যাজ
 *   <hrms:d8Name value="LAPTOP"/>                - ধরন/ক্যাটাগরির বাংলা নাম
 *   <hrms:progressBar value="75"/>               - অগ্রগতির বার (০-১০০)
 *   <hrms:stars value="4"/>                      - ১-৫ রেটিং তারা
 */
class Day8TagLib {

    static namespace = 'hrms'

    private static final Map<String, Map<String, List<String>>> BADGES = [
        goal     : [ACTIVE: ['primary', 'চলমান'], COMPLETED: ['success', 'অর্জিত'], CANCELLED: ['secondary', 'বাতিল']],
        cycle    : [DRAFT: ['secondary', 'খসড়া'], OPEN: ['success', 'চলমান'], CLOSED: ['dark', 'বন্ধ']],
        appraisal: [PENDING_SELF: ['warning text-dark', 'নিজের মূল্যায়ন বাকি'], PENDING_MANAGER: ['info text-dark', 'ম্যানেজারের অপেক্ষায়'],
                    COMPLETED: ['success', 'সম্পন্ন']],
        course   : [OPEN: ['success', 'চালু'], CANCELLED: ['danger', 'বাতিল']],
        phase    : [UPCOMING: ['info text-dark', 'আসন্ন'], RUNNING: ['primary', 'চলমান'], ENDED: ['secondary', 'শেষ হয়েছে']],
        enroll   : [ENROLLED: ['primary', 'নথিভুক্ত'], COMPLETED: ['success', 'উত্তীর্ণ'], FAILED: ['danger', 'অনুত্তীর্ণ'], CANCELLED: ['secondary', 'বাতিল']],
        claim    : [PENDING_MANAGER: ['warning text-dark', 'ম্যানেজারের অপেক্ষায়'], PENDING_ACCOUNTS: ['info text-dark', 'হিসাব বিভাগের অপেক্ষায়'],
                    APPROVED: ['primary', 'অনুমোদিত'], PAID: ['success', 'পরিশোধিত'], REJECTED: ['danger', 'প্রত্যাখ্যাত'], CANCELLED: ['secondary', 'বাতিল']],
        travel   : [PENDING: ['warning text-dark', 'অপেক্ষমাণ'], APPROVED: ['success', 'অনুমোদিত'], REJECTED: ['danger', 'প্রত্যাখ্যাত'], CANCELLED: ['secondary', 'বাতিল']],
        asset    : [AVAILABLE: ['success', 'মজুত'], ASSIGNED: ['primary', 'বরাদ্দ'], MAINTENANCE: ['warning text-dark', 'মেরামতে'], RETIRED: ['secondary', 'অবসরপ্রাপ্ত']],
        benefit  : [ACTIVE: ['success', 'সচল'], ENDED: ['secondary', 'শেষ']]
    ]

    /** kind = goal | cycle | appraisal | course | phase | enroll | claim | travel | asset | benefit */
    def d8Badge = { attrs ->
        String status = attrs.status as String
        List<String> v = BADGES[attrs.kind as String]?.get(status) ?: ['light text-dark', (status ?: '').replace('_', ' ')]
        out << "<span class=\"badge text-bg-${v[0]}\">${v[1].encodeAsHTML()}</span>"
    }

    private static final Map<String, String> NAMES = [
        // প্রশিক্ষণ: ক্যাটাগরি ও মাধ্যম
        TECHNICAL: 'টেকনিক্যাল', SOFT_SKILL: 'সফট স্কিল', COMPLIANCE: 'কমপ্লায়েন্স', LEADERSHIP: 'নেতৃত্ব',
        CLASSROOM: 'ক্লাসরুম', ONLINE: 'অনলাইন', WORKSHOP: 'ওয়ার্কশপ',
        // খরচের ক্যাটাগরি
        TRAVEL: 'ভ্রমণ', FOOD: 'খাবার', ACCOMMODATION: 'থাকা', TRANSPORT: 'যাতায়াত', OFFICE_SUPPLIES: 'অফিস সামগ্রী',
        TRAINING: 'প্রশিক্ষণ', COMMUNICATION: 'যোগাযোগ',
        // সম্পদের ধরন
        LAPTOP: 'ল্যাপটপ', DESKTOP: 'ডেস্কটপ', MONITOR: 'মনিটর', PHONE: 'ফোন', SIM: 'সিম', ID_CARD: 'আইডি কার্ড', ACCESS_CARD: 'অ্যাক্সেস কার্ড',
        // ভ্রমণের মাধ্যম
        BUS: 'বাস', TRAIN: 'ট্রেন', AIR: 'বিমান', CAR: 'গাড়ি',
        // সুবিধার ধরন
        HEALTH_INSURANCE: 'স্বাস্থ্য বীমা', LIFE_INSURANCE: 'জীবন বীমা', MOBILE: 'মোবাইল ভাতা', MEAL: 'খাবার ভাতা', EDUCATION: 'শিক্ষা সহায়তা',
        // সব জায়গায় এক
        OTHER: 'অন্যান্য'
    ]

    /** কোড -> বাংলা নাম; অজানা কোড হলে কোডটাই (সুন্দর করে) দেখায় */
    def d8Name = { attrs ->
        String v = attrs.value as String
        out << (v == null ? '—' : (NAMES[v] ?: v.replace('_', ' '))).toString().encodeAsHTML()
    }

    /** value = ০-১০০ (null/বেশি/কম হলে সীমার মধ্যে আনা হয়) */
    def progressBar = { attrs ->
        BigDecimal p = 0.0
        try { p = attrs.value == null ? 0.0 : new BigDecimal(attrs.value.toString()) } catch (NumberFormatException ignored) { p = 0.0 }
        int pct = Math.max(0, Math.min(100, p.setScale(0, java.math.RoundingMode.HALF_UP).intValue()))
        String color = pct >= 100 ? 'bg-success' : (pct >= 50 ? 'bg-primary' : 'bg-warning')
        out << "<div class=\"progress\" role=\"progressbar\" aria-valuenow=\"${pct}\" aria-valuemin=\"0\" aria-valuemax=\"100\" style=\"height:8px;\">" +
                "<div class=\"progress-bar ${color}\" style=\"width:${pct}%;\"></div></div>"
    }

    /** value = ১-৫; না থাকলে ড্যাশ */
    def stars = { attrs ->
        Integer n = null
        try { n = attrs.value == null || attrs.value.toString().trim() == '' ? null : new BigDecimal(attrs.value.toString()).intValue() } catch (NumberFormatException ignored) { n = null }
        if (n == null) {
            out << '—'
        } else {
            n = Math.max(0, Math.min(5, n))
            StringBuilder sb = new StringBuilder("<span class=\"text-warning text-nowrap\" title=\"${n}/5\" aria-label=\"${n}/5\">")
            (1..5).each { int i -> sb << (i <= n ? '<i class="bi bi-star-fill"></i>' : '<i class="bi bi-star"></i>') }
            out << sb.append('</span>').toString()
        }
    }
}

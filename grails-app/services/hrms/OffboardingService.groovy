package hrms

import grails.gorm.transactions.Transactional

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Day 9: চাকরি ছাড়া।
 *
 * নিয়ম:
 *  - কর্মী পদত্যাগের আবেদন দেয় (একসাথে একটাই চালু কেস)। ম্যানেজার থাকলে আগে ম্যানেজার, তারপর HR; না থাকলে সরাসরি HR।
 *  - নোটিস পিরিয়ড কর্মীর ধরন অনুযায়ী (ফুল-টাইম ৩০, চুক্তি/পার্ট-টাইম ১৫, ইন্টার্ন/প্রবেশন ৭ দিন)।
 *  - HR গ্রহণ করলে শেষ কর্মদিবস ঠিক হয় ও ক্লিয়ারেন্স চেকলিস্ট তৈরি হয়। কম নোটিস মওকুফ না হলে নিষ্পত্তিতে কর্তন।
 *  - HR এর পক্ষ থেকে শুরু (চাকরিচ্যুতি, অবসর, চুক্তি শেষ) করলে কেস সরাসরি ACCEPTED।
 *  - সম্পন্ন করতে: সব ক্লিয়ারেন্স ধাপ, নিষ্পত্তি PAID ও শেষ কর্মদিবস পার হতে হবে; তখন কর্মী INACTIVE ও লগইন বন্ধ।
 */
@Transactional
class OffboardingService {

    /** [ধাপের নাম, কোন বিভাগ টিক দেবে] */
    static final List<List<String>> CLEARANCE_TEMPLATE = [
            ['ল্যাপটপ ও ডিভাইস ফেরত', 'IT'],
            ['ইমেইল ও সিস্টেম অ্যাক্সেস বন্ধ', 'IT'],
            ['আইডি কার্ড ও অফিসের চাবি জমা', 'ADMIN'],
            ['কাজ ও নথি হস্তান্তর', 'MANAGER'],
            ['ঋণ, অগ্রিম ও দাবির হিসাব মিলানো', 'ACCOUNTS'],
            ['নথিপত্র যাচাই ও এক্সিট ইন্টারভিউ', 'HR']
    ]

    static final List<String> RESIGNATION_BLOCKED_REASONS = ['MISCONDUCT', 'PERFORMANCE', 'CONTRACT_END']

    def accessService
    def notificationService
    def auditService

    private static LocalDate today() { LocalDate.now(AttendanceService.ZONE) }

    private static LocalDateTime now() { LocalDateTime.now(AttendanceService.ZONE) }

    private static Map fail(String m) { [ok: false, message: m] }

    private String actor() { accessService.currentUser()?.username ?: 'system' }

    private static String link(Separation s) { "/offboarding/show/${s.id}".toString() }

    /** কর্মীর ধরন অনুযায়ী নোটিস পিরিয়ড (দিন) */
    static int noticeDaysFor(Employee e) {
        switch (e?.employeeType) {
            case ['INTERN', 'PROBATION']: return 7
            case ['CONTRACT', 'PART_TIME']: return 15
            default: return 30
        }
    }

    // ------------------------------------------------------------------ দেখার অনুমতি ও অপেক্ষমাণ তালিকা
    boolean canSee(Separation s) {
        s != null && accessService.canView(s.employee)
    }

    /** আমার সিদ্ধান্তের অপেক্ষায় থাকা কেস (HR/Admin: ম্যানেজার ও HR ধাপ; ম্যানেজার: অধীনদের ম্যানেজার ধাপ) */
    List<Separation> pendingForMe() {
        Long meId = accessService.currentEmployee()?.id
        List<Separation> result = []
        if (accessService.isHrAdmin()) {
            result.addAll(Separation.findAllByStatusInList(['PENDING_MANAGER', 'PENDING_HR'], [sort: 'id', order: 'desc']))
        } else if (AccessService.has('ROLE_MANAGER')) {
            Set<Long> ids = accessService.approvableIds() ?: ([] as Set<Long>)
            if (ids) {
                List<Long> idList = ids as List<Long>
                result.addAll(Separation.createCriteria().list {
                    eq('status', 'PENDING_MANAGER')
                    inList('employee.id', idList)
                    order('id', 'desc')
                } as List<Separation>)
            }
        }
        result.findAll { Separation s -> s.employee.id != meId }
    }

    // ------------------------------------------------------------------ আবেদন
    /** কর্মীর নিজের পদত্যাগের আবেদন */
    Map submit(Employee me, String reasonCategory, String reason, LocalDate lastDay) {
        if (me == null) return fail('আপনার কর্মী প্রোফাইল নেই।')
        if (Separation.countByEmployeeAndStatusInList(me, Separation.LIVE_STATUSES) > 0) return fail('আপনার একটি চাকরি-ছাড়ার অনুরোধ আগেই চালু আছে।')
        if (!Separation.REASONS.contains(reasonCategory) || RESIGNATION_BLOCKED_REASONS.contains(reasonCategory)) return fail('কারণের ধরন সঠিক নয়।')
        if (!reason?.trim()) return fail('বিস্তারিত কারণ লিখুন।')
        if (lastDay == null) return fail('শেষ কর্মদিবসের তারিখ দিন।')
        if (!lastDay.isAfter(today())) return fail('শেষ কর্মদিবস আজকের পরের কোনো দিন হতে হবে।')

        Separation s = new Separation(employee: me, exitType: 'RESIGNATION', reasonCategory: reasonCategory, reason: reason.trim().take(1000),
                noticeDate: today(), requestedLastDay: lastDay, noticeDays: noticeDaysFor(me),
                status: me.reportingManager ? 'PENDING_MANAGER' : 'PENDING_HR', initiatedBy: 'employee')
        if (!s.save(flush: true)) return fail('আবেদন সংরক্ষণ করা যায়নি।')

        if (s.status == 'PENDING_MANAGER') {
            notificationService.notifyEmployee(me.reportingManager, 'পদত্যাগের আবেদন', "${me.name} পদত্যাগের আবেদন করেছেন।".toString(), link(s), 'APPROVAL')
        } else {
            notificationService.notifyRoles(['ROLE_HR', 'ROLE_ADMIN'], 'পদত্যাগের আবেদন', "${me.name} পদত্যাগের আবেদন করেছেন।".toString(), link(s), 'APPROVAL')
        }
        auditService.record('SEPARATION_SUBMITTED', 'Separation', s.id, "${me.name}: ${s.exitType}, শেষ দিন ${lastDay}".toString())
        [ok: true, message: 'পদত্যাগের আবেদন জমা হয়েছে।', separation: s]
    }

    /** HR এর পক্ষ থেকে শুরু (চাকরিচ্যুতি, অবসর, চুক্তি শেষ): সরাসরি ACCEPTED */
    Map hrInitiate(Employee emp, String exitType, String reasonCategory, String reason, LocalDate lastDay) {
        if (!accessService.isHrAdmin()) return fail('শুধু HR/Admin এই কেস শুরু করতে পারে।')
        if (emp == null) return fail('কর্মী বেছে নিন।')
        if (!accessService.canManage(emp)) return fail('নিজের বিরুদ্ধে কেস শুরু করা যায় না।')
        if (!Separation.TYPES.contains(exitType) || exitType == 'RESIGNATION') return fail('ধরন সঠিক নয়।')
        if (!Separation.REASONS.contains(reasonCategory)) return fail('কারণের ধরন সঠিক নয়।')
        if (!reason?.trim()) return fail('বিস্তারিত কারণ লিখুন।')
        if (lastDay == null) return fail('শেষ কর্মদিবসের তারিখ দিন।')
        if (!lastDay.isAfter(today())) return fail('শেষ কর্মদিবস আজকের পরের কোনো দিন হতে হবে।')
        if (emp.status == 'INACTIVE') return fail('এই কর্মী আগেই নিষ্ক্রিয়।')
        if (Separation.countByEmployeeAndStatusInList(emp, Separation.LIVE_STATUSES) > 0) return fail('এই কর্মীর একটি কেস আগেই চালু আছে।')

        Separation s = new Separation(employee: emp, exitType: exitType, reasonCategory: reasonCategory, reason: reason.trim().take(1000),
                noticeDate: today(), requestedLastDay: lastDay, lastWorkingDay: lastDay, noticeDays: 0, noticeWaived: true,
                status: 'ACCEPTED', initiatedBy: 'hr', hrBy: actor(), hrAt: now())
        if (!s.save(flush: true)) return fail('কেস সংরক্ষণ করা যায়নি।')
        createClearance(s)

        notificationService.notifyEmployee(emp, 'চাকরি-ছাড়ার কেস শুরু হয়েছে', "শেষ কর্মদিবস: ${lastDay}".toString(), link(s), 'WARNING')
        auditService.record('SEPARATION_HR_INITIATED', 'Separation', s.id, "${emp.name}: ${exitType}, শেষ দিন ${lastDay}".toString())
        [ok: true, message: 'কেস শুরু হয়েছে; ক্লিয়ারেন্স চেকলিস্ট তৈরি হয়েছে।', separation: s]
    }

    // ------------------------------------------------------------------ সিদ্ধান্ত
    Map managerDecide(Separation s, boolean approve, String note) {
        if (s.status != 'PENDING_MANAGER') return fail('এই কেস এখন ম্যানেজারের ধাপে নেই।')
        if (!accessService.canManage(s.employee)) return fail('এই কেসে সিদ্ধান্ত দেওয়ার অনুমতি আপনার নেই।')
        if (!approve && !note?.trim()) return fail('প্রত্যাখ্যানের কারণ লিখুন।')

        s.managerBy = actor()
        s.managerAt = now()
        s.managerNote = note?.trim()?.take(255)
        s.status = approve ? 'PENDING_HR' : 'REJECTED'
        s.save(flush: true)

        if (approve) {
            notificationService.notifyRoles(['ROLE_HR', 'ROLE_ADMIN'], 'পদত্যাগের আবেদন (HR ধাপ)', "${s.employee.name} - ম্যানেজার পাঠিয়েছেন।".toString(), link(s), 'APPROVAL')
        } else {
            notificationService.notifyEmployee(s.employee, 'পদত্যাগের আবেদন প্রত্যাখ্যাত', s.managerNote, link(s), 'WARNING')
        }
        auditService.record(approve ? 'SEPARATION_MANAGER_APPROVED' : 'SEPARATION_MANAGER_REJECTED', 'Separation', s.id, s.employee.name)
        [ok: true, message: approve ? 'HR এর কাছে পাঠানো হয়েছে।' : 'আবেদন প্রত্যাখ্যান করা হয়েছে।']
    }

    Map hrDecide(Separation s, boolean approve, String note, LocalDate lastDay, boolean waive) {
        if (!(s.status in ['PENDING_MANAGER', 'PENDING_HR'])) return fail('এই কেসে এখন HR এর সিদ্ধান্তের সুযোগ নেই।')
        if (!accessService.isHrAdmin() || !accessService.canManage(s.employee)) return fail('এই কেসে সিদ্ধান্ত দেওয়ার অনুমতি আপনার নেই।')
        if (!approve && !note?.trim()) return fail('প্রত্যাখ্যানের কারণ লিখুন।')

        if (approve) {
            LocalDate lwd = lastDay ?: s.requestedLastDay
            if (lwd == null || lwd.isBefore(today())) return fail('শেষ কর্মদিবস আজ বা তার পরের কোনো দিন হতে হবে।')
            s.lastWorkingDay = lwd
            s.noticeWaived = waive
            s.status = 'ACCEPTED'
        } else {
            s.status = 'REJECTED'
        }
        s.hrBy = actor()
        s.hrAt = now()
        s.hrNote = note?.trim()?.take(255)
        s.save(flush: true)

        if (approve) {
            createClearance(s)
            notificationService.notifyEmployee(s.employee, 'পদত্যাগ গৃহীত', "শেষ কর্মদিবস: ${s.lastWorkingDay}".toString(), link(s), 'INFO')
        } else {
            notificationService.notifyEmployee(s.employee, 'পদত্যাগের আবেদন প্রত্যাখ্যাত', s.hrNote, link(s), 'WARNING')
        }
        auditService.record(approve ? 'SEPARATION_HR_ACCEPTED' : 'SEPARATION_HR_REJECTED', 'Separation', s.id, s.employee.name)
        [ok: true, message: approve ? 'পদত্যাগ গৃহীত হয়েছে; ক্লিয়ারেন্স চেকলিস্ট তৈরি হয়েছে।' : 'আবেদন প্রত্যাখ্যান করা হয়েছে।']
    }

    Map withdraw(Separation s) {
        if (accessService.currentEmployee()?.id != s.employee.id) return fail('শুধু আবেদনকারী নিজে প্রত্যাহার করতে পারেন।')
        if (!(s.status in ['PENDING_MANAGER', 'PENDING_HR'])) return fail('গৃহীত বা শেষ হওয়া আবেদন প্রত্যাহার করা যায় না।')
        s.status = 'WITHDRAWN'
        s.save(flush: true)
        auditService.record('SEPARATION_WITHDRAWN', 'Separation', s.id, s.employee.name)
        [ok: true, message: 'আবেদন প্রত্যাহার করা হয়েছে।']
    }

    /** HR কেস বাতিল করে (নিষ্পত্তি অনুমোদনের আগে পর্যন্ত); ক্লিয়ারেন্স ও খসড়া নিষ্পত্তি মুছে যায় */
    Map cancelExit(Separation s, String note) {
        if (!accessService.isHrAdmin() || !accessService.canManage(s.employee)) return fail('কেস বাতিলের অনুমতি আপনার নেই।')
        if (!(s.status in Separation.LIVE_STATUSES)) return fail('এই কেস আর বাতিল করা যায় না।')
        if (!note?.trim()) return fail('বাতিলের কারণ লিখুন।')
        FinalSettlement fs = FinalSettlement.findBySeparation(s)
        if (fs != null && fs.status != 'DRAFT') return fail('নিষ্পত্তি অনুমোদিত বা পরিশোধিত হয়ে গেছে; কেস বাতিল করা যাবে না।')

        ClearanceItem.executeUpdate('delete from ClearanceItem c where c.separation = :s', [s: s])
        if (fs != null) fs.delete(flush: true)
        s.status = 'WITHDRAWN'
        s.hrBy = actor()
        s.hrAt = now()
        s.hrNote = note.trim().take(255)
        s.save(flush: true)

        notificationService.notifyEmployee(s.employee, 'চাকরি-ছাড়ার কেস বাতিল', s.hrNote, link(s), 'INFO')
        auditService.record('SEPARATION_CANCELLED', 'Separation', s.id, s.employee.name)
        [ok: true, message: 'কেস বাতিল করা হয়েছে।']
    }

    // ------------------------------------------------------------------ ক্লিয়ারেন্স
    /** চেকলিস্ট না থাকলে টেমপ্লেট থেকে বানায় */
    void createClearance(Separation s) {
        if (ClearanceItem.countBySeparation(s) > 0) return
        int order = 0
        CLEARANCE_TEMPLATE.each { List<String> t ->
            new ClearanceItem(separation: s, title: t[0], ownerRole: t[1], sortOrder: order++).save(flush: true)
        }
    }

    List<ClearanceItem> clearanceOf(Separation s) {
        ClearanceItem.findAllBySeparation(s, [sort: 'sortOrder', order: 'asc'])
    }

    Map clearanceProgress(Separation s) {
        List<ClearanceItem> items = ClearanceItem.findAllBySeparation(s)
        int done = items.count { ClearanceItem i -> i.done } as int
        [done: done, total: items.size(), allDone: !items.isEmpty() && done == items.size()]
    }

    /** কে কোন ধাপ টিক দিতে পারবে: IT/ADMIN/HR ধাপ HR-Admin; ACCOUNTS ধাপ Admin-হিসাব বিভাগ; MANAGER ধাপ ম্যানেজার (বা HR) */
    boolean canTick(ClearanceItem i) {
        Separation s = i.separation
        if (s.status != 'ACCEPTED') return false
        if (accessService.currentEmployee()?.id == s.employee.id) return false
        switch (i.ownerRole) {
            case ['IT', 'ADMIN', 'HR']: return AccessService.has('ROLE_ADMIN,ROLE_HR')
            case 'ACCOUNTS': return AccessService.has('ROLE_ADMIN,ROLE_ACCOUNTANT')
            case 'MANAGER': return accessService.canManage(s.employee)
            default: return false
        }
    }

    Map tick(ClearanceItem item, String remarks) {
        if (!canTick(item)) return fail('এই ধাপ টিক দেওয়ার অনুমতি আপনার নেই।')
        if (item.done) return fail('ধাপটি আগেই সম্পন্ন হয়েছে।')
        item.done = true
        item.doneBy = actor()
        item.doneAt = now()
        item.remarks = remarks?.trim()?.take(255)
        item.save(flush: true)
        auditService.record('CLEARANCE_TICKED', 'ClearanceItem', item.id, "${item.separation.employee.name}: ${item.title}".toString())
        [ok: true, message: 'ধাপটি সম্পন্ন হিসেবে চিহ্নিত হয়েছে।']
    }

    Map reopen(ClearanceItem item) {
        if (!canTick(item)) return fail('এই ধাপ আবার খোলার অনুমতি আপনার নেই।')
        if (!item.done) return fail('ধাপটি এমনিতেই খোলা আছে।')
        item.done = false
        item.doneBy = null
        item.doneAt = null
        item.remarks = null
        item.save(flush: true)
        auditService.record('CLEARANCE_REOPENED', 'ClearanceItem', item.id, "${item.separation.employee.name}: ${item.title}".toString())
        [ok: true, message: 'ধাপটি আবার খোলা হয়েছে।']
    }

    // ------------------------------------------------------------------ সম্পন্ন
    Map complete(Separation s, String exitNote) {
        if (!accessService.isHrAdmin() || !accessService.canManage(s.employee)) return fail('কেস সম্পন্ন করার অনুমতি আপনার নেই।')
        if (s.status != 'ACCEPTED') return fail('শুধু গৃহীত কেস সম্পন্ন করা যায়।')
        if (!(clearanceProgress(s).allDone)) return fail('সব ক্লিয়ারেন্স ধাপ সম্পন্ন করুন।')
        FinalSettlement fs = FinalSettlement.findBySeparation(s)
        if (fs == null || fs.status != 'PAID') return fail('চূড়ান্ত নিষ্পত্তি পরিশোধ হয়নি।')
        LocalDate lwd = s.lastWorkingDay ?: s.requestedLastDay
        if (today().isBefore(lwd)) return fail("শেষ কর্মদিবস (${lwd}) এখনো আসেনি।".toString())

        Employee emp = s.employee
        emp.status = 'INACTIVE'
        if (!emp.save(flush: true)) {
            emp.discard()
            return fail('কর্মীকে নিষ্ক্রিয় করা যায়নি (তথ্যে ভুল আছে)।')
        }
        User.findAllByEmployee(emp).each { User u ->
            u.enabled = false
            u.save(flush: true)
        }

        long seq = Separation.countByLetterNoLike("REL-${lwd.year}-%".toString()) + 1
        s.letterNo = String.format('REL-%d-%05d', lwd.year, seq)
        s.exitInterviewNote = exitNote?.trim() ? exitNote.trim().take(1000) : null
        s.status = 'COMPLETED'
        s.completedBy = actor()
        s.completedAt = now()
        s.save(flush: true)

        auditService.record('SEPARATION_COMPLETED', 'Separation', s.id, "${emp.name}: ${s.letterNo}".toString())
        [ok: true, message: "কেস সম্পন্ন হয়েছে; কর্মী নিষ্ক্রিয় ও লগইন বন্ধ। চিঠি নং ${s.letterNo}".toString()]
    }
}

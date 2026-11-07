package hrms

import grails.gorm.transactions.Transactional

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Day 9: কর্মী সম্পর্ক - ঘোষণা, শৃঙ্খলা কেস, অভিযোগ।
 *
 * নিয়ম:
 *  - ঘোষণা: HR/Admin দেয়; চালু শর্ত active + প্রকাশের তারিখ এসে গেছে + মেয়াদ শেষ হয়নি; চাইলে সবাইকে নোটিফিকেশন।
 *  - শৃঙ্খলা (গোপনীয়): কেস খোলে HR/Admin বা ম্যানেজার (নিজের অধীনের কর্মীর ওপর); দেখতে পায় কর্মী নিজে, যে খুলেছে সে, ও HR/Admin।
 *    কারণ দর্শানো জবাব ছাড়া বন্ধ হয় না; HR নিজের কেস বন্ধ করতে পারে না।
 *  - অভিযোগ: কর্মী জমা দেয়; দেখে কর্মী নিজে ও HR/Admin; HR নিজের অভিযোগে সিদ্ধান্ত দিতে পারে না।
 */
@Transactional
class RelationsService {

    def accessService
    def notificationService
    def auditService

    private static LocalDate today() { LocalDate.now(AttendanceService.ZONE) }

    private static LocalDateTime now() { LocalDateTime.now(AttendanceService.ZONE) }

    private static Map fail(String m) { [ok: false, message: m] }

    private String actor() { accessService.currentUser()?.username ?: 'system' }

    private boolean isOwn(Employee e) { e != null && accessService.currentEmployee()?.id == e.id }

    // ================================================================== ঘোষণা
    /** এখন চালু ঘোষণা: pinned আগে, তারপর নতুন আগে */
    List<Announcement> current(int max = 50) {
        LocalDate today = today()
        Announcement.createCriteria().list(max: max) {
            eq('active', true)
            le('publishDate', today)
            or {
                isNull('expiryDate')
                ge('expiryDate', today)
            }
            order('pinned', 'desc')
            order('publishDate', 'desc')
            order('id', 'desc')
        } as List<Announcement>
    }

    Map postAnnouncement(String title, String body, String category, boolean pinned, LocalDate publishDate, LocalDate expiryDate, boolean notify) {
        if (!accessService.isHrAdmin()) return fail('ঘোষণা দেওয়ার অনুমতি শুধু HR/Admin এর।')
        if (!title?.trim()) return fail('শিরোনাম দিন।')
        if (!body?.trim()) return fail('ঘোষণার বিবরণ দিন।')
        if (!Announcement.CATEGORIES.contains(category)) return fail('ধরন সঠিক নয়।')
        LocalDate pub = publishDate ?: today()
        if (expiryDate != null && expiryDate.isBefore(pub)) return fail('মেয়াদ প্রকাশের তারিখের আগে হতে পারবে না।')

        Announcement a = new Announcement(title: title.trim().take(150), body: body.trim().take(4000), category: category, pinned: pinned,
                publishDate: pub, expiryDate: expiryDate, postedBy: actor())
        if (!a.save(flush: true)) return fail('ঘোষণা সংরক্ষণ করা যায়নি।')

        // পরে প্রকাশের তারিখ হলে এখনই নোটিফিকেশন পাঠানো হয় না (ঘোষণা তখনো চালু নয়)
        if (notify && !pub.isAfter(today())) {
            notificationService.notifyAll("নতুন ঘোষণা: ${a.title}".toString(), a.body.take(100), '/announcement', a.category == 'URGENT' ? 'WARNING' : 'INFO')
        }
        auditService.record('ANNOUNCEMENT_POSTED', 'Announcement', a.id, a.title)
        [ok: true, message: 'ঘোষণা প্রকাশ হয়েছে।', announcement: a]
    }

    Map toggleAnnouncement(Announcement a) {
        a.active = !a.active
        a.save(flush: true)
        auditService.record(a.active ? 'ANNOUNCEMENT_ACTIVATED' : 'ANNOUNCEMENT_DEACTIVATED', 'Announcement', a.id, a.title)
        [ok: true, message: a.active ? 'ঘোষণা চালু হয়েছে।' : 'ঘোষণা বন্ধ হয়েছে।']
    }

    Map deleteAnnouncement(Announcement a) {
        Long id = a.id
        String title = a.title
        a.delete(flush: true)
        auditService.record('ANNOUNCEMENT_DELETED', 'Announcement', id, title)
        [ok: true, message: 'ঘোষণা মুছে ফেলা হয়েছে।']
    }

    // ================================================================== শৃঙ্খলা কেস
    boolean canSeeCase(DisciplinaryCase c) {
        if (c == null) return false
        accessService.isHrAdmin() || isOwn(c.employee) || (c.raisedBy != null && c.raisedBy == accessService.currentUser()?.username)
    }

    /** আমার দেখার অনুমতি আছে এমন কেস (HR/Admin: সব) */
    List<DisciplinaryCase> visibleCases(String status) {
        boolean hr = accessService.isHrAdmin()
        Employee me = accessService.currentEmployee()
        String uname = accessService.currentUser()?.username
        if (!hr && me == null && !uname) return []
        DisciplinaryCase.createCriteria().list {
            if (status) eq('status', status)
            if (!hr) {
                or {
                    if (me != null) eq('employee', me)
                    if (uname) eq('raisedBy', uname)
                }
            }
            order('id', 'desc')
        } as List<DisciplinaryCase>
    }

    Map raiseCase(Employee emp, String caseType, LocalDate incidentDate, String subject, String description) {
        if (!accessService.isApprover()) return fail('কেস খোলার অনুমতি শুধু HR/Admin ও ম্যানেজারদের।')
        if (emp == null) return fail('কর্মী বেছে নিন।')
        if (!accessService.canManage(emp)) return fail('এই কর্মীর বিরুদ্ধে কেস খোলার অনুমতি আপনার নেই।')
        if (!DisciplinaryCase.TYPES.contains(caseType)) return fail('কেসের ধরন সঠিক নয়।')
        if (incidentDate == null) return fail('ঘটনার তারিখ দিন।')
        if (incidentDate.isAfter(today())) return fail('ঘটনার তারিখ ভবিষ্যতের হতে পারে না।')
        if (!subject?.trim()) return fail('বিষয় লিখুন।')
        if (!description?.trim()) return fail('বিবরণ লিখুন।')

        DisciplinaryCase c = new DisciplinaryCase(employee: emp, caseType: caseType, incidentDate: incidentDate,
                subject: subject.trim().take(150), description: description.trim().take(2000), raisedBy: actor())
        if (!c.save(flush: true)) return fail('কেস সংরক্ষণ করা যায়নি।')

        String link = "/discipline/show/${c.id}".toString()
        notificationService.notifyEmployee(emp, 'শৃঙ্খলা সংক্রান্ত নোটিস', c.subject, link, 'WARNING')
        notificationService.notifyRoles(['ROLE_HR', 'ROLE_ADMIN'], 'নতুন শৃঙ্খলা কেস', "${emp.name}: ${c.subject}".toString(), link, 'INFO',
                accessService.currentUser()?.id)
        auditService.record('DISCIPLINE_RAISED', 'DisciplinaryCase', c.id, "${emp.name}: ${caseType}".toString())
        [ok: true, message: 'কেস খোলা হয়েছে; কর্মীকে জানানো হয়েছে।', discipline: c]
    }

    /** কর্মীর জবাব (শুধু নিজের OPEN কেসে) */
    Map respondCase(DisciplinaryCase c, String response) {
        if (!isOwn(c.employee)) return fail('শুধু সংশ্লিষ্ট কর্মী জবাব দিতে পারেন।')
        if (c.status != 'OPEN') return fail('এই কেসে এখন জবাব দেওয়া যায় না।')
        if (!response?.trim()) return fail('জবাব লিখুন।')

        c.employeeResponse = response.trim().take(2000)
        c.respondedAt = now()
        c.status = 'RESPONDED'
        c.save(flush: true)

        String link = "/discipline/show/${c.id}".toString()
        notificationService.notifyRoles(['ROLE_HR', 'ROLE_ADMIN'], 'শৃঙ্খলা কেসে জবাব এসেছে', "${c.employee.name}: ${c.subject}".toString(), link, 'APPROVAL')
        if (c.raisedBy) notificationService.notifyUser(User.findByUsername(c.raisedBy), 'শৃঙ্খলা কেসে জবাব এসেছে', "${c.employee.name}: ${c.subject}".toString(), link, 'INFO')
        auditService.record('DISCIPLINE_RESPONDED', 'DisciplinaryCase', c.id, c.employee.name)
        [ok: true, message: 'আপনার জবাব জমা হয়েছে।']
    }

    /** HR এর সিদ্ধান্ত ও বন্ধ */
    Map closeCase(DisciplinaryCase c, String outcome, String note) {
        if (!accessService.isHrAdmin() || isOwn(c.employee)) return fail('কেস বন্ধ করার অনুমতি আপনার নেই।')
        if (c.status == 'CLOSED') return fail('কেস আগেই বন্ধ হয়েছে।')
        if (c.caseType == 'SHOW_CAUSE' && c.status == 'OPEN') return fail('কারণ দর্শানোর জবাব আসার আগে কেস বন্ধ করা যাবে না।')
        if (!DisciplinaryCase.OUTCOMES.contains(outcome)) return fail('সিদ্ধান্ত বেছে নিন।')
        if (outcome != 'NO_ACTION' && !note?.trim()) return fail('ব্যবস্থা নিলে কারণ/বিবরণ লিখুন।')

        c.outcome = outcome
        c.outcomeNote = note?.trim()?.take(500)
        c.status = 'CLOSED'
        c.closedBy = actor()
        c.closedAt = now()
        c.save(flush: true)

        notificationService.notifyEmployee(c.employee, 'শৃঙ্খলা কেসের সিদ্ধান্ত', c.subject, "/discipline/show/${c.id}".toString(), 'WARNING')
        auditService.record('DISCIPLINE_CLOSED', 'DisciplinaryCase', c.id, "${c.employee.name}: ${outcome}".toString())
        [ok: true, message: 'সিদ্ধান্ত রেকর্ড হয়েছে; কেস বন্ধ।']
    }

    // ================================================================== অভিযোগ
    boolean canSeeGrievance(Grievance g) {
        g != null && (accessService.isHrAdmin() || isOwn(g.employee))
    }

    /** HR/Admin: সব; অন্যরা: শুধু নিজের */
    List<Grievance> visibleGrievances(String status) {
        boolean hr = accessService.isHrAdmin()
        Employee me = accessService.currentEmployee()
        if (!hr && me == null) return []
        Grievance.createCriteria().list {
            if (status) eq('status', status)
            if (!hr) eq('employee', me)
            order('id', 'desc')
        } as List<Grievance>
    }

    Map submitGrievance(Employee emp, String category, String subject, String description) {
        if (emp == null) return fail('আপনার লগইনের সাথে কোনো কর্মী প্রোফাইল যুক্ত নেই।')
        if (!Grievance.CATEGORIES.contains(category)) return fail('অভিযোগের ধরন সঠিক নয়।')
        if (!subject?.trim()) return fail('বিষয় লিখুন।')
        if (!description?.trim()) return fail('বিবরণ লিখুন।')

        Grievance g = new Grievance(employee: emp, category: category, subject: subject.trim().take(150), description: description.trim().take(2000))
        if (!g.save(flush: true)) return fail('অভিযোগ সংরক্ষণ করা যায়নি।')

        notificationService.notifyRoles(['ROLE_HR', 'ROLE_ADMIN'], 'নতুন অভিযোগ', g.subject, "/grievance/show/${g.id}".toString(), 'APPROVAL',
                accessService.currentUser()?.id)
        auditService.record('GRIEVANCE_SUBMITTED', 'Grievance', g.id, category)
        [ok: true, message: 'অভিযোগ জমা হয়েছে।', grievance: g]
    }

    List<GrievanceNote> notesOf(Grievance g) {
        GrievanceNote.findAllByGrievance(g, [sort: 'id', order: 'asc'])
    }

    /** কর্মী বা HR মন্তব্য যোগ করে (খোলা অভিযোগে) */
    Map addGrievanceNote(Grievance g, String note) {
        if (!canSeeGrievance(g)) return fail('এই অভিযোগে মন্তব্যের অনুমতি নেই।')
        if (g.status in ['RESOLVED', 'REJECTED']) return fail('বন্ধ হয়ে যাওয়া অভিযোগে মন্তব্য যোগ করা যায় না।')
        if (!note?.trim()) return fail('মন্তব্য লিখুন।')

        boolean own = isOwn(g.employee)
        GrievanceNote n = new GrievanceNote(grievance: g, author: actor(), fromHr: accessService.isHrAdmin() && !own, note: note.trim().take(1000))
        if (!n.save(flush: true)) return fail('মন্তব্য সংরক্ষণ করা যায়নি।')

        String link = "/grievance/show/${g.id}".toString()
        if (own) {
            notificationService.notifyRoles(['ROLE_HR', 'ROLE_ADMIN'], 'অভিযোগে নতুন মন্তব্য', g.subject, link, 'INFO', accessService.currentUser()?.id)
        } else {
            notificationService.notifyEmployee(g.employee, 'অভিযোগে HR এর মন্তব্য', g.subject, link, 'INFO')
        }
        [ok: true, message: 'মন্তব্য যোগ হয়েছে।']
    }

    /** HR: পর্যালোচনায় নেওয়া / সমাধান / প্রত্যাখ্যান */
    Map updateGrievance(Grievance g, String target, String resolution) {
        if (!accessService.isHrAdmin() || isOwn(g.employee)) return fail('অভিযোগে সিদ্ধান্ত দেওয়ার অনুমতি আপনার নেই।')
        if (g.status in ['RESOLVED', 'REJECTED']) return fail('অভিযোগ আগেই বন্ধ হয়েছে।')
        if (!(target in ['IN_REVIEW', 'RESOLVED', 'REJECTED'])) return fail('অবস্থা সঠিক নয়।')
        if (target == 'IN_REVIEW' && g.status != 'OPEN') return fail('শুধু নতুন অভিযোগ পর্যালোচনায় নেওয়া যায়।')
        if (target in ['RESOLVED', 'REJECTED'] && !resolution?.trim()) return fail(target == 'RESOLVED' ? 'সমাধানের বিবরণ লিখুন।' : 'প্রত্যাখ্যানের কারণ লিখুন।')

        g.status = target
        g.handledBy = actor()
        if (target != 'IN_REVIEW') {
            g.resolution = resolution.trim().take(1000)
            g.resolvedAt = now()
        }
        g.save(flush: true)

        String title = target == 'IN_REVIEW' ? 'অভিযোগ পর্যালোচনায়' : (target == 'RESOLVED' ? 'অভিযোগের সমাধান হয়েছে' : 'অভিযোগ প্রত্যাখ্যাত')
        notificationService.notifyEmployee(g.employee, title, g.subject, "/grievance/show/${g.id}".toString(), target == 'REJECTED' ? 'WARNING' : 'INFO')
        auditService.record('GRIEVANCE_' + target, 'Grievance', g.id, g.subject)
        [ok: true, message: target == 'IN_REVIEW' ? 'অভিযোগ পর্যালোচনায় নেওয়া হয়েছে।' : (target == 'RESOLVED' ? 'অভিযোগ সমাধান হিসেবে রেকর্ড হয়েছে।' : 'অভিযোগ প্রত্যাখ্যান করা হয়েছে।')]
    }
}

package hrms

import grails.gorm.transactions.Transactional

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Day 7: অনবোর্ডিং চেকলিস্ট, সম্পদ বরাদ্দ (asset allocation) ও পরীক্ষাকাল (probation) ট্র্যাকিং।
 *  - নতুন কর্মীর জন্য সক্রিয় OnboardingTemplate থেকে কাজ তৈরি হয় (যোগদানের তারিখ + dueAfterDays)।
 *  - পরীক্ষাকাল: ONGOING -> CONFIRMED / NOT_CONFIRMED; সর্বোচ্চ ২ বার, প্রতিবার ১-৩ মাস বাড়ানো যায়।
 */
@Transactional
class OnboardingService {

    def accessService

    private static Map fail(String m) { [ok: false, message: m] }

    private static LocalDate today() { LocalDate.now(AttendanceService.ZONE) }

    private String actor() { accessService.currentUser()?.username }

    // ================================================================== চেকলিস্ট
    /** টেমপ্লেট থেকে কাজ তৈরি। কর্মীর আগে থেকে কাজ থাকলে কিছু করে না। ফেরত: তৈরি হওয়া কাজের সংখ্যা */
    int startOnboarding(Employee emp) {
        if (OnboardingTask.countByEmployee(emp) > 0) return 0
        List<OnboardingTemplate> templates = OnboardingTemplate.findAllByActive(true, [sort: 'sortOrder'])
        templates.each { OnboardingTemplate t ->
            new OnboardingTask(employee: emp, title: t.title, category: t.category, ownerRole: t.ownerRole,
                    dueDate: emp.joinDate.plusDays(t.dueAfterDays), sortOrder: t.sortOrder).save(failOnError: true)
        }
        templates.size()
    }

    Map completeTask(OnboardingTask t, String remarks) {
        if (t.done) return fail('কাজটি আগেই সম্পন্ন।')
        t.done = true
        t.doneBy = actor()
        t.doneAt = LocalDateTime.now(AttendanceService.ZONE)
        t.remarks = remarks?.trim()?.take(255) ?: null
        t.save(failOnError: true, flush: true)
        [ok: true, message: "'${t.title}' সম্পন্ন চিহ্নিত হয়েছে।".toString()]
    }

    Map reopenTask(OnboardingTask t) {
        if (!t.done) return fail('কাজটি এখনো বাকি।')
        t.done = false
        t.doneBy = null
        t.doneAt = null
        t.save(failOnError: true, flush: true)
        [ok: true, message: 'কাজটি আবার বাকি করা হয়েছে।']
    }

    Map addTask(Employee emp, String title, String category, String owner, LocalDate due) {
        if (!title?.trim()) return fail('কাজের নাম লিখুন।')
        if (!OnboardingTemplate.CATEGORIES.contains(category)) return fail('ধরন সঠিক নয়।')
        if (!OnboardingTemplate.OWNERS.contains(owner)) return fail('দায়িত্বপ্রাপ্ত বিভাগ সঠিক নয়।')
        int nextOrder = ((OnboardingTask.executeQuery('select max(t.sortOrder) from OnboardingTask t where t.employee = :e', [e: emp])[0] ?: 0) as int) + 1
        OnboardingTask t = new OnboardingTask(employee: emp, title: title.trim().take(150), category: category, ownerRole: owner,
                dueDate: due, sortOrder: nextOrder)
        if (!t.save(flush: true)) return fail('কাজ সংরক্ষণ করা যায়নি।')
        [ok: true, message: 'কাজ যোগ করা হয়েছে।']
    }

    Map deleteTask(OnboardingTask t) {
        if (t.done) return fail('সম্পন্ন কাজ মোছা যায় না - আগে "আবার বাকি" করুন।')
        t.delete(flush: true)
        [ok: true, message: 'কাজ মুছে ফেলা হয়েছে।']
    }

    /** [total, done, percent] */
    Map progress(Employee emp) {
        int total = OnboardingTask.countByEmployee(emp)
        int done = OnboardingTask.countByEmployeeAndDone(emp, true)
        [total: total, done: done, percent: total ? (int) Math.round(done * 100.0d / total) : 0]
    }

    /** তালিকার জন্য: কর্মী id -> [total, done, percent] (এক কোয়েরিতে) */
    Map<Long, Map> progressByEmployee() {
        Map<Long, Map> m = [:]
        OnboardingTask.executeQuery('select t.employee.id, count(t), sum(case when t.done = true then 1 else 0 end) from OnboardingTask t group by t.employee.id').each { row ->
            int total = (row[1] as Number).intValue()
            int done = ((row[2] ?: 0) as Number).intValue()
            m[row[0] as Long] = [total: total, done: done, percent: total ? (int) Math.round(done * 100.0d / total) : 0]
        }
        m
    }

    // ================================================================== সম্পদ
    Map issueAsset(AssetAllocation a) {
        a.status = 'ISSUED'
        a.returnedDate = null
        a.issuedBy = actor()
        a.validate()
        if (a.issuedDate != null && a.issuedDate.isAfter(today())) a.errors.rejectValue('issuedDate', 'invalid', 'বরাদ্দের তারিখ ভবিষ্যতের হতে পারে না।')
        if (a.employee?.status == 'INACTIVE') return fail('নিষ্ক্রিয় কর্মীকে সম্পদ দেওয়া যায় না।')
        if (a.serialNo?.trim()) {
            a.serialNo = a.serialNo.trim()
            AssetAllocation other = AssetAllocation.findBySerialNoAndStatus(a.serialNo, 'ISSUED')
            if (other != null) return fail("এই সিরিয়াল (${a.serialNo}) এখন ${other.employee.name} এর কাছে আছে - আগে ফেরত নিন।".toString())
        }
        if (a.hasErrors()) return [ok: false, message: 'ফর্মের তথ্য ঠিক করুন (নাম ও তারিখ বাধ্যতামূলক)।']
        a.save(failOnError: true, flush: true)
        [ok: true, message: "'${a.assetName}' বরাদ্দ করা হয়েছে।".toString()]
    }

    Map returnAsset(AssetAllocation a, LocalDate date, String remarks) {
        if (a.status != 'ISSUED') return fail('সম্পদটি আগেই ফেরত নেওয়া হয়েছে।')
        LocalDate d = date ?: today()
        if (d.isBefore(a.issuedDate)) return fail('ফেরতের তারিখ বরাদ্দের আগে হতে পারে না।')
        if (d.isAfter(today())) return fail('ফেরতের তারিখ ভবিষ্যতের হতে পারে না।')
        a.status = 'RETURNED'
        a.returnedDate = d
        if (remarks?.trim()) a.remarks = remarks.trim().take(255)
        a.save(failOnError: true, flush: true)
        [ok: true, message: "'${a.assetName}' ফেরত নেওয়া হয়েছে।".toString()]
    }

    // ================================================================== পরীক্ষাকাল
    ProbationRecord startProbation(Employee emp, LocalDate start, Integer months, String confirmType) {
        ProbationRecord existing = ProbationRecord.findByEmployee(emp)
        if (existing != null) return existing
        String type = (confirmType && confirmType != 'PROBATION' && Employee.TYPES.contains(confirmType)) ? confirmType : 'FULL_TIME'
        new ProbationRecord(employee: emp, startDate: start, endDate: start.plusMonths(Math.max(1, months ?: 3)), confirmType: type)
                .save(failOnError: true, flush: true)
    }

    /** পরীক্ষাকাল শুরু করা (যেসব কর্মীর রেকর্ড নেই) - মেয়াদ মাসে */
    Map startProbationManually(Employee emp, Integer months) {
        if (ProbationRecord.findByEmployee(emp) != null) return fail('এই কর্মীর পরীক্ষাকালের রেকর্ড আগেই আছে।')
        if (months == null || months < 1 || months > 12) return fail('মেয়াদ ১ থেকে ১২ মাসের মধ্যে দিন।')
        ProbationRecord r = startProbation(emp, emp.joinDate, months, 'FULL_TIME')
        if (emp.employeeType != 'PROBATION') {
            emp.employeeType = 'PROBATION'
            emp.save(failOnError: true)
        }
        [ok: true, message: "পরীক্ষাকাল শুরু (শেষ: ${r.endDate})।".toString()]
    }

    Map confirmProbation(ProbationRecord r, String note) {
        if (r.status != 'ONGOING') return fail('এই পরীক্ষাকালের সিদ্ধান্ত আগেই হয়ে গেছে।')
        Employee emp = r.employee
        String from = emp.employeeType
        emp.employeeType = r.confirmType
        emp.save(failOnError: true)
        r.status = 'CONFIRMED'
        decide(r, note)
        new EmploymentHistory(employee: emp, eventType: 'OTHER', effectiveDate: today(),
                remarks: "পরীক্ষাকাল শেষে স্থায়ী (${from} -> ${r.confirmType})".toString(), recordedBy: actor()).save(failOnError: true)
        [ok: true, message: "${emp.name} স্থায়ী (${r.confirmType}) হয়েছেন।".toString()]
    }

    Map extendProbation(ProbationRecord r, Integer months, String note) {
        if (r.status != 'ONGOING') return fail('শুধু চলমান পরীক্ষাকাল বাড়ানো যায়।')
        if (r.extensions >= ProbationRecord.MAX_EXTENSIONS) return fail("সর্বোচ্চ ${ProbationRecord.MAX_EXTENSIONS} বার বাড়ানো যায়।".toString())
        if (months == null || months < 1 || months > 3) return fail('১ থেকে ৩ মাস বাড়ানো যায়।')
        if (!note?.trim()) return fail('মেয়াদ বাড়ানোর কারণ লিখুন।')
        r.endDate = r.endDate.plusMonths(months)
        r.extensions = r.extensions + 1
        r.reviewNote = note.trim().take(500)
        r.save(failOnError: true, flush: true)
        [ok: true, message: "পরীক্ষাকাল ${months} মাস বাড়ানো হয়েছে (নতুন শেষ: ${r.endDate})।".toString()]
    }

    /** স্থায়ী করা হলো না - চাকরি ছাড়ার প্রক্রিয়া (Offboarding) Day 9 এ আসছে, তাই কর্মীর স্ট্যাটাস এখানে বদলায় না */
    Map notConfirmProbation(ProbationRecord r, String note) {
        if (r.status != 'ONGOING') return fail('এই পরীক্ষাকালের সিদ্ধান্ত আগেই হয়ে গেছে।')
        if (!note?.trim()) return fail('কারণ লিখুন।')
        r.status = 'NOT_CONFIRMED'
        decide(r, note)
        [ok: true, message: 'স্থায়ী না করার সিদ্ধান্ত রেকর্ড হয়েছে। (চাকরি ছাড়ার প্রক্রিয়া Day 9 এর Offboarding মডিউলে।)']
    }

    private void decide(ProbationRecord r, String note) {
        r.decidedBy = actor()
        r.decidedAt = LocalDateTime.now(AttendanceService.ZONE)
        if (note?.trim()) r.reviewNote = note.trim().take(500)
        r.save(failOnError: true, flush: true)
    }

    /** চলমান পরীক্ষাকাল (শেষ তারিখ অনুযায়ী) */
    List<ProbationRecord> ongoingProbations() {
        ProbationRecord.findAllByStatus('ONGOING', [sort: 'endDate', order: 'asc'])
    }
}

package hrms

import grails.gorm.transactions.Transactional

import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Day 8: লক্ষ্য/KPI ও মূল্যায়ন চক্র।
 *  - লক্ষ্য: কর্মী নিজের, ম্যানেজার অধীনের, HR/Admin সবার জন্য দিতে পারে। এক বছরের সচল লক্ষ্যের weight এর যোগফল <= ১০০।
 *  - চক্র: DRAFT -> OPEN (সব সক্রিয় কর্মীর Appraisal তৈরি) -> CLOSED।
 *  - মূল্যায়ন: কর্মী নিজে (১-৫) -> ম্যানেজার (১-৫) -> COMPLETED। কেউ নিজের মূল্যায়ন করতে পারে না।
 */
@Transactional
class PerformanceService {

    static final int MAX_TOTAL_WEIGHT = 100

    def accessService

    private static Map fail(String m) { [ok: false, message: m] }

    private static LocalDate today() { LocalDate.now(AttendanceService.ZONE) }

    private String actor() { accessService.currentUser()?.username }

    // ================================================================== লক্ষ্য / KPI
    boolean canEditGoalsOf(Employee emp) {
        emp != null && (accessService.currentEmployee()?.id == emp.id || accessService.canManage(emp))
    }

    /** কোন কর্মীদের লক্ষ্য এই ব্যবহারকারী দেখতে পারবে; null = সবাই */
    Set<Long> visibleGoalOwnerIds() {
        if (accessService.isHrAdmin()) return null
        Long me = accessService.currentEmployee()?.id
        Set<Long> ids = [] as Set<Long>
        if (me != null) ids << me
        ids.addAll(accessService.approvableIds() ?: [])
        ids
    }

    int usedWeight(Employee emp, int year, Long excludeGoalId = null) {
        List<Goal> goals = Goal.findAllByEmployeeAndStatusNotEqual(emp, 'CANCELLED')
        goals.findAll { Goal g -> g.dueDate.year == year && g.id != excludeGoalId }.sum { Goal g -> g.weight } as Integer ?: 0
    }

    Map createGoal(Employee emp, String title, String description, BigDecimal target, String unit, Integer weight, LocalDate due) {
        if (emp == null) return fail('কর্মী নির্বাচন করুন।')
        if (!canEditGoalsOf(emp)) return fail('এই কর্মীর জন্য লক্ষ্য দেওয়ার অনুমতি আপনার নেই।')
        if (emp.status == 'INACTIVE') return fail('নিষ্ক্রিয় কর্মীর লক্ষ্য দেওয়া যায় না।')
        Goal g = new Goal(employee: emp, title: title?.trim(), description: description?.trim() ?: null, targetValue: target,
                unit: unit?.trim() ?: '%', weight: weight, dueDate: due, createdBy: actor())
        if (!g.validate()) return fail('ফর্মের তথ্য ঠিক করুন (শিরোনাম, লক্ষ্যমাত্রা > 0, weight ১-১০০, শেষ তারিখ বাধ্যতামূলক)।')
        if (due.isBefore(today())) return fail('লক্ষ্যের শেষ তারিখ অতীতের হতে পারে না।')
        int used = usedWeight(emp, due.year)
        if (used + weight > MAX_TOTAL_WEIGHT) {
            return fail("${due.year} সালের জন্য ${emp.name} এর লক্ষ্যের weight আগেই ${used}; আর সর্বোচ্চ ${MAX_TOTAL_WEIGHT - used} দেওয়া যাবে (মোট ${MAX_TOTAL_WEIGHT})।".toString())
        }
        g.save(failOnError: true, flush: true)
        [ok: true, message: "লক্ষ্য '${g.title}' যোগ হয়েছে।".toString(), goal: g]
    }

    Map updateProgress(Goal g, BigDecimal current) {
        if (!canEditGoalsOf(g.employee)) return fail('অগ্রগতি হালনাগাদের অনুমতি নেই।')
        if (g.status == 'CANCELLED') return fail('বাতিল লক্ষ্যের অগ্রগতি বদলানো যায় না।')
        if (current == null || current < 0) return fail('অগ্রগতির মান ০ বা তার বেশি দিন।')
        g.currentValue = current
        // লক্ষ্যমাত্রায় পৌঁছালে সম্পন্ন; কমিয়ে ফেললে আবার সচল
        g.status = current >= g.targetValue ? 'COMPLETED' : 'ACTIVE'
        g.save(failOnError: true, flush: true)
        [ok: true, message: g.status == 'COMPLETED' ? "'${g.title}' লক্ষ্য অর্জিত হয়েছে।".toString() : 'অগ্রগতি হালনাগাদ হয়েছে।']
    }

    Map cancelGoal(Goal g) {
        if (!canEditGoalsOf(g.employee)) return fail('লক্ষ্য বাতিলের অনুমতি নেই।')
        if (g.status == 'CANCELLED') return fail('লক্ষ্য আগেই বাতিল।')
        if (g.status == 'COMPLETED') return fail('অর্জিত লক্ষ্য বাতিল করা যায় না।')
        g.status = 'CANCELLED'
        g.save(failOnError: true, flush: true)
        [ok: true, message: 'লক্ষ্য বাতিল হয়েছে।']
    }

    /** চক্রের সময়ের মধ্যে শেষ-তারিখ পড়ে এমন লক্ষ্যের weighted অর্জন (%). লক্ষ্য না থাকলে null */
    BigDecimal goalScore(Employee emp, AppraisalCycle cycle) {
        List<Goal> goals = goalsFor(emp, cycle)
        int totalWeight = goals.sum { Goal g -> g.weight } as Integer ?: 0
        if (!goals || totalWeight == 0) return null
        BigDecimal sum = goals.sum { Goal g -> g.progressPercent() * g.weight } as BigDecimal
        (sum / totalWeight).setScale(0, RoundingMode.HALF_UP)
    }

    List<Goal> goalsFor(Employee emp, AppraisalCycle cycle) {
        Goal.findAllByEmployeeAndStatusNotEqualAndDueDateBetween(emp, 'CANCELLED', cycle.startDate, cycle.endDate, [sort: 'dueDate'])
    }

    // ================================================================== চক্র
    Map createCycle(String name, LocalDate start, LocalDate end, LocalDate due) {
        if (!name?.trim()) return fail('চক্রের নাম দিন (যেমন: 2026 বার্ষিক মূল্যায়ন)।')
        if (start == null || end == null || due == null) return fail('শুরু, শেষ ও চূড়ান্ত - তিনটি তারিখই দিন।')
        if (end.isBefore(start)) return fail('শেষ তারিখ শুরুর আগে হতে পারে না।')
        if (due.isBefore(end)) return fail('চূড়ান্ত তারিখ মূল্যায়নের সময়কাল শেষের আগে হতে পারে না।')
        if (AppraisalCycle.countByName(name.trim()) > 0) return fail('এই নামে চক্র আগেই আছে।')
        AppraisalCycle c = new AppraisalCycle(name: name.trim().take(100), startDate: start, endDate: end, dueDate: due, createdBy: actor())
        AppraisalCycle clash = AppraisalCycle.findAll('from AppraisalCycle c where c.startDate <= :e and c.endDate >= :s', [s: start, e: end])[0]
        if (clash) return fail("সময়কাল '${clash.name}' চক্রের সাথে মিলে যাচ্ছে।".toString())
        c.save(failOnError: true, flush: true)
        [ok: true, message: "চক্র '${c.name}' তৈরি হয়েছে (DRAFT)।".toString(), cycle: c]
    }

    Map openCycle(AppraisalCycle c) {
        if (c.status != 'DRAFT') return fail('শুধু DRAFT চক্র চালু করা যায়।')
        int n = createMissingAppraisals(c)
        if (n == 0) return fail('এই সময়কালের জন্য কোনো যোগ্য কর্মী নেই।')
        c.status = 'OPEN'
        c.save(failOnError: true, flush: true)
        [ok: true, message: "চক্র চালু হয়েছে, ${n} জনের মূল্যায়ন তৈরি হয়েছে।".toString()]
    }

    /** চক্র চলাকালে যোগ দেওয়া কর্মীদের জন্য বাকি মূল্যায়ন তৈরি */
    Map syncCycle(AppraisalCycle c) {
        if (c.status != 'OPEN') return fail('শুধু চালু (OPEN) চক্রে কর্মী যোগ করা যায়।')
        int n = createMissingAppraisals(c)
        [ok: true, message: n ? "${n} জন নতুন কর্মীর মূল্যায়ন যোগ হয়েছে।".toString() : 'নতুন যোগ করার মতো কর্মী নেই।']
    }

    private int createMissingAppraisals(AppraisalCycle c) {
        int n = 0
        Employee.findAll("from Employee e where e.status <> 'INACTIVE' and e.joinDate <= :end order by e.name", [end: c.endDate]).each { Employee e ->
            if (Appraisal.countByCycleAndEmployee(c, e) == 0) {
                new Appraisal(cycle: c, employee: e, reviewer: e.reportingManager).save(failOnError: true)
                n++
            }
        }
        n
    }

    Map closeCycle(AppraisalCycle c) {
        if (c.status != 'OPEN') return fail('শুধু চালু চক্র বন্ধ করা যায়।')
        int pending = Appraisal.countByCycleAndStatusNotEqual(c, 'COMPLETED')
        c.status = 'CLOSED'
        c.save(failOnError: true, flush: true)
        [ok: true, message: pending ? "চক্র বন্ধ হয়েছে। ${pending}টি মূল্যায়ন অসম্পূর্ণ থেকে গেল।".toString() : 'চক্র বন্ধ হয়েছে, সব মূল্যায়ন সম্পন্ন।']
    }

    Map deleteCycle(AppraisalCycle c) {
        if (c.status != 'DRAFT') return fail('চালু হয়ে যাওয়া চক্র মোছা যায় না।')
        c.delete(flush: true)
        [ok: true, message: 'চক্র মুছে ফেলা হয়েছে।']
    }

    // ================================================================== মূল্যায়ন
    boolean canSeeAppraisal(Appraisal a) {
        accessService.isHrAdmin() || accessService.currentEmployee()?.id == a.employee.id || accessService.canManage(a.employee)
    }

    boolean isOwn(Appraisal a) { accessService.currentEmployee()?.id == a.employee.id }

    /** ম্যানেজার-মূল্যায়ন এখন করা যাবে কি? (নিজের নয়; সময়মতো নিজের মূল্যায়ন জমা পড়ার পর, বা চূড়ান্ত তারিখ পেরোলে) */
    boolean canReview(Appraisal a) {
        if (a.cycle.status != 'OPEN' || a.status == 'COMPLETED') return false
        if (!accessService.canManage(a.employee)) return false
        a.status == 'PENDING_MANAGER' || today().isAfter(a.cycle.dueDate)
    }

    Map submitSelf(Appraisal a, Integer rating, String comment) {
        if (!isOwn(a)) return fail('শুধু কর্মী নিজেই নিজের মূল্যায়ন জমা দিতে পারবেন।')
        if (a.cycle.status != 'OPEN') return fail('এই চক্র এখন চালু নেই।')
        if (a.status != 'PENDING_SELF') return fail('নিজের মূল্যায়ন আগেই জমা হয়েছে।')
        if (rating == null || rating < 1 || rating > 5) return fail('রেটিং ১ থেকে ৫ এর মধ্যে বেছে নিন।')
        if (!comment?.trim()) return fail('মন্তব্য লিখুন (কী কী অর্জন করেছেন)।')
        a.selfRating = rating
        a.selfComment = comment.trim().take(1000)
        a.selfSubmittedAt = LocalDateTime.now(AttendanceService.ZONE)
        a.status = 'PENDING_MANAGER'
        a.save(failOnError: true, flush: true)
        [ok: true, message: 'আপনার মূল্যায়ন ম্যানেজারের কাছে গেছে।']
    }

    Map submitManager(Appraisal a, Integer rating, String comment) {
        if (a.cycle.status != 'OPEN') return fail('এই চক্র এখন চালু নেই।')
        if (a.status == 'COMPLETED') return fail('মূল্যায়ন আগেই সম্পন্ন।')
        if (isOwn(a)) return fail('নিজের মূল্যায়ন নিজে করা যায় না।')
        if (!accessService.canManage(a.employee)) return fail('এই কর্মীর মূল্যায়ন করার অনুমতি আপনার নেই।')
        if (a.status == 'PENDING_SELF' && !today().isAfter(a.cycle.dueDate)) return fail('কর্মী নিজের মূল্যায়ন জমা না দেওয়া পর্যন্ত অপেক্ষা করুন (চূড়ান্ত তারিখ পেরোলে করা যাবে)।')
        if (rating == null || rating < 1 || rating > 5) return fail('রেটিং ১ থেকে ৫ এর মধ্যে বেছে নিন।')
        if ((rating <= 2 || rating == 5) && !comment?.trim()) return fail('খুব কম (১-২) বা সর্বোচ্চ (৫) রেটিংয়ের কারণ মন্তব্যে লিখুন।')
        a.managerRating = rating
        a.managerComment = comment?.trim()?.take(1000)
        a.goalScore = goalScore(a.employee, a.cycle)
        a.managerSubmittedAt = LocalDateTime.now(AttendanceService.ZONE)
        a.reviewedBy = actor()
        a.status = 'COMPLETED'
        a.save(failOnError: true, flush: true)
        [ok: true, message: "${a.employee.name} এর মূল্যায়ন সম্পন্ন হয়েছে।".toString()]
    }

    /** এই ব্যবহারকারী যেসব মূল্যায়ন দেখতে পারে (চক্র অনুযায়ী) */
    List<Appraisal> visibleAppraisals(AppraisalCycle cycle) {
        if (cycle == null) return []
        Set<Long> ids = visibleGoalOwnerIds()
        Appraisal.createCriteria().list {
            eq('cycle', cycle)
            if (ids != null) inList('employee.id', ids ?: [-1L])
            order('id', 'asc')
        } as List<Appraisal>
    }

    /** আমাকে যেসব মূল্যায়ন এখন করতে হবে (ম্যানেজার-ধাপ) */
    int pendingReviewCount() {
        List<Appraisal> open = Appraisal.findAll("from Appraisal a where a.cycle.status = 'OPEN' and a.status = 'PENDING_MANAGER'")
        open.count { Appraisal a -> canReview(a) }
    }
}

package hrms

import grails.gorm.transactions.Transactional
import org.springframework.web.multipart.MultipartFile

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Day 8: খরচের দাবি (reimbursement) ও ভ্রমণের অনুরোধ।
 *
 * খরচ: PENDING_MANAGER -> (ম্যানেজার) PENDING_ACCOUNTS -> (হিসাব বিভাগ/Admin) APPROVED -> (পরিশোধ) PAID।
 *   ম্যানেজার না থাকলে প্রথম ধাপ বাদ। নিজের দাবি নিজে অনুমোদন/পরিশোধ করা যায় না। ৫০০ ৳ এর বেশি হলে রসিদ লাগবে।
 * ভ্রমণ: PENDING -> APPROVED / REJECTED (ম্যানেজার বা HR/Admin)। অনুমোদিত ছুটির সাথে বা অন্য ভ্রমণের সাথে তারিখ মিলবে না।
 */
@Transactional
class ExpenseService {

    static final int MAX_TRAVEL_DAYS = 60

    def accessService

    private static Map fail(String m) { [ok: false, message: m] }

    private static LocalDate today() { LocalDate.now(AttendanceService.ZONE) }

    private static LocalDateTime now() { LocalDateTime.now(AttendanceService.ZONE) }

    private String actor() { accessService.currentUser()?.username }

    private boolean isAccounts() { AccessService.has('ROLE_ADMIN,ROLE_ACCOUNTANT') }

    private boolean isOwn(Employee e) { accessService.currentEmployee()?.id == e?.id }

    // ================================================================== দৃশ্যমানতা
    /** দাবি/অনুরোধ দেখার অধিকার: নিজের, অধীনের (ম্যানেজার), এবং HR/Admin/হিসাব বিভাগ সবার */
    boolean canSee(Employee owner) {
        owner != null && (isOwn(owner) || AccessService.has('ROLE_ADMIN,ROLE_HR,ROLE_ACCOUNTANT') || accessService.canManage(owner))
    }

    Set<Long> visibleOwnerIds() {
        if (AccessService.has('ROLE_ADMIN,ROLE_HR,ROLE_ACCOUNTANT')) return null
        Long me = accessService.currentEmployee()?.id
        Set<Long> ids = [] as Set<Long>
        if (me != null) ids << me
        ids.addAll(accessService.approvableIds() ?: [])
        ids
    }

    // ================================================================== খরচের দাবি
    Map submitClaim(ExpenseClaim c, MultipartFile file) {
        Employee emp = c.employee
        if (emp == null) return fail('আপনার সাথে কোনো কর্মী প্রোফাইল যুক্ত নেই।')
        if (!isOwn(emp)) return fail('শুধু নিজের নামে দাবি করা যায়।')
        if (emp.status == 'INACTIVE') return fail('নিষ্ক্রিয় কর্মী দাবি করতে পারেন না।')
        c.title = c.title?.trim()
        c.description = c.description?.trim() ?: null
        if (!c.validate(['category', 'title', 'expenseDate', 'amount', 'description'])) {
            return fail('ফর্মের তথ্য ঠিক করুন (বিবরণ, তারিখ, ১ থেকে ৫,০০,০০০ ৳ এর মধ্যে পরিমাণ বাধ্যতামূলক)।')
        }
        if (c.expenseDate.isAfter(today())) return fail('খরচের তারিখ ভবিষ্যতের হতে পারে না।')
        if (c.expenseDate.isBefore(today().minusDays(ExpenseClaim.MAX_AGE_DAYS))) return fail("${ExpenseClaim.MAX_AGE_DAYS} দিনের পুরনো খরচের দাবি নেওয়া হয় না।".toString())
        if (ExpenseClaim.countByEmployeeAndCategoryAndExpenseDateAndAmountAndStatusInList(emp, c.category, c.expenseDate, c.amount, ExpenseClaim.LIVE_STATUSES) > 0) {
            return fail('একই ধরন, তারিখ ও পরিমাণের দাবি আগেই জমা আছে।')
        }
        if (c.travel != null) {
            if (c.travel.employee.id != emp.id) return fail('ভ্রমণ অনুরোধটি আপনার নয়।')
            if (c.travel.status != 'APPROVED') return fail('শুধু অনুমোদিত ভ্রমণের সাথে খরচ যুক্ত করা যায়।')
        }

        boolean hasFile = file != null && !file.empty
        if (c.amount > ExpenseClaim.RECEIPT_REQUIRED_ABOVE && !hasFile) {
            return fail("${ExpenseClaim.RECEIPT_REQUIRED_ABOVE.intValue()} ৳ এর বেশি খরচে রসিদ আপলোড বাধ্যতামূলক।".toString())
        }
        if (hasFile) {
            String fileName = new File(file.originalFilename ?: 'receipt').name
            String ext = fileName.contains('.') ? fileName.toLowerCase().tokenize('.').last() : ''
            if (!ExpenseClaim.RECEIPT_EXTENSIONS.contains(ext)) return fail("রসিদ শুধু ${ExpenseClaim.RECEIPT_EXTENSIONS.join(', ')} হতে পারে।".toString())
            if (file.size > ExpenseClaim.RECEIPT_MAX_BYTES) return fail('রসিদের সাইজ সর্বোচ্চ ২ MB।')
            c.receiptData = file.bytes
            c.receiptName = fileName.take(255)
            c.receiptType = file.contentType
            c.receiptSize = file.size
            c.hasReceipt = true
        }
        c.status = emp.reportingManager != null ? 'PENDING_MANAGER' : 'PENDING_ACCOUNTS'
        c.save(failOnError: true, flush: true)
        [ok: true, message: 'খরচের দাবি জমা হয়েছে।', claim: c]
    }

    /** ম্যানেজার-ধাপ: approve=true হলে হিসাব বিভাগে যায় */
    Map managerDecide(ExpenseClaim c, boolean approve, String note) {
        if (c.status != 'PENDING_MANAGER') return fail('দাবিটি এখন ম্যানেজারের ধাপে নেই।')
        if (!accessService.canManage(c.employee)) return fail('এই দাবি অনুমোদনের অনুমতি আপনার নেই (নিজের দাবি নিজে অনুমোদন করা যায় না)।')
        if (!approve && !note?.trim()) return fail('প্রত্যাখ্যানের কারণ লিখুন।')
        c.managerBy = actor()
        c.managerAt = now()
        c.managerNote = note?.trim()?.take(255) ?: null
        c.status = approve ? 'PENDING_ACCOUNTS' : 'REJECTED'
        c.save(failOnError: true, flush: true)
        [ok: true, message: approve ? 'ম্যানেজার অনুমোদন দিয়েছেন, এখন হিসাব বিভাগের ধাপ।' : 'দাবি প্রত্যাখ্যাত হয়েছে।']
    }

    Map accountsDecide(ExpenseClaim c, boolean approve, String note) {
        if (c.status != 'PENDING_ACCOUNTS') return fail('দাবিটি এখন হিসাব বিভাগের ধাপে নেই।')
        if (!isAccounts()) return fail('এই ধাপ হিসাব বিভাগ/Admin এর।')
        if (isOwn(c.employee)) return fail('নিজের দাবি নিজে অনুমোদন করা যায় না।')
        if (!approve && !note?.trim()) return fail('প্রত্যাখ্যানের কারণ লিখুন।')
        c.accountsBy = actor()
        c.accountsAt = now()
        c.accountsNote = note?.trim()?.take(255) ?: null
        c.status = approve ? 'APPROVED' : 'REJECTED'
        c.save(failOnError: true, flush: true)
        [ok: true, message: approve ? 'দাবি অনুমোদিত, এখন পরিশোধ করুন।' : 'দাবি প্রত্যাখ্যাত হয়েছে।']
    }

    Map markPaid(ExpenseClaim c, LocalDate date, String ref) {
        if (c.status != 'APPROVED') return fail('শুধু অনুমোদিত দাবি পরিশোধ করা যায়।')
        if (!isAccounts()) return fail('পরিশোধ হিসাব বিভাগ/Admin করবে।')
        if (isOwn(c.employee)) return fail('নিজের দাবি নিজে পরিশোধ করা যায় না।')
        if (date == null) return fail('পরিশোধের তারিখ দিন।')
        if (date.isAfter(today())) return fail('পরিশোধের তারিখ ভবিষ্যতের হতে পারে না।')
        if (date.isBefore(c.expenseDate)) return fail('পরিশোধের তারিখ খরচের তারিখের আগে হতে পারে না।')
        if (!ref?.trim()) return fail('পেমেন্ট রেফারেন্স (চেক/ট্রানজ্যাকশন নম্বর) দিন।')
        c.paidBy = actor()
        c.paidDate = date
        c.paymentRef = ref.trim().take(80)
        c.status = 'PAID'
        c.save(failOnError: true, flush: true)
        [ok: true, message: 'দাবি পরিশোধিত হিসেবে রেকর্ড হয়েছে।']
    }

    Map cancelClaim(ExpenseClaim c) {
        if (!isOwn(c.employee) && !accessService.isHrAdmin()) return fail('দাবি প্রত্যাহারের অনুমতি নেই।')
        if (!(c.status in ['PENDING_MANAGER', 'PENDING_ACCOUNTS'])) return fail('শুধু অপেক্ষমাণ দাবি প্রত্যাহার করা যায়।')
        c.status = 'CANCELLED'
        c.save(failOnError: true, flush: true)
        [ok: true, message: 'দাবি প্রত্যাহার করা হয়েছে।']
    }

    /** আমার অনুমোদনের অপেক্ষায় থাকা দাবি */
    List<ExpenseClaim> pendingClaimsForMe() {
        List<ExpenseClaim> list = []
        ExpenseClaim.findAllByStatus('PENDING_MANAGER', [sort: 'id']).each { ExpenseClaim c -> if (accessService.canManage(c.employee)) list << c }
        if (isAccounts()) {
            ExpenseClaim.findAllByStatus('PENDING_ACCOUNTS', [sort: 'id']).each { ExpenseClaim c -> if (!isOwn(c.employee)) list << c }
        }
        list
    }

    /** পরিশোধের অপেক্ষায় (APPROVED) - হিসাব বিভাগের জন্য */
    List<ExpenseClaim> payableClaims() {
        isAccounts() ? ExpenseClaim.findAllByStatus('APPROVED', [sort: 'id']).findAll { ExpenseClaim c -> !isOwn(c.employee) } : []
    }

    // ================================================================== ভ্রমণ
    Map submitTravel(TravelRequest t) {
        Employee emp = t.employee
        if (emp == null) return fail('আপনার সাথে কোনো কর্মী প্রোফাইল যুক্ত নেই।')
        if (!isOwn(emp)) return fail('শুধু নিজের নামে অনুরোধ করা যায়।')
        if (emp.status == 'INACTIVE') return fail('নিষ্ক্রিয় কর্মী ভ্রমণের অনুরোধ করতে পারেন না।')
        t.destination = t.destination?.trim()
        t.purpose = t.purpose?.trim()
        if (!t.validate()) return fail('ফর্মের তথ্য ঠিক করুন (গন্তব্য, উদ্দেশ্য, তারিখ বাধ্যতামূলক; ফেরার তারিখ যাওয়ার আগে হবে না)।')
        if (t.fromDate.isBefore(today())) return fail('যাত্রার তারিখ অতীতের হতে পারে না।')
        long days = t.toDate.toEpochDay() - t.fromDate.toEpochDay() + 1
        if (days > MAX_TRAVEL_DAYS) return fail("একটি অনুরোধে সর্বোচ্চ ${MAX_TRAVEL_DAYS} দিন।".toString())
        int clash = TravelRequest.countByEmployeeAndStatusInListAndFromDateLessThanEqualsAndToDateGreaterThanEquals(emp, ['PENDING', 'APPROVED'], t.toDate, t.fromDate)
        if (clash > 0) return fail('এই তারিখে আপনার আরেকটি ভ্রমণ অনুরোধ (অপেক্ষমাণ/অনুমোদিত) আছে।')
        int leaveClash = LeaveRequest.countByEmployeeAndStatusAndFromDateLessThanEqualsAndToDateGreaterThanEquals(emp, 'APPROVED', t.toDate, t.fromDate)
        if (leaveClash > 0) return fail('এই তারিখে আপনার অনুমোদিত ছুটি আছে।')
        t.status = 'PENDING'
        t.save(failOnError: true, flush: true)
        [ok: true, message: 'ভ্রমণের অনুরোধ জমা হয়েছে।', travel: t]
    }

    Map decideTravel(TravelRequest t, boolean approve, String note) {
        if (t.status != 'PENDING') return fail('অনুরোধটির সিদ্ধান্ত আগেই হয়ে গেছে।')
        if (!accessService.canManage(t.employee)) return fail('এই অনুরোধে সিদ্ধান্তের অনুমতি আপনার নেই (নিজের অনুরোধ নিজে অনুমোদন করা যায় না)।')
        if (!approve && !note?.trim()) return fail('প্রত্যাখ্যানের কারণ লিখুন।')
        if (approve && t.fromDate.isBefore(today())) return fail('যাত্রার তারিখ পেরিয়ে গেছে, অনুমোদন করা যায় না।')
        t.status = approve ? 'APPROVED' : 'REJECTED'
        t.decidedBy = actor()
        t.decidedAt = now()
        t.decisionNote = note?.trim()?.take(255) ?: null
        t.save(failOnError: true, flush: true)
        [ok: true, message: approve ? 'ভ্রমণ অনুমোদিত হয়েছে।' : 'ভ্রমণ প্রত্যাখ্যাত হয়েছে।']
    }

    Map cancelTravel(TravelRequest t) {
        if (!isOwn(t.employee) && !accessService.isHrAdmin()) return fail('অনুরোধ প্রত্যাহারের অনুমতি নেই।')
        if (t.status == 'PENDING') {
            // ঠিক আছে
        } else if (t.status == 'APPROVED') {
            if (!t.fromDate.isAfter(today())) return fail('যাত্রা শুরু হয়ে গেছে, বাতিল করা যায় না।')
            if (ExpenseClaim.countByTravelAndStatusInList(t, ExpenseClaim.LIVE_STATUSES) > 0) return fail('এই ভ্রমণের সাথে খরচের দাবি যুক্ত আছে, আগে সেগুলো প্রত্যাহার করুন।')
        } else {
            return fail('এই অবস্থায় প্রত্যাহার করা যায় না।')
        }
        t.status = 'CANCELLED'
        t.save(failOnError: true, flush: true)
        [ok: true, message: 'ভ্রমণের অনুরোধ প্রত্যাহার হয়েছে।']
    }

    List<TravelRequest> pendingTravelForMe() {
        TravelRequest.findAllByStatus('PENDING', [sort: 'fromDate']).findAll { TravelRequest t -> accessService.canManage(t.employee) }
    }

    /** ড্যাশবোর্ডের জন্য: আমার অপেক্ষমাণ খরচ + ভ্রমণ */
    int pendingCountForMe() {
        pendingClaimsForMe().size() + pendingTravelForMe().size()
    }
}

package hrms

import grails.gorm.transactions.Transactional

import java.time.LocalDate

/**
 * Day 8: সম্পদ রেজিস্টার ও সুবিধা (benefits)।
 * সম্পদ: AVAILABLE -> ASSIGNED (কর্মীকে বরাদ্দ) -> ফেরত -> AVAILABLE/MAINTENANCE; AVAILABLE/MAINTENANCE -> RETIRED।
 *   বরাদ্দ থাকা সম্পদ retire বা মোছা যায় না। একই সিরিয়াল একসাথে দু'জনের কাছে থাকতে পারে না (অনবোর্ডিং এ হাতে দেওয়া সম্পদসহ)।
 * সুবিধা: প্ল্যানের minServiceMonths পূরণ হলে HR/Admin কর্মীকে নথিভুক্ত করে; একই প্ল্যানে একসাথে একটাই ACTIVE।
 */
@Transactional
class AssetService {

    def accessService

    private static Map fail(String m) { [ok: false, message: m] }

    private static LocalDate today() { LocalDate.now(AttendanceService.ZONE) }

    private String actor() { accessService.currentUser()?.username }

    // ================================================================== সম্পদ রেজিস্টার
    private String nextTag() {
        long n = Asset.count() + 1
        String tag = 'AST-' + String.format('%04d', n)
        while (Asset.countByAssetTag(tag) > 0) {
            n++
            tag = 'AST-' + String.format('%04d', n)
        }
        tag
    }

    Map saveAsset(Asset a) {
        boolean isNew = a.id == null
        a.name = a.name?.trim()
        a.serialNo = a.serialNo?.trim() ?: null
        a.remarks = a.remarks?.trim() ?: null
        if (isNew) {
            a.assetTag = a.assetTag?.trim()?.toUpperCase() ?: nextTag()
            a.status = 'AVAILABLE'
        }
        if (!a.validate()) {
            return fail(a.errors.hasFieldErrors('assetTag') ? 'এই ট্যাগ আগেই আছে।' : a.errors.hasFieldErrors('serialNo') ? 'এই সিরিয়াল আগেই আছে।' : 'ফর্মের তথ্য ঠিক করুন (নাম বাধ্যতামূলক, খরচ ০ বা বেশি)।')
        }
        if (a.purchaseDate != null && a.purchaseDate.isAfter(today())) return fail('ক্রয়ের তারিখ ভবিষ্যতের হতে পারে না।')
        a.save(failOnError: true, flush: true)
        [ok: true, message: "সম্পদ ${a.assetTag} সংরক্ষিত হয়েছে।".toString()]
    }

    AssetAllocation currentAllocation(Asset a) {
        AssetAllocation.findByAssetAndStatus(a, 'ISSUED')
    }

    Map issue(Asset a, Employee emp, LocalDate date, String remarks) {
        if (emp == null) return fail('কর্মী নির্বাচন করুন।')
        if (a.status != 'AVAILABLE') return fail('শুধু AVAILABLE সম্পদ বরাদ্দ করা যায়।')
        if (emp.status == 'INACTIVE') return fail('নিষ্ক্রিয় কর্মীকে সম্পদ দেওয়া যায় না।')
        LocalDate d = date ?: today()
        if (d.isAfter(today())) return fail('বরাদ্দের তারিখ ভবিষ্যতের হতে পারে না।')
        if (a.serialNo) {
            AssetAllocation other = AssetAllocation.findBySerialNoAndStatus(a.serialNo, 'ISSUED')
            if (other != null) return fail("এই সিরিয়াল এখন ${other.employee.name} এর কাছে আছে।".toString())
        }
        new AssetAllocation(employee: emp, asset: a, assetType: a.assetType, assetName: a.name, serialNo: a.serialNo,
                issuedDate: d, status: 'ISSUED', remarks: remarks?.trim()?.take(255) ?: null, issuedBy: actor()).save(failOnError: true)
        a.status = 'ASSIGNED'
        a.save(failOnError: true, flush: true)
        [ok: true, message: "${a.assetTag} ${emp.name} কে বরাদ্দ হয়েছে।".toString()]
    }

    /** ফেরত নিয়ে সম্পদ AVAILABLE (বা মেরামতের জন্য MAINTENANCE) করা */
    Map receive(AssetAllocation al, LocalDate date, String remarks, boolean needsMaintenance) {
        if (al.status != 'ISSUED') return fail('সম্পদটি আগেই ফেরত নেওয়া হয়েছে।')
        LocalDate d = date ?: today()
        if (d.isBefore(al.issuedDate)) return fail('ফেরতের তারিখ বরাদ্দের আগে হতে পারে না।')
        if (d.isAfter(today())) return fail('ফেরতের তারিখ ভবিষ্যতের হতে পারে না।')
        al.status = 'RETURNED'
        al.returnedDate = d
        if (remarks?.trim()) al.remarks = remarks.trim().take(255)
        al.save(failOnError: true, flush: true)
        if (al.asset != null) {
            al.asset.status = needsMaintenance ? 'MAINTENANCE' : 'AVAILABLE'
            al.asset.save(failOnError: true, flush: true)
        }
        [ok: true, message: "'${al.assetName}' ফেরত নেওয়া হয়েছে।".toString()]
    }

    Map setStatus(Asset a, String target) {
        if (a.status == 'ASSIGNED') return fail('কর্মীর কাছে থাকা সম্পদের অবস্থা বদলানো যায় না - আগে ফেরত নিন।')
        if (a.status == 'RETIRED') return fail('বাতিল (RETIRED) সম্পদ আর বদলানো যায় না।')
        boolean ok = (a.status == 'AVAILABLE' && target in ['MAINTENANCE', 'RETIRED']) || (a.status == 'MAINTENANCE' && target in ['AVAILABLE', 'RETIRED'])
        if (!ok) return fail('এই পরিবর্তন সম্ভব নয়।')
        a.status = target
        a.save(failOnError: true, flush: true)
        [ok: true, message: "${a.assetTag} এখন ${target}।".toString()]
    }

    Map deleteAsset(Asset a) {
        if (AssetAllocation.countByAsset(a) > 0) return fail('বরাদ্দের ইতিহাস আছে, তাই মোছা যায় না - চাইলে RETIRED করুন।')
        String tag = a.assetTag
        a.delete(flush: true)
        [ok: true, message: "সম্পদ ${tag} মুছে ফেলা হয়েছে।".toString()]
    }

    // ================================================================== সুবিধা
    int serviceMonths(Employee e, LocalDate on) {
        (int) java.time.temporal.ChronoUnit.MONTHS.between(e.joinDate, on)
    }

    Map enrollBenefit(BenefitPlan plan, Employee emp, LocalDate start, String remarks) {
        if (plan == null || emp == null) return fail('প্ল্যান ও কর্মী নির্বাচন করুন।')
        if (!plan.active) return fail('প্ল্যানটি এখন সক্রিয় নয়।')
        if (emp.status == 'INACTIVE') return fail('নিষ্ক্রিয় কর্মীকে সুবিধা দেওয়া যায় না।')
        LocalDate d = start ?: today()
        if (d.isBefore(emp.joinDate)) return fail('শুরুর তারিখ যোগদানের আগে হতে পারে না।')
        if (serviceMonths(emp, d) < plan.minServiceMonths) return fail("এই প্ল্যানের জন্য কমপক্ষে ${plan.minServiceMonths} মাস চাকরির বয়স লাগে।".toString())
        if (BenefitEnrollment.countByPlanAndEmployeeAndStatus(plan, emp, 'ACTIVE') > 0) return fail('কর্মী এই সুবিধা আগেই পাচ্ছেন।')
        new BenefitEnrollment(plan: plan, employee: emp, startDate: d, remarks: remarks?.trim()?.take(255) ?: null, enrolledBy: actor()).save(failOnError: true, flush: true)
        [ok: true, message: "${emp.name} কে '${plan.name}' সুবিধায় যুক্ত করা হয়েছে।".toString()]
    }

    Map endBenefit(BenefitEnrollment b, LocalDate end) {
        if (b.status != 'ACTIVE') return fail('সুবিধাটি আগেই শেষ হয়েছে।')
        LocalDate d = end ?: today()
        if (d.isBefore(b.startDate)) return fail('শেষ তারিখ শুরুর আগে হতে পারে না।')
        b.endDate = d
        b.status = 'ENDED'
        b.save(failOnError: true, flush: true)
        [ok: true, message: 'সুবিধা শেষ করা হয়েছে।']
    }
}

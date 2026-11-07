package hrms

import grails.plugin.springsecurity.annotation.Secured

/**
 * মাস্টার ডেটার সাধারণ CRUD: শাখা, গ্রেড, কস্ট সেন্টার, শিফট, ছুটির নীতি, বেতনের খাত।
 * URL: /master/branch, /master/grade, /master/costcenter, /master/shift (UrlMappings এ আছে)।
 * Day 3 এ shift/holiday যোগ করতে শুধু TYPES এ নতুন লাইন দিলেই হবে।
 */
@Secured(['ROLE_ADMIN', 'ROLE_HR'])
class MasterController {

    static allowedMethods = [save: 'POST', update: 'POST', delete: 'POST']

    static final Map<String, Map> TYPES = [
        branch    : [clazz: Branch, title: 'শাখা', prop: 'branch', order: 'name', fields: [
                [name: 'name', label: 'শাখার নাম', required: true],
                [name: 'address', label: 'ঠিকানা'],
                [name: 'phone', label: 'ফোন']]],
        grade     : [clazz: Grade, title: 'গ্রেড', prop: 'grade', order: 'gradeLevel', fields: [
                [name: 'name', label: 'গ্রেডের নাম', required: true],
                [name: 'gradeLevel', label: 'লেভেল (সংখ্যা)', required: true, number: true],
                [name: 'minSalary', label: 'সর্বনিম্ন বেতন', number: true],
                [name: 'maxSalary', label: 'সর্বোচ্চ বেতন', number: true]]],
        costcenter: [clazz: CostCenter, title: 'কস্ট সেন্টার', prop: 'costCenter', order: 'code', fields: [
                [name: 'code', label: 'কোড', required: true],
                [name: 'name', label: 'নাম', required: true]]],
        shift     : [clazz: Shift, title: 'শিফট', prop: 'shift', order: 'startTime', fields: [
                [name: 'name', label: 'শিফটের নাম', required: true],
                [name: 'startTime', label: 'শুরু', required: true, time: true],
                [name: 'endTime', label: 'শেষ', required: true, time: true],
                [name: 'graceMinutes', label: 'গ্রেস টাইম (মিনিট)', required: true, number: true],
                [name: 'earlyOutMinutes', label: 'আর্লি-আউট সীমা (মিনিট)', required: true, number: true]],
                usedBy: { Shift s -> Attendance.countByShift(s) }, usedLabel: 'উপস্থিতির রেকর্ডে'],
        leavetype : [clazz: LeaveType, title: 'ছুটির নীতি', prop: 'leaveType', order: 'id', fields: [
                [name: 'code', label: 'কোড (যেমন AL)', required: true],
                [name: 'name', label: 'ছুটির নাম', required: true],
                [name: 'annualDays', label: 'বার্ষিক কোটা (দিন, 0 = সীমাহীন)', required: true, number: true],
                [name: 'accrual', label: 'Accrual', required: true, select: LeaveType.ACCRUALS],
                [name: 'paid', label: 'বেতনসহ ছুটি', checkbox: true],
                [name: 'carryForward', label: 'পরের বছরে carry forward', checkbox: true],
                [name: 'maxCarryForward', label: 'সর্বোচ্চ carry forward (দিন)', number: true],
                [name: 'approvalLevels', label: 'অনুমোদনের ধাপ (1 = ম্যানেজার, 2 = ম্যানেজার + HR)', required: true, select: [1, 2]],
                [name: 'gender', label: 'শুধু কার জন্য', select: LeaveType.GENDERS, blankLabel: 'সবার জন্য'],
                [name: 'active', label: 'সক্রিয়', checkbox: true]],
                usedBy: { LeaveType t -> LeaveRequest.countByLeaveType(t) + LeaveBalance.countByLeaveType(t) }, usedLabel: 'ছুটির আবেদন/ব্যালেন্সে'],
        salaryhead: [clazz: SalaryHead, title: 'ভাতা/কর্তনের খাত', prop: 'salaryHead', order: 'headType', fields: [
                [name: 'code', label: 'কোড (যেমন MOB)', required: true],
                [name: 'name', label: 'খাতের নাম', required: true],
                [name: 'headType', label: 'ধরন (EARNING = ভাতা, DEDUCTION = কর্তন)', required: true, select: SalaryHead.TYPES],
                [name: 'calcType', label: 'হিসাবের ধরন (FIXED = টাকা, PERCENT_BASIC = বেসিকের %)', required: true, select: SalaryHead.CALC_TYPES],
                [name: 'defaultValue', label: 'ডিফল্ট মান (টাকা বা শতাংশ)', required: true, number: true],
                [name: 'active', label: 'সক্রিয়', checkbox: true]],
                usedBy: { SalaryHead h -> EmployeeSalaryHead.countByHead(h) }, usedLabel: 'কর্মীর বেতন কাঠামোতে'],
        onboardingtemplate: [clazz: OnboardingTemplate, title: 'অনবোর্ডিং কাজ', prop: 'onboardingTemplate', order: 'sortOrder', fields: [
                [name: 'title', label: 'কাজের নাম', required: true],
                [name: 'category', label: 'ধরন', required: true, select: OnboardingTemplate.CATEGORIES],
                [name: 'ownerRole', label: 'দায়িত্বপ্রাপ্ত বিভাগ', required: true, select: OnboardingTemplate.OWNERS],
                [name: 'dueAfterDays', label: 'যোগদানের কত দিনের মধ্যে', required: true, number: true],
                [name: 'sortOrder', label: 'ক্রম (ছোট সংখ্যা আগে)', required: true, number: true],
                [name: 'active', label: 'নতুন কর্মীর চেকলিস্টে যোগ হবে', checkbox: true]],
                usedBy: { OnboardingTemplate t -> 0 }, usedLabel: ''],
        benefitplan: [clazz: BenefitPlan, title: 'সুবিধার প্ল্যান', prop: 'benefitPlan', order: 'name', fields: [
                [name: 'name', label: 'প্ল্যানের নাম', required: true],
                [name: 'benefitType', label: 'ধরন', required: true, select: BenefitPlan.TYPES],
                [name: 'provider', label: 'সরবরাহকারী (যেমন বীমা কোম্পানি)'],
                [name: 'description', label: 'বিবরণ'],
                [name: 'monthlyValue', label: 'কোম্পানির মাসিক খরচ (৳)', number: true],
                [name: 'minServiceMonths', label: 'যোগদানের কত মাস পর থেকে', required: true, number: true],
                [name: 'active', label: 'সক্রিয়', checkbox: true]],
                usedBy: { BenefitPlan p -> BenefitEnrollment.countByPlan(p) }, usedLabel: 'কর্মীর সুবিধার তালিকায়']
    ]

    def index() {
        Map cfg = typeConfig()
        if (!cfg) return
        [type: params.type, cfg: cfg, items: cfg.clazz.list(sort: cfg.order)]
    }

    def create() {
        Map cfg = typeConfig()
        if (!cfg) return
        render(view: 'form', model: [type: params.type, cfg: cfg, instance: cfg.clazz.newInstance()])
    }

    def save() {
        Map cfg = typeConfig()
        if (!cfg) return
        def obj = cfg.clazz.newInstance()
        bindData(obj, params, [include: cfg.fields.findAll { !it.checkbox }*.name])
        cfg.fields.findAll { it.checkbox }.each { f -> obj[f.name as String] = params[f.name as String] != null }
        if (!obj.save(flush: true)) {
            render(view: 'form', model: [type: params.type, cfg: cfg, instance: obj])
            return
        }
        flash.success = "${cfg.title} '${obj}' যোগ করা হয়েছে।".toString()
        redirect(action: 'index', params: [type: params.type])
    }

    def edit(Long id) {
        Map cfg = typeConfig()
        if (!cfg) return
        def obj = id ? cfg.clazz.get(id) : null
        if (!obj) { notFound(cfg); return }
        render(view: 'form', model: [type: params.type, cfg: cfg, instance: obj])
    }

    def update(Long id) {
        Map cfg = typeConfig()
        if (!cfg) return
        def obj = id ? cfg.clazz.get(id) : null
        if (!obj) { notFound(cfg); return }
        bindData(obj, params, [include: cfg.fields.findAll { !it.checkbox }*.name])
        cfg.fields.findAll { it.checkbox }.each { f -> obj[f.name as String] = params[f.name as String] != null }
        if (!obj.save(flush: true)) {
            render(view: 'form', model: [type: params.type, cfg: cfg, instance: obj])
            return
        }
        flash.success = "${cfg.title} '${obj}' হালনাগাদ হয়েছে।".toString()
        redirect(action: 'index', params: [type: params.type])
    }

    def delete(Long id) {
        Map cfg = typeConfig()
        if (!cfg) return
        def obj = id ? cfg.clazz.get(id) : null
        if (!obj) { notFound(cfg); return }

        // কোথাও ব্যবহৃত হলে মোছা যাবে না
        String prop = cfg.prop
        long used = cfg.usedBy ? (cfg.usedBy as Closure).call(obj) as long : Employee.createCriteria().count { eq(prop, obj) }
        if (used > 0) {
            String where = cfg.usedLabel ?: 'কর্মীর প্রোফাইলে'
            flash.error = "এই ${cfg.title} ${used}টি ${where} ব্যবহৃত, তাই মোছা যাবে না।".toString()
            redirect(action: 'index', params: [type: params.type])
            return
        }
        String label = obj.toString()
        obj.delete(flush: true)
        flash.success = "${cfg.title} '${label}' মুছে ফেলা হয়েছে।".toString()
        redirect(action: 'index', params: [type: params.type])
    }

    // ------------------------------------------------------------------ helpers
    /** অজানা type হলে 404 দিয়ে null ফেরত দেয় */
    private Map typeConfig() {
        Map cfg = TYPES[params.type as String]
        if (!cfg) response.sendError(404)
        cfg
    }

    private void notFound(Map cfg) {
        flash.error = "${cfg.title} পাওয়া যায়নি।".toString()
        redirect(action: 'index', params: [type: params.type])
    }
}
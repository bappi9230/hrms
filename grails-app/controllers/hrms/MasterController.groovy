package hrms

import grails.plugin.springsecurity.annotation.Secured

/**
 * মাস্টার ডেটার সাধারণ CRUD: শাখা, গ্রেড, কস্ট সেন্টার।
 * URL: /master/branch, /master/grade, /master/costcenter (UrlMappings এ আছে)।
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
                [name: 'name', label: 'নাম', required: true]]]
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
        bindData(obj, params, [include: cfg.fields*.name])
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
        bindData(obj, params, [include: cfg.fields*.name])
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

        // কোনো কর্মী এটি ব্যবহার করলে মোছা যাবে না
        String prop = cfg.prop
        long used = Employee.createCriteria().count { eq(prop, obj) }
        if (used > 0) {
            flash.error = "এই ${cfg.title} ${used} জন কর্মীর প্রোফাইলে ব্যবহৃত, তাই মোছা যাবে না।".toString()
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

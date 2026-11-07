package hrms

import grails.gorm.transactions.Transactional

/** Day 9: অডিট ট্রেইল। গুরুত্বপূর্ণ কাজের রেকর্ড রাখে (শুধু যোগ হয়) এবং HR/Admin এর জন্য খোঁজার সুবিধা দেয় */
@Transactional
class AuditService {

    def accessService

    /** অডিট লাইন যোগ করে; সংরক্ষণ ব্যর্থ হলে মূল কাজ আটকায় না */
    AuditLog record(String action, String entityType, Long entityId, String summary) {
        try {
            String actor = accessService.currentUser()?.username ?: 'system'
            AuditLog row = new AuditLog(actor: actor, action: action, entityType: entityType, entityId: entityId, summary: summary?.take(500))
            return row.save(flush: true)
        } catch (Exception ex) {
            log.warn("Audit record failed (${action}): ${ex.message}")
            return null
        }
    }

    /** ফিল্টার সহ পাতা-ভাগ করা তালিকা (totalCount আছে) */
    List<AuditLog> search(String actor, String act, Date from, Date to, int max, int offset) {
        AuditLog.createCriteria().list(max: max, offset: offset) {
            if (actor) eq('actor', actor)
            if (act) ilike('action', "%${act}%".toString())
            if (from) ge('dateCreated', from)
            if (to) le('dateCreated', to)
            order('id', 'desc')
        } as List<AuditLog>
    }
}

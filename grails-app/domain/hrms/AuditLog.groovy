package hrms

/** Day 9: অডিট ট্রেইল। কে, কখন, কী করল - শুধু যোগ হয়, বদলানো/মোছা হয় না */
class AuditLog {

    String actor                  // ব্যবহারকারীর username (বা system)
    String action                 // যেমন SEPARATION_SUBMITTED
    String entityType             // যেমন Separation
    Long entityId
    String summary
    Date dateCreated

    static constraints = {
        actor blank: false, maxSize: 50
        action blank: false, maxSize: 60
        entityType nullable: true, maxSize: 60
        entityId nullable: true
        summary nullable: true, maxSize: 500
    }

    static mapping = {
        table 'hrms_audit_log'
        action column: 'action_name'
        sort id: 'desc'
    }

    String toString() { "${actor}: ${action}".toString() }
}

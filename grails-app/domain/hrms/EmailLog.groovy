package hrms

/** Day 9: পাঠানো (বা বাদ পড়া/ব্যর্থ) ইমেইলের লগ। মেইল সার্ভার না থাকলে SKIPPED হিসেবে জমা থাকে */
class EmailLog {

    static final List<String> STATUSES = ['SENT', 'SKIPPED', 'FAILED']

    String toAddress
    String subject
    String body
    String status = 'SKIPPED'
    String error
    Date dateCreated

    static constraints = {
        toAddress blank: false, maxSize: 150
        subject blank: false, maxSize: 200
        body nullable: true, maxSize: 4000
        status inList: STATUSES
        error nullable: true, maxSize: 500
    }

    static mapping = {
        table 'hrms_email_log'
        body type: 'text'
    }

    String toString() { "${toAddress}: ${subject}".toString() }
}

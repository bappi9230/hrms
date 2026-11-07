package hrms

/** Day 9: ইন-অ্যাপ নোটিফিকেশন (টপবারের ঘণ্টা)। link অ্যাপের ভেতরের পথ হতে হবে, যেমন /grievance/show/1 */
class Notification {

    static final List<String> TYPES = ['INFO', 'WARNING', 'APPROVAL', 'SUCCESS']

    User user
    String title
    String message
    String link
    String type = 'INFO'
    Boolean seen = false
    Date dateCreated

    static constraints = {
        user nullable: false
        title blank: false, maxSize: 150
        message nullable: true, maxSize: 500
        link nullable: true, maxSize: 255
        type inList: TYPES
    }

    static mapping = {
        table 'hrms_notification'
        type column: 'notif_type'
        link column: 'link_url'
    }

    String toString() { title }
}

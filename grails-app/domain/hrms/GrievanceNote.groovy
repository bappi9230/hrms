package hrms

/** Day 9: অভিযোগের আলোচনার মন্তব্য (কর্মী ও HR এর মধ্যে) */
class GrievanceNote {

    Grievance grievance
    String author                 // লেখকের username
    Boolean fromHr = false
    String note
    Date dateCreated

    static constraints = {
        grievance nullable: false
        author nullable: true, maxSize: 50
        note blank: false, maxSize: 1000
    }

    static mapping = {
        table 'hrms_grievance_note'
    }

    String toString() { "#${id}".toString() }
}

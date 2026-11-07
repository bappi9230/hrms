package hrms

import java.time.LocalDateTime

/** Day 9: কর্মীর অভিযোগ। OPEN -> IN_REVIEW -> RESOLVED / REJECTED। শুধু কর্মী নিজে ও HR/Admin দেখে */
class Grievance {

    static final List<String> CATEGORIES = ['WORKPLACE', 'HARASSMENT', 'PAYROLL', 'FACILITY', 'MANAGER', 'OTHER']
    static final List<String> STATUSES   = ['OPEN', 'IN_REVIEW', 'RESOLVED', 'REJECTED']

    Employee employee
    String category = 'OTHER'
    String subject
    String description
    String status = 'OPEN'

    String resolution
    String handledBy
    LocalDateTime resolvedAt
    Date dateCreated

    static constraints = {
        employee nullable: false
        category inList: CATEGORIES
        subject blank: false, maxSize: 150
        description blank: false, maxSize: 2000
        status inList: STATUSES
        resolution nullable: true, maxSize: 1000
        handledBy nullable: true, maxSize: 50
        resolvedAt nullable: true
    }

    static mapping = {
        table 'hrms_grievance'
    }

    String toString() { "${subject} (#${id})".toString() }
}

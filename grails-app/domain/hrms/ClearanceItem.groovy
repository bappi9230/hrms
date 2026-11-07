package hrms

import java.time.LocalDateTime

/** Day 9: ক্লিয়ারেন্স চেকলিস্টের একটি ধাপ। ownerRole = কোন বিভাগ টিক দেবে (IT, ADMIN, ACCOUNTS, MANAGER, HR) */
class ClearanceItem {

    static final List<String> OWNER_ROLES = ['IT', 'ADMIN', 'ACCOUNTS', 'MANAGER', 'HR']

    Separation separation
    String title
    String ownerRole = 'HR'
    Integer sortOrder = 0
    Boolean done = false
    String doneBy
    LocalDateTime doneAt
    String remarks

    static constraints = {
        separation nullable: false
        title blank: false, maxSize: 150
        ownerRole inList: OWNER_ROLES
        sortOrder nullable: false
        done nullable: false
        doneBy nullable: true, maxSize: 50
        doneAt nullable: true
        remarks nullable: true, maxSize: 255
    }

    static mapping = {
        table 'hrms_clearance_item'
    }

    String toString() { title }
}

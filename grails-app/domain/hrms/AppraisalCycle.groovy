package hrms

import java.time.LocalDate

/** Day 8: মূল্যায়ন চক্র। DRAFT -> OPEN (সব কর্মীর জন্য Appraisal তৈরি হয়) -> CLOSED (আর কোনো সম্পাদনা নয়) */
class AppraisalCycle {

    static final List<String> STATUSES = ['DRAFT', 'OPEN', 'CLOSED']

    String name
    LocalDate startDate          // মূল্যায়নের সময়কাল
    LocalDate endDate
    LocalDate dueDate            // এর মধ্যে সব মূল্যায়ন শেষ করতে হবে
    String status = 'DRAFT'
    String createdBy
    Date dateCreated

    static constraints = {
        name blank: false, unique: true, maxSize: 100
        startDate nullable: false
        endDate nullable: false, validator: { LocalDate v, AppraisalCycle c ->
            if (v != null && c.startDate != null && v.isBefore(c.startDate)) return ['appraisalCycle.endDate.beforeStart']
            true
        }
        dueDate nullable: false, validator: { LocalDate v, AppraisalCycle c ->
            if (v != null && c.startDate != null && v.isBefore(c.startDate)) return ['appraisalCycle.dueDate.beforeStart']
            true
        }
        status inList: STATUSES
        createdBy nullable: true, maxSize: 50
    }

    static mapping = {
        table 'hrms_appraisal_cycle'
    }

    String toString() { name }
}

package hrms

import java.time.LocalDate

/** Day 8: প্রশিক্ষণ কোর্স (ক্যাটালগ)। capacity 0 = আসন সীমাহীন */
class Course {

    static final List<String> CATEGORIES = ['TECHNICAL', 'SOFT_SKILL', 'COMPLIANCE', 'LEADERSHIP', 'OTHER']
    static final List<String> MODES = ['CLASSROOM', 'ONLINE', 'WORKSHOP']
    static final List<String> STATUSES = ['OPEN', 'CANCELLED']

    String title
    String description
    String category = 'TECHNICAL'
    String trainer
    String trainingMode = 'CLASSROOM'
    String location
    LocalDate startDate
    LocalDate endDate
    Integer hours = 8
    Integer capacity = 0
    String status = 'OPEN'
    String createdBy
    Date dateCreated

    static constraints = {
        title blank: false, maxSize: 150
        description nullable: true, maxSize: 1000
        category inList: CATEGORIES
        trainer nullable: true, maxSize: 100
        trainingMode inList: MODES
        location nullable: true, maxSize: 150
        startDate nullable: false
        endDate nullable: false, validator: { LocalDate v, Course c ->
            if (v != null && c.startDate != null && v.isBefore(c.startDate)) return ['course.endDate.beforeStart']
            true
        }
        hours nullable: false, min: 1, max: 500
        capacity nullable: false, min: 0, max: 10000
        status inList: STATUSES
        createdBy nullable: true, maxSize: 50
    }

    static mapping = {
        table 'hrms_course'
    }

    /** UPCOMING / RUNNING / ENDED */
    String phase(LocalDate today) {
        if (today.isBefore(startDate)) return 'UPCOMING'
        if (today.isAfter(endDate)) return 'ENDED'
        'RUNNING'
    }

    String toString() { title }
}

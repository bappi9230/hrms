package hrms

import java.time.LocalDate
import java.time.LocalDateTime

/** Day 7: একজন কর্মীর অনবোর্ডিং চেকলিস্টের একটি কাজ */
class OnboardingTask {

    Employee employee
    String title
    String category = 'HR'
    String ownerRole = 'HR'
    LocalDate dueDate
    Integer sortOrder = 10
    boolean done = false
    String doneBy
    LocalDateTime doneAt
    String remarks

    static constraints = {
        employee nullable: false
        title blank: false, maxSize: 150
        category inList: OnboardingTemplate.CATEGORIES
        ownerRole inList: OnboardingTemplate.OWNERS
        dueDate nullable: true
        sortOrder nullable: false
        doneBy nullable: true, maxSize: 50
        doneAt nullable: true
        remarks nullable: true, maxSize: 255
    }

    static mapping = {
        table 'hrms_onboarding_task'
    }

    boolean isOverdue() {
        !done && dueDate != null && dueDate.isBefore(LocalDate.now(AttendanceService.ZONE))
    }

    String toString() { title }
}

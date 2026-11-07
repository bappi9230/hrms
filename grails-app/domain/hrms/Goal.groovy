package hrms

import java.time.LocalDate

/**
 * Day 8: লক্ষ্য / KPI। কর্মীর নিজের, ম্যানেজার বা HR এর দেওয়া। অগ্রগতি = বর্তমান / লক্ষ্যমাত্রা।
 * একজন কর্মীর একই বছরের (due date অনুযায়ী) সব সচল লক্ষ্যের weight এর যোগফল ১০০ এর বেশি হতে পারে না।
 */
class Goal {

    static final List<String> STATUSES = ['ACTIVE', 'COMPLETED', 'CANCELLED']

    Employee employee
    String title
    String description
    BigDecimal targetValue = 100.0
    BigDecimal currentValue = 0.0
    String unit = '%'
    Integer weight = 10
    LocalDate dueDate
    String status = 'ACTIVE'
    String createdBy
    Date dateCreated

    static constraints = {
        employee nullable: false
        title blank: false, maxSize: 150
        description nullable: true, maxSize: 500
        targetValue nullable: false, min: 0.01
        currentValue nullable: false, min: 0.0
        unit blank: false, maxSize: 20
        weight nullable: false, min: 1, max: 100
        dueDate nullable: false
        status inList: STATUSES
        createdBy nullable: true, maxSize: 50
    }

    static mapping = {
        table 'hrms_goal'
    }

    /** ০-১০০ এর মধ্যে অগ্রগতি শতাংশ */
    BigDecimal progressPercent() {
        if (!targetValue || targetValue <= 0) return 0.0
        BigDecimal p = (currentValue ?: 0.0) * 100 / targetValue
        p.min(100.0).setScale(0, java.math.RoundingMode.HALF_UP)
    }

    String toString() { title }
}

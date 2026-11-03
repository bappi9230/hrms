package hrms

import java.time.LocalDate

/** Day 5: বেতন বৃদ্ধি / সংশোধনের লগ (আগের ও পরের gross সহ) */
class SalaryRevision {

    static final List<String> TYPES = ['INCREMENT', 'REVISION']   // REVISION = কমানোও যায়

    Employee employee
    String revisionType = 'INCREMENT'
    LocalDate effectiveDate
    BigDecimal oldGross
    BigDecimal newGross
    String remarks
    String createdBy
    Date dateCreated

    static transients = ['changeAmount', 'changePercent']

    static constraints = {
        employee nullable: false
        revisionType inList: TYPES
        effectiveDate nullable: false
        oldGross nullable: false, min: 0.0
        newGross nullable: false, min: 0.0
        remarks nullable: true, maxSize: 255
        createdBy nullable: true, maxSize: 50
    }

    static mapping = {
        table 'hrms_salary_revision'
    }

    BigDecimal getChangeAmount() { (newGross ?: 0.0) - (oldGross ?: 0.0) }

    BigDecimal getChangePercent() {
        (oldGross == null || oldGross == 0) ? null : (changeAmount * 100).divide(oldGross, 1, java.math.RoundingMode.HALF_UP)
    }
}

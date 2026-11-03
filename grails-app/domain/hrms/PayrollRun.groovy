package hrms

import java.time.LocalDateTime
import java.time.YearMonth

/** Day 5: একটি মাসের পে-রোল চালানো। DRAFT এ বারবার চালানো/সংশোধন যায়; FINALIZED হলে আর বদলানো যায় না। */
class PayrollRun {

    static final List<String> STATUSES = ['DRAFT', 'FINALIZED']

    Integer payYear
    Integer payMonth
    String status = 'DRAFT'
    Integer workingDays = 0
    Integer employeeCount = 0
    BigDecimal totalGross = 0
    BigDecimal totalDeduction = 0
    BigDecimal totalNet = 0
    String generatedBy
    LocalDateTime generatedAt
    String finalizedBy
    LocalDateTime finalizedAt

    static transients = ['period']

    static constraints = {
        payYear nullable: false, unique: 'payMonth'
        payMonth nullable: false, min: 1, max: 12
        status inList: STATUSES
        workingDays nullable: false, min: 0
        employeeCount nullable: false, min: 0
        generatedBy nullable: true, maxSize: 50
        generatedAt nullable: true
        finalizedBy nullable: true, maxSize: 50
        finalizedAt nullable: true
    }

    static mapping = {
        table 'hrms_payroll_run'
    }

    YearMonth getPeriod() { YearMonth.of(payYear, payMonth) }

    String toString() { "${payYear}-${String.format('%02d', payMonth)}".toString() }
}

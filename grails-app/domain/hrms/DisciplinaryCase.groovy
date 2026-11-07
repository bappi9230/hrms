package hrms

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Day 9: শৃঙ্খলা কেস (গোপনীয়)। ম্যানেজার/HR খোলে; কর্মী জবাব দেয়; HR সিদ্ধান্ত দিয়ে বন্ধ করে।
 * OPEN -> RESPONDED -> CLOSED। কারণ দর্শানো (SHOW_CAUSE) জবাব ছাড়া বন্ধ করা যায় না।
 */
class DisciplinaryCase {

    static final List<String> TYPES    = ['VERBAL_WARNING', 'WRITTEN_WARNING', 'SHOW_CAUSE', 'FINAL_WARNING', 'SUSPENSION']
    static final List<String> STATUSES = ['OPEN', 'RESPONDED', 'CLOSED']
    static final List<String> OUTCOMES = ['NO_ACTION', 'WARNING_ISSUED', 'SUSPENDED', 'DISMISSAL_RECOMMENDED']

    Employee employee
    String caseType = 'VERBAL_WARNING'
    LocalDate incidentDate
    String subject
    String description
    String raisedBy
    String status = 'OPEN'

    String employeeResponse
    LocalDateTime respondedAt

    String outcome
    String outcomeNote
    String closedBy
    LocalDateTime closedAt
    Date dateCreated

    static constraints = {
        employee nullable: false
        caseType inList: TYPES
        incidentDate nullable: false
        subject blank: false, maxSize: 150
        description blank: false, maxSize: 2000
        raisedBy nullable: true, maxSize: 50
        status inList: STATUSES
        employeeResponse nullable: true, maxSize: 2000
        respondedAt nullable: true
        outcome nullable: true, inList: OUTCOMES
        outcomeNote nullable: true, maxSize: 500
        closedBy nullable: true, maxSize: 50
        closedAt nullable: true
    }

    static mapping = {
        table 'hrms_disciplinary_case'
    }

    String toString() { "${caseType} #${id}".toString() }
}

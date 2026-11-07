package hrms

import java.time.LocalDate
import java.time.LocalDateTime

/** Day 8: ভ্রমণের অনুরোধ। PENDING -> APPROVED / REJECTED; প্রত্যাহার: CANCELLED */
class TravelRequest {

    static final List<String> MODES = ['BUS', 'TRAIN', 'AIR', 'CAR', 'OTHER']
    static final List<String> STATUSES = ['PENDING', 'APPROVED', 'REJECTED', 'CANCELLED']

    Employee employee
    String destination
    String purpose
    LocalDate fromDate
    LocalDate toDate
    String travelMode = 'BUS'
    BigDecimal estimatedCost
    String status = 'PENDING'
    String decidedBy
    LocalDateTime decidedAt
    String decisionNote
    Date dateCreated

    static constraints = {
        employee nullable: false
        destination blank: false, maxSize: 120
        purpose blank: false, maxSize: 500
        fromDate nullable: false
        toDate nullable: false, validator: { LocalDate v, TravelRequest t ->
            if (v != null && t.fromDate != null && v.isBefore(t.fromDate)) return ['travelRequest.toDate.beforeFrom']
            true
        }
        travelMode inList: MODES
        estimatedCost nullable: true, min: 0.0
        status inList: STATUSES
        decidedBy nullable: true, maxSize: 50
        decidedAt nullable: true
        decisionNote nullable: true, maxSize: 255
    }

    static mapping = {
        table 'hrms_travel_request'
    }

    String toString() { "${destination} (#${id})".toString() }
}

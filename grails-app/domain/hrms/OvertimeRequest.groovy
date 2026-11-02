package hrms

/** Day 3 এ সম্পূর্ণ হবে (ওভারটাইম আবেদন ও অনুমোদন) */
class OvertimeRequest {

    static final List<String> STATUSES = ['PENDING', 'APPROVED', 'REJECTED']

    Employee employee
    String status = 'PENDING'

    static constraints = {
        employee nullable: false
        status inList: STATUSES
    }
}

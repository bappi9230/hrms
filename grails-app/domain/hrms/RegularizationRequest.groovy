package hrms

/** Day 3 এ সম্পূর্ণ হবে (উপস্থিতি সংশোধনের আবেদন) */
class RegularizationRequest {

    static final List<String> STATUSES = ['PENDING', 'APPROVED', 'REJECTED']

    Employee employee
    String status = 'PENDING'

    static constraints = {
        employee nullable: false
        status inList: STATUSES
    }
}

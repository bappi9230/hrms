package hrms

import java.time.LocalDate

/** Day 7: কর্মীকে দেওয়া কোম্পানির সম্পদ (ল্যাপটপ, SIM, ID কার্ড ...)। ফেরত নিলে RETURNED। */
class AssetAllocation {

    static final List<String> TYPES = ['LAPTOP', 'DESKTOP', 'MONITOR', 'PHONE', 'SIM', 'ID_CARD', 'ACCESS_CARD', 'OTHER']
    static final List<String> STATUSES = ['ISSUED', 'RETURNED']

    Employee employee
    String assetType = 'LAPTOP'
    String assetName
    String serialNo
    LocalDate issuedDate
    LocalDate returnedDate
    String status = 'ISSUED'
    String remarks
    String issuedBy

    static constraints = {
        employee nullable: false
        assetType inList: TYPES
        assetName blank: false, maxSize: 120
        serialNo nullable: true, maxSize: 80
        issuedDate nullable: false
        returnedDate nullable: true
        status inList: STATUSES
        remarks nullable: true, maxSize: 255
        issuedBy nullable: true, maxSize: 50
    }

    static mapping = {
        table 'hrms_asset_allocation'
    }

    String toString() { assetName }
}

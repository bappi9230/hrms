package hrms

import java.time.LocalDate

/** Day 8: সম্পদ রেজিস্টার (কোম্পানির সব সম্পদ)। কাকে দেওয়া হয়েছে তা AssetAllocation এ থাকে। */
class Asset {

    static final List<String> TYPES = AssetAllocation.TYPES
    static final List<String> STATUSES = ['AVAILABLE', 'ASSIGNED', 'MAINTENANCE', 'RETIRED']

    String assetTag
    String assetType = 'LAPTOP'
    String name
    String serialNo
    LocalDate purchaseDate
    BigDecimal purchaseCost
    String status = 'AVAILABLE'
    String remarks
    Date dateCreated

    static constraints = {
        assetTag blank: false, unique: true, maxSize: 30
        assetType inList: TYPES
        name blank: false, maxSize: 120
        serialNo nullable: true, unique: true, maxSize: 80
        purchaseDate nullable: true
        purchaseCost nullable: true, min: 0.0
        status inList: STATUSES
        remarks nullable: true, maxSize: 255
    }

    static mapping = {
        table 'hrms_asset'
    }

    String toString() { "${assetTag} - ${name}".toString() }
}

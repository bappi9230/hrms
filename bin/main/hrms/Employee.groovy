package hrms

import java.time.LocalDate

class Employee {

    static final List<String> STATUSES = ['ACTIVE', 'INACTIVE', 'ON_LEAVE']
    static final List<String> GENDERS  = ['MALE', 'FEMALE', 'OTHER']
    static final List<String> TYPES    = ['FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERN', 'PROBATION']
    static final List<String> PHOTO_TYPES = ['image/jpeg', 'image/png', 'image/webp']
    static final int PHOTO_MAX_BYTES = 1024 * 1024   // 1 MB

    String name
    String email
    String phone
    LocalDate joinDate
    BigDecimal salary
    String status = 'ACTIVE'

    LocalDate dateOfBirth
    String gender
    String nid                        // Bangladesh NID: 10, 13 or 17 digits
    String presentAddress
    String permanentAddress
    String emergencyContactName
    String emergencyContactPhone
    String emergencyContactRelation
    String employeeType = 'FULL_TIME'

    byte[] photo
    String photoContentType
    boolean hasPhoto = false          // so lists never need to load the photo bytes

    static belongsTo = [department: Department, designation: Designation]

    static constraints = {
        name blank: false, maxSize: 100
        email blank: false, email: true, unique: true
        phone nullable: true, maxSize: 20
        joinDate nullable: false
        salary nullable: false, min: 0.0
        status inList: STATUSES
        department nullable: false
        designation nullable: false

        dateOfBirth nullable: true, validator: { LocalDate v ->
            if (v == null) return true
            if (v.isAfter(LocalDate.now().minusYears(16))) return ['employee.dateOfBirth.tooYoung']
            true
        }
        gender nullable: true, inList: GENDERS
        nid nullable: true, unique: true, matches: /^(\d{10}|\d{13}|\d{17})$/
        presentAddress nullable: true, maxSize: 255
        permanentAddress nullable: true, maxSize: 255
        emergencyContactName nullable: true, maxSize: 100
        emergencyContactPhone nullable: true, maxSize: 20
        emergencyContactRelation nullable: true, maxSize: 50
        employeeType inList: TYPES

        photo nullable: true, maxSize: PHOTO_MAX_BYTES
        photoContentType nullable: true, maxSize: 50
    }

    static mapping = {
        photo sqlType: 'blob'
    }

    String toString() { name }
}
package hrms

class Employee {

    String name
    String email
    String phone
    Date joinDate
    BigDecimal salary
    String status = 'ACTIVE'

    static belongsTo = [department: Department, designation: Designation]

    static constraints = {
        name blank: false, maxSize: 100
        email blank: false, email: true, unique: true
        phone nullable: true, maxSize: 20
        joinDate nullable: false
        salary nullable: false, min: 0.0
        status inList: ['ACTIVE', 'INACTIVE', 'ON_LEAVE']
        department nullable: false
        designation nullable: false
    }

    String toString() { name }
}

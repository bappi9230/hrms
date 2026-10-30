package hrms

class Designation {

    String title
    BigDecimal baseSalary

    static constraints = {
        title blank: false, unique: true, maxSize: 100
        baseSalary nullable: true, min: 0.0
    }

    String toString() { title }
}

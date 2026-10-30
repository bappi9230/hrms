package hrms

class Department {

    String name
    String description

    static constraints = {
        name blank: false, unique: true, maxSize: 100
        description nullable: true, maxSize: 255
    }

    String toString() { name }
}

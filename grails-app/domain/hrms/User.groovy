package hrms

class User implements Serializable {

    private static final long serialVersionUID = 1

    String username
    String password
    boolean enabled = true
    boolean accountExpired
    boolean accountLocked
    boolean passwordExpired

    Employee employee

    Set<Role> getAuthorities() {
        (UserRole.findAllByUser(this) as List<UserRole>)*.role as Set<Role>
    }

    static constraints = {
        username blank: false, unique: true, maxSize: 50
        password blank: false, password: true, maxSize: 255
        employee nullable: true
    }

    static mapping = {
        table 'hrms_user'
        password column: '`password`'
    }

    @Override
    boolean equals(Object other) {
        other instanceof User && other.username == username
    }

    @Override
    int hashCode() { username?.hashCode() ?: 0 }

    String toString() { username }
}
package hrms

class Role implements Serializable {

    private static final long serialVersionUID = 1

    static final String ADMIN      = 'ROLE_ADMIN'
    static final String HR         = 'ROLE_HR'
    static final String MANAGER    = 'ROLE_MANAGER'
    static final String EMPLOYEE   = 'ROLE_EMPLOYEE'
    static final String ACCOUNTANT = 'ROLE_ACCOUNTANT'

    String authority

    static constraints = {
        authority blank: false, unique: true, maxSize: 50
    }

    static mapping = {
        table 'hrms_role'
    }

    @Override
    boolean equals(Object other) {
        other instanceof Role && other.authority == authority
    }

    @Override
    int hashCode() { authority?.hashCode() ?: 0 }

    String toString() { authority }
}
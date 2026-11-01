package hrms

class UserRole implements Serializable {

    private static final long serialVersionUID = 1

    User user
    Role role

    @Override
    boolean equals(Object other) {
        if (!(other instanceof UserRole)) return false
        other.user?.id == user?.id && other.role?.id == role?.id
    }

    @Override
    int hashCode() {
        int hash = 17
        if (user != null) hash = hash * 31 + user.id.hashCode()
        if (role != null) hash = hash * 31 + role.id.hashCode()
        hash
    }

    static boolean exists(long userId, long roleId) {
        UserRole.where { user.id == userId && role.id == roleId }.count() > 0
    }

    static UserRole create(User user, Role role, boolean flush = false) {
        def instance = new UserRole(user: user, role: role)
        instance.save(flush: flush, insert: true)
        instance
    }

    static constraints = {
        user nullable: false
        role nullable: false, validator: { Role r, UserRole ur ->
            if (ur.user?.id && UserRole.exists(ur.user.id, r.id)) {
                return ['userRole.exists']
            }
            true
        }
    }

    static mapping = {
        table 'hrms_user_role'
        id composite: ['user', 'role']
        version false
    }
}
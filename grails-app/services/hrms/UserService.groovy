package hrms

import grails.gorm.transactions.Transactional

@Transactional
class UserService {

    def passwordEncoder

    static final int MIN_PASSWORD_LENGTH = 8

    User createUser(String username, String rawPassword, Collection<String> roleNames, Employee employee = null) {
        User user = new User(username: username, password: passwordEncoder.encode(rawPassword), employee: employee)
        user.save(failOnError: true)
        roleNames.each { String name ->
            Role role = Role.findByAuthority(name)
            if (role && !UserRole.exists(user.id, role.id)) {
                UserRole.create(user, role)
            }
        }
        user
    }

    boolean passwordMatches(User user, String rawPassword) {
        rawPassword && passwordEncoder.matches(rawPassword, user.password)
    }

    /** Returns null on success, or an error message on failure. */
    String changePassword(User user, String current, String newPassword, String confirm) {
        if (!passwordMatches(user, current)) return 'বর্তমান পাসওয়ার্ড সঠিক নয়।'
        if (!newPassword || newPassword.length() < MIN_PASSWORD_LENGTH) {
            return "নতুন পাসওয়ার্ড কমপক্ষে ${MIN_PASSWORD_LENGTH} অক্ষরের হতে হবে।".toString()
        }
        if (newPassword != confirm) return 'নতুন পাসওয়ার্ড ও নিশ্চিতকরণ মিলছে না।'
        if (newPassword == current) return 'নতুন পাসওয়ার্ড আগেরটির মতো হতে পারবে না।'

        User managed = User.get(user.id)
        managed.password = passwordEncoder.encode(newPassword)
        managed.save(failOnError: true)
        null
    }
}
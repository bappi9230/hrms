package hrms

import grails.plugin.springsecurity.annotation.Secured

@Secured(['isAuthenticated()'])
class AccountController {

    static allowedMethods = [updatePassword: 'POST']

    def springSecurityService
    def userService

    def changePassword() { }

    def updatePassword() {
        User user = springSecurityService.currentUser as User
        String error = userService.changePassword(user, params.currentPassword as String,
                params.newPassword as String, params.confirmPassword as String)
        if (error) {
            flash.error = error
            redirect(action: 'changePassword')
            return
        }
        flash.success = 'পাসওয়ার্ড সফলভাবে পরিবর্তন হয়েছে।'
        redirect(controller: 'dashboard', action: 'index')
    }
}
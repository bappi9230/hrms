package hrms

import grails.plugin.springsecurity.annotation.Secured

@Secured('permitAll')
class AuthController {

    def springSecurityService

    def login() {
        if (springSecurityService.isLoggedIn()) {
            redirect(uri: '/')
            return
        }
        [postUrl: createLink(uri: '/login/authenticate'), loginError: params.login_error != null]
    }

    def denied() {
        response.status = 403
    }
}
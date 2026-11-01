package hrms

import grails.plugin.springsecurity.annotation.Secured

@Secured(['ROLE_ADMIN', 'ROLE_HR'])
class DepartmentController {

    static scaffold = Department
}
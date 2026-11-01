package hrms

import java.time.LocalDate

class BootStrap {

    UserService userService

    static final String DEMO_PASSWORD = 'Hrms@2026'

    def init = { servletContext ->
        Employee.withTransaction {
            seedOrganization()
            seedSecurity()
        }
    }

    def destroy = {
    }

    private void seedOrganization() {
        if (Department.count() > 0) return

        def it1 = new Department(name: 'IT', description: 'Information Technology').save(failOnError: true)
        def hr  = new Department(name: 'HR', description: 'Human Resources').save(failOnError: true)
        def fin = new Department(name: 'Finance', description: 'Accounts and Finance').save(failOnError: true)

        def dev  = new Designation(title: 'Software Engineer', baseSalary: 50000).save(failOnError: true)
        def mgr  = new Designation(title: 'Manager', baseSalary: 80000).save(failOnError: true)
        def exec = new Designation(title: 'Executive', baseSalary: 35000).save(failOnError: true)

        new Employee(name: 'Rahim Uddin', email: 'rahim@example.com', phone: '01711111111',
                joinDate: LocalDate.now().minusYears(2), salary: 55000, department: it1, designation: dev,
                dateOfBirth: LocalDate.of(1995, 5, 12), gender: 'MALE', nid: '1995123456789',
                presentAddress: 'House 12, Road 3, Mirpur-10, Dhaka', permanentAddress: 'Village: Char Kali, Barishal',
                emergencyContactName: 'Abdul Karim Uddin', emergencyContactPhone: '01711000001',
                emergencyContactRelation: 'Father', employeeType: 'FULL_TIME').save(failOnError: true)

        new Employee(name: 'Karim Hossain', email: 'karim@example.com', phone: '01822222222',
                joinDate: LocalDate.now().minusYears(5), salary: 85000, department: it1, designation: mgr,
                dateOfBirth: LocalDate.of(1988, 11, 3), gender: 'MALE', nid: '1988765432109',
                presentAddress: 'Flat 5B, Dhanmondi, Dhaka', permanentAddress: 'Cumilla Sadar, Cumilla',
                emergencyContactName: 'Rokeya Begum', emergencyContactPhone: '01822000002',
                emergencyContactRelation: 'Spouse', employeeType: 'FULL_TIME').save(failOnError: true)

        new Employee(name: 'Sumaiya Akter', email: 'sumaiya@example.com', phone: '01933333333',
                joinDate: LocalDate.now().minusYears(1), salary: 38000, department: fin, designation: exec,
                dateOfBirth: LocalDate.of(1997, 2, 20), gender: 'FEMALE', nid: '1997246813579',
                presentAddress: 'Uttara Sector 7, Dhaka', permanentAddress: 'Sylhet Sadar, Sylhet',
                emergencyContactName: 'Mahmuda Akter', emergencyContactPhone: '01933000003',
                emergencyContactRelation: 'Mother', employeeType: 'FULL_TIME').save(failOnError: true)

        new Employee(name: 'Nasrin Sultana', email: 'nasrin@example.com', phone: '01644444444',
                joinDate: LocalDate.now().minusMonths(4), salary: 40000, department: hr, designation: exec,
                dateOfBirth: LocalDate.of(1996, 8, 30), gender: 'FEMALE', nid: '1996135792468',
                presentAddress: 'Mohammadpur, Dhaka', permanentAddress: 'Rajshahi Sadar, Rajshahi',
                emergencyContactName: 'Jahangir Alam', emergencyContactPhone: '01644000004',
                emergencyContactRelation: 'Brother', employeeType: 'PROBATION').save(failOnError: true)
    }

    private void seedSecurity() {
        [Role.ADMIN, Role.HR, Role.MANAGER, Role.EMPLOYEE, Role.ACCOUNTANT].each { String authority ->
            if (!Role.findByAuthority(authority)) {
                new Role(authority: authority).save(failOnError: true)
            }
        }

        [
            [username: 'admin',      roles: [Role.ADMIN],                      employee: null],
            [username: 'hr',         roles: [Role.HR, Role.EMPLOYEE],          employee: 'nasrin@example.com'],
            [username: 'manager',    roles: [Role.MANAGER, Role.EMPLOYEE],     employee: 'karim@example.com'],
            [username: 'accountant', roles: [Role.ACCOUNTANT, Role.EMPLOYEE], employee: 'sumaiya@example.com'],
            [username: 'employee',   roles: [Role.EMPLOYEE],                  employee: 'rahim@example.com'],
        ].each { Map u ->
            if (!User.findByUsername(u.username as String)) {
                Employee emp = u.employee ? Employee.findByEmail(u.employee as String) : null
                userService.createUser(u.username as String, DEMO_PASSWORD, u.roles as List<String>, emp)
            }
        }
    }
}
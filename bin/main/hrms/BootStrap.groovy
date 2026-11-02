package hrms

import java.time.LocalDate

class BootStrap {

    UserService userService
    EmploymentService employmentService

    static final String DEMO_PASSWORD = 'Hrms@2026'

    def init = { servletContext ->
        Employee.withTransaction {
            seedOrganization()
            seedStructure()
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

    /** Day 2: শাখা, গ্রেড, কস্ট সেন্টার, রিপোর্টিং ম্যানেজার, চাকরির ইতিহাস ও নমুনা নথি (আগের ডেটা থাকলেও কাজ করে) */
    private void seedStructure() {
        if (Branch.count() == 0) {
            def head = new Branch(name: 'Head Office', address: 'Motijheel, Dhaka', phone: '029000001').save(failOnError: true)
            def ctg  = new Branch(name: 'Chattogram Branch', address: 'Agrabad, Chattogram', phone: '0312000002').save(failOnError: true)

            def g1 = new Grade(name: 'G1', gradeLevel: 1, minSalary: 25000, maxSalary: 40000).save(failOnError: true)
            def g2 = new Grade(name: 'G2', gradeLevel: 2, minSalary: 40000, maxSalary: 65000).save(failOnError: true)
            def g3 = new Grade(name: 'G3', gradeLevel: 3, minSalary: 65000, maxSalary: 120000).save(failOnError: true)

            def ccTech = new CostCenter(code: 'CC-100', name: 'Technology').save(failOnError: true)
            def ccHr   = new CostCenter(code: 'CC-200', name: 'Human Resources').save(failOnError: true)
            def ccFin  = new CostCenter(code: 'CC-300', name: 'Finance').save(failOnError: true)

            Employee karim   = Employee.findByEmail('karim@example.com')
            Employee rahim   = Employee.findByEmail('rahim@example.com')
            Employee sumaiya = Employee.findByEmail('sumaiya@example.com')
            Employee nasrin  = Employee.findByEmail('nasrin@example.com')

            // [কর্মী, শাখা, গ্রেড, কস্ট সেন্টার, ম্যানেজার]
            [[karim, head, g3, ccTech, null],
             [rahim, head, g2, ccTech, karim],
             [sumaiya, head, g1, ccFin, karim],
             [nasrin, ctg, g1, ccHr, karim]].each { row ->
                Employee e = row[0] as Employee
                if (e) {
                    e.branch = row[1] as Branch
                    e.grade = row[2] as Grade
                    e.costCenter = row[3] as CostCenter
                    e.reportingManager = row[4] as Employee
                    e.save(failOnError: true)
                }
            }
        }

        if (EmploymentHistory.count() == 0) {
            Employee.list().each { Employee e -> employmentService.logJoined(e, 'system') }

            // নমুনা নথি: ড্যাশবোর্ডের "মেয়াদ শেষ হচ্ছে" তালিকা দেখার জন্য
            Employee rahim = Employee.findByEmail('rahim@example.com')
            if (rahim) {
                byte[] bytes = 'Sample contract (demo data)'.getBytes('UTF-8')
                new EmployeeDocument(employee: rahim, docType: 'CONTRACT', title: 'Employment Contract (sample)',
                        fileName: 'contract-sample.txt', contentType: 'text/plain', fileSize: (long) bytes.length,
                        fileData: bytes, expiryDate: LocalDate.now().plusDays(20), uploadedBy: 'system').save(failOnError: true)
            }
        }
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
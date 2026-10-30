package hrms

class BootStrap {

    def init = { servletContext ->
        if (Department.count() == 0) {
            def it1 = new Department(name: 'IT', description: 'Information Technology').save(failOnError: true)
            def hr = new Department(name: 'HR', description: 'Human Resources').save(failOnError: true)
            def fin = new Department(name: 'Finance', description: 'Accounts and Finance').save(failOnError: true)

            def dev = new Designation(title: 'Software Engineer', baseSalary: 50000).save(failOnError: true)
            def mgr = new Designation(title: 'Manager', baseSalary: 80000).save(failOnError: true)
            def exec = new Designation(title: 'Executive', baseSalary: 35000).save(failOnError: true)

            new Employee(name: 'Rahim Uddin', email: 'rahim@example.com', phone: '01711111111',
                    joinDate: new Date(), salary: 55000, department: it1, designation: dev).save(failOnError: true)
            new Employee(name: 'Karim Hossain', email: 'karim@example.com', phone: '01822222222',
                    joinDate: new Date(), salary: 85000, department: hr, designation: mgr).save(failOnError: true)
            new Employee(name: 'Sumaiya Akter', email: 'sumaiya@example.com', phone: '01933333333',
                    joinDate: new Date(), salary: 38000, department: fin, designation: exec).save(failOnError: true)
        }
    }

    def destroy = {
    }
}

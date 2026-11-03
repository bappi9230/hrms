package hrms

import java.time.LocalDate
import java.time.LocalTime

class BootStrap {

    UserService userService
    EmploymentService employmentService
    LeaveService leaveService
    SalaryService salaryService
    private Timer leaveTimer

    static final String DEMO_PASSWORD = 'Hrms@2026'

    def init = { servletContext ->
        Employee.withTransaction {
            seedOrganization()
            seedStructure()
            seedSecurity()
            seedAttendance()
            seedLeave()
            seedPayroll()
            leaveService.syncStatus()
            salaryService.syncProfileSalaries()
        }

        // Day 4: প্রতি ঘণ্টায় ON_LEAVE স্ট্যাটাস সিঙ্ক (রাত পেরোলে নতুন দিনের জন্য)
        leaveTimer = new Timer('leave-status-sync', true)
        leaveTimer.schedule(new TimerTask() {
            void run() {
                try {
                    Employee.withNewSession { Employee.withTransaction { leaveService.syncStatus(); salaryService.syncProfileSalaries() } }
                } catch (Throwable t) {
                    org.slf4j.LoggerFactory.getLogger('hrms.BootStrap').warn('Leave status sync failed: ' + t.message)
                }
            }
        }, 3600000L, 3600000L)
    }

    def destroy = {
        leaveTimer?.cancel()
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

    /** Day 3: নমুনা শিফট ও গত কয়েক দিনের উপস্থিতি (টেস্টের সুবিধার জন্য) */
    private void seedAttendance() {
        if (Shift.count() == 0) {
            new Shift(name: 'সাধারণ শিফট', startTime: LocalTime.of(9, 0), endTime: LocalTime.of(18, 0),
                    graceMinutes: 10, earlyOutMinutes: 15).save(failOnError: true)
        }

        if (Attendance.count() == 0) {
            Shift shift = Shift.list(sort: 'id', max: 1)[0]
            Employee rahim = Employee.findByEmail('rahim@example.com')
            if (shift && rahim) {
                // গত ৪টি কর্মদিবস (শুক্রবার বাদ): একদিন দেরিতে আসা
                int made = 0
                int back = 1
                while (made < 4) {
                    LocalDate d = LocalDate.now().minusDays(back++)
                    if (d.dayOfWeek == java.time.DayOfWeek.FRIDAY) continue
                    boolean late = (made == 1)
                    new Attendance(employee: rahim, attDate: d, shift: shift, source: 'MANUAL',
                            status: late ? 'LATE' : 'PRESENT',
                            checkIn: late ? LocalTime.of(9, 35) : LocalTime.of(8, 55),
                            checkOut: LocalTime.of(18, 5),
                            lateMinutes: late ? 35 : 0).save(failOnError: true)
                    made++
                }
            }
        }
    }

    /** Day 4: ছুটির ধরন ও নমুনা আবেদন (নিয়মগুলো টেস্ট করার সুবিধার জন্য) */
    private void seedLeave() {
        if (LeaveType.count() == 0) {
            // code, name, annualDays, accrual, paid, carryForward, maxCarry, approvalLevels, gender
            [['AL', 'বার্ষিক ছুটি',      14, 'MONTHLY', true,  true,  5, 2, null],
             ['CL', 'নৈমিত্তিক ছুটি',    10, 'YEARLY',  true,  false, 0, 1, null],
             ['SL', 'অসুস্থতার ছুটি',     14, 'YEARLY',  true,  false, 0, 1, null],
             ['ML', 'মাতৃত্বকালীন ছুটি', 112, 'YEARLY',  true,  false, 0, 2, 'FEMALE'],
             ['LWP', 'বিনা বেতনের ছুটি',   0, 'YEARLY',  false, false, 0, 2, null]].each { r ->
                new LeaveType(code: r[0], name: r[1], annualDays: r[2], accrual: r[3], paid: r[4],
                        carryForward: r[5], maxCarryForward: r[6], approvalLevels: r[7], gender: r[8]).save(failOnError: true)
            }
        }

        Employee rahim = Employee.findByEmail('rahim@example.com')
        LeaveType al = LeaveType.findByCode('AL')
        if (rahim && al && LeaveBalance.count() == 0) {
            // গত বছরে ১৪ এর মধ্যে ৬ দিন ব্যবহৃত -> বাকি ৮, carry forward সীমা ৫ -> এ বছর ৫ দিন আসবে
            int lastYear = LocalDate.now().year - 1
            new LeaveBalance(employee: rahim, leaveType: al, balanceYear: lastYear, used: 6).save(failOnError: true)
            leaveService.ensureBalances(rahim, LocalDate.now().year)

            // নমুনা আবেদন: একটি অপেক্ষমাণ (ধাপ ১/২: ম্যানেজার)
            LocalDate start = LocalDate.now().plusDays(10)
            while (leaveService.isOffDay(start)) start = start.plusDays(1)
            leaveService.apply(rahim, LeaveType.findByCode('CL'), start, start.plusDays(1), 'পারিবারিক অনুষ্ঠান')
        }
    }

    /**
     * Day 5: বেতন কাঠামো, ভাতা/কর্তনের খাত এবং গত মাসের নমুনা উপস্থিতি/ছুটি/ওভারটাইম।
     * গত মাসের পে-রোল চালালে নানা অবস্থা (অনুপস্থিতি, দেরি, বিনা বেতন ছুটি, OT) দেখা যাবে।
     */
    private void seedPayroll() {
        if (SalaryHead.count() == 0) {
            // code, name, type, calc, value
            [['MOB', 'মোবাইল ভাতা', 'EARNING', 'FIXED', 500],
             ['TIF', 'টিফিন ভাতা', 'EARNING', 'FIXED', 1000],
             ['PERF', 'পারফরম্যান্স ভাতা', 'EARNING', 'PERCENT_BASIC', 10],
             ['CAN', 'ক্যান্টিন কর্তন', 'DEDUCTION', 'FIXED', 500]].each { r ->
                new SalaryHead(code: r[0], name: r[1], headType: r[2], calcType: r[3], defaultValue: r[4]).save(failOnError: true)
            }
        }

        salaryService.ensureAllStructures()

        if (EmployeeSalaryHead.count() == 0) {
            Employee rahim = Employee.findByEmail('rahim@example.com')
            Employee karim = Employee.findByEmail('karim@example.com')
            Employee sumaiya = Employee.findByEmail('sumaiya@example.com')
            if (rahim) {
                salaryService.assignHead(rahim, SalaryHead.findByCode('MOB'), null)
                salaryService.assignHead(rahim, SalaryHead.findByCode('CAN'), null)
            }
            if (karim) {
                salaryService.assignHead(karim, SalaryHead.findByCode('MOB'), null)
                salaryService.assignHead(karim, SalaryHead.findByCode('TIF'), null)
                salaryService.assignHead(karim, SalaryHead.findByCode('PERF'), null)
            }
            if (sumaiya) salaryService.assignHead(sumaiya, SalaryHead.findByCode('TIF'), 800)
        }

        if (OvertimeRequest.count() == 0 && PayrollRun.count() == 0) seedLastMonthActivity()
    }

    private void seedLastMonthActivity() {
        LocalDate today = LocalDate.now(AttendanceService.ZONE)
        java.time.YearMonth lastMonth = java.time.YearMonth.from(today).minusMonths(1)
        Shift shift = Shift.list(sort: 'id', max: 1)[0]
        if (shift == null) return

        List<LocalDate> workDays = (1..lastMonth.lengthOfMonth()).collect { lastMonth.atDay(it) }
                .findAll { LocalDate d -> d.dayOfWeek != java.time.DayOfWeek.FRIDAY }

        Employee rahim = Employee.findByEmail('rahim@example.com')
        Employee karim = Employee.findByEmail('karim@example.com')
        Employee sumaiya = Employee.findByEmail('sumaiya@example.com')
        Employee nasrin = Employee.findByEmail('nasrin@example.com')

        // কোন কর্মীর কোন (০ থেকে গোনা) কর্মদিবস অনুপস্থিত / দেরি
        Map<Employee, Map> plan = [:]
        if (rahim)   plan[rahim]   = [absent: [4], late: [1, 8, 15]]            // ১ অনুপস্থিত, ৩ দেরি (= ১ দিনের কর্তন)
        if (karim)   plan[karim]   = [absent: [], late: []]                     // নিখুঁত
        if (sumaiya) plan[sumaiya] = [absent: [], late: [6]]
        if (nasrin)  plan[nasrin]  = [absent: [10], late: [2, 3]]

        // ছুটি: সুমাইয়া ১ দিন বেতনসহ (CL), রহিম ২ দিন বিনা বেতন (LWP) - নির্দিষ্ট কর্মদিবসগুলো
        Map<Employee, List> leavePlan = [:]
        if (sumaiya) leavePlan[sumaiya] = [LeaveType.findByCode('CL'), [workDays[12]]]
        if (rahim)   leavePlan[rahim]   = [LeaveType.findByCode('LWP'), [workDays[18], workDays[19]]]

        // একই কর্মীর ছুটির দিনে উপস্থিতি বসাব না
        Map<Long, Set<LocalDate>> leaveByEmp = [:]
        leavePlan.each { Employee e, List v ->
            leaveByEmp[e.id] = (v[1] as List<LocalDate>) as Set<LocalDate>
        }

        plan.each { Employee e, Map p ->
            workDays.eachWithIndex { LocalDate d, int idx ->
                if (leaveByEmp[e.id]?.contains(d)) return
                if (d.isBefore(e.joinDate)) return
                if (Attendance.findByEmployeeAndAttDate(e, d) != null) return
                if ((p.absent as List).contains(idx)) return        // কোনো রেকর্ড নেই = অনুপস্থিত
                boolean late = (p.late as List).contains(idx)
                new Attendance(employee: e, attDate: d, shift: shift, source: 'MANUAL',
                        status: late ? 'LATE' : 'PRESENT',
                        checkIn: late ? LocalTime.of(9, 40) : LocalTime.of(8, 58),
                        checkOut: LocalTime.of(18, 3),
                        lateMinutes: late ? 40 : 0).save(failOnError: true)
            }
        }

        // অনুমোদিত ছুটির আবেদন (সরাসরি তৈরি - আবেদনের ৩০ দিনের নিয়ম এখানে লাগে না) ও ব্যালেন্সে ব্যবহার
        leavePlan.each { Employee e, List v ->
            LeaveType type = v[0] as LeaveType
            List<LocalDate> days = v[1] as List<LocalDate>
            if (type == null) return
            LeaveRequest req = new LeaveRequest(employee: e, leaveType: type, fromDate: days.first(), toDate: days.last(),
                    days: days.size(), reason: 'নমুনা ছুটি (Day 5 seed)', status: 'APPROVED',
                    stages: e.reportingManager ? 'MANAGER' : 'HR').save(failOnError: true, flush: true)
            LeaveBalance bal = leaveService.ensureBalance(e, type, days.first().year)
            bal.used = bal.used + days.size()
            bal.save(failOnError: true)
            days.each { LocalDate d ->
                new Attendance(employee: e, attDate: d, status: 'ON_LEAVE', source: 'MANUAL', remarks: "LEAVE#${req.id}".toString())
                        .save(failOnError: true)
            }
        }

        // অনুমোদিত ওভারটাইম: রহিম ৩.৫ ঘণ্টা (২+১.৫), করিম ৩ ঘণ্টা; একটি অপেক্ষমাণ (পে-রোলে ধরা হবে না)
        if (rahim) {
            new OvertimeRequest(employee: rahim, otDate: workDays[5], hours: 2.0, reason: 'রিলিজ ডেপ্লয়মেন্ট', status: 'APPROVED', decidedBy: 'manager').save(failOnError: true)
            new OvertimeRequest(employee: rahim, otDate: workDays[9], hours: 1.5, reason: 'বাগ ফিক্স', status: 'APPROVED', decidedBy: 'manager').save(failOnError: true)
            new OvertimeRequest(employee: rahim, otDate: workDays[20], hours: 4.0, reason: 'অপেক্ষমাণ নমুনা', status: 'PENDING').save(failOnError: true)
        }
        if (karim) new OvertimeRequest(employee: karim, otDate: workDays[7], hours: 3.0, reason: 'ক্লায়েন্ট ডেমো', status: 'APPROVED', decidedBy: 'admin').save(failOnError: true)
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
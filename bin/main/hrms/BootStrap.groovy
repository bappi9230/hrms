package hrms

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class BootStrap {

    UserService userService
    EmploymentService employmentService
    LeaveService leaveService
    SalaryService salaryService
    LoanService loanService
    BonusService bonusService
    OnboardingService onboardingService
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
            seedPayrollDay6()
            seedRecruitment()
            seedDay8()
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

    /**
     * Day 6: পেমেন্ট তথ্য (ব্যাংক/bKash/Nagad), একটি চলমান ঋণ (করিম, গত মাস থেকে শুরু) ও একটি পারফরম্যান্স বোনাস (রহিম, গত মাস)।
     * গত মাসের পে-রোল চালালে PF, TDS, ঋণের কিস্তি ও বোনাস দেখা যাবে।
     */
    private void seedPayrollDay6() {
        if (PaymentInfo.count() == 0) {
            Employee rahim = Employee.findByEmail('rahim@example.com')
            Employee karim = Employee.findByEmail('karim@example.com')
            Employee sumaiya = Employee.findByEmail('sumaiya@example.com')
            Employee nasrin = Employee.findByEmail('nasrin@example.com')
            if (rahim) new PaymentInfo(employee: rahim, method: 'BANK', bankName: 'Dutch-Bangla Bank', bankBranch: 'Mirpur', accountNo: '1011234567890', routingNo: '090261234').save(failOnError: true)
            if (karim) new PaymentInfo(employee: karim, method: 'BANK', bankName: 'BRAC Bank', bankBranch: 'Dhanmondi', accountNo: '1501203456789', routingNo: '060261234').save(failOnError: true)
            if (sumaiya) new PaymentInfo(employee: sumaiya, method: 'BKASH', walletNo: '01933333333').save(failOnError: true)
            if (nasrin) new PaymentInfo(employee: nasrin, method: 'NAGAD', walletNo: '01644444444').save(failOnError: true)
        }

        if (Loan.count() == 0 && Bonus.count() == 0 && PayrollRun.count() == 0) {
            java.time.YearMonth last = java.time.YearMonth.from(LocalDate.now(AttendanceService.ZONE)).minusMonths(1)
            Employee karim = Employee.findByEmail('karim@example.com')
            Employee rahim = Employee.findByEmail('rahim@example.com')
            if (karim) {
                Map r = loanService.create(karim, 'LOAN', 60000, 6, last.year, last.monthValue, 'নমুনা ঋণ (Day 6 seed)')
                if (r.ok) loanService.approve(r.loan as Loan, 'seed')
            }
            if (rahim) {
                new Bonus(employee: rahim, bonusType: 'PERFORMANCE', title: 'Q3 performance (sample)', payYear: last.year, payMonth: last.monthValue,
                        amount: 5000, note: 'নমুনা', createdBy: 'system').save(failOnError: true)
            }
        }
    }

    /**
     * Day 7: অনবোর্ডিং টেমপ্লেট, একটি অনুমোদিত + একটি অপেক্ষমাণ জনবল চাহিদা, একটি খোলা বিজ্ঞপ্তি এবং প্রতিটি ধাপে নমুনা প্রার্থী।
     *  - Imran: ইন্টারভিউ হয়ে গেছে কিন্তু ফিডব্যাক বাকি (ইন্টারভিউয়ার: Karim = manager ইউজার)
     *  - Sadia: ইন্টারভিউ সম্পন্ন (HIRE) -> অফার তৈরি করা যাবে
     *  - Mahfuz: অফার গৃহীত -> কর্মীতে রূপান্তর করা যাবে
     *  - Nasrin: পরীক্ষাকালের মেয়াদ পার (সিদ্ধান্ত বাকি), অসম্পূর্ণ অনবোর্ডিং চেকলিস্ট, একটি ল্যাপটপ বরাদ্দ
     */
    private void seedRecruitment() {
        if (OnboardingTemplate.count() == 0) {
            // title, category, owner, dueAfterDays
            [['NID ও পাসপোর্ট সাইজ ছবি জমা', 'DOCUMENTS', 'HR', 0],
             ['শিক্ষাগত সনদের কপি জমা', 'DOCUMENTS', 'HR', 3],
             ['নিয়োগপত্র/চুক্তিপত্রে স্বাক্ষর', 'DOCUMENTS', 'HR', 1],
             ['ব্যাংক/মোবাইল ওয়ালেটের তথ্য সংগ্রহ', 'DOCUMENTS', 'ACCOUNTS', 3],
             ['অফিসিয়াল ইমেইল অ্যাকাউন্ট তৈরি', 'IT', 'IT', 0],
             ['ল্যাপটপ/ডিভাইস বুঝিয়ে দেওয়া', 'IT', 'IT', 0],
             ['বসার জায়গা ও ID কার্ড', 'ADMIN', 'ADMIN', 2],
             ['কোম্পানি পরিচিতি ও নীতিমালা ওরিয়েন্টেশন', 'TRAINING', 'HR', 1],
             ['ম্যানেজারের সাথে প্রথম সপ্তাহের পরিকল্পনা', 'HR', 'MANAGER', 5]].eachWithIndex { r, i ->
                new OnboardingTemplate(title: r[0], category: r[1], ownerRole: r[2], dueAfterDays: r[3], sortOrder: (i + 1) * 10).save(failOnError: true)
            }
        }

        Employee karim = Employee.findByEmail('karim@example.com')
        Employee nasrin = Employee.findByEmail('nasrin@example.com')

        // পরীক্ষাকাল, চেকলিস্ট ও সম্পদ - Nasrin (PROBATION ধরনের কর্মী)
        if (nasrin && ProbationRecord.count() == 0 && nasrin.employeeType == 'PROBATION') {
            onboardingService.startProbation(nasrin, nasrin.joinDate, 3, 'FULL_TIME')
        }
        if (nasrin && OnboardingTask.count() == 0) {
            onboardingService.startOnboarding(nasrin)
            OnboardingTask.findAllByEmployee(nasrin, [sort: 'sortOrder', max: 6]).each { OnboardingTask t ->
                t.done = true
                t.doneBy = 'hr'
                t.doneAt = LocalDateTime.now().minusDays(100)
                t.save(failOnError: true)
            }
        }
        if (nasrin && AssetAllocation.count() == 0) {
            new AssetAllocation(employee: nasrin, assetType: 'LAPTOP', assetName: 'Dell Latitude 5440', serialNo: 'DL-5440-0007',
                    issuedDate: nasrin.joinDate, issuedBy: 'system').save(failOnError: true)
        }

        if (ManpowerRequisition.count() > 0) return
        LocalDate today = LocalDate.now(AttendanceService.ZONE)
        LocalDateTime now = LocalDateTime.now(AttendanceService.ZONE)
        Department it1 = Department.findByName('IT')
        Department hr = Department.findByName('HR')
        Designation dev = Designation.findByTitle('Software Engineer')
        Designation exec = Designation.findByTitle('Executive')
        Branch head = Branch.findByName('Head Office')
        if (!it1 || !hr || !dev || !exec) return

        ManpowerRequisition r1 = new ManpowerRequisition(title: 'Software Engineer (Backend)', department: it1, designation: dev, branch: head,
                vacancies: 2, employeeType: 'FULL_TIME', minSalary: 50000, maxSalary: 70000, expectedJoinDate: today.plusDays(45),
                justification: 'নতুন প্রজেক্টের জন্য ২ জন ব্যাকএন্ড ডেভেলপার দরকার।', status: 'APPROVED', requestedBy: 'manager',
                decidedBy: 'hr', decidedAt: now.minusDays(10), decisionNote: 'অনুমোদিত (নমুনা)').save(failOnError: true)
        new ManpowerRequisition(title: 'HR Executive', department: hr, designation: exec, vacancies: 1, employeeType: 'FULL_TIME',
                minSalary: 35000, maxSalary: 45000, expectedJoinDate: today.plusDays(60),
                justification: 'নিয়োগ ও অনবোর্ডিংয়ের কাজ বাড়ছে, একজন সহায়ক দরকার।', status: 'PENDING', requestedBy: 'manager').save(failOnError: true)

        JobPosting post = new JobPosting(requisition: r1, title: 'Software Engineer (Backend)',
                description: 'REST API, ডাটাবেস ডিজাইন ও কোড রিভিউসহ ব্যাকএন্ড সার্ভিস তৈরির দায়িত্ব।',
                requirements: 'Java/Groovy বা সমমানের অভিজ্ঞতা, SQL জ্ঞান, ২+ বছরের অভিজ্ঞতা।', location: 'Head Office, Motijheel',
                deadline: today.plusDays(30), postedBy: 'hr').save(failOnError: true)

        // নাম, ইমেইল, ধাপ, সূত্র, প্রত্যাশিত বেতন, অভিজ্ঞতা, ধাপে কত দিন
        Map<String, Candidate> cands = [:]
        [['Tahmid Hasan', 'tahmid@example.com', 'APPLIED', 'WEBSITE', 55000, 2.0, 1],
         ['Farhana Yasmin', 'farhana@example.com', 'SCREENING', 'LINKEDIN', 60000, 3.0, 3],
         ['Imran Chowdhury', 'imran@example.com', 'INTERVIEW', 'REFERRAL', 65000, 4.0, 2],
         ['Sadia Islam', 'sadia@example.com', 'INTERVIEW', 'JOB_PORTAL', 62000, 3.5, 5],
         ['Mahfuz Alam', 'mahfuz@example.com', 'OFFER', 'REFERRAL', 60000, 5.0, 4],
         ['Rubel Mia', 'rubel@example.com', 'REJECTED', 'WALK_IN', 120000, 1.0, 6]].each { r ->
            cands[r[0] as String] = new Candidate(name: r[0], email: r[1], phone: '017' + (10000000 + (r[0].hashCode() & 0xFFFFF)).toString().take(8),
                    jobPosting: post, currentStage: r[2], source: r[3], expectedSalary: r[4], experienceYears: r[5],
                    stageChangedAt: now.minusDays(r[6] as int), createdBy: 'hr',
                    rejectReason: r[2] == 'REJECTED' ? 'বেতনের প্রত্যাশা বাজেটের অনেক বেশি।' : null).save(failOnError: true)
        }

        // Imran: ২ ঘণ্টা আগে ইন্টারভিউ হয়েছে - ফিডব্যাক বাকি (Karim দেবে)
        new Interview(candidate: cands['Imran Chowdhury'], roundNo: 1, roundName: 'Technical', scheduledAt: now.minusHours(2), durationMinutes: 45,
                mode: 'ONSITE', location: 'Meeting Room 2', interviewer: karim, scheduledBy: 'hr').save(failOnError: true)
        // Sadia: সম্পন্ন, HIRE
        new Interview(candidate: cands['Sadia Islam'], roundNo: 1, roundName: 'Technical', scheduledAt: now.minusDays(2), durationMinutes: 60,
                mode: 'ONLINE', location: 'Google Meet', interviewer: karim, status: 'COMPLETED', rating: 4, recommendation: 'HIRE',
                feedback: 'সমস্যা সমাধানে ভালো, ডাটাবেস জ্ঞান শক্ত। টিম কালচারে মানানসই।', feedbackAt: now.minusDays(2), feedbackBy: 'manager',
                scheduledBy: 'hr').save(failOnError: true)
        // Mahfuz: সম্পন্ন + গৃহীত অফার (রূপান্তরের জন্য প্রস্তুত)
        new Interview(candidate: cands['Mahfuz Alam'], roundNo: 1, roundName: 'Technical', scheduledAt: now.minusDays(7), durationMinutes: 60,
                mode: 'ONSITE', location: 'Meeting Room 1', interviewer: karim, status: 'COMPLETED', rating: 5, recommendation: 'HIRE',
                feedback: 'অত্যন্ত শক্তিশালী ব্যাকএন্ড অভিজ্ঞতা, দ্রুত শিখতে পারে।', feedbackAt: now.minusDays(7), feedbackBy: 'manager',
                scheduledBy: 'hr').save(failOnError: true)
        new JobOffer(candidate: cands['Mahfuz Alam'], department: it1, designation: dev, branch: head, employeeType: 'FULL_TIME', salary: 60000,
                joinDate: today.plusDays(14), probationMonths: 3, validUntil: today.plusDays(7), status: 'ACCEPTED', offeredBy: 'hr',
                decidedAt: now.minusDays(1), decisionNote: 'প্রার্থী ফোনে গ্রহণ করেছেন').save(failOnError: true)
    }

    // ------------------------------------------------------------------ Day 8: পারফরম্যান্স, প্রশিক্ষণ, খরচ/ভ্রমণ, সম্পদ, সুবিধা
    private void seedDay8() {
        LocalDate today = LocalDate.now(AttendanceService.ZONE)
        LocalDateTime now = LocalDateTime.now(AttendanceService.ZONE)
        Employee rahim = Employee.findByEmail('rahim@example.com')
        Employee karim = Employee.findByEmail('karim@example.com')
        Employee sumaiya = Employee.findByEmail('sumaiya@example.com')
        Employee nasrin = Employee.findByEmail('nasrin@example.com')
        if (!rahim || !karim || !sumaiya || !nasrin) return
        int year = today.year

        // ---- লক্ষ্য ও মূল্যায়ন চক্র
        if (AppraisalCycle.count() == 0) {
            AppraisalCycle open = new AppraisalCycle(name: "${year} বার্ষিক মূল্যায়ন".toString(), startDate: LocalDate.of(year, 1, 1), endDate: LocalDate.of(year, 12, 31),
                    dueDate: LocalDate.of(year + 1, 1, 15), status: 'OPEN', createdBy: 'hr').save(failOnError: true)
            new AppraisalCycle(name: "${year + 1} বার্ষিক মূল্যায়ন".toString(), startDate: LocalDate.of(year + 1, 1, 1), endDate: LocalDate.of(year + 1, 12, 31),
                    dueDate: LocalDate.of(year + 2, 1, 15), status: 'DRAFT', createdBy: 'hr').save(failOnError: true)

            [[rahim, 'রিলিজ সাইকেল সময়মতো শেষ করা', 12, 9, 'টি', 40, 'karim'],
             [rahim, 'কোড রিভিউ কভারেজ', 100, 60, '%', 30, 'karim'],
             [rahim, 'নতুন কর্মীকে মেন্টরিং', 4, 4, 'সেশন', 30, 'rahim'],
             [karim, 'টিমের স্প্রিন্ট ভেলোসিটি বৃদ্ধি', 100, 70, '%', 60, 'karim'],
             [karim, 'দুইজন নতুন ডেভেলপার নিয়োগ', 2, 1, 'জন', 40, 'karim'],
             [sumaiya, 'মাসিক ক্লোজিং ৫ দিনে শেষ', 12, 10, 'মাস', 100, 'sumaiya']].each { r ->
                BigDecimal target = r[2] as BigDecimal
                BigDecimal cur = r[3] as BigDecimal
                new Goal(employee: r[0] as Employee, title: r[1] as String, targetValue: target, currentValue: cur, unit: r[4] as String,
                        weight: r[5] as Integer, dueDate: LocalDate.of(year, 12, 15), status: cur >= target ? 'COMPLETED' : 'ACTIVE',
                        createdBy: r[6] as String).save(failOnError: true)
            }

            // রহিম: নিজের মূল্যায়ন জমা (ম্যানেজারের পালা), সুমাইয়া: সম্পন্ন, বাকিরা: নিজের পালা
            new Appraisal(cycle: open, employee: rahim, reviewer: karim, status: 'PENDING_MANAGER', selfRating: 4,
                    selfComment: 'এ বছর দুটি বড় রিলিজ সময়মতো দিয়েছি এবং একজন নতুন সহকর্মীকে মেন্টর করেছি।',
                    selfSubmittedAt: now.minusDays(3)).save(failOnError: true)
            new Appraisal(cycle: open, employee: sumaiya, reviewer: karim, status: 'COMPLETED', selfRating: 4,
                    selfComment: 'মাসিক ক্লোজিং নিয়মিত ও নির্ভুল রেখেছি।', selfSubmittedAt: now.minusDays(9),
                    managerRating: 4, managerComment: 'নির্ভরযোগ্য ও সময়নিষ্ঠ।', goalScore: 83.0, managerSubmittedAt: now.minusDays(6), reviewedBy: 'manager').save(failOnError: true)
            new Appraisal(cycle: open, employee: nasrin, reviewer: karim, status: 'PENDING_SELF').save(failOnError: true)
            new Appraisal(cycle: open, employee: karim, status: 'PENDING_SELF').save(failOnError: true)
        }

        // ---- প্রশিক্ষণ
        if (Course.count() == 0) {
            Course ended = new Course(title: 'অফিস নিরাপত্তা ও কমপ্লায়েন্স', description: 'অগ্নি নিরাপত্তা, তথ্য সুরক্ষা ও আচরণবিধি।', category: 'COMPLIANCE',
                    trainer: 'মো. আনিসুর রহমান', trainingMode: 'CLASSROOM', location: 'Training Room', startDate: today.minusDays(30), endDate: today.minusDays(29),
                    hours: 6, capacity: 30, createdBy: 'hr').save(failOnError: true)
            Course running = new Course(title: 'Effective Communication', description: 'লিখিত ও মৌখিক যোগাযোগের কৌশল।', category: 'SOFT_SKILL',
                    trainer: 'Farzana Karim', trainingMode: 'ONLINE', location: 'Google Meet', startDate: today.minusDays(1), endDate: today.plusDays(2),
                    hours: 12, capacity: 20, createdBy: 'hr').save(failOnError: true)
            Course upcoming = new Course(title: 'Grails ও Spring Boot ওয়ার্কশপ', description: 'REST API, GORM ও সিকিউরিটি হাতেকলমে।', category: 'TECHNICAL',
                    trainer: 'Tanvir Ahmed', trainingMode: 'WORKSHOP', location: 'Head Office, Room 3', startDate: today.plusDays(10), endDate: today.plusDays(11),
                    hours: 16, capacity: 2, createdBy: 'hr').save(failOnError: true)
            new Course(title: 'ম্যানেজারদের জন্য নেতৃত্ব কর্মসূচি', category: 'LEADERSHIP', trainer: 'Dr. S. Chowdhury', trainingMode: 'CLASSROOM',
                    location: 'Training Room', startDate: today.plusDays(25), endDate: today.plusDays(27), hours: 24, capacity: 0, createdBy: 'hr').save(failOnError: true)

            TrainingEnrollment done = new TrainingEnrollment(course: ended, employee: nasrin, status: 'COMPLETED', score: 88,
                    completedDate: today.minusDays(29), enrolledBy: 'hr').save(failOnError: true, flush: true)
            done.certificateNo = "CERT-${today.minusDays(29).year}-${String.format('%05d', done.id)}".toString()
            done.save(failOnError: true)
            new TrainingEnrollment(course: ended, employee: rahim, status: 'FAILED', score: 41, completedDate: today.minusDays(29), enrolledBy: 'hr').save(failOnError: true)
            new TrainingEnrollment(course: running, employee: rahim, enrolledBy: 'employee').save(failOnError: true)
            new TrainingEnrollment(course: running, employee: karim, enrolledBy: 'manager').save(failOnError: true)
            new TrainingEnrollment(course: upcoming, employee: rahim, enrolledBy: 'employee').save(failOnError: true)   // ২ আসনের ১টি ভরা
        }

        // ---- ভ্রমণ ও খরচ
        if (TravelRequest.count() == 0) {
            TravelRequest trip = new TravelRequest(employee: rahim, destination: 'Chattogram', purpose: 'ক্লায়েন্ট সাইটে সিস্টেম ডেপ্লয়মেন্ট', fromDate: today.minusDays(12),
                    toDate: today.minusDays(10), travelMode: 'TRAIN', estimatedCost: 6000, status: 'APPROVED', decidedBy: 'manager', decidedAt: now.minusDays(15),
                    decisionNote: 'অনুমোদিত').save(failOnError: true)
            new TravelRequest(employee: rahim, destination: 'Sylhet', purpose: 'বিক্রেতার সাথে বৈঠক', fromDate: today.plusDays(8), toDate: today.plusDays(9),
                    travelMode: 'BUS', estimatedCost: 4500, status: 'PENDING').save(failOnError: true)
            new TravelRequest(employee: karim, destination: 'Cox\'s Bazar', purpose: 'বার্ষিক কৌশল সম্মেলন', fromDate: today.plusDays(20), toDate: today.plusDays(22),
                    travelMode: 'AIR', estimatedCost: 25000, status: 'PENDING').save(failOnError: true)

            if (ExpenseClaim.count() == 0) {
                new ExpenseClaim(employee: rahim, travel: trip, category: 'TRANSPORT', title: 'Chattogram ট্রেন ভাড়া (আসা-যাওয়া)', expenseDate: today.minusDays(12), amount: 480,
                        status: 'PENDING_MANAGER').save(failOnError: true)
                new ExpenseClaim(employee: rahim, travel: trip, category: 'FOOD', title: 'ভ্রমণকালীন খাবার', expenseDate: today.minusDays(11), amount: 450,
                        status: 'PENDING_ACCOUNTS', managerBy: 'manager', managerAt: now.minusDays(8), managerNote: 'ঠিক আছে').save(failOnError: true)
                new ExpenseClaim(employee: karim, category: 'COMMUNICATION', title: 'মাসিক ইন্টারনেট বিল', expenseDate: today.minusDays(5), amount: 500,
                        status: 'PENDING_ACCOUNTS').save(failOnError: true)
                new ExpenseClaim(employee: sumaiya, category: 'OFFICE_SUPPLIES', title: 'ফাইল ও স্টেশনারি', expenseDate: today.minusDays(20), amount: 320,
                        status: 'PAID', accountsBy: 'admin', accountsAt: now.minusDays(17), paidBy: 'admin', paidDate: today.minusDays(15), paymentRef: 'BKASH-TX-88213').save(failOnError: true)
                new ExpenseClaim(employee: nasrin, category: 'FOOD', title: 'ইন্টারভিউ প্যানেলের নাস্তা', expenseDate: today.minusDays(9), amount: 400,
                        status: 'REJECTED', managerBy: 'manager', managerAt: now.minusDays(7), managerNote: 'রসিদ ছাড়া গ্রহণযোগ্য নয়').save(failOnError: true)
            }
        }

        // ---- সম্পদ রেজিস্টার (Nasrin এর ল্যাপটপ Day 7 এর বরাদ্দের সাথে যুক্ত)
        if (Asset.count() == 0) {
            Asset lap = new Asset(assetTag: 'AST-0001', assetType: 'LAPTOP', name: 'Dell Latitude 5440', serialNo: 'DL-5440-0007', purchaseDate: today.minusMonths(14),
                    purchaseCost: 98000, status: 'ASSIGNED').save(failOnError: true)
            AssetAllocation al = AssetAllocation.findBySerialNo('DL-5440-0007')
            if (al) { al.asset = lap; al.save(failOnError: true) } else lap.status = 'AVAILABLE'
            new Asset(assetTag: 'AST-0002', assetType: 'LAPTOP', name: 'Lenovo ThinkPad E14', serialNo: 'LN-E14-1182', purchaseDate: today.minusMonths(5), purchaseCost: 85000).save(failOnError: true)
            new Asset(assetTag: 'AST-0003', assetType: 'MONITOR', name: 'Dell 24" P2422H', serialNo: 'DL-P24-5531', purchaseDate: today.minusMonths(9), purchaseCost: 22000).save(failOnError: true)
            new Asset(assetTag: 'AST-0004', assetType: 'PHONE', name: 'Samsung Galaxy A34', serialNo: 'SM-A34-9902', purchaseDate: today.minusMonths(11), purchaseCost: 38000,
                    status: 'MAINTENANCE', remarks: 'স্ক্রিন মেরামতে আছে').save(failOnError: true)
            new Asset(assetTag: 'AST-0005', assetType: 'DESKTOP', name: 'HP ProDesk 400', serialNo: 'HP-PD4-2007', purchaseDate: today.minusYears(5), purchaseCost: 55000,
                    status: 'RETIRED', remarks: 'পুরনো, বাতিল').save(failOnError: true)
        }

        // ---- সুবিধা
        if (BenefitPlan.count() == 0) {
            BenefitPlan health = new BenefitPlan(name: 'স্বাস্থ্য বীমা', benefitType: 'HEALTH_INSURANCE', provider: 'Green Delta Insurance', monthlyValue: 1500,
                    minServiceMonths: 3, description: 'কর্মীর জন্য বার্ষিক ২ লক্ষ ৳ পর্যন্ত হাসপাতাল খরচ।').save(failOnError: true)
            BenefitPlan mobile = new BenefitPlan(name: 'মোবাইল বিল সহায়তা', benefitType: 'MOBILE', provider: 'Grameenphone', monthlyValue: 500,
                    minServiceMonths: 0).save(failOnError: true)
            new BenefitPlan(name: 'যাতায়াত ভাতা', benefitType: 'TRANSPORT', monthlyValue: 2000, minServiceMonths: 6,
                    description: 'অফিসে আসা-যাওয়ার জন্য মাসিক ভাতা।').save(failOnError: true)
            new BenefitPlan(name: 'জীবন বীমা', benefitType: 'LIFE_INSURANCE', provider: 'MetLife', monthlyValue: 800, minServiceMonths: 12, active: false).save(failOnError: true)
            [[health, rahim], [health, karim], [health, sumaiya], [mobile, karim], [mobile, rahim]].each { row ->
                Employee e = row[1] as Employee
                new BenefitEnrollment(plan: row[0] as BenefitPlan, employee: e, startDate: e.joinDate.plusMonths(3).isAfter(today) ? e.joinDate : e.joinDate.plusMonths(3),
                        enrolledBy: 'hr').save(failOnError: true)
            }
        }
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
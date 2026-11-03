package hrms

import grails.gorm.transactions.Transactional

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

/**
 * Day 4: ছুটি ব্যবস্থাপনা - ব্যালেন্স, আবেদন, multi-level অনুমোদন, ON_LEAVE সিঙ্ক।
 *
 * অনুমোদনের ধাপ (stages):
 *   ম্যানেজার আছে + approvalLevels=1  -> MANAGER
 *   ম্যানেজার আছে + approvalLevels=2  -> MANAGER, HR
 *   ম্যানেজার নেই                      -> HR
 * ধাপ নিয়ম: নিজের আবেদন নিজে অনুমোদন করা যায় না; ADMIN/HR ম্যানেজার-ধাপও করতে পারে।
 */
@Transactional
class LeaveService {

    /** সাপ্তাহিক ছুটি। Holiday Calendar (Day 3) যোগ হলে isOffDay এ শুধু শর্ত বাড়াতে হবে। */
    static final Set<DayOfWeek> WEEKEND = [DayOfWeek.FRIDAY] as Set

    def accessService

    // ------------------------------------------------------------------ তারিখ
    boolean isOffDay(LocalDate d) {
        WEEKEND.contains(d.dayOfWeek)
    }

    /** from..to এর মধ্যে কর্মদিবসগুলো */
    List<LocalDate> workDays(LocalDate from, LocalDate to) {
        List<LocalDate> result = []
        if (from == null || to == null || to.isBefore(from)) return result
        LocalDate d = from
        while (!d.isAfter(to)) {
            if (!isOffDay(d)) result << d
            d = d.plusDays(1)
        }
        result
    }

    private static LocalDate today() { LocalDate.now(AttendanceService.ZONE) }

    /** accrual গণনার তারিখ: চলতি বছর হলে আজ, ভবিষ্যৎ বছর হলে বছরের শুরু, অতীত হলে শেষ */
    private static LocalDate accrualDate(int year) {
        LocalDate t = today()
        year == t.year ? t : (year > t.year ? LocalDate.of(year, 1, 1) : LocalDate.of(year, 12, 31))
    }

    // ------------------------------------------------------------------ ব্যালেন্স
    /** ব্যালেন্স না থাকলে বানায়; carry forward থাকলে আগের বছরের অবশিষ্ট (সীমা পর্যন্ত) নিয়ে আসে */
    LeaveBalance ensureBalance(Employee emp, LeaveType type, int year) {
        LeaveBalance b = LeaveBalance.findByEmployeeAndLeaveTypeAndBalanceYear(emp, type, year)
        if (b != null) return b

        BigDecimal carry = 0.0
        if (type.carryForward && type.maxCarryForward > 0) {
            LeaveBalance prev = LeaveBalance.findByEmployeeAndLeaveTypeAndBalanceYear(emp, type, year - 1)
            if (prev != null) {
                BigDecimal left = prev.remainingAsOf(LocalDate.of(year - 1, 12, 31))
                carry = left > 0 ? (left < type.maxCarryForward ? left : type.maxCarryForward) : 0.0
            }
        }
        new LeaveBalance(employee: emp, leaveType: type, balanceYear: year, carriedForward: carry).save(failOnError: true, flush: true)
    }

    /** কর্মীর সব প্রযোজ্য ছুটির ধরনের ব্যালেন্স (বছর অনুযায়ী) */
    List<LeaveBalance> ensureBalances(Employee emp, int year) {
        LeaveType.findAllByActive(true, [sort: 'id']).findAll { LeaveType t -> t.eligible(emp) }.collect { LeaveType t ->
            ensureBalance(emp, t, year)
        }
    }

    /** ব্যালেন্স কার্ড/টেবিলের জন্য হিসাব করা মান */
    Map view(LeaveBalance b) {
        LocalDate asOf = accrualDate(b.balanceYear)
        [balance  : b, type: b.leaveType, limited: b.leaveType.hasQuota(),
         accrued  : b.accruedAsOf(asOf), carried: b.carriedForward, used: b.used, pending: b.pending,
         remaining: b.remainingAsOf(asOf), available: b.availableAsOf(asOf)]
    }

    // ------------------------------------------------------------------ আবেদন
    Map apply(Employee emp, LeaveType type, LocalDate from, LocalDate to, String reason) {
        if (emp == null) return fail('এই অ্যাকাউন্টের সাথে কোনো কর্মী যুক্ত নেই।')
        if (type == null || !type.active) return fail('ছুটির ধরন নির্বাচন করুন।')
        if (!type.eligible(emp)) return fail("${type.name} এই কর্মীর জন্য প্রযোজ্য নয়।".toString())
        if (from == null || to == null) return fail('শুরু ও শেষের তারিখ দিন।')
        if (to.isBefore(from)) return fail('শেষের তারিখ শুরুর তারিখের আগে হতে পারে না।')
        if (from.year != to.year) return fail('ছুটি একই বছরের মধ্যে হতে হবে। বছর পেরোলে দুটি আলাদা আবেদন করুন।')
        LocalDate t = today()
        if (from.year < t.year || from.year > t.year + 1) return fail('শুধু চলতি বা আগামী বছরের ছুটির আবেদন করা যাবে।')
        if (from.isBefore(t.minusDays(30))) return fail('৩০ দিনের বেশি আগের তারিখে আবেদন করা যাবে না।')
        if (!reason?.trim()) return fail('ছুটির কারণ লিখুন।')

        List<LocalDate> days = workDays(from, to)
        if (days.isEmpty()) return fail('নির্বাচিত তারিখগুলো সবই সাপ্তাহিক ছুটির দিন।')

        long overlap = LeaveRequest.createCriteria().count {
            eq('employee', emp)
            'in'('status', ['PENDING', 'APPROVED'])
            le('fromDate', to)
            ge('toDate', from)
        }
        if (overlap > 0) return fail('এই সময়ের সাথে আগের একটি ছুটির আবেদন (অপেক্ষমাণ/অনুমোদিত) মিলে যাচ্ছে।')

        LeaveBalance bal = ensureBalance(emp, type, from.year)
        if (type.hasQuota()) {
            BigDecimal avail = bal.availableAsOf(accrualDate(from.year))
            if (days.size() > avail) {
                return fail("${type.name}-এর যথেষ্ট ব্যালেন্স নেই (প্রয়োজন ${days.size()} দিন, খালি ${avail.stripTrailingZeros().toPlainString()} দিন)।".toString())
            }
        }

        List<String> stages = []
        if (emp.reportingManager != null) stages << 'MANAGER'
        if (emp.reportingManager == null || type.approvalLevels >= 2) stages << 'HR'

        LeaveRequest r = new LeaveRequest(employee: emp, leaveType: type, fromDate: from, toDate: to,
                days: days.size(), reason: reason.trim(), stages: stages.join(','))
        if (!r.save(flush: true)) return fail('আবেদন সংরক্ষণ করা যায়নি।')

        bal.pending += days.size()
        bal.save(flush: true)
        [ok: true, message: "ছুটির আবেদন জমা হয়েছে (${days.size()} দিন)।".toString(), request: r]
    }

    // ------------------------------------------------------------------ অনুমোদন
    /** এই ব্যবহারকারী এখন এই আবেদনের সিদ্ধান্ত দিতে পারবে কি */
    boolean canDecide(LeaveRequest r) {
        if (r == null || r.status != 'PENDING') return false
        Employee me = accessService.currentEmployee()
        if (me != null && r.employee.id == me.id) return false
        boolean hrAdmin = accessService.isHrAdmin()
        if (r.currentStage() == 'HR') return hrAdmin
        hrAdmin || (me != null && r.employee.reportingManager?.id == me.id)
    }

    /** আমার সিদ্ধান্তের অপেক্ষায় থাকা আবেদন */
    List<LeaveRequest> pendingForMe() {
        if (accessService.currentUser() == null) return []
        LeaveRequest.findAllByStatus('PENDING', [sort: 'appliedAt', order: 'asc']).findAll { LeaveRequest r -> canDecide(r) }
    }

    Map approve(LeaveRequest r, String remarks) {
        if (!canDecide(r)) return fail('এই আবেদনে সিদ্ধান্ত দেওয়ার অনুমতি আপনার নেই বা আবেদনটি আর অপেক্ষমাণ নেই।')
        recordStep(r, 'APPROVED', remarks)

        if (r.currentLevel < r.stageList().size()) {
            r.currentLevel = r.currentLevel + 1
            r.save(flush: true)
            return [ok: true, message: 'ধাপ অনুমোদিত হয়েছে, পরের ধাপের অপেক্ষায়।']
        }

        r.status = 'APPROVED'
        r.save(flush: true)
        LeaveBalance bal = ensureBalance(r.employee, r.leaveType, r.fromDate.year)
        bal.pending = nonNeg(bal.pending - r.days)
        bal.used = bal.used + r.days
        bal.save(flush: true)
        markAttendance(r)
        syncStatus()
        [ok: true, message: 'ছুটি চূড়ান্তভাবে অনুমোদিত হয়েছে।']
    }

    Map reject(LeaveRequest r, String remarks) {
        if (!canDecide(r)) return fail('এই আবেদনে সিদ্ধান্ত দেওয়ার অনুমতি আপনার নেই বা আবেদনটি আর অপেক্ষমাণ নেই।')
        if (!remarks?.trim()) return fail('বাতিলের কারণ (মন্তব্য) লিখুন।')
        recordStep(r, 'REJECTED', remarks)
        r.status = 'REJECTED'
        r.save(flush: true)
        releasePending(r)
        [ok: true, message: 'ছুটির আবেদন বাতিল করা হয়েছে।']
    }

    /** আবেদনকারী নিজে (অপেক্ষমাণ, বা শুরুর আগে অনুমোদিত) অথবা HR/Admin (যেকোনো সময়) */
    boolean canCancel(LeaveRequest r) {
        if (r == null || !(r.status in ['PENDING', 'APPROVED'])) return false
        if (accessService.isHrAdmin()) return true
        Employee me = accessService.currentEmployee()
        if (me == null || me.id != r.employee.id) return false
        r.status == 'PENDING' || r.fromDate.isAfter(today())
    }

    Map cancel(LeaveRequest r) {
        if (!canCancel(r)) return fail('এই আবেদন বাতিল করা যাবে না।')
        boolean wasApproved = r.status == 'APPROVED'
        r.status = 'CANCELLED'
        r.cancelledBy = accessService.currentUser()?.username
        r.save(flush: true)

        if (wasApproved) {
            LeaveBalance bal = ensureBalance(r.employee, r.leaveType, r.fromDate.year)
            bal.used = nonNeg(bal.used - r.days)
            bal.save(flush: true)
            Attendance.executeUpdate("delete from Attendance a where a.status = 'ON_LEAVE' and a.remarks = :tag".toString(),
                    [tag: attendanceTag(r)])
            syncStatus()
        } else {
            releasePending(r)
        }
        [ok: true, message: 'ছুটির আবেদন বাতিল করা হয়েছে।']
    }

    private void recordStep(LeaveRequest r, String decision, String remarks) {
        User u = accessService.currentUser()
        new LeaveApproval(leaveRequest: r, stepLevel: r.currentLevel, stage: r.currentStage(), decision: decision,
                actedBy: u?.username ?: 'system', actedByName: accessService.currentEmployee()?.name ?: u?.username,
                remarks: remarks?.trim()).save(failOnError: true, flush: true)
    }

    private void releasePending(LeaveRequest r) {
        LeaveBalance bal = ensureBalance(r.employee, r.leaveType, r.fromDate.year)
        bal.pending = nonNeg(bal.pending - r.days)
        bal.save(flush: true)
    }

    // ------------------------------------------------------------------ উপস্থিতি ও স্ট্যাটাস সিঙ্ক
    private static String attendanceTag(LeaveRequest r) { "LEAVE#${r.id}".toString() }

    /** আজ বা তার আগের কর্মদিবসগুলোর উপস্থিতিতে ON_LEAVE বসায় (চেক-ইন থাকলে ছোঁয় না) */
    private void markAttendance(LeaveRequest r) {
        LocalDate t = today()
        workDays(r.fromDate, r.toDate).findAll { LocalDate d -> !d.isAfter(t) }.each { LocalDate d ->
            Attendance att = Attendance.findByEmployeeAndAttDate(r.employee, d)
            if (att == null) {
                new Attendance(employee: r.employee, attDate: d, status: 'ON_LEAVE', source: 'MANUAL',
                        remarks: attendanceTag(r)).save(flush: true)
            } else if (att.checkIn == null && att.status != 'ON_LEAVE') {
                att.status = 'ON_LEAVE'
                att.remarks = attendanceTag(r)
                att.save(flush: true)
            }
        }
    }

    /**
     * আজ অনুমোদিত ছুটিতে থাকা কর্মীদের ACTIVE -> ON_LEAVE, আর ছুটি শেষ হলে ON_LEAVE -> ACTIVE।
     * আজকের উপস্থিতি রেকর্ডেও ON_LEAVE বসায়। বারবার চালালে ক্ষতি নেই (idempotent)।
     */
    void syncStatus() {
        LocalDate t = today()
        List<LeaveRequest> active = LeaveRequest.executeQuery(
                "from LeaveRequest r where r.status = 'APPROVED' and r.fromDate <= :t and r.toDate >= :t", [t: t])
        List<Long> onLeaveIds = active.collect { it.employee.id as Long }.unique()

        if (onLeaveIds) {
            Employee.executeUpdate("update Employee e set e.status = 'ON_LEAVE' where e.status = 'ACTIVE' and e.id in (:ids)", [ids: onLeaveIds])
            Employee.executeUpdate("update Employee e set e.status = 'ACTIVE' where e.status = 'ON_LEAVE' and e.id not in (:ids)", [ids: onLeaveIds])
        } else {
            Employee.executeUpdate("update Employee e set e.status = 'ACTIVE' where e.status = 'ON_LEAVE'")
        }

        if (!isOffDay(t)) active.each { LeaveRequest r -> markAttendance(r) }
    }

    // ------------------------------------------------------------------ তালিকা ও ক্যালেন্ডার
    List<LeaveRequest> myRequests(Employee emp) {
        LeaveRequest.findAllByEmployee(emp, [sort: 'appliedAt', order: 'desc', max: 50])
    }

    /** যে কর্মীদের ক্যালেন্ডারে দেখানো হবে: অনুমোদনকারী/HR = visible সবাই, সাধারণ কর্মী = নিজের বিভাগের সহকর্মী */
    List<Employee> calendarEmployees() {
        Employee me = accessService.currentEmployee()
        if (accessService.isApprover() || accessService.has('ROLE_ACCOUNTANT')) {
            Set<Long> ids = accessService.visibleEmployeeIds()
            return Employee.createCriteria().list {
                if (ids != null) inList('id', ids ?: [-1L])
                order('name', 'asc')
            }
        }
        if (me == null) return []
        Employee.findAllByDepartment(me.department, [sort: 'name'])
    }

    /** মাসের ক্যালেন্ডার: [days:[LocalDate], rows:[[employee, cells:[date -> [type, status, own]]]]] */
    Map calendar(YearMonth month) {
        LocalDate first = month.atDay(1)
        LocalDate last = month.atEndOfMonth()
        List<Employee> emps = calendarEmployees()
        Map<Long, List<LeaveRequest>> byEmp = [:].withDefault { [] as List<LeaveRequest> }
        if (emps) {
            LeaveRequest.createCriteria().list {
                'in'('employee', emps)
                'in'('status', ['PENDING', 'APPROVED'])
                le('fromDate', last)
                ge('toDate', first)
            }.each { LeaveRequest r -> byEmp[r.employee.id] << r }
        }
        List<LocalDate> days = (1..month.lengthOfMonth()).collect { month.atDay(it) }
        List rows = emps.collect { Employee e ->
            Map<LocalDate, Map> cells = [:]
            byEmp[e.id].each { LeaveRequest r ->
                days.each { LocalDate d ->
                    if (!d.isBefore(r.fromDate) && !d.isAfter(r.toDate)) {
                        // APPROVED আগে থাকে
                        if (!cells.containsKey(d) || r.status == 'APPROVED') cells[d] = [request: r, status: r.status]
                    }
                }
            }
            [employee: e, cells: cells]
        }
        [days: days, rows: rows]
    }

    private static BigDecimal nonNeg(BigDecimal v) { v < 0 ? 0.0 : v }

    private static Map fail(String message) {
        [ok: false, message: message]
    }
}

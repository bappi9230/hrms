package hrms

import grails.plugin.springsecurity.annotation.Secured
import org.springframework.web.multipart.MultipartFile

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeParseException

@Secured(['ROLE_ADMIN', 'ROLE_HR'])
class EmployeeController {

    static allowedMethods = [save: 'POST', update: 'POST', delete: 'POST', importCsv: 'POST', recordChange: 'POST']

    static final List<String> SORTABLE = ['name', 'email', 'joinDate', 'status', 'employeeType']

    static final List<String> CSV_COLUMNS = ['name', 'email', 'phone', 'joinDate', 'salary', 'department', 'designation',
                                              'branch', 'grade', 'status', 'employeeType', 'dateOfBirth', 'gender', 'nid']

    def accessService
    def attendanceService
    def leaveService
    def employmentService
    def salaryService

    // ======================================================================= list + search/filter
    @Secured(['ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER'])
    def index() {
        int max = Math.min(params.int('max') ?: 10, 100)
        String sort = SORTABLE.contains(params.sort) ? params.sort : 'name'
        String order = params.order == 'desc' ? 'desc' : 'asc'
        Set<Long> ids = accessService.visibleEmployeeIds()

        def list = Employee.createCriteria().list(
                [max: max, offset: params.int('offset') ?: 0],
                filterClosure(ids, [sort: sort, order: order]))

        [
            employeeList : list,
            employeeCount: list.totalCount,
            filterParams : currentFilters(),
            departments  : Department.list(sort: 'name'),
            branches     : Branch.list(sort: 'name'),
            statuses     : Employee.STATUSES,
            types        : Employee.TYPES
        ]
    }

    /** The active search / filter values (blank ones removed) - reused for paging, sorting and export links. */
    private Map currentFilters() {
        Map f = [q: params.q?.toString()?.trim(), dept: params.dept, branch: params.branch, status: params.status, type: params.type]
        f.findAll { it.value }
    }

    private Closure filterClosure(Set<Long> ids, Map ordering) {
        String q = params.q?.toString()?.trim()
        Long dept = params.long('dept')
        Long branch = params.long('branch')
        String statusFilter = params.status
        String typeFilter = params.type
        return {
            if (ids != null) inList('id', ids ?: [-1L])
            if (q) {
                or {
                    ilike('name', "%${q}%")
                    ilike('email', "%${q}%")
                    ilike('phone', "%${q}%")
                    ilike('nid', "%${q}%")
                }
            }
            if (dept) eq('department.id', dept)
            if (branch) eq('branch.id', branch)
            if (statusFilter) eq('status', statusFilter)
            if (typeFilter) eq('employeeType', typeFilter)
            if (ordering) order(ordering.sort as String, ordering.order as String)
        }
    }

    // ======================================================================= profile
    @Secured(['isAuthenticated()'])
    def show(Long id) {
        Employee employee = id ? Employee.get(id) : null
        if (!employee) { notFound(); return }
        if (!accessService.canView(employee)) { redirect(controller: 'auth', action: 'denied'); return }

        boolean canSeeDocs = accessService.isHrAdmin() || accessService.currentEmployee()?.id == employee.id
        List documents = !canSeeDocs ? [] : EmployeeDocument.createCriteria().list {
            projections {
                property('id'); property('docType'); property('title'); property('fileName')
                property('fileSize'); property('expiryDate'); property('uploadedBy'); property('dateCreated')
            }
            eq('employee', employee)
            order('dateCreated', 'desc')
        }.collect { r ->
            [id: r[0], docType: r[1], title: r[2], fileName: r[3], fileSize: r[4], expiryDate: r[5],
             uploadedBy: r[6], dateCreated: r[7], expiryStatus: EmployeeDocument.expiryStatusOf(r[5] as LocalDate)]
        }

        boolean canEdit = accessService.isHrAdmin()
        Map model = [
            employee    : employee,
            canSeeSalary: accessService.canSeeSalary(employee),
            salaryStructure: accessService.canSeeSalary(employee) ? salaryService.currentStructure(employee) : null,
            canEdit     : canEdit,
            documents   : documents,
            canSeeDocs  : canSeeDocs,
            history     : EmploymentHistory.findAllByEmployee(employee, [sort: 'effectiveDate', order: 'desc']),
            attendance  : attendanceService.monthSummary(employee, YearMonth.now()),
            leaveBalances: leaveService.ensureBalances(employee, LocalDate.now().year).collect { leaveService.view(it) },
            leaveRequests: LeaveRequest.findAllByEmployee(employee, [sort: 'appliedAt', order: 'desc', max: 10]),
            docTypes    : EmployeeDocument.TYPES,
            eventTypes  : ['PROMOTION', 'TRANSFER', 'OTHER']
        ]
        if (canEdit) {
            model.departments = Department.list(sort: 'name')
            model.designations = Designation.list(sort: 'title')
            model.branches = Branch.list(sort: 'name')
        }
        model
    }

    @Secured(['isAuthenticated()'])
    def myProfile() {
        Employee me = accessService.currentEmployee()
        if (me) {
            redirect(action: 'show', id: me.id)
        } else {
            flash.warning = 'এই অ্যাকাউন্টের সাথে কোনো কর্মীর প্রোফাইল যুক্ত নেই।'
            redirect(controller: 'dashboard', action: 'index')
        }
    }

    @Secured(['isAuthenticated()'])
    def photo(Long id) {
        Employee employee = id ? Employee.get(id) : null
        if (!employee?.hasPhoto || !employee.photo) {
            response.sendError(404)
            return
        }
        response.contentType = employee.photoContentType ?: 'image/jpeg'
        response.setHeader('Cache-Control', 'private, max-age=300')
        response.outputStream << employee.photo
        response.outputStream.flush()
    }

    // ======================================================================= create / edit
    def create() {
        render(view: 'create', model: formModel(new Employee(joinDate: LocalDate.now())))
    }

    def save() {
        Employee employee = new Employee()
        bindEmployee(employee)
        String photoError = applyPhoto(employee)
        if (photoError) employee.errors.rejectValue('photo', 'employee.photo.invalid', photoError)

        if (employee.hasErrors() || !employee.save(flush: true)) {
            render(view: 'create', model: formModel(employee))
            return
        }
        employmentService.logJoined(employee, accessService.currentUser()?.username)
        flash.success = "কর্মী '${employee.name}' সফলভাবে যোগ করা হয়েছে।".toString()
        redirect(action: 'show', id: employee.id)
    }

    def edit(Long id) {
        Employee employee = id ? Employee.get(id) : null
        if (!employee) { notFound(); return }
        render(view: 'edit', model: formModel(employee))
    }

    def update(Long id) {
        Employee employee = id ? Employee.get(id) : null
        if (!employee) { notFound(); return }
        bindEmployee(employee)
        String photoError = applyPhoto(employee)
        if (photoError) employee.errors.rejectValue('photo', 'employee.photo.invalid', photoError)

        if (employee.hasErrors() || !employee.save(flush: true)) {
            render(view: 'edit', model: formModel(employee))
            return
        }
        flash.success = "'${employee.name}' এর তথ্য হালনাগাদ হয়েছে।".toString()
        redirect(action: 'show', id: employee.id)
    }

    @Secured(['ROLE_ADMIN'])
    def delete(Long id) {
        Employee employee = id ? Employee.get(id) : null
        if (!employee) { notFound(); return }
        if (User.countByEmployee(employee) > 0) {
            flash.error = 'এই কর্মীর একটি লগইন অ্যাকাউন্ট আছে, আগে সেটি সরান।'
            redirect(action: 'show', id: id)
            return
        }
        if (Employee.countByReportingManager(employee) > 0) {
            flash.error = 'এই কর্মীর অধীনে অন্য কর্মী আছে, আগে তাদের ম্যানেজার পরিবর্তন করুন।'
            redirect(action: 'show', id: id)
            return
        }
        if (PayrollLine.countByEmployee(employee) > 0) {
            flash.error = 'এই কর্মী পে-রোলের হিসাবে আছেন, তাই মোছা যাবে না। চাকরি শেষ হলে স্ট্যাটাস INACTIVE করুন।'
            redirect(action: 'show', id: id)
            return
        }
        if (Loan.countByEmployee(employee) > 0 || Bonus.countByEmployee(employee) > 0) {
            flash.error = 'এই কর্মীর ঋণ/বোনাসের রেকর্ড আছে, তাই মোছা যাবে না। চাকরি শেষ হলে স্ট্যাটাস INACTIVE করুন।'
            redirect(action: 'show', id: id)
            return
        }
        if (Interview.countByInterviewer(employee) > 0 || Candidate.countByEmployee(employee) > 0) {
            flash.error = 'এই কর্মী ইন্টারভিউয়ার হিসেবে আছেন বা নিয়োগের রেকর্ডে যুক্ত, তাই মোছা যাবে না। চাকরি শেষ হলে স্ট্যাটাস INACTIVE করুন।'
            redirect(action: 'show', id: id)
            return
        }
        String name = employee.name
        employmentService.deleteEmployee(employee)
        flash.success = "কর্মী '${name}' মুছে ফেলা হয়েছে।".toString()
        redirect(action: 'index')
    }

    // ======================================================================= promotion / transfer
    def recordChange(Long id) {
        Employee employee = id ? Employee.get(id) : null
        if (!employee) { notFound(); return }

        String error = null
        LocalDate effective = null
        BigDecimal newSalary = null
        try {
            effective = LocalDate.parse(params.effectiveDate as String)
            if (params.salary) newSalary = new BigDecimal(params.salary as String)
        } catch (Exception ignored) {
            error = 'তারিখ বা বেতনের মান সঠিক নয়।'
        }
        String eventType = EmploymentHistory.EVENT_TYPES.contains(params.eventType) ? params.eventType : 'OTHER'

        if (!error) {
            def history = employmentService.recordChange(employee, eventType, effective, [
                    department : params.long('departmentId') ? Department.get(params.long('departmentId')) : null,
                    designation: params.long('designationId') ? Designation.get(params.long('designationId')) : null,
                    branch     : params.long('branchId') ? Branch.get(params.long('branchId')) : null,
                    salary     : newSalary
            ], params.remarks?.toString()?.trim() ?: null, accessService.currentUser()?.username)
            if (history) flash.success = 'পরিবর্তন রেকর্ড করা হয়েছে এবং প্রোফাইল হালনাগাদ হয়েছে।'
            else error = 'কোনো পরিবর্তন পাওয়া যায়নি - নতুন মান বর্তমানের মতোই।'
        }
        if (error) flash.error = error
        redirect(action: 'show', id: employee.id, fragment: 'tab-history')
    }

    // ======================================================================= CSV export / import
    def exportCsv() {
        List<Employee> list = Employee.createCriteria().list(filterClosure(accessService.visibleEmployeeIds(), [sort: 'name', order: 'asc']))
        response.characterEncoding = 'UTF-8'
        response.contentType = 'text/csv; charset=UTF-8'
        response.setHeader('Content-Disposition', 'attachment; filename="employees.csv"')
        Writer w = response.writer
        w << CsvUtil.BOM
        w << CsvUtil.line(CSV_COLUMNS) << '\r\n'
        list.each { Employee e ->
            w << CsvUtil.line([e.name, e.email, e.phone, e.joinDate, e.salary, e.department?.name, e.designation?.title,
                               e.branch?.name, e.grade?.name, e.status, e.employeeType, e.dateOfBirth, e.gender, e.nid]) << '\r\n'
        }
        w.flush()
    }

    def importTemplate() {
        response.characterEncoding = 'UTF-8'
        response.contentType = 'text/csv; charset=UTF-8'
        response.setHeader('Content-Disposition', 'attachment; filename="employee_import_template.csv"')
        Writer w = response.writer
        w << CsvUtil.BOM
        w << CsvUtil.line(CSV_COLUMNS) << '\r\n'
        w << CsvUtil.line(['Sample Name', 'sample@example.com', '01700000000', '2026-01-15', '45000', 'IT', 'Software Engineer',
                           'Head Office', 'G2', 'ACTIVE', 'FULL_TIME', '1996-04-20', 'MALE', '']) << '\r\n'
        w.flush()
    }

    def importForm() {
        [result: null]
    }

    def importCsv() {
        MultipartFile file = request.getFile('file')
        if (!file || file.empty) {
            flash.error = 'CSV ফাইল নির্বাচন করুন।'
            redirect(action: 'importForm')
            return
        }
        List<List<String>> rows = CsvUtil.parse(new String(file.bytes, 'UTF-8'))
        if (rows.size() < 2) {
            flash.error = 'ফাইলে কোনো ডেটা নেই (প্রথম লাইন হেডার, তারপর কর্মীর লাইন লাগবে)।'
            redirect(action: 'importForm')
            return
        }
        List<String> header = rows[0].collect { it.trim().toLowerCase() }
        List<String> missing = ['name', 'email', 'joindate', 'salary', 'department', 'designation'].findAll { !header.contains(it) }
        if (missing) {
            flash.error = "হেডারে এই কলাম নেই: ${missing.join(', ')}".toString()
            redirect(action: 'importForm')
            return
        }

        String by = accessService.currentUser()?.username
        int imported = 0
        List<Map> errors = []
        rows.drop(1).eachWithIndex { List<String> row, int idx ->
            Map values = [:]
            header.eachWithIndex { String h, int i -> values[h] = i < row.size() ? row[i]?.trim() : null }
            String error = importRow(values, by)
            if (error) errors << [line: idx + 2, email: values.email, message: error]
            else imported++
        }
        render(view: 'importForm', model: [result: [total: rows.size() - 1, imported: imported, failed: errors.size(), errors: errors]])
    }

    /** Returns null on success, otherwise a message describing why the row was skipped. */
    private String importRow(Map v, String by) {
        try {
            Department dept = v.department ? Department.findByName(v.department as String) : null
            if (!dept) return "বিভাগ '${v.department}' পাওয়া যায়নি"
            Designation desig = v.designation ? Designation.findByTitle(v.designation as String) : null
            if (!desig) return "পদবি '${v.designation}' পাওয়া যায়নি"
            Branch branch = v.branch ? Branch.findByName(v.branch as String) : null
            if (v.branch && !branch) return "শাখা '${v.branch}' পাওয়া যায়নি"
            Grade grade = v.grade ? Grade.findByName(v.grade as String) : null
            if (v.grade && !grade) return "গ্রেড '${v.grade}' পাওয়া যায়নি"

            Employee e = new Employee(
                    name: v.name, email: v.email, phone: v.phone ?: null,
                    joinDate: LocalDate.parse(v.joindate as String), salary: new BigDecimal(v.salary as String),
                    department: dept, designation: desig, branch: branch, grade: grade,
                    status: (v.status ?: 'ACTIVE').toString().toUpperCase(),
                    employeeType: (v.employeetype ?: 'FULL_TIME').toString().toUpperCase(),
                    dateOfBirth: v.dateofbirth ? LocalDate.parse(v.dateofbirth as String) : null,
                    gender: v.gender ? v.gender.toString().toUpperCase() : null,
                    nid: v.nid ?: null)
            if (!e.save(flush: true)) {
                return e.errors.allErrors.collect { g.message(error: it) }.join('; ')
            }
            employmentService.logJoined(e, by)
            return null
        } catch (DateTimeParseException ignored) {
            return 'তারিখের ফরম্যাট yyyy-MM-dd হতে হবে (যেমন 2026-01-15)'
        } catch (NumberFormatException ignored) {
            return 'বেতন সংখ্যা হতে হবে'
        } catch (Exception ex) {
            return ex.message ?: ex.class.simpleName
        }
    }

    // ======================================================================= helpers
    private void bindEmployee(Employee employee) {
        // photo fields are handled separately (applyPhoto); never bind them from params
        bindData(employee, params, [exclude: ['photo', 'photoContentType', 'hasPhoto', 'id', 'version']])
    }

    /** Returns an error message, or null if the upload is fine / absent. */
    private String applyPhoto(Employee employee) {
        if (params.removePhoto == 'on') {
            employee.photo = null
            employee.photoContentType = null
            employee.hasPhoto = false
        }
        MultipartFile file = request.getFile('photoFile')
        if (!file || file.empty) return null
        if (!Employee.PHOTO_TYPES.contains(file.contentType)) return 'ছবি শুধু JPG, PNG বা WEBP হতে পারে।'
        if (file.size > Employee.PHOTO_MAX_BYTES) return 'ছবির সাইজ সর্বোচ্চ ১ MB হতে পারে।'
        employee.photo = file.bytes
        employee.photoContentType = file.contentType
        employee.hasPhoto = true
        null
    }

    private Map formModel(Employee employee) {
        [
            employee    : employee,
            departments : Department.list(sort: 'name'),
            designations: Designation.list(sort: 'title'),
            branches    : Branch.list(sort: 'name'),
            grades      : Grade.list(sort: 'gradeLevel'),
            costCenters : CostCenter.list(sort: 'code'),
            managers    : Employee.list(sort: 'name'),
            genders     : Employee.GENDERS,
            types       : Employee.TYPES,
            statuses    : Employee.STATUSES
        ]
    }

    private void notFound() {
        flash.error = 'কর্মী পাওয়া যায়নি।'
        redirect(action: 'index')
    }
}
package hrms

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.web.multipart.MultipartFile

@Secured(['ROLE_ADMIN', 'ROLE_HR'])
class EmployeeController {

    static allowedMethods = [save: 'POST', update: 'POST', delete: 'POST']

    static final List<String> SORTABLE = ['name', 'email', 'joinDate', 'status', 'employeeType']

    def springSecurityService

    @Secured(['ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER'])
    def index() {
        int max = Math.min(params.int('max') ?: 10, 100)
        String sort = SORTABLE.contains(params.sort) ? params.sort : 'name'
        String order = params.order == 'desc' ? 'desc' : 'asc'
        [
            employeeList : Employee.list(max: max, offset: params.int('offset') ?: 0, sort: sort, order: order),
            employeeCount: Employee.count()
        ]
    }

    @Secured(['isAuthenticated()'])
    def show(Long id) {
        Employee employee = id ? Employee.get(id) : null
        if (!employee) { notFound(); return }
        if (!canView(employee)) { redirect(controller: 'auth', action: 'denied'); return }
        [employee: employee, canSeeSalary: canSeeSalary(employee), canEdit: canEdit()]
    }

    @Secured(['isAuthenticated()'])
    def myProfile() {
        User user = springSecurityService.currentUser as User
        if (user?.employee) {
            redirect(action: 'show', id: user.employee.id)
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

    def create() {
        respondForm(new Employee(joinDate: java.time.LocalDate.now()))
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
        flash.success = "কর্মী '${employee.name}' সফলভাবে যোগ করা হয়েছে।".toString()
        redirect(action: 'show', id: employee.id)
    }

    def edit(Long id) {
        Employee employee = id ? Employee.get(id) : null
        if (!employee) { notFound(); return }
        respondForm(employee)
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
        String name = employee.name
        employee.delete(flush: true)
        flash.success = "কর্মী '${name}' মুছে ফেলা হয়েছে।".toString()
        redirect(action: 'index')
    }

    private void bindEmployee(Employee employee) {
        bindData(employee, params, [exclude: ['photo', 'photoContentType', 'hasPhoto', 'id', 'version']])
    }

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
            genders     : Employee.GENDERS,
            types       : Employee.TYPES,
            statuses    : Employee.STATUSES
        ]
    }

    private void respondForm(Employee employee) {
        render(view: actionName, model: formModel(employee))
    }

    private boolean canEdit() {
        SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_HR')
    }

    private boolean isOwn(Employee employee) {
        User user = springSecurityService.currentUser as User
        user?.employee?.id == employee.id
    }

    private boolean canView(Employee employee) {
        SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_HR,ROLE_MANAGER,ROLE_ACCOUNTANT') || isOwn(employee)
    }

    private boolean canSeeSalary(Employee employee) {
        SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_HR,ROLE_ACCOUNTANT') || isOwn(employee)
    }

    private void notFound() {
        flash.error = 'কর্মী পাওয়া যায়নি।'
        redirect(action: 'index')
    }
}
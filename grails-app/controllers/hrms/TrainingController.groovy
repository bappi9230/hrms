package hrms

import grails.plugin.springsecurity.annotation.Secured
import org.springframework.beans.factory.annotation.Value

import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Day 8: প্রশিক্ষণ ক্যাটালগ (কার্ড), নথিভুক্তি, ফল ও সার্টিফিকেট */
@Secured(['isAuthenticated()'])
class TrainingController {

    static allowedMethods = [save: 'POST', update: 'POST', cancelCourse: 'POST', deleteCourse: 'POST',
                             enroll: 'POST', cancelEnrollment: 'POST', result: 'POST']

    @Value('${hrms.company.name:HRMS Demo Ltd.}')
    String companyName

    def accessService
    def trainingService

    private static LocalDate today() { LocalDate.now(AttendanceService.ZONE) }

    def index() {
        String category = Course.CATEGORIES.contains(params.category) ? params.category : null
        String when = ['upcoming', 'ended', 'all'].contains(params.when) ? params.when : 'upcoming'
        LocalDate t = today()
        List<Course> courses = Course.createCriteria().list {
            if (category) eq('category', category)
            if (when == 'upcoming') ge('endDate', t)
            else if (when == 'ended') lt('endDate', t)
            order('startDate', when == 'ended' ? 'desc' : 'asc')
        } as List<Course>
        Employee me = accessService.currentEmployee()
        Map<Long, TrainingEnrollment> mine = [:]
        if (me) TrainingEnrollment.findAllByEmployee(me).each { TrainingEnrollment e -> mine[e.course.id] = e }
        [courses: courses, categories: Course.CATEGORIES, category: category, when: when, today: t, mine: mine, me: me,
         seatsLeft: courses.collectEntries { Course c -> [(c.id): trainingService.seatsLeft(c)] },
         taken: courses.collectEntries { Course c -> [(c.id): trainingService.seatsTaken(c)] }]
    }

    def my() {
        Employee me = accessService.currentEmployee()
        List<TrainingEnrollment> list = me ? TrainingEnrollment.findAllByEmployee(me, [sort: 'id', order: 'desc']) : []
        [enrollments: list, today: today()]
    }

    def show(Long id) {
        Course c = id ? Course.get(id) : null
        if (!c) { notFound(); return }
        boolean hr = accessService.isHrAdmin()
        Employee me = accessService.currentEmployee()
        List<TrainingEnrollment> list = hr ? TrainingEnrollment.findAllByCourse(c, [sort: 'id'])
                : (me ? TrainingEnrollment.findAllByCourseAndEmployee(c, me) : [])
        TrainingEnrollment mine = me ? TrainingEnrollment.findByCourseAndEmployee(c, me) : null
        List<Employee> candidates = []
        if (hr) {
            Set<Long> taken = TrainingEnrollment.findAllByCourseAndStatusInList(c, ['ENROLLED', 'COMPLETED']).collect { it.employee.id } as Set<Long>
            candidates = Employee.findAllByStatusNotEqual('INACTIVE', [sort: 'name']).findAll { !taken.contains(it.id) }
        }
        [course: c, enrollments: list, mine: mine, hr: hr, candidates: candidates, today: today(),
         seatsLeft: trainingService.seatsLeft(c), taken: trainingService.seatsTaken(c)]
    }

    // ------------------------------------------------------------------ কোর্স তৈরি/সম্পাদনা (HR/Admin)
    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def create() {
        render(view: 'form', model: formModel(new Course(startDate: today().plusDays(7), endDate: today().plusDays(7))))
    }

    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def save() {
        Course c = new Course()
        bind(c)
        Map r = trainingService.saveCourse(c)
        if (r.ok) {
            flash.success = r.message
            redirect(action: 'show', id: c.id)
        } else {
            flash.error = r.message
            render(view: 'form', model: formModel(c))
        }
    }

    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def edit(Long id) {
        Course c = id ? Course.get(id) : null
        if (!c) { notFound(); return }
        render(view: 'form', model: formModel(c))
    }

    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def update(Long id) {
        Course c = id ? Course.get(id) : null
        if (!c) { notFound(); return }
        bind(c)
        Map r = trainingService.saveCourse(c)
        if (r.ok) {
            flash.success = r.message
            redirect(action: 'show', id: c.id)
        } else {
            flash.error = r.message
            c.discard()          // অসম্পূর্ণ বদল সেশনে থেকে যেন না যায়
            render(view: 'form', model: formModel(c))
        }
    }

    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def cancelCourse(Long id) { courseAction(id) { Course c -> trainingService.cancelCourse(c) } }

    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def deleteCourse(Long id) {
        Course c = id ? Course.get(id) : null
        if (!c) { notFound(); return }
        Map r = trainingService.deleteCourse(c)
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: r.ok ? 'index' : 'show', id: r.ok ? null : id)
    }

    private void courseAction(Long id, Closure<Map> action) {
        Course c = id ? Course.get(id) : null
        if (!c) { notFound(); return }
        Map r = action.call(c)
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'show', id: id)
    }

    // ------------------------------------------------------------------ নথিভুক্তি
    /** id = কোর্স; HR/Admin হলে employee প্যারামিটারে অন্য কর্মী, নইলে নিজে */
    def enroll(Long id) {
        Course c = id ? Course.get(id) : null
        if (!c) { notFound(); return }
        Employee emp = params.long('employee') ? Employee.get(params.long('employee')) : accessService.currentEmployee()
        Map r = trainingService.enroll(c, emp)
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: params.back == 'index' ? 'index' : 'show', id: params.back == 'index' ? null : id)
    }

    /** id = নথিভুক্তি */
    def cancelEnrollment(Long id) {
        TrainingEnrollment e = id ? TrainingEnrollment.get(id) : null
        if (!e) { notFound(); return }
        Map r = trainingService.cancelEnrollment(e)
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: params.back == 'my' ? 'my' : 'show', id: params.back == 'my' ? null : e.course.id)
    }

    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def result(Long id) {
        TrainingEnrollment e = id ? TrainingEnrollment.get(id) : null
        if (!e) { notFound(); return }
        Integer score = params.score?.toString()?.trim() ? params.int('score') : null
        Map r = trainingService.recordResult(e, params.result as String, score)
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'show', id: e.course.id)
    }

    /** প্রিন্টযোগ্য সার্টিফিকেট (id = নথিভুক্তি) */
    def certificate(Long id) {
        TrainingEnrollment e = id ? TrainingEnrollment.get(id) : null
        if (!e || !trainingService.canSeeCertificate(e)) {
            flash.error = 'সার্টিফিকেট পাওয়া যায়নি।'
            redirect(action: 'my')
            return
        }
        [enrollment: e, company: companyName]
    }

    // ------------------------------------------------------------------ helpers
    private void bind(Course c) {
        c.title = params.title?.toString()?.trim()
        c.description = params.description?.toString()?.trim() ?: null
        c.category = params.category
        c.trainer = params.trainer?.toString()?.trim() ?: null
        c.trainingMode = params.trainingMode
        c.location = params.location?.toString()?.trim() ?: null
        c.startDate = parseDate(params.startDate as String)
        c.endDate = parseDate(params.endDate as String)
        c.hours = params.int('hours')
        c.capacity = params.int('capacity') ?: 0
    }

    private Map formModel(Course c) {
        [course: c, categories: Course.CATEGORIES, modes: Course.MODES]
    }

    private static LocalDate parseDate(String s) {
        try { s ? LocalDate.parse(s) : null } catch (DateTimeParseException ignored) { null }
    }

    private void notFound() {
        flash.error = 'কোর্স পাওয়া যায়নি।'
        redirect(action: 'index')
    }
}

package hrms

import grails.plugin.springsecurity.annotation.Secured
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.multipart.MultipartFile

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

/** Day 7: প্রার্থী, ইন্টারভিউ, অফার ও কর্মীতে রূপান্তর */
@Secured(['ROLE_ADMIN', 'ROLE_HR'])
class CandidateController {

    static allowedMethods = [save: 'POST', reject: 'POST', reopen: 'POST', scheduleInterview: 'POST', cancelInterview: 'POST',
                             feedback: 'POST', saveOffer: 'POST', respondOffer: 'POST', withdrawOffer: 'POST', convert: 'POST']

    def accessService
    def recruitmentService

    @Value('${hrms.company.name:HRMS Demo Ltd.}')
    String companyName

    // ------------------------------------------------------------------ প্রার্থী যোগ
    def create() {
        JobPosting p = params.long('posting') ? JobPosting.get(params.long('posting')) : null
        render(view: 'create', model: formModel(new Candidate(jobPosting: p)))
    }

    def save() {
        Candidate c = new Candidate(
                name: params.name?.toString()?.trim(), email: params.email?.toString()?.trim(), phone: params.phone?.toString()?.trim() ?: null,
                jobPosting: params.long('jobPosting') ? JobPosting.get(params.long('jobPosting')) : null,
                source: params.source ?: 'WEBSITE', expectedSalary: decimal(params.expectedSalary),
                experienceYears: decimal(params.experienceYears), note: params.note?.toString()?.trim() ?: null)
        Map res = recruitmentService.addCandidate(c, request.getFile('resume') as MultipartFile)
        if (res.ok) {
            flash.success = res.message
            redirect(action: 'show', id: c.id)
        } else {
            flash.error = res.message
            render(view: 'create', model: formModel(c))
        }
    }

    // ------------------------------------------------------------------ প্রোফাইল
    def show(Long id) {
        Candidate c = id ? Candidate.get(id) : null
        if (!c) { notFound(); return }
        render(view: 'show', model: showModel(c))
    }

    private Map showModel(Candidate c) {
        List<Interview> interviews = Interview.findAllByCandidate(c, [sort: 'roundNo'])
        JobOffer offer = recruitmentService.currentOffer(c)
        [candidate: c, interviews: interviews, offer: offer, offers: JobOffer.findAllByCandidate(c, [sort: 'id', order: 'desc']),
         requisition: c.jobPosting.requisition,
         recommended: Interview.countByCandidateAndStatusAndRecommendationInList(c, 'COMPLETED', ['HIRE', 'HOLD']) > 0,
         employees: Employee.findAllByStatusNotEqual('INACTIVE', [sort: 'name']), grades: Grade.list(sort: 'gradeLevel'),
         stages: Candidate.MOVABLE_STAGES, modes: Interview.MODES, recommendations: Interview.RECOMMENDATIONS,
         defaultInterviewAt: LocalDateTime.now(AttendanceService.ZONE).plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0).toString().substring(0, 16),
         defaultRoundName: interviews.any { it.status != 'CANCELLED' } ? 'Technical / Final' : 'HR / Screening']
    }

    @Secured(['ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER'])
    def resume(Long id) {
        Candidate c = id ? Candidate.get(id) : null
        CandidateResume cv = c ? CandidateResume.findByCandidate(c) : null
        if (!cv) { response.sendError(404); return }
        Employee me = accessService.currentEmployee()
        boolean interviewer = me != null && Interview.countByCandidateAndInterviewer(c, me) > 0
        if (!accessService.isHrAdmin() && !interviewer) { redirect(controller: 'auth', action: 'denied'); return }
        response.contentType = cv.contentType ?: 'application/octet-stream'
        response.setHeader('Content-Disposition', "attachment; filename*=UTF-8''" + URLEncoder.encode(cv.fileName, 'UTF-8').replace('+', '%20'))
        response.setHeader('X-Content-Type-Options', 'nosniff')
        response.outputStream << cv.fileData
        response.outputStream.flush()
    }

    def reject(Long id) { act(id) { Candidate c -> recruitmentService.rejectCandidate(c, params.reason as String) } }

    def reopen(Long id) { act(id) { Candidate c -> recruitmentService.reopenCandidate(c) } }

    // ------------------------------------------------------------------ ইন্টারভিউ
    def scheduleInterview(Long id) {
        Candidate c = id ? Candidate.get(id) : null
        if (!c) { notFound(); return }
        Interview iv = new Interview(candidate: c, roundName: params.roundName?.toString()?.trim(), scheduledAt: parseDateTime(params.scheduledAt as String),
                durationMinutes: params.int('durationMinutes'), mode: params.mode ?: 'ONSITE', location: params.location?.toString()?.trim() ?: null,
                interviewer: params.long('interviewer') ? Employee.get(params.long('interviewer')) : null)
        Map res = recruitmentService.scheduleInterview(iv)
        if (res.ok) {
            flash.success = res.message
        } else {
            flash.error = iv.errors.allErrors ? res.message + ' ' + iv.errors.allErrors.collect { g.message(error: it) }.join(' ') : res.message
        }
        redirect(action: 'show', id: id, fragment: 'interviews')
    }

    def cancelInterview(Long id) {
        Interview iv = id ? Interview.get(id) : null
        if (!iv) { notFound(); return }
        Map res = recruitmentService.cancelInterview(iv)
        flash[res.ok ? 'success' : 'error'] = res.message
        redirect(action: 'show', id: iv.candidate.id, fragment: 'interviews')
    }

    /** ইন্টারভিউয়ার নিজে বা HR/Admin - তাই ROLE_MANAGER ও চলে; আসল যাচাই service এ */
    @Secured(['ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER'])
    def feedback(Long id) {
        Interview iv = id ? Interview.get(id) : null
        if (!iv) { notFound(); return }
        Map res = recruitmentService.giveFeedback(iv, params.int('rating'), params.recommendation as String, params.feedback as String)
        flash[res.ok ? 'success' : 'error'] = res.message
        if (params.back == 'mine') redirect(action: 'myInterviews')
        else redirect(action: 'show', id: iv.candidate.id, fragment: 'interviews')
    }

    /** আমার (ইন্টারভিউয়ার হিসেবে) ইন্টারভিউ */
    @Secured(['ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER'])
    def myInterviews() {
        Employee me = accessService.currentEmployee()
        List<Interview> list = me ? Interview.findAllByInterviewer(me, [sort: 'scheduledAt', order: 'desc']) : []
        [interviews: list, now: LocalDateTime.now(AttendanceService.ZONE)]
    }

    // ------------------------------------------------------------------ অফার
    def createOffer(Long id) {
        Candidate c = id ? Candidate.get(id) : null
        if (!c) { notFound(); return }
        ManpowerRequisition r = c.jobPosting.requisition
        LocalDate today = LocalDate.now(AttendanceService.ZONE)
        JobOffer o = new JobOffer(candidate: c, department: r.department, designation: r.designation, branch: r.branch,
                employeeType: r.employeeType == 'PROBATION' ? 'FULL_TIME' : r.employeeType,
                salary: c.expectedSalary ?: r.maxSalary ?: r.minSalary, joinDate: r.expectedJoinDate && !r.expectedJoinDate.isBefore(today) ? r.expectedJoinDate : today.plusDays(30),
                validUntil: today.plusDays(7), probationMonths: 3)
        render(view: 'createOffer', model: offerModel(o))
    }

    def saveOffer(Long id) {
        Candidate c = id ? Candidate.get(id) : null
        if (!c) { notFound(); return }
        JobOffer o = new JobOffer(candidate: c,
                department: params.long('department') ? Department.get(params.long('department')) : null,
                designation: params.long('designation') ? Designation.get(params.long('designation')) : null,
                branch: params.long('branch') ? Branch.get(params.long('branch')) : null,
                employeeType: params.employeeType ?: 'FULL_TIME', salary: decimal(params.salary),
                joinDate: parseDate(params.joinDate as String), probationMonths: params.int('probationMonths'),
                validUntil: parseDate(params.validUntil as String), terms: params.terms?.toString()?.trim() ?: null)
        Map res = recruitmentService.saveOffer(o)
        if (res.ok) {
            flash.success = res.message
            redirect(action: 'show', id: id)
        } else {
            flash.error = res.message
            render(view: 'createOffer', model: offerModel(o))
        }
    }

    def respondOffer(Long id) {
        JobOffer o = id ? JobOffer.get(id) : null
        if (!o) { notFound(); return }
        Map res = recruitmentService.respondOffer(o, params.decision == 'ACCEPT', params.note as String)
        flash[res.ok ? 'success' : 'error'] = res.message
        redirect(action: 'show', id: o.candidate.id)
    }

    def withdrawOffer(Long id) {
        JobOffer o = id ? JobOffer.get(id) : null
        if (!o) { notFound(); return }
        Map res = recruitmentService.withdrawOffer(o, params.note as String)
        flash[res.ok ? 'success' : 'error'] = res.message
        redirect(action: 'show', id: o.candidate.id)
    }

    /** ছাপার উপযোগী অফার লেটার */
    def offerLetter(Long id) {
        JobOffer o = id ? JobOffer.get(id) : null
        if (!o) { notFound(); return }
        [offer: o, candidate: o.candidate, requisition: o.candidate.jobPosting.requisition,
         company: companyName]
    }

    // ------------------------------------------------------------------ রূপান্তর
    def convert(Long id) {
        Candidate c = id ? Candidate.get(id) : null
        if (!c) { notFound(); return }
        Employee mgr = params.long('manager') ? Employee.get(params.long('manager')) : null
        Grade grade = params.long('grade') ? Grade.get(params.long('grade')) : null
        Map res = recruitmentService.convertToEmployee(c, mgr, grade)
        if (res.ok) {
            flash.success = res.message
            redirect(controller: 'onboarding', action: 'show', id: (res.employee as Employee).id)
        } else {
            flash.error = res.message
            redirect(action: 'show', id: id)
        }
    }

    // ------------------------------------------------------------------ helpers
    private void act(Long id, Closure<Map> action) {
        Candidate c = id ? Candidate.get(id) : null
        if (!c) { notFound(); return }
        Map res = action.call(c)
        flash[res.ok ? 'success' : 'error'] = res.message
        redirect(action: 'show', id: id)
    }

    private Map formModel(Candidate c) {
        [candidate: c, postings: JobPosting.findAllByStatus('OPEN', [sort: 'id', order: 'desc']).findAll { it.acceptingApplications }, sources: Candidate.SOURCES]
    }

    private Map offerModel(JobOffer o) {
        [offer: o, candidate: o.candidate, departments: Department.list(sort: 'name'), designations: Designation.list(sort: 'title'),
         branches: Branch.list(sort: 'name'), types: Employee.TYPES - ['PROBATION']]
    }

    private static BigDecimal decimal(def v) {
        try { v?.toString()?.trim() ? new BigDecimal(v.toString().trim()) : null } catch (NumberFormatException ignored) { null }
    }

    private static LocalDate parseDate(String s) {
        try { s ? LocalDate.parse(s) : null } catch (DateTimeParseException ignored) { null }
    }

    /** <input type="datetime-local"> এর মান: 2026-11-05T10:30 */
    private static LocalDateTime parseDateTime(String s) {
        try { s ? LocalDateTime.parse(s) : null } catch (DateTimeParseException ignored) { null }
    }

    private void notFound() {
        flash.error = 'তথ্য পাওয়া যায়নি।'
        redirect(controller: 'recruitment', action: 'index')
    }
}

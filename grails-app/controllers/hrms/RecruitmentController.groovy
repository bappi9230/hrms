package hrms

import grails.plugin.springsecurity.annotation.Secured
import groovy.json.JsonOutput

import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Day 7: নিয়োগ - Kanban বোর্ড ও চাকরির বিজ্ঞপ্তি */
@Secured(['ROLE_ADMIN', 'ROLE_HR'])
class RecruitmentController {

    static allowedMethods = [savePosting: 'POST', closePosting: 'POST', reopenPosting: 'POST', move: 'POST']

    def recruitmentService

    // ------------------------------------------------------------------ Kanban বোর্ড
    def index() {
        JobPosting posting = params.long('posting') ? JobPosting.get(params.long('posting')) : null
        Map data = recruitmentService.boardData(posting)
        [board: data, posting: posting, postings: JobPosting.list(sort: 'id', order: 'desc'),
         stages: Candidate.BOARD_STAGES, overview: recruitmentService.overview()]
    }

    /** ড্র্যাগ করে ধাপ বদল (AJAX → JSON) অথবা বাটন থেকে (→ redirect) */
    def move(Long id) {
        Candidate c = id ? Candidate.get(id) : null
        Map res = c ? recruitmentService.moveStage(c, params.stage as String) : [ok: false, message: 'প্রার্থী পাওয়া যায়নি।']
        if (params.ajax) {
            render(contentType: 'application/json', text: JsonOutput.toJson([ok: res.ok, message: res.message]))
            return
        }
        flash[res.ok ? 'success' : 'error'] = res.message
        redirect(controller: 'candidate', action: 'show', id: id)
    }

    // ------------------------------------------------------------------ চাকরির বিজ্ঞপ্তি
    def postings() {
        List<JobPosting> list = JobPosting.list(sort: 'id', order: 'desc')
        Map<Long, Integer> counts = [:]
        Candidate.executeQuery('select c.jobPosting.id, count(c) from Candidate c group by c.jobPosting.id').each { counts[it[0] as Long] = (it[1] as Number).intValue() }
        [postings: list, counts: counts]
    }

    def createPosting() {
        ManpowerRequisition r = params.long('requisition') ? ManpowerRequisition.get(params.long('requisition')) : null
        render(view: 'createPosting', model: postingModel(new JobPosting(requisition: r, title: r?.title,
                deadline: LocalDate.now(AttendanceService.ZONE).plusDays(30))))
    }

    def savePosting() {
        JobPosting p = new JobPosting(
                requisition: params.long('requisition') ? ManpowerRequisition.get(params.long('requisition')) : null,
                title: params.title?.toString()?.trim(), description: params.description?.toString()?.trim(),
                requirements: params.requirements?.toString()?.trim() ?: null, location: params.location?.toString()?.trim() ?: null,
                deadline: parseDate(params.deadline as String))
        Map res = recruitmentService.savePosting(p)
        if (res.ok) {
            flash.success = res.message
            redirect(action: 'postings')
        } else {
            flash.error = res.message
            render(view: 'createPosting', model: postingModel(p))
        }
    }

    def closePosting(Long id) { postingAction(id) { JobPosting p -> recruitmentService.closePosting(p) } }

    def reopenPosting(Long id) { postingAction(id) { JobPosting p -> recruitmentService.reopenPosting(p) } }

    private void postingAction(Long id, Closure<Map> action) {
        JobPosting p = id ? JobPosting.get(id) : null
        if (!p) {
            flash.error = 'বিজ্ঞপ্তি পাওয়া যায়নি।'
        } else {
            Map res = action.call(p)
            flash[res.ok ? 'success' : 'error'] = res.message
        }
        redirect(action: 'postings')
    }

    private Map postingModel(JobPosting p) {
        // শুধু অনুমোদিত ও পদ খালি আছে এমন চাহিদা
        List<ManpowerRequisition> open = ManpowerRequisition.findAllByStatus('APPROVED', [sort: 'id', order: 'desc']).findAll { it.remaining() > 0 }
        [posting: p, requisitions: open]
    }

    private static LocalDate parseDate(String s) {
        try { s ? LocalDate.parse(s) : null } catch (DateTimeParseException ignored) { null }
    }
}

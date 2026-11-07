package hrms

import grails.plugin.springsecurity.annotation.Secured

/** Day 9: কর্মীর অভিযোগ - জমা, আলোচনা, HR এর সমাধান */
@Secured(['isAuthenticated()'])
class GrievanceController {

    static allowedMethods = [save: 'POST', note: 'POST', update: 'POST']

    def accessService
    def relationsService

    def index() {
        String status = Grievance.STATUSES.contains(params.status) ? params.status : null
        [grievances: relationsService.visibleGrievances(status), status: status, hasEmployee: accessService.currentEmployee() != null, isHr: accessService.isHrAdmin()]
    }

    def create() {
        if (!accessService.currentEmployee()) { noProfile(); return }
        [categories: Grievance.CATEGORIES, form: [:]]
    }

    def save() {
        Map r = relationsService.submitGrievance(accessService.currentEmployee(), params.category as String, params.subject as String, params.description as String)
        flash[r.ok ? 'success' : 'error'] = r.message
        if (r.ok) {
            redirect(action: 'show', id: (r.grievance as Grievance).id)
        } else {
            render(view: 'create', model: [categories: Grievance.CATEGORIES, form: params])
        }
    }

    def show(Long id) {
        Grievance g = id ? Grievance.get(id) : null
        if (!g || !relationsService.canSeeGrievance(g)) { notFound(); return }
        boolean own = accessService.currentEmployee()?.id == g.employee.id
        boolean hr = accessService.isHrAdmin() && !own
        [grv: g, own: own, hr: hr, notes: relationsService.notesOf(g), open: !(g.status in ['RESOLVED', 'REJECTED'])]
    }

    def note(Long id) { act(id) { Grievance g -> relationsService.addGrievanceNote(g, params.note as String) } }

    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def update(Long id) { act(id) { Grievance g -> relationsService.updateGrievance(g, params.target as String, params.resolution as String) } }

    private void act(Long id, Closure<Map> action) {
        Grievance g = id ? Grievance.get(id) : null
        if (!g || !relationsService.canSeeGrievance(g)) { notFound(); return }
        Map r = action.call(g)
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'show', id: id)
    }

    private void noProfile() {
        flash.error = 'আপনার লগইনের সাথে কোনো কর্মী প্রোফাইল যুক্ত নেই, তাই অভিযোগ জমা দেওয়া যাবে না।'
        redirect(action: 'index')
    }

    private void notFound() {
        flash.error = 'অভিযোগ পাওয়া যায়নি বা দেখার অনুমতি নেই।'
        redirect(action: 'index')
    }
}

package hrms

import grails.plugin.springsecurity.annotation.Secured

import java.time.YearMonth

/** Day 6: উৎসব ও পারফরম্যান্স বোনাস - পে-মাস অনুযায়ী তালিকা, সবাইকে উৎসব বোনাস তৈরি, কর্মী ধরে যোগ, বাতিল */
@Secured(['ROLE_ADMIN', 'ROLE_ACCOUNTANT'])
class BonusController {

    static allowedMethods = [generateFestival: 'POST', add: 'POST', cancel: 'POST']

    def bonusService

    def index() {
        YearMonth now = YearMonth.now(AttendanceService.ZONE)
        Integer year = params.int('year') ?: now.year
        Integer month = params.int('month') ?: now.monthValue
        List<Bonus> bonuses = Bonus.findAll("from Bonus b where b.payYear = :y and b.payMonth = :m order by b.employee.name, b.id", [y: year, m: month])
        [bonuses: bonuses, year: year, month: month,
         total: bonuses.findAll { it.status != 'CANCELLED' }.sum(0.0) { it.amount },
         employees: Employee.findAllByStatusNotEqual('INACTIVE', [sort: 'name']),
         locked: PayrollRun.findByPayYearAndPayMonth(year, month)?.status == 'FINALIZED',
         minMonths: BonusService.MIN_SERVICE_MONTHS]
    }

    def generateFestival() {
        Map r = bonusService.generateFestival(params.title as String, params.int('year'), params.int('month'), decimal(params.percent))
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'index', params: [year: params.year, month: params.month])
    }

    def add() {
        Employee emp = params.long('employee') ? Employee.get(params.long('employee')) : null
        Map r = bonusService.add(emp, params.bonusType as String, params.title as String, params.int('year'), params.int('month'),
                decimal(params.amount), params.note as String)
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'index', params: [year: params.year, month: params.month])
    }

    def cancel(Long id) {
        Bonus b = id ? Bonus.get(id) : null
        if (!b) {
            flash.error = 'বোনাস পাওয়া যায়নি।'
            redirect(action: 'index')
            return
        }
        Map r = bonusService.cancel(b)
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'index', params: [year: b.payYear, month: b.payMonth])
    }

    private static BigDecimal decimal(def v) {
        try {
            v?.toString()?.trim() ? new BigDecimal(v.toString().trim()) : null
        } catch (NumberFormatException ignored) {
            null
        }
    }
}

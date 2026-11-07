package hrms

import grails.gorm.transactions.Transactional

/**
 * Day 9: "অনুমোদন ইনবক্স" ও পোর্টালের "আমার সিদ্ধান্তের অপেক্ষায়" তালিকা।
 * প্রতিটি মডিউলের নিজের pendingForMe() ব্যবহার করে, তাই দৃশ্যমানতার নিয়ম (কে কী অনুমোদন করতে পারে) আলাদা করে লিখতে হয়নি।
 *
 * প্রতিটি আইটেম একটি Map: group, icon, title, detail, controller, action, id, params
 * (ইনবক্স ভিউ ও পোর্টাল ভিউ এই কী-গুলোই পড়ে)
 */
@Transactional(readOnly = true)
class ApprovalService {

    def accessService
    def leaveService
    def expenseService
    def performanceService
    def offboardingService
    def settlementService

    List<Map> pendingItems() {
        List<Map> items = []
        if (accessService.currentUser() == null) return items

        // ছুটি
        leaveService.pendingForMe().each { LeaveRequest r ->
            items << item('ছুটির আবেদন', 'bi-calendar2-check', "${r.employee.name} - ${r.leaveType?.name ?: 'ছুটি'}",
                    "${r.fromDate} থেকে ${r.toDate}", 'leave', 'show', r.id)
        }
        // খরচের দাবি (ম্যানেজার ধাপ + হিসাব বিভাগ ধাপ)
        expenseService.pendingClaimsForMe().each { ExpenseClaim c ->
            items << item('খরচের দাবি', 'bi-receipt-cutoff', "${c.employee.name} - ${c.title}",
                    "${c.amount} ৳ (${c.status == 'PENDING_ACCOUNTS' ? 'হিসাব বিভাগ' : 'ম্যানেজার'} ধাপ)", 'expense', 'show', c.id)
        }
        // ভ্রমণ
        expenseService.pendingTravelForMe().each { TravelRequest t ->
            items << item('ভ্রমণের অনুরোধ', 'bi-geo-alt', "${t.employee.name} - ${t.destination}",
                    "${t.fromDate} থেকে ${t.toDate}", 'travel', 'index', null)
        }
        // মূল্যায়ন (ম্যানেজার ধাপ)
        Appraisal.findAll("from Appraisal a where a.cycle.status = 'OPEN' and a.status = 'PENDING_MANAGER' order by a.id").each { Appraisal a ->
            if (performanceService.canReview(a)) {
                items << item('মূল্যায়ন', 'bi-clipboard2-check', "${a.employee.name} - ${a.cycle.name}",
                        'ম্যানেজারের মূল্যায়ন বাকি', 'performance', 'appraisal', a.id)
            }
        }
        // চাকরি ছাড়ার আবেদন
        offboardingService.pendingForMe().each { Separation s ->
            items << item('চাকরি ছাড়ার আবেদন', 'bi-box-arrow-right', "${s.employee.name} - ${s.exitType}",
                    s.status == 'PENDING_HR' ? 'HR ধাপ' : 'ম্যানেজার ধাপ', 'offboarding', 'show', s.id)
        }
        // চূড়ান্ত নিষ্পত্তি (হিসাব বিভাগ)
        settlementService.pendingForAccounts().each { FinalSettlement f ->
            items << item('চূড়ান্ত নিষ্পত্তি', 'bi-cash-stack', "${f.employee.name} - চূড়ান্ত নিষ্পত্তি",
                    f.status == 'APPROVED' ? 'পরিশোধ বাকি' : 'অনুমোদন বাকি', 'offboarding', 'show', f.separation.id)
        }
        items
    }

    static Map item(String group, String icon, String title, String detail, String controller, String action, Long id) {
        [group: group, icon: icon, title: title, detail: detail, controller: controller, action: action, id: id, params: [:]]
    }
}

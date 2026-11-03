package hrms

import grails.plugin.springsecurity.annotation.Secured

/** Day 6: কর্মীর বেতন পাওয়ার মাধ্যম (ব্যাংক / bKash / Nagad / নগদ) - ব্যাংক ও ওয়ালেট শিটের উৎস */
@Secured(['ROLE_ADMIN', 'ROLE_ACCOUNTANT'])
class PaymentInfoController {

    static allowedMethods = [save: 'POST']

    def index() {
        Map<Long, PaymentInfo> infos = [:]
        PaymentInfo.list().each { PaymentInfo p -> infos[p.employee.id] = p }
        List<Employee> employees = Employee.findAllByStatusNotEqual('INACTIVE', [sort: 'name'])
        [employees: employees, infos: infos, missing: employees.count { Employee e -> infos[e.id] == null || !infos[e.id].complete }]
    }

    /** id = কর্মীর id */
    def edit(Long id) {
        Employee emp = id ? Employee.get(id) : null
        if (!emp) { notFound(); return }
        [employee: emp, info: PaymentInfo.findByEmployee(emp) ?: new PaymentInfo(employee: emp)]
    }

    def save(Long id) {
        Employee emp = id ? Employee.get(id) : null
        if (!emp) { notFound(); return }
        PaymentInfo info = PaymentInfo.findByEmployee(emp) ?: new PaymentInfo(employee: emp)
        String method = params.method as String
        String error = null

        if (!PaymentInfo.METHODS.contains(method)) {
            error = 'পেমেন্টের মাধ্যম বেছে নিন।'
        } else if (method == 'BANK') {
            if (!params.bankName?.toString()?.trim()) error = 'ব্যাংকের নাম দিন।'
            else if (!(params.accountNo?.toString()?.trim() ==~ /^[0-9\-]{6,30}$/)) error = 'অ্যাকাউন্ট নম্বর সঠিক নয় (৬-৩০ সংখ্যা)।'
            else if (params.routingNo?.toString()?.trim() && !(params.routingNo.toString().trim() ==~ /^\d{9}$/)) error = 'রাউটিং নম্বর ৯ সংখ্যার হতে হবে।'
        } else if (method == 'BKASH' || method == 'NAGAD') {
            if (!(params.walletNo?.toString()?.trim() ==~ /^01[3-9]\d{8}$/)) error = 'মোবাইল নম্বর সঠিক নয় (যেমন 01712345678)।'
        }

        if (error) {
            flash.error = error
            info.method = PaymentInfo.METHODS.contains(method) ? method : info.method
            bindData(info, params, [include: ['bankName', 'bankBranch', 'accountName', 'accountNo', 'routingNo', 'walletNo']])
            render(view: 'edit', model: [employee: emp, info: info])
            return
        }

        info.method = method
        bindData(info, params, [include: ['bankName', 'bankBranch', 'accountName', 'accountNo', 'routingNo', 'walletNo']])
        ['bankName', 'bankBranch', 'accountName', 'accountNo', 'routingNo', 'walletNo'].each { String f ->
            info[f] = info[f]?.toString()?.trim() ?: null
        }
        // বেছে নেওয়া মাধ্যমের সাথে অপ্রাসঙ্গিক ঘর ফাঁকা করা
        if (method != 'BANK') { info.bankName = null; info.bankBranch = null; info.accountName = null; info.accountNo = null; info.routingNo = null }
        if (method != 'BKASH' && method != 'NAGAD') info.walletNo = null

        if (!info.save(flush: true)) {
            render(view: 'edit', model: [employee: emp, info: info])
            return
        }
        flash.success = "${emp.name} এর পেমেন্ট তথ্য সংরক্ষিত হয়েছে।".toString()
        redirect(action: 'index')
    }

    private void notFound() {
        flash.error = 'কর্মী পাওয়া যায়নি।'
        redirect(action: 'index')
    }
}

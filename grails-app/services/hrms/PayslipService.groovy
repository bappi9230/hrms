package hrms

import grails.gorm.transactions.Transactional
import hrms.payroll.PayslipPdf
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.mail.javamail.MimeMessageHelper

import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.Month
import java.time.format.TextStyle

/**
 * Day 6: পে-স্লিপ PDF ও ইমেইল।
 *
 * PDF: নির্ভরতাহীন ছোট PDF লেখক (hrms.payroll.SimplePdf) ব্যবহার করে, A4, এক পাতা।
 *      সীমাবদ্ধতা: PDF এ শুধু Latin (WinAnsi) অক্ষর - বাংলা নাম '?' হয়ে যায়; তাই লেবেল ইংরেজিতে রাখা হয়েছে।
 *      বাংলা পে-স্লিপ দরকার হলে "পে-স্লিপ" HTML পাতা থেকে ব্রাউজারের প্রিন্ট/PDF সেভ ব্যবহার করুন।
 * ইমেইল: spring-boot-starter-mail। SPRING_MAIL_HOST (ও দরকারে PORT/USERNAME/PASSWORD) সেট না থাকলে পাঠানো হয় না।
 */
@Transactional
class PayslipService {

    @Value('${hrms.company.name:HRMS Demo Ltd.}')
    String companyName

    @Value('${hrms.mail.from:no-reply@hrms.local}')
    String mailFrom

    /** spring.mail.host সেট থাকলে Spring এই বিন বানায় (নাম অনুযায়ী ইনজেক্ট); না থাকলে null */
    def mailSender

    boolean isMailConfigured() { mailSender != null }

    private static final DecimalFormat MONEY = new DecimalFormat('#,##0')

    private static String money(BigDecimal v) { MONEY.format(v ?: 0.0) }

    private static boolean ascii(String s) {
        if (s == null) return false
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) > 126) return false
        }
        true
    }

    /** PDF এ বাংলা লেবেল ছাপা যায় না - কোড দেখে ইংরেজি নাম */
    private static String pdfLabel(PayrollLineItem i) {
        Map known = ['PF': 'Provident fund (employee)', 'TDS': 'Income tax (TDS)', 'LOAN': 'Loan / advance installment',
                     'BONUS-F': 'Festival bonus', 'BONUS-P': 'Performance bonus', 'BONUS': 'Bonus']
        if (known.containsKey(i.headCode)) return known[i.headCode] as String
        if (ascii(i.headName)) return i.headName
        return (i.headType == 'EARNING' ? 'Allowance' : 'Deduction') + " (${i.headCode})".toString()
    }

    static String periodName(PayrollRun run) {
        Month.of(run.payMonth).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + ' ' + run.payYear
    }

    byte[] pdf(PayrollLine line) {
        PayrollRun run = line.payrollRun
        Employee emp = line.employee
        List<PayrollLineItem> items = PayrollLineItem.findAllByLine(line, [sort: 'id'])

        List<String[]> earnings = []
        earnings << (['Basic', money(line.basic)] as String[])
        earnings << (['House rent', money(line.houseRent)] as String[])
        earnings << (['Medical allowance', money(line.medical)] as String[])
        earnings << (['Conveyance', money(line.conveyance)] as String[])
        items.findAll { it.headType == 'EARNING' }.each { PayrollLineItem i -> earnings << ([pdfLabel(i), money(i.amount)] as String[]) }
        if (line.overtimeAmount > 0) earnings << (["Overtime (${line.overtimeHours} h)".toString(), money(line.overtimeAmount)] as String[])

        BigDecimal adj = line.adjustment ?: 0.0
        if (adj > 0) earnings << (['Adjustment', money(adj)] as String[])

        List<String[]> deductions = []
        if (line.absentDeduction > 0) deductions << (['Absence / unpaid leave', money(line.absentDeduction)] as String[])
        if (line.lateDeduction > 0) deductions << (['Late attendance', money(line.lateDeduction)] as String[])
        items.findAll { it.headType == 'DEDUCTION' }.each { PayrollLineItem i -> deductions << ([pdfLabel(i), money(i.amount)] as String[]) }
        if (adj < 0) deductions << (['Adjustment', money(adj.abs())] as String[])

        BigDecimal totalEarn = line.grossEarnings + (adj > 0 ? adj : 0.0)
        BigDecimal totalDed = line.totalDeduction + (adj < 0 ? adj.abs() : 0.0)

        List<String[]> left = [
                ['Name', ascii(line.employeeName) ? line.employeeName : "Employee #${emp.id}".toString()] as String[],
                ['Designation', ascii(line.designationTitle) ? (line.designationTitle ?: '-') : '-'] as String[],
                ['Department', ascii(line.departmentName) ? (line.departmentName ?: '-') : '-'] as String[]]
        List<String[]> right = [
                ['Employee ID', emp.id.toString()] as String[],
                ['Joined', emp.joinDate.format(java.time.format.DateTimeFormatter.ofPattern('dd-MM-yyyy'))] as String[],
                ['Days worked', "${line.presentDays + line.paidLeaveDays + line.holidayDays} of ${line.workingDays}".toString()] as String[]]

        List<String[]> notes = []
        if (line.pfEmployer > 0) notes << (['Employer provident fund contribution (not deducted)', money(line.pfEmployer)] as String[])
        if (line.gratuityProvision > 0) notes << (['Gratuity provision this month (not deducted)', money(line.gratuityProvision)] as String[])
        PaymentInfo pay = PaymentInfo.findByEmployee(emp)
        if (pay != null) notes << (['Paid via', paymentText(pay)] as String[])

        PayslipPdf.render(companyName, periodName(run), run.status == 'FINALIZED' ? '' : 'DRAFT - not final',
                left, right, earnings, deductions, money(totalEarn) + ' Tk', money(totalDed) + ' Tk', money(line.netPay) + ' Tk', null, notes)
    }

    static String paymentText(PaymentInfo p) {
        if (p.method == 'BANK') {
            String acc = p.accountNo ?: ''
            String masked = acc.length() > 4 ? ('****' + acc.substring(acc.length() - 4)) : acc
            return "Bank - ${ascii(p.bankName) ? p.bankName : 'bank'} ${masked}".toString()
        }
        if (p.method == 'BKASH' || p.method == 'NAGAD') {
            String w = p.walletNo ?: ''
            return (p.method == 'BKASH' ? 'bKash ' : 'Nagad ') + (w.length() > 4 ? ('*******' + w.substring(w.length() - 4)) : w)
        }
        'Cash'
    }

    static String fileName(PayrollLine line) {
        String name = (line.employeeName ?: 'employee').replaceAll(/[^A-Za-z0-9]+/, '-').replaceAll(/^-+|-+$/, '')
        "payslip-${line.payrollRun}-${name ?: line.employee.id}.pdf".toString()
    }

    // ------------------------------------------------------------------ ইমেইল
    Map email(PayrollLine line) {
        if (!mailConfigured) return [ok: false, message: 'ইমেইল সার্ভার ঠিক করা নেই (SPRING_MAIL_HOST সেট করুন) - README দেখুন।']
        if (line.payrollRun.status != 'FINALIZED') return [ok: false, message: 'চূড়ান্ত হওয়ার আগে পে-স্লিপ ইমেইল করা যায় না।']
        String to = line.employee.email
        if (!to) return [ok: false, message: 'কর্মীর ইমেইল নেই।']
        try {
            def msg = mailSender.createMimeMessage()
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, 'UTF-8')
            helper.setFrom(mailFrom)
            helper.setTo(to)
            helper.setSubject("Payslip - ${periodName(line.payrollRun)}".toString())
            helper.setText("Dear ${line.employeeName},\n\nPlease find your payslip for ${periodName(line.payrollRun)} attached.\n" +
                    "Net pay: ${money(line.netPay)} Tk\n\nRegards,\n${companyName} HR / Accounts\n".toString(), false)
            helper.addAttachment(fileName(line), new ByteArrayResource(pdf(line)), 'application/pdf')
            mailSender.send(msg)
        } catch (Exception ex) {
            log.warn("Payslip email to ${to} failed: ${ex.message}")
            return [ok: false, message: "ইমেইল পাঠানো যায়নি (${to}): ${ex.message}".toString()]
        }
        line.emailedAt = LocalDateTime.now(AttendanceService.ZONE)
        line.emailedTo = to
        line.save(flush: true)
        [ok: true, message: "পে-স্লিপ পাঠানো হয়েছে: ${to}".toString()]
    }

    /** চূড়ান্ত পে-রোলের সবাইকে (যাদের এখনো পাঠানো হয়নি, onlyUnsent=true হলে) */
    Map emailAll(PayrollRun run, boolean onlyUnsent) {
        if (!mailConfigured) return [ok: false, message: 'ইমেইল সার্ভার ঠিক করা নেই (SPRING_MAIL_HOST সেট করুন) - README দেখুন।']
        if (run.status != 'FINALIZED') return [ok: false, message: 'পে-রোল চূড়ান্ত না হলে সবাইকে ইমেইল করা যায় না।']
        int sent = 0
        List<String> failed = []
        PayrollLine.findAllByPayrollRun(run, [sort: 'employeeName']).each { PayrollLine l ->
            if (onlyUnsent && l.emailedAt != null) return
            Map r = email(l)
            if (r.ok) sent++ else failed << "${l.employeeName}".toString()
        }
        String msg = "${sent} জনকে পাঠানো হয়েছে।"
        if (failed) msg += " ব্যর্থ: ${failed.join(', ')}"
        [ok: failed.isEmpty(), message: msg, sent: sent]
    }
}

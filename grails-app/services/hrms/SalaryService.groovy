package hrms

import grails.gorm.transactions.Transactional

import java.math.RoundingMode
import java.time.LocalDate

/**
 * Day 5: বেতন কাঠামো, ভাতা/কর্তনের খাত বরাদ্দ, ইনক্রিমেন্ট ও বেতন সংশোধন।
 *
 * নিয়ম:
 *  - Employee.salary = বর্তমান gross (প্রোফাইল, রিপোর্ট ও CSV এর জন্য); কাঠামোই আসল উৎস।
 *  - কাঠামো না থাকলে Employee.salary থেকে ডিফল্ট ভাগে (60/25/10/5) আপনা-আপনি বানানো হয়।
 *  - সংশোধন ভবিষ্যৎ তারিখেও দেওয়া যায়; তখন Employee.salary বদলায় কার্যকর তারিখে (syncProfileSalaries)।
 */
@Transactional
class SalaryService {

    // gross এর কত শতাংশ কোন অংশে (বেসিক বাকিটা নেয়, যাতে যোগফল ঠিক থাকে)
    static final BigDecimal HOUSE_RENT_PCT = 25.0
    static final BigDecimal MEDICAL_PCT    = 10.0
    static final BigDecimal CONVEYANCE_PCT = 5.0

    def employmentService
    def accessService

    private static LocalDate today() { LocalDate.now(AttendanceService.ZONE) }

    // ------------------------------------------------------------------ কাঠামো
    /** gross কে ডিফল্ট অনুপাতে ভাগ করে (পূর্ণ টাকায়) */
    Map splitGross(BigDecimal gross) {
        BigDecimal g = (gross ?: 0.0).setScale(0, RoundingMode.HALF_UP)
        BigDecimal rent = (g * HOUSE_RENT_PCT / 100).setScale(0, RoundingMode.HALF_UP)
        BigDecimal med  = (g * MEDICAL_PCT / 100).setScale(0, RoundingMode.HALF_UP)
        BigDecimal conv = (g * CONVEYANCE_PCT / 100).setScale(0, RoundingMode.HALF_UP)
        [basic: g - rent - med - conv, houseRent: rent, medical: med, conveyance: conv]
    }

    /** asOf তারিখে চালু কাঠামো; কিছু না থাকলে (বা কাঠামো আরও পরের তারিখের হলে) প্রথমটা/ডিফল্ট */
    SalaryStructure structureFor(Employee emp, LocalDate asOf) {
        SalaryStructure s = SalaryStructure.findByEmployeeAndEffectiveFromLessThanEquals(emp, asOf, [sort: 'effectiveFrom', order: 'desc'])
        if (s != null) return s
        SalaryStructure earliest = SalaryStructure.findByEmployee(emp, [sort: 'effectiveFrom', order: 'asc'])
        if (earliest != null) return earliest
        createDefault(emp)
    }

    SalaryStructure currentStructure(Employee emp) {
        structureFor(emp, today())
    }

    private SalaryStructure createDefault(Employee emp) {
        Map p = splitGross(emp.salary)
        new SalaryStructure(employee: emp, effectiveFrom: emp.joinDate, basic: p.basic, houseRent: p.houseRent,
                medical: p.medical, conveyance: p.conveyance, remarks: 'ডিফল্ট ভাগ (স্বয়ংক্রিয়)', createdBy: 'system')
                .save(failOnError: true, flush: true)
    }

    /** সব কর্মীর কাঠামো আছে কিনা নিশ্চিত করে (ডেটা আগে থেকে থাকলেও কাজ করে) */
    void ensureAllStructures() {
        Employee.list().each { Employee e -> currentStructure(e) }
    }

    List<SalaryStructure> history(Employee emp) {
        SalaryStructure.findAllByEmployee(emp, [sort: 'effectiveFrom', order: 'desc'])
    }

    /**
     * কাঠামো সংরক্ষণ। একই effectiveFrom এ আগে থেকে থাকলে সেটাই হালনাগাদ হয়।
     * ফেরত: [ok, message, structure]
     */
    Map saveStructure(Employee emp, LocalDate effective, BigDecimal basic, BigDecimal houseRent,
                      BigDecimal medical, BigDecimal conveyance, String remarks) {
        if (emp == null) return fail('কর্মী পাওয়া যায়নি।')
        if (effective == null) return fail('কার্যকর তারিখ দিন।')
        if (basic == null || basic <= 0) return fail('বেসিক বেতন শূন্যের বেশি হতে হবে।')
        if ([houseRent, medical, conveyance].any { it != null && it < 0 }) return fail('কোনো অংশই ঋণাত্মক হতে পারবে না।')

        SalaryStructure s = SalaryStructure.findByEmployeeAndEffectiveFrom(emp, effective) ?:
                new SalaryStructure(employee: emp, effectiveFrom: effective)
        s.basic = r0(basic)
        s.houseRent = r0(houseRent ?: 0.0)
        s.medical = r0(medical ?: 0.0)
        s.conveyance = r0(conveyance ?: 0.0)
        s.remarks = remarks?.trim() ?: null
        s.createdBy = accessService.currentUser()?.username
        if (!s.save(flush: true)) return fail('কাঠামো সংরক্ষণ করা যায়নি।')
        syncProfileSalary(emp)
        [ok: true, message: 'বেতন কাঠামো সংরক্ষণ হয়েছে।', structure: s]
    }

    /** Employee.salary কে আজ চালু কাঠামোর gross এর সাথে মেলায় */
    void syncProfileSalary(Employee emp) {
        SalaryStructure s = structureFor(emp, today())
        if (emp.salary == null || emp.salary.compareTo(s.gross) != 0) {
            emp.salary = s.gross
            emp.save(flush: true)
        }
    }

    /** ভবিষ্যৎ তারিখের সংশোধন আজ কার্যকর হয়ে থাকলে প্রোফাইলের বেতন হালনাগাদ (BootStrap/টাইমার থেকে) */
    void syncProfileSalaries() {
        Employee.list().each { Employee e ->
            if (SalaryStructure.countByEmployee(e) > 0) syncProfileSalary(e)
        }
    }

    // ------------------------------------------------------------------ ভাতা / কর্তন খাত
    List<EmployeeSalaryHead> headsOf(Employee emp) {
        EmployeeSalaryHead.findAllByEmployee(emp).sort { it.head.headType + it.head.name }
    }

    Map assignHead(Employee emp, SalaryHead head, BigDecimal amount) {
        if (emp == null || head == null) return fail('কর্মী ও খাত নির্বাচন করুন।')
        if (!head.active) return fail('এই খাতটি নিষ্ক্রিয়।')
        if (amount != null && amount < 0) return fail('পরিমাণ ঋণাত্মক হতে পারবে না।')
        EmployeeSalaryHead e = EmployeeSalaryHead.findByEmployeeAndHead(emp, head) ?: new EmployeeSalaryHead(employee: emp, head: head)
        e.amount = amount
        if (!e.save(flush: true)) return fail('খাত সংরক্ষণ করা যায়নি।')
        [ok: true, message: "${head.name} যুক্ত হয়েছে।".toString()]
    }

    Map removeHead(Employee emp, Long assignmentId) {
        EmployeeSalaryHead e = assignmentId ? EmployeeSalaryHead.get(assignmentId) : null
        if (e == null || e.employee.id != emp.id) return fail('খাতটি পাওয়া যায়নি।')
        String name = e.head.name
        e.delete(flush: true)
        [ok: true, message: "${name} সরানো হয়েছে।".toString()]
    }

    // ------------------------------------------------------------------ ইনক্রিমেন্ট / সংশোধন
    /**
     * নতুন gross দিলে বর্তমান কাঠামোর অনুপাত ধরে সব অংশ নতুন করে ভাগ হয় (বেসিক বাকিটা নেয়)।
     * INCREMENT এ gross কমানো যায় না; কমাতে হলে REVISION বেছে নিন।
     */
    Map revise(Employee emp, String type, LocalDate effective, BigDecimal newGross, String remarks) {
        if (emp == null) return fail('কর্মী পাওয়া যায়নি।')
        if (!(type in SalaryRevision.TYPES)) return fail('ধরন সঠিক নয়।')
        if (effective == null) return fail('কার্যকর তারিখ দিন।')
        if (newGross == null || newGross <= 0) return fail('নতুন gross বেতন শূন্যের বেশি হতে হবে।')

        SalaryStructure base = structureFor(emp, effective)
        BigDecimal oldGross = base.gross
        BigDecimal ng = newGross.setScale(0, RoundingMode.HALF_UP)
        if (ng.compareTo(oldGross) == 0) return fail('নতুন বেতন বর্তমানের সমান - কোনো পরিবর্তন নেই।')
        if (type == 'INCREMENT' && ng < oldGross) return fail('ইনক্রিমেন্টে বেতন কমানো যায় না। কমাতে হলে "সংশোধন" বেছে নিন।')

        // বর্তমান অংশগুলোর অনুপাত অনুযায়ী ভাগ; পুরোনো gross শূন্য হলে ডিফল্ট ভাগ
        Map parts
        if (oldGross > 0) {
            BigDecimal rent = (base.houseRent * ng / oldGross).setScale(0, RoundingMode.HALF_UP)
            BigDecimal med  = (base.medical * ng / oldGross).setScale(0, RoundingMode.HALF_UP)
            BigDecimal conv = (base.conveyance * ng / oldGross).setScale(0, RoundingMode.HALF_UP)
            parts = [basic: ng - rent - med - conv, houseRent: rent, medical: med, conveyance: conv]
        } else {
            parts = splitGross(ng)
        }

        Map saved = saveStructure(emp, effective, parts.basic as BigDecimal, parts.houseRent as BigDecimal,
                parts.medical as BigDecimal, parts.conveyance as BigDecimal, remarks)
        if (!saved.ok) return saved

        new SalaryRevision(employee: emp, revisionType: type, effectiveDate: effective, oldGross: oldGross, newGross: ng,
                remarks: remarks?.trim() ?: null, createdBy: accessService.currentUser()?.username).save(failOnError: true, flush: true)

        // চাকরির ইতিহাসেও এক লাইন (বেতনের আগে-পরের মান সহ)
        new EmploymentHistory(employee: emp, eventType: 'OTHER', effectiveDate: effective, oldSalary: oldGross, newSalary: ng,
                remarks: (type == 'INCREMENT' ? 'ইনক্রিমেন্ট' : 'বেতন সংশোধন') + (remarks?.trim() ? ': ' + remarks.trim() : ''),
                recordedBy: accessService.currentUser()?.username).save(failOnError: true, flush: true)

        [ok: true, message: "বেতন ${oldGross.toPlainString()} থেকে ${ng.toPlainString()} করা হয়েছে (কার্যকর ${effective})।".toString()]
    }

    List<SalaryRevision> revisions(Employee emp = null, int max = 100) {
        if (emp) return SalaryRevision.findAllByEmployee(emp, [sort: 'effectiveDate', order: 'desc', max: max])
        SalaryRevision.list(sort: 'effectiveDate', order: 'desc', max: max)
    }

    // ------------------------------------------------------------------ helpers
    private static BigDecimal r0(BigDecimal v) { v.setScale(0, RoundingMode.HALF_UP) }

    private static Map fail(String message) { [ok: false, message: message] }
}

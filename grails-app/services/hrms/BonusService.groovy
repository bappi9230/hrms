package hrms

import grails.gorm.transactions.Transactional

import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * Day 6: উৎসব ও পারফরম্যান্স বোনাস।
 *
 * উৎসব বোনাস (সবার জন্য একসাথে): বেসিকের নির্দিষ্ট % ।
 *  - পে-মাসের ১ তারিখে চাকরির বয়স ৩ মাসের কম হলে নেই; ৩-১১ মাসে মাসের অনুপাতে (মাস ÷ ১২); ১২ মাস+ পূর্ণ।
 *  - একই শিরোনাম + মাসে কর্মীর বোনাস আগে থাকলে আবার তৈরি হয় না।
 * পারফরম্যান্স/অন্য বোনাস: কর্মী ধরে হাতে টাকা।
 * বোনাস পে-রোলে আয় হিসেবে যায় (PF/TDS এ ধরা হয় না); পে-রোল চূড়ান্ত হলে PAID।
 */
@Transactional
class BonusService {

    static final int MIN_SERVICE_MONTHS = 3

    def accessService
    def salaryService

    private static Map fail(String m) { [ok: false, message: m] }

    /** ওই মাসের পে-রোল চূড়ান্ত হয়ে থাকলে বোনাস যোগ/বাতিল চলবে না */
    private static boolean periodLocked(Integer year, Integer month) {
        PayrollRun run = PayrollRun.findByPayYearAndPayMonth(year, month)
        run != null && run.status == 'FINALIZED'
    }

    Map generateFestival(String title, Integer year, Integer month, BigDecimal percentOfBasic) {
        if (!title?.trim()) return fail('বোনাসের নাম দিন (যেমন Eid-ul-Fitr)।')
        if (year == null || month == null || month < 1 || month > 12) return fail('পে-মাস সঠিক নয়।')
        if (percentOfBasic == null || percentOfBasic <= 0 || percentOfBasic > 500) return fail('বেসিকের শতাংশ ১ থেকে ৫০০ এর মধ্যে হতে হবে।')
        if (periodLocked(year, month)) return fail('ওই মাসের পে-রোল চূড়ান্ত হয়ে গেছে।')

        LocalDate firstOfMonth = YearMonth.of(year, month).atDay(1)
        String user = accessService.currentUser()?.username
        int created = 0, skippedNew = 0, skippedExisting = 0

        Employee.findAll("from Employee e where e.status <> 'INACTIVE' order by e.name").each { Employee e ->
            long months = ChronoUnit.MONTHS.between(e.joinDate, firstOfMonth)
            if (months < MIN_SERVICE_MONTHS) { skippedNew++; return }
            if (Bonus.countByEmployeeAndBonusTypeAndTitleAndPayYearAndPayMonthAndStatusNotEqual(e, 'FESTIVAL', title.trim(), year, month, 'CANCELLED') > 0) {
                skippedExisting++
                return
            }
            BigDecimal basic = salaryService.structureFor(e, firstOfMonth.withDayOfMonth(firstOfMonth.lengthOfMonth())).basic
            BigDecimal factor = months >= 12 ? 1.0 : new BigDecimal(months).divide(new BigDecimal(12), 6, RoundingMode.HALF_UP)
            BigDecimal amount = (basic * percentOfBasic / 100 * factor).setScale(0, RoundingMode.HALF_UP)
            if (amount <= 0) return
            new Bonus(employee: e, bonusType: 'FESTIVAL', title: title.trim(), payYear: year, payMonth: month, amount: amount,
                    note: months >= 12 ? "বেসিকের ${percentOfBasic.stripTrailingZeros().toPlainString()}%".toString()
                            : "বেসিকের ${percentOfBasic.stripTrailingZeros().toPlainString()}% × ${months}/12 (১ বছরের কম চাকরি)".toString(),
                    createdBy: user).save(failOnError: true)
            created++
        }
        [ok: true, message: "${created} জনের বোনাস তৈরি হয়েছে; ৩ মাসের কম চাকরির কারণে বাদ ${skippedNew} জন, আগেই আছে ${skippedExisting} জন।".toString()]
    }

    Map add(Employee emp, String type, String title, Integer year, Integer month, BigDecimal amount, String note) {
        if (emp == null) return fail('কর্মী বেছে নিন।')
        if (!Bonus.TYPES.contains(type)) return fail('বোনাসের ধরন সঠিক নয়।')
        if (!title?.trim()) return fail('শিরোনাম দিন।')
        if (year == null || month == null || month < 1 || month > 12) return fail('পে-মাস সঠিক নয়।')
        if (amount == null || amount <= 0) return fail('টাকার পরিমাণ দিন।')
        if (emp.status == 'INACTIVE') return fail('নিষ্ক্রিয় কর্মীকে বোনাস দেওয়া যায় না।')
        if (periodLocked(year, month)) return fail('ওই মাসের পে-রোল চূড়ান্ত হয়ে গেছে।')
        Bonus b = new Bonus(employee: emp, bonusType: type, title: title.trim(), payYear: year, payMonth: month,
                amount: amount.setScale(0, RoundingMode.HALF_UP), note: note?.trim() ?: null, createdBy: accessService.currentUser()?.username)
        if (!b.save(flush: true)) return fail(b.errors.allErrors*.defaultMessage?.join('; ') ?: 'সংরক্ষণ করা যায়নি।')
        [ok: true, message: 'বোনাস যোগ হয়েছে।']
    }

    Map cancel(Bonus b) {
        if (b.status != 'PLANNED') return fail('শুধু "পরিকল্পিত" বোনাস বাতিল করা যায়।')
        b.status = 'CANCELLED'
        b.save(failOnError: true, flush: true)
        [ok: true, message: 'বোনাস বাতিল হয়েছে। খসড়া পে-রোল থাকলে "আবার হিসাব" করুন।']
    }
}

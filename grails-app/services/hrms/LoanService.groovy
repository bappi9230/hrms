package hrms

import grails.gorm.transactions.Transactional

import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

/**
 * Day 6: ঋণ ও বেতন অগ্রিম।
 *
 * নিয়ম:
 *  - হিসাব বিভাগ/Admin আবেদন তোলে (PENDING); শুধু Admin অনুমোদন বা প্রত্যাখ্যান করে।
 *  - ঋণের সীমা: কর্মীর বর্তমান gross এর ৬ গুণ (অগ্রিমে ১ গুণ)। কিস্তি gross এর ৫০% এর বেশি হবে না।
 *  - একজনের একটি ACTIVE/PENDING ঋণ (LOAN) থাকলে নতুন ঋণ নয়; অগ্রিমও একসাথে একটাই।
 *  - অনুমোদনে কিস্তির তালিকা তৈরি: সমান কিস্তি, শেষ কিস্তিতে ভগ্নাংশ মিলানো। সুদ নেই (কর্মী ঋণ)।
 *  - প্রথম কিস্তি কাটার মাস চূড়ান্ত হওয়া পে-রোলের মাস হতে পারবে না।
 */
@Transactional
class LoanService {

    static final BigDecimal MAX_LOAN_GROSS_MULTIPLE = 6.0
    static final BigDecimal MAX_ADVANCE_GROSS_MULTIPLE = 1.0
    static final BigDecimal MAX_INSTALLMENT_PCT_OF_GROSS = 50.0

    def accessService
    def salaryService

    private static Map fail(String m) { [ok: false, message: m] }

    // ------------------------------------------------------------------ আবেদন
    Map create(Employee emp, String type, BigDecimal amount, Integer installments, Integer startYear, Integer startMonth, String reason) {
        if (emp == null) return fail('কর্মী বেছে নিন।')
        if (!Loan.TYPES.contains(type)) return fail('ধরন সঠিক নয়।')
        if (amount == null || amount <= 0) return fail('টাকার পরিমাণ দিন।')
        amount = amount.setScale(0, RoundingMode.HALF_UP)
        if (installments == null || installments < 1 || installments > 60) return fail('কিস্তির সংখ্যা ১ থেকে ৬০ এর মধ্যে হতে হবে।')
        if (type == 'ADVANCE' && installments > 3) return fail('অগ্রিম সর্বোচ্চ ৩ কিস্তিতে শোধ করা যায়।')
        if (startYear == null || startMonth == null || startMonth < 1 || startMonth > 12) return fail('প্রথম কিস্তির মাস সঠিক নয়।')
        if (emp.status == 'INACTIVE') return fail('নিষ্ক্রিয় কর্মীকে ঋণ দেওয়া যায় না।')

        YearMonth first = YearMonth.of(startYear, startMonth)
        PayrollRun existing = PayrollRun.findByPayYearAndPayMonth(startYear, startMonth)
        if (existing != null && existing.status == 'FINALIZED') return fail('ওই মাসের পে-রোল চূড়ান্ত হয়ে গেছে - পরের মাস বেছে নিন।')
        if (first.atEndOfMonth().isBefore(LocalDate.now(AttendanceService.ZONE).minusMonths(1))) return fail('প্রথম কিস্তির মাস অনেক আগের - সঠিক মাস দিন।')

        BigDecimal gross = salaryService.currentStructure(emp).gross
        BigDecimal limit = gross * (type == 'LOAN' ? MAX_LOAN_GROSS_MULTIPLE : MAX_ADVANCE_GROSS_MULTIPLE)
        if (amount > limit) return fail("সীমা পেরিয়েছে: সর্বোচ্চ ${limit.setScale(0, RoundingMode.HALF_UP)} ৳ (gross এর ${type == 'LOAN' ? 6 : 1} গুণ)।".toString())

        if (Loan.countByEmployeeAndLoanTypeAndStatusInList(emp, type, ['PENDING', 'ACTIVE']) > 0) {
            return fail('এই কর্মীর একই ধরনের একটি ঋণ/অগ্রিম এখনো চলছে বা অপেক্ষমাণ।')
        }

        BigDecimal each = amount.divide(new BigDecimal(installments), 0, RoundingMode.UP)
        if (each * 100 > gross * MAX_INSTALLMENT_PCT_OF_GROSS) {
            return fail("কিস্তি (${each} ৳) gross এর ${MAX_INSTALLMENT_PCT_OF_GROSS.intValue()}% এর বেশি - কিস্তির সংখ্যা বাড়ান।".toString())
        }

        Loan loan = new Loan(employee: emp, loanType: type, amount: amount, installments: installments, installmentAmount: each,
                startYear: startYear, startMonth: startMonth, reason: reason?.trim() ?: null,
                requestedBy: accessService.currentUser()?.username)
        if (!loan.save(flush: true)) return fail(loan.errors.allErrors*.defaultMessage?.join('; ') ?: 'সংরক্ষণ করা যায়নি।')
        [ok: true, loan: loan, message: 'আবেদন সংরক্ষিত হয়েছে - Admin এর অনুমোদনের অপেক্ষায়।']
    }

    // ------------------------------------------------------------------ সিদ্ধান্ত
    Map approve(Loan loan, String note) {
        if (loan.status != 'PENDING') return fail('শুধু অপেক্ষমাণ আবেদন অনুমোদন করা যায়।')
        PayrollRun run = PayrollRun.findByPayYearAndPayMonth(loan.startYear, loan.startMonth)
        if (run != null && run.status == 'FINALIZED') return fail('প্রথম কিস্তির মাসের পে-রোল চূড়ান্ত হয়ে গেছে - আবেদন বাতিল করে নতুন মাসে আবার তুলুন।')

        loan.status = 'ACTIVE'
        loan.decidedBy = accessService.currentUser()?.username
        loan.decidedAt = LocalDateTime.now(AttendanceService.ZONE)
        loan.decisionNote = note?.trim() ?: null
        loan.save(failOnError: true, flush: true)

        YearMonth due = YearMonth.of(loan.startYear, loan.startMonth)
        BigDecimal remaining = loan.amount
        for (int n = 1; n <= loan.installments; n++) {
            BigDecimal amt = (n == loan.installments) ? remaining : loan.installmentAmount.min(remaining)
            if (amt > 0) {
                new LoanInstallment(loan: loan, seqNo: n, dueYear: due.year, dueMonth: due.monthValue, amount: amt).save(failOnError: true)
            }
            remaining -= amt
            due = due.plusMonths(1)
        }
        [ok: true, message: 'ঋণ অনুমোদিত হয়েছে, কিস্তির তালিকা তৈরি হয়েছে।']
    }

    Map reject(Loan loan, String note) {
        if (loan.status != 'PENDING') return fail('শুধু অপেক্ষমাণ আবেদন প্রত্যাখ্যান করা যায়।')
        if (!note?.trim()) return fail('প্রত্যাখ্যানের কারণ লিখুন।')
        loan.status = 'REJECTED'
        loan.decidedBy = accessService.currentUser()?.username
        loan.decidedAt = LocalDateTime.now(AttendanceService.ZONE)
        loan.decisionNote = note.trim()
        loan.save(failOnError: true, flush: true)
        [ok: true, message: 'আবেদন প্রত্যাখ্যাত হয়েছে।']
    }

    /** অপেক্ষমাণ আবেদন প্রত্যাহার */
    Map cancel(Loan loan) {
        if (loan.status != 'PENDING') return fail('শুধু অপেক্ষমাণ আবেদন প্রত্যাহার করা যায়।')
        loan.status = 'CANCELLED'
        loan.save(failOnError: true, flush: true)
        [ok: true, message: 'আবেদন প্রত্যাহার করা হয়েছে।']
    }

    /** আগাম নিষ্পত্তি: বাকি কিস্তি পে-রোলের বাইরে শোধ হয়েছে (নগদ/ব্যাংকে) - SETTLED, ঋণ CLOSED */
    Map settle(Loan loan, String note) {
        if (loan.status != 'ACTIVE') return fail('শুধু চলমান ঋণ নিষ্পত্তি করা যায়।')
        // কোনো খসড়া পে-রোলে ধরা কিস্তি থাকলে আগে সেই খসড়া আবার হিসাব করতে হবে - তাই এখানে আটকাই
        if (LoanInstallment.executeQuery("select count(i) from LoanInstallment i where i.loan = :l and i.status = 'PENDING' and i.payrollRun is not null", [l: loan])[0] > 0) {
            return fail('একটি খসড়া পে-রোলে কিস্তি ধরা আছে - আগে সেই পে-রোল চূড়ান্ত করুন বা মুছুন।')
        }
        LoanInstallment.executeUpdate("update LoanInstallment i set i.status = 'SETTLED' where i.loan = :l and i.status = 'PENDING'", [l: loan])
        loan.status = 'CLOSED'
        loan.decisionNote = ((loan.decisionNote ? loan.decisionNote + ' | ' : '') + 'আগাম নিষ্পত্তি: ' + (note?.trim() ?: '-')).take(255)
        loan.save(failOnError: true, flush: true)
        [ok: true, message: 'ঋণ নিষ্পত্তি হয়েছে (বাকি কিস্তি SETTLED)।']
    }

    // ------------------------------------------------------------------ তথ্য
    List<LoanInstallment> installmentsOf(Loan loan) {
        LoanInstallment.findAllByLoan(loan, [sort: 'seqNo'])
    }

    BigDecimal outstanding(Loan loan) {
        (LoanInstallment.executeQuery("select sum(i.amount) from LoanInstallment i where i.loan = :l and i.status = 'PENDING'", [l: loan])[0] ?: 0.0) as BigDecimal
    }
}

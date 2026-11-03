package hrms

import java.math.RoundingMode
import java.time.LocalDate

/** Day 4: কর্মী + ছুটির ধরন + বছর অনুযায়ী ব্যালেন্স */
class LeaveBalance {

    Employee employee
    LeaveType leaveType
    Integer balanceYear
    BigDecimal carriedForward = 0       // আগের বছর থেকে আসা
    BigDecimal used = 0                 // অনুমোদিত ও ব্যবহৃত
    BigDecimal pending = 0              // আবেদন করা, অনুমোদনের অপেক্ষায় (আটকে রাখা)

    static constraints = {
        employee nullable: false, unique: ['leaveType', 'balanceYear']
        leaveType nullable: false
        balanceYear nullable: false
        carriedForward nullable: false, min: 0.0
        used nullable: false, min: 0.0
        pending nullable: false, min: 0.0
    }

    static mapping = {
        table 'hrms_leave_balance'
    }

    /** নির্দিষ্ট তারিখ পর্যন্ত কতটুকু অর্জিত (accrual) হয়েছে */
    BigDecimal accruedAsOf(LocalDate asOf) {
        BigDecimal annual = leaveType.annualDays ?: 0.0
        if (leaveType.accrual != 'MONTHLY') return annual
        int months = asOf.year == balanceYear ? asOf.monthValue : (asOf.year > balanceYear ? 12 : 0)
        (annual * months / 12).setScale(1, RoundingMode.HALF_UP)
    }

    BigDecimal remainingAsOf(LocalDate asOf) {
        accruedAsOf(asOf) + carriedForward - used
    }

    /** আবেদনের জন্য খালি: অর্জিত + carry - ব্যবহৃত - অপেক্ষমাণ */
    BigDecimal availableAsOf(LocalDate asOf) {
        remainingAsOf(asOf) - pending
    }
}

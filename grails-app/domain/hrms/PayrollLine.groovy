package hrms

/**
 * Day 5: পে-রোলে একজন কর্মীর একটি লাইন। কর্মীর নাম/বিভাগ ও সব হিসাব তখনকার মানে জমা থাকে (snapshot),
 * তাই পরে প্রোফাইল বদলালেও চূড়ান্ত বেতন শিট বদলায় না। সব টাকা পূর্ণ টাকায় (scale 0)।
 */
class PayrollLine {

    PayrollRun payrollRun
    Employee employee
    String employeeName
    String departmentName
    String designationTitle

    // উপস্থিতির সারসংক্ষেপ (কর্মদিবস = শুক্রবার বাদে, যোগদানের তারিখ থেকে)
    Integer workingDays = 0
    Integer presentDays = 0
    Integer lateDays = 0
    Integer holidayDays = 0
    Integer paidLeaveDays = 0
    Integer unpaidLeaveDays = 0
    Integer absentDays = 0
    Integer lateDeductionDays = 0

    // আয়
    BigDecimal basic = 0
    BigDecimal houseRent = 0
    BigDecimal medical = 0
    BigDecimal conveyance = 0
    BigDecimal otherAllowance = 0
    BigDecimal overtimeHours = 0           // যে ঘণ্টা ধরে হিসাব হয়েছে
    BigDecimal overtimeHoursManual         // হিসাব বিভাগের হাতে দেওয়া ঘণ্টা (null = অনুমোদিত OT থেকে)
    BigDecimal overtimeAmount = 0
    BigDecimal grossEarnings = 0

    // কর্তন
    BigDecimal absentDeduction = 0
    BigDecimal lateDeduction = 0
    BigDecimal otherDeduction = 0
    BigDecimal totalDeduction = 0

    // হাতে দেওয়া সমন্বয় (+ বা -)
    BigDecimal adjustment = 0
    String adjustmentNote

    BigDecimal structureGross = 0          // কাঠামোর পূর্ণ মাসের gross
    BigDecimal netPay = 0
    String remarks                         // সতর্কবার্তা

    static constraints = {
        payrollRun nullable: false
        employee nullable: false
        employeeName maxSize: 100
        departmentName nullable: true, maxSize: 100
        designationTitle nullable: true, maxSize: 100
        overtimeHoursManual nullable: true, min: 0.0
        adjustmentNote nullable: true, maxSize: 255
        remarks nullable: true, maxSize: 500
    }

    static mapping = {
        table 'hrms_payroll_line'
    }
}

package hrms

/** Day 5: কোন কর্মী কোন ভাতা/কর্তন পায়। amount খালি থাকলে খাতের ডিফল্ট নীতি চলে, দিলে সেই নির্দিষ্ট টাকা। */
class EmployeeSalaryHead {

    Employee employee
    SalaryHead head
    BigDecimal amount           // ঐচ্ছিক: নির্দিষ্ট টাকার ওভাররাইড

    static constraints = {
        employee nullable: false, unique: 'head'
        head nullable: false
        amount nullable: true, min: 0.0
    }

    static mapping = {
        table 'hrms_employee_salary_head'
    }
}

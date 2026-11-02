package hrms

/** কর্মীর গ্রেড (যেমন: G1, G2) - gradeLevel যত বড়, গ্রেড তত উঁচু */
class Grade {

    String name
    Integer gradeLevel
    BigDecimal minSalary
    BigDecimal maxSalary

    static constraints = {
        name blank: false, unique: true, maxSize: 50
        gradeLevel nullable: false, min: 1
        minSalary nullable: true, min: 0.0
        maxSalary nullable: true, min: 0.0, validator: { BigDecimal max, Grade g ->
            // সর্বোচ্চ বেতন সর্বনিম্ন বেতনের চেয়ে কম হতে পারবে না
            if (max != null && g.minSalary != null && max < g.minSalary) return ['grade.maxSalary.lessThanMin']
            true
        }
    }

    String toString() { name }
}

package hrms

import java.time.LocalDate

/** চাকরির ইতিহাস: যোগদান, পদোন্নতি, বদলি - প্রতিটি পরিবর্তনের আগে-পরের মান নামসহ জমা থাকে */
class EmploymentHistory {

    static final List<String> EVENT_TYPES = ['PROMOTION', 'TRANSFER', 'OTHER']   // ব্যবহারকারী বেছে নিতে পারবে
    static final List<String> ALL_TYPES = ['JOINED'] + EVENT_TYPES               // JOINED শুধু সিস্টেম দেয়

    Employee employee
    String eventType
    LocalDate effectiveDate

    String fromDepartment
    String toDepartment
    String fromDesignation
    String toDesignation
    String fromBranch
    String toBranch
    BigDecimal oldSalary
    BigDecimal newSalary

    String remarks
    String recordedBy
    Date dateCreated

    static constraints = {
        employee nullable: false
        eventType inList: ALL_TYPES
        effectiveDate nullable: false
        fromDepartment nullable: true, maxSize: 100
        toDepartment nullable: true, maxSize: 100
        fromDesignation nullable: true, maxSize: 100
        toDesignation nullable: true, maxSize: 100
        fromBranch nullable: true, maxSize: 100
        toBranch nullable: true, maxSize: 100
        oldSalary nullable: true
        newSalary nullable: true
        remarks nullable: true, maxSize: 255
        recordedBy nullable: true, maxSize: 50
    }
}

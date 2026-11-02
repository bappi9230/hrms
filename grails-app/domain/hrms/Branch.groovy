package hrms

/** শাখা / অফিস (যেমন: প্রধান কার্যালয়, চট্টগ্রাম শাখা) */
class Branch {

    String name
    String address
    String phone

    static constraints = {
        name blank: false, unique: true, maxSize: 100
        address nullable: true, maxSize: 255
        phone nullable: true, maxSize: 20
    }

    String toString() { name }
}

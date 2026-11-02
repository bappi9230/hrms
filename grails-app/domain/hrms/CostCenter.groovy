package hrms

/** কস্ট সেন্টার - বেতন খরচ কোন খাতে যাবে তা ঠিক করে (Day 5 এর পে-রোলে লাগবে) */
class CostCenter {

    String code
    String name

    static constraints = {
        code blank: false, unique: true, maxSize: 30
        name blank: false, maxSize: 100
    }

    String toString() { "${code} - ${name}".toString() }
}

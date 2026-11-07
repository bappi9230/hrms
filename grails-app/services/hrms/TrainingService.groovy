package hrms

import grails.gorm.transactions.Transactional

import java.time.LocalDate

/**
 * Day 8: প্রশিক্ষণ ক্যাটালগ, নথিভুক্তি ও সার্টিফিকেট।
 *  - নথিভুক্তি: কর্মী নিজে বা HR/Admin। কোর্স OPEN, শেষ হয়নি, আসন খালি থাকতে হবে।
 *  - ফল (পাস/ফেল) ও সার্টিফিকেট: শুধু HR/Admin, কোর্স শুরুর পর।
 */
@Transactional
class TrainingService {

    def accessService

    private static Map fail(String m) { [ok: false, message: m] }

    private static LocalDate today() { LocalDate.now(AttendanceService.ZONE) }

    private String actor() { accessService.currentUser()?.username }

    /** যাদের আসন দখল করে আছে: ENROLLED + COMPLETED + FAILED */
    int seatsTaken(Course c) {
        c.id == null ? 0 : TrainingEnrollment.countByCourseAndStatusInList(c, ['ENROLLED', 'COMPLETED', 'FAILED'])
    }

    /** null = আসন সীমাহীন */
    Integer seatsLeft(Course c) {
        c.capacity > 0 ? Math.max(0, c.capacity - seatsTaken(c)) : null
    }

    // ================================================================== কোর্স
    Map saveCourse(Course c) {
        boolean isNew = c.id == null
        if (isNew) c.createdBy = actor()
        if (!c.validate()) return fail('ফর্মের তথ্য ঠিক করুন (শিরোনাম, তারিখ, ঘণ্টা বাধ্যতামূলক; শেষ তারিখ শুরুর আগে হবে না)।')
        if (isNew && c.endDate.isBefore(today())) return fail('শেষ হয়ে যাওয়া তারিখে নতুন কোর্স তৈরি করা যায় না।')
        if (!isNew) {
            if (c.status == 'CANCELLED') return fail('বাতিল কোর্স সম্পাদনা করা যায় না।')
            if (c.capacity > 0 && c.capacity < seatsTaken(c)) return fail("ইতিমধ্যে ${seatsTaken(c)} জন আছেন, আসন তার চেয়ে কম করা যাবে না।".toString())
        }
        c.save(failOnError: true, flush: true)
        [ok: true, message: "কোর্স '${c.title}' সংরক্ষিত হয়েছে।".toString()]
    }

    Map cancelCourse(Course c) {
        if (c.status == 'CANCELLED') return fail('কোর্স আগেই বাতিল।')
        if (TrainingEnrollment.countByCourseAndStatusInList(c, ['COMPLETED', 'FAILED']) > 0) return fail('ফল দেওয়া হয়ে গেছে, তাই কোর্স বাতিল করা যায় না।')
        int n = TrainingEnrollment.executeUpdate("update TrainingEnrollment e set e.status = 'CANCELLED' where e.course = :c and e.status = 'ENROLLED'", [c: c])
        c.status = 'CANCELLED'
        c.save(failOnError: true, flush: true)
        [ok: true, message: "কোর্স বাতিল হয়েছে (${n} জনের নথিভুক্তি বাতিল)।".toString()]
    }

    Map deleteCourse(Course c) {
        if (TrainingEnrollment.countByCourse(c) > 0) return fail('নথিভুক্তির রেকর্ড আছে, তাই মোছা যায় না - চাইলে কোর্স বাতিল করুন।')
        String t = c.title
        c.delete(flush: true)
        [ok: true, message: "কোর্স '${t}' মুছে ফেলা হয়েছে।".toString()]
    }

    // ================================================================== নথিভুক্তি
    Map enroll(Course c, Employee emp) {
        if (c == null || emp == null) return fail('কোর্স বা কর্মী পাওয়া যায়নি।')
        boolean self = accessService.currentEmployee()?.id == emp.id
        if (!self && !accessService.isHrAdmin()) return fail('অন্যকে নথিভুক্ত করার অনুমতি শুধু HR/Admin এর।')
        if (emp.status == 'INACTIVE') return fail('নিষ্ক্রিয় কর্মীকে নথিভুক্ত করা যায় না।')
        if (c.status != 'OPEN') return fail('কোর্সটি বাতিল হয়েছে।')
        if (c.endDate.isBefore(today())) return fail('কোর্সটি শেষ হয়ে গেছে।')

        TrainingEnrollment e = TrainingEnrollment.findByCourseAndEmployee(c, emp)
        if (e != null && e.status in ['ENROLLED', 'COMPLETED']) return fail(e.status == 'COMPLETED' ? 'এই কোর্স আগেই সম্পন্ন করা হয়েছে।' : 'আগেই নথিভুক্ত আছেন।')
        Integer left = seatsLeft(c)
        if (left != null && left <= 0) return fail('আসন খালি নেই।')

        if (e == null) {
            e = new TrainingEnrollment(course: c, employee: emp)
        } else {   // আগের CANCELLED / FAILED সারি আবার চালু
            e.status = 'ENROLLED'
            e.score = null
            e.completedDate = null
        }
        e.enrolledBy = actor()
        e.save(failOnError: true, flush: true)
        [ok: true, message: "${emp.name} '${c.title}' কোর্সে নথিভুক্ত হয়েছেন।".toString()]
    }

    Map cancelEnrollment(TrainingEnrollment e) {
        boolean self = accessService.currentEmployee()?.id == e.employee.id
        if (!self && !accessService.isHrAdmin()) return fail('এই নথিভুক্তি বাতিলের অনুমতি নেই।')
        if (e.status != 'ENROLLED') return fail('শুধু চলমান নথিভুক্তি বাতিল করা যায়।')
        if (self && !accessService.isHrAdmin() && !e.course.startDate.isAfter(today())) return fail('কোর্স শুরু হয়ে গেছে, বাতিল করতে HR এর সাথে যোগাযোগ করুন।')
        e.status = 'CANCELLED'
        e.save(failOnError: true, flush: true)
        [ok: true, message: 'নথিভুক্তি বাতিল হয়েছে।']
    }

    /** result = PASS | FAIL (শুধু HR/Admin; কোর্স শুরু হওয়ার পর) */
    Map recordResult(TrainingEnrollment e, String result, Integer score) {
        if (!accessService.isHrAdmin()) return fail('ফল দেওয়ার অনুমতি শুধু HR/Admin এর।')
        if (e.status != 'ENROLLED') return fail('শুধু চলমান নথিভুক্তির ফল দেওয়া যায়।')
        if (e.course.status != 'OPEN') return fail('বাতিল কোর্সের ফল দেওয়া যায় না।')
        if (e.course.startDate.isAfter(today())) return fail('কোর্স এখনো শুরু হয়নি।')
        if (!['PASS', 'FAIL'].contains(result)) return fail('ফল বেছে নিন (পাস / ফেল)।')
        if (score != null && (score < 0 || score > 100)) return fail('স্কোর ০ থেকে ১০০ এর মধ্যে হবে।')
        e.score = score
        e.completedDate = today()
        if (result == 'PASS') {
            e.status = 'COMPLETED'
            e.save(flush: true, failOnError: true)
            e.certificateNo = "CERT-${today().year}-${String.format('%05d', e.id)}".toString()
        } else {
            e.status = 'FAILED'
        }
        e.save(failOnError: true, flush: true)
        [ok: true, message: result == 'PASS' ? "${e.employee.name} সম্পন্ন করেছেন, সার্টিফিকেট ${e.certificateNo}।".toString() : 'ফল "ফেল" হিসেবে রেকর্ড হয়েছে।']
    }

    boolean canSeeCertificate(TrainingEnrollment e) {
        e.status == 'COMPLETED' && (accessService.isHrAdmin() || accessService.currentEmployee()?.id == e.employee.id)
    }
}

package hrms

import java.time.LocalTime

/** কর্মসময়ের শিফট (যেমন: সাধারণ ৯:০০ – ১৮:০০)। এই ডেমোতে রাত পেরোনো শিফট ধরা হয়নি। */
class Shift {

    String name
    LocalTime startTime
    LocalTime endTime
    Integer graceMinutes = 10        // শুরুর পর এত মিনিট পর্যন্ত দেরি গণ্য হয় না
    Integer earlyOutMinutes = 15     // শেষের আগে এত মিনিটের বেশি আগে বের হলে আর্লি-আউট

    static constraints = {
        name blank: false, unique: true, maxSize: 50
        startTime nullable: false
        endTime nullable: false, validator: { LocalTime end, Shift s ->
            if (end != null && s.startTime != null && !end.isAfter(s.startTime)) return ['shift.endTime.beforeStart']
            true
        }
        graceMinutes nullable: false, min: 0, max: 120
        earlyOutMinutes nullable: false, min: 0, max: 120
    }

    static mapping = {
        table 'hrms_shift'
    }

    String toString() { "${name} (${startTime}–${endTime})".toString() }
}

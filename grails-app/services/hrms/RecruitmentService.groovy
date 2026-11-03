package hrms

import grails.gorm.transactions.Transactional
import org.springframework.web.multipart.MultipartFile

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Day 7: নিয়োগ - জনবল চাহিদা, চাকরির বিজ্ঞপ্তি, প্রার্থী (Kanban ধাপ), ইন্টারভিউ, অফার এবং প্রার্থীকে কর্মীতে রূপান্তর।
 *
 * মূল নিয়ম:
 *  - বিজ্ঞপ্তি শুধু APPROVED চাহিদা থেকে, এবং তখনই যখন পদ খালি আছে।
 *  - প্রার্থী ধাপ: APPLIED -> SCREENING -> INTERVIEW -> OFFER -> HIRED। OFFER এ নিতে অন্তত একটি সম্পন্ন ইন্টারভিউতে HIRE/HOLD সুপারিশ লাগে।
 *  - HIRED কেবল রূপান্তরে (ACCEPTED অফার থেকে) হয়; ড্র্যাগ করে HIRED এ নেওয়া যায় না।
 *  - একজন প্রার্থীর একটি চলমান (SENT/ACCEPTED) অফারই থাকতে পারে।
 *  - রূপান্তরে কর্মী + চাকরির ইতিহাস + বেতন কাঠামো + অনবোর্ডিং চেকলিস্ট + পরীক্ষাকাল + সিভি নথি আপনা-আপনি তৈরি হয়।
 */
@Transactional
class RecruitmentService {

    def accessService
    def employmentService
    def onboardingService
    def salaryService

    private static Map fail(String m) { [ok: false, message: m] }

    private static LocalDate today() { LocalDate.now(AttendanceService.ZONE) }

    private String actor() { accessService.currentUser()?.username }

    private static final String FIX_FORM = 'ফর্মের লাল ঘরগুলো ঠিক করুন।'

    // ================================================================== জনবল চাহিদা
    Map saveRequisition(ManpowerRequisition r) {
        r.status = 'PENDING'
        r.requestedBy = actor()
        r.validate()
        if (r.expectedJoinDate != null && r.expectedJoinDate.isBefore(today())) {
            r.errors.rejectValue('expectedJoinDate', 'invalid', 'প্রত্যাশিত যোগদানের তারিখ অতীতে হতে পারে না।')
        }
        if (r.hasErrors()) return [ok: false, requisition: r, message: FIX_FORM]
        r.save(failOnError: true, flush: true)
        [ok: true, requisition: r, message: 'জনবল চাহিদা জমা হয়েছে - HR/Admin এর অনুমোদনের অপেক্ষায়।']
    }

    Map approveRequisition(ManpowerRequisition r, String note) {
        if (r.status != 'PENDING') return fail('শুধু অপেক্ষমাণ চাহিদা অনুমোদন করা যায়।')
        decide(r, 'APPROVED', note)
        [ok: true, message: 'চাহিদা অনুমোদিত হয়েছে - এখন চাকরির বিজ্ঞপ্তি দেওয়া যাবে।']
    }

    Map rejectRequisition(ManpowerRequisition r, String note) {
        if (r.status != 'PENDING') return fail('শুধু অপেক্ষমাণ চাহিদা প্রত্যাখ্যান করা যায়।')
        if (!note?.trim()) return fail('প্রত্যাখ্যানের কারণ লিখুন।')
        decide(r, 'REJECTED', note)
        [ok: true, message: 'চাহিদা প্রত্যাখ্যাত হয়েছে।']
    }

    private void decide(ManpowerRequisition r, String status, String note) {
        r.status = status
        r.decidedBy = actor()
        r.decidedAt = LocalDateTime.now(AttendanceService.ZONE)
        r.decisionNote = note?.trim() ?: null
        r.save(failOnError: true, flush: true)
    }

    /** আবেদনকারী (বা HR/Admin) অপেক্ষমাণ চাহিদা প্রত্যাহার করতে পারে */
    Map cancelRequisition(ManpowerRequisition r) {
        if (r.status != 'PENDING') return fail('শুধু অপেক্ষমাণ চাহিদা প্রত্যাহার করা যায়।')
        if (!accessService.isHrAdmin() && r.requestedBy != actor()) return fail('অন্যের চাহিদা প্রত্যাহার করা যায় না।')
        r.status = 'CANCELLED'
        r.save(failOnError: true, flush: true)
        [ok: true, message: 'চাহিদা প্রত্যাহার করা হয়েছে।']
    }

    /** চাহিদা বন্ধ: এর সব খোলা বিজ্ঞপ্তিও বন্ধ হয় */
    Map closeRequisition(ManpowerRequisition r) {
        if (r.status != 'APPROVED') return fail('শুধু অনুমোদিত চাহিদা বন্ধ করা যায়।')
        closeRequisitionInternal(r)
        [ok: true, message: 'চাহিদা ও এর সব বিজ্ঞপ্তি বন্ধ করা হয়েছে।']
    }

    private void closeRequisitionInternal(ManpowerRequisition r) {
        r.status = 'CLOSED'
        r.save(failOnError: true, flush: true)
        JobPosting.executeUpdate("update JobPosting p set p.status = 'CLOSED' where p.requisition = :r and p.status = 'OPEN'", [r: r])
    }

    // ================================================================== চাকরির বিজ্ঞপ্তি
    Map savePosting(JobPosting p) {
        p.postedBy = actor()
        p.validate()
        ManpowerRequisition r = p.requisition
        if (r != null) {
            if (r.status != 'APPROVED') {
                p.errors.rejectValue('requisition', 'invalid', 'শুধু অনুমোদিত চাহিদা থেকে বিজ্ঞপ্তি দেওয়া যায়।')
            } else if (r.remaining() <= 0) {
                p.errors.rejectValue('requisition', 'invalid', 'এই চাহিদার সব পদ পূরণ হয়ে গেছে।')
            }
        }
        if (p.deadline != null && p.deadline.isBefore(today())) {
            p.errors.rejectValue('deadline', 'invalid', 'আবেদনের শেষ তারিখ অতীতে হতে পারে না।')
        }
        if (p.hasErrors()) return [ok: false, posting: p, message: FIX_FORM]
        p.save(failOnError: true, flush: true)
        [ok: true, posting: p, message: "বিজ্ঞপ্তি '${p.title}' প্রকাশিত হয়েছে।".toString()]
    }

    Map closePosting(JobPosting p) {
        if (p.status != 'OPEN') return fail('বিজ্ঞপ্তিটি আগেই বন্ধ।')
        p.status = 'CLOSED'
        p.save(failOnError: true, flush: true)
        [ok: true, message: 'বিজ্ঞপ্তি বন্ধ করা হয়েছে।']
    }

    Map reopenPosting(JobPosting p) {
        if (p.status != 'CLOSED') return fail('বিজ্ঞপ্তিটি আগে থেকেই খোলা।')
        if (p.requisition.status != 'APPROVED' || p.requisition.remaining() <= 0) return fail('চাহিদাটি আর খোলা নেই বা সব পদ পূরণ হয়ে গেছে।')
        if (p.deadline != null && p.deadline.isBefore(today())) return fail('শেষ তারিখ পার হয়ে গেছে - নতুন বিজ্ঞপ্তি দিন।')
        p.status = 'OPEN'
        p.save(failOnError: true, flush: true)
        [ok: true, message: 'বিজ্ঞপ্তি আবার খোলা হয়েছে।']
    }

    // ================================================================== প্রার্থী
    Map addCandidate(Candidate c, MultipartFile resume) {
        c.createdBy = actor()
        c.currentStage = 'APPLIED'
        c.stageChangedAt = LocalDateTime.now(AttendanceService.ZONE)
        c.email = c.email?.trim()?.toLowerCase()
        c.validate()
        if (c.jobPosting != null && !c.jobPosting.acceptingApplications) {
            c.errors.rejectValue('jobPosting', 'invalid', 'এই বিজ্ঞপ্তিতে এখন আবেদন নেওয়া হচ্ছে না।')
        }

        boolean hasFile = resume != null && !resume.empty
        if (hasFile) {
            String ext = resume.originalFilename ? resume.originalFilename.toLowerCase().tokenize('.').last() : ''
            if (!Candidate.RESUME_EXTENSIONS.contains(ext)) return [ok: false, candidate: c, message: "সিভি শুধু ${Candidate.RESUME_EXTENSIONS.join(', ')} হতে পারে।".toString()]
            if (resume.size > Candidate.RESUME_MAX_BYTES) return [ok: false, candidate: c, message: 'সিভির সাইজ সর্বোচ্চ ২ MB হতে পারে।']
        }
        if (c.hasErrors()) return [ok: false, candidate: c, message: FIX_FORM]

        c.hasResume = hasFile
        c.save(failOnError: true, flush: true)
        if (hasFile) {
            new CandidateResume(candidate: c, fileName: new File(resume.originalFilename).name.take(255),
                    contentType: resume.contentType, fileSize: resume.size, fileData: resume.bytes).save(failOnError: true, flush: true)
        }
        [ok: true, candidate: c, message: "প্রার্থী '${c.name}' যোগ করা হয়েছে (ধাপ: আবেদন)।".toString()]
    }

    /** Kanban এ ধাপ বদল (ড্র্যাগ বা বাটন) */
    Map moveStage(Candidate c, String target) {
        if (c.currentStage in ['HIRED', 'REJECTED']) return fail(c.currentStage == 'HIRED' ? 'নিয়োগ হয়ে যাওয়া প্রার্থীর ধাপ বদলানো যায় না।' : 'প্রত্যাখ্যাত প্রার্থীকে আগে "আবার খুলুন" দিন।')
        if (!Candidate.MOVABLE_STAGES.contains(target)) return fail('এই ধাপে সরাসরি নেওয়া যায় না - HIRED হয় কর্মীতে রূপান্তরে, আর বাদ দিতে "প্রত্যাখ্যান" ব্যবহার করুন।')
        if (c.currentStage == target) return [ok: true, message: 'ধাপ অপরিবর্তিত।']

        if (target == 'OFFER') {
            boolean recommended = Interview.countByCandidateAndStatusAndRecommendationInList(c, 'COMPLETED', ['HIRE', 'HOLD']) > 0
            if (!recommended) return fail('অফার ধাপে নিতে অন্তত একটি সম্পন্ন ইন্টারভিউয়ে HIRE/HOLD সুপারিশ লাগবে।')
        }
        if (c.currentStage == 'OFFER' && JobOffer.findAllByCandidate(c).any { JobOffer o -> o.live }) {
            return fail('চলমান অফার আছে - আগে অফার প্রত্যাহার করুন।')
        }
        c.currentStage = target
        c.stageChangedAt = LocalDateTime.now(AttendanceService.ZONE)
        c.save(failOnError: true, flush: true)
        [ok: true, message: "'${c.name}' এখন ${target} ধাপে।".toString()]
    }

    Map rejectCandidate(Candidate c, String reason) {
        if (c.currentStage in ['HIRED', 'REJECTED']) return fail('এই প্রার্থীকে আর প্রত্যাখ্যান করা যায় না।')
        if (!reason?.trim()) return fail('প্রত্যাখ্যানের কারণ লিখুন।')
        closeOpenWork(c, 'প্রার্থী প্রত্যাখ্যাত')
        c.currentStage = 'REJECTED'
        c.rejectReason = reason.trim().take(255)
        c.stageChangedAt = LocalDateTime.now(AttendanceService.ZONE)
        c.save(failOnError: true, flush: true)
        [ok: true, message: "'${c.name}' প্রত্যাখ্যাত হয়েছে।".toString()]
    }

    /** প্রত্যাখ্যাত প্রার্থীকে আবার SCREENING এ ফেরানো */
    Map reopenCandidate(Candidate c) {
        if (c.currentStage != 'REJECTED') return fail('শুধু প্রত্যাখ্যাত প্রার্থীকে আবার খোলা যায়।')
        c.currentStage = 'SCREENING'
        c.rejectReason = null
        c.stageChangedAt = LocalDateTime.now(AttendanceService.ZONE)
        c.save(failOnError: true, flush: true)
        [ok: true, message: "'${c.name}' আবার বাছাই ধাপে ফিরেছে।".toString()]
    }

    /** প্রার্থী বাদ পড়লে চলমান ইন্টারভিউ ও অফার বন্ধ হয় */
    private void closeOpenWork(Candidate c, String why) {
        Interview.findAllByCandidateAndStatus(c, 'SCHEDULED').each { Interview i ->
            i.status = 'CANCELLED'
            i.save(failOnError: true)
        }
        JobOffer.findAllByCandidateAndStatus(c, 'SENT').each { JobOffer o ->
            o.status = 'WITHDRAWN'
            o.decidedAt = LocalDateTime.now(AttendanceService.ZONE)
            o.decisionNote = why
            o.save(failOnError: true)
        }
    }

    // ================================================================== ইন্টারভিউ
    Map scheduleInterview(Interview iv) {
        Candidate c = iv.candidate
        iv.scheduledBy = actor()
        iv.status = 'SCHEDULED'
        iv.validate()
        if (c != null) {
            if (!(c.currentStage in ['SCREENING', 'INTERVIEW'])) {
                return [ok: false, interview: iv, message: 'ইন্টারভিউ দিতে প্রার্থীকে আগে "বাছাই" (SCREENING) বা "ইন্টারভিউ" ধাপে নিন।']
            }
            if (iv.scheduledAt != null && iv.scheduledAt.isBefore(LocalDateTime.now(AttendanceService.ZONE).minusDays(7))) {
                iv.errors.rejectValue('scheduledAt', 'invalid', 'সময় ৭ দিনের বেশি আগের হতে পারে না।')
            }
        }
        if (iv.interviewer != null && iv.interviewer.status == 'INACTIVE') {
            iv.errors.rejectValue('interviewer', 'invalid', 'নিষ্ক্রিয় কর্মী ইন্টারভিউয়ার হতে পারে না।')
        }
        if (iv.scheduledAt != null && iv.interviewer != null && iv.durationMinutes != null && !iv.hasErrors()) {
            Interview clash = Interview.findAllByInterviewerAndStatus(iv.interviewer, 'SCHEDULED').find { Interview o ->
                iv.scheduledAt < o.endsAt() && o.scheduledAt < iv.endsAt()
            }
            if (clash) return [ok: false, interview: iv, message: "${iv.interviewer.name} ওই সময়ে আরেকটি ইন্টারভিউতে আছেন (${clash.candidate.name})।".toString()]
        }
        if (iv.hasErrors()) return [ok: false, interview: iv, message: FIX_FORM]

        iv.roundNo = Interview.countByCandidateAndStatusNotEqual(c, 'CANCELLED') + 1
        iv.save(failOnError: true, flush: true)
        if (c.currentStage == 'SCREENING') {
            c.currentStage = 'INTERVIEW'
            c.stageChangedAt = LocalDateTime.now(AttendanceService.ZONE)
            c.save(failOnError: true, flush: true)
        }
        [ok: true, interview: iv, message: "রাউন্ড ${iv.roundNo} ইন্টারভিউ নির্ধারিত হয়েছে।".toString()]
    }

    Map cancelInterview(Interview iv) {
        if (iv.status != 'SCHEDULED') return fail('শুধু নির্ধারিত ইন্টারভিউ বাতিল করা যায়।')
        iv.status = 'CANCELLED'
        iv.save(failOnError: true, flush: true)
        [ok: true, message: 'ইন্টারভিউ বাতিল হয়েছে।']
    }

    /** ইন্টারভিউয়ার নিজে (বা HR/Admin) ফিডব্যাক দেয় */
    Map giveFeedback(Interview iv, Integer rating, String recommendation, String text) {
        if (iv.status != 'SCHEDULED') return fail('এই ইন্টারভিউয়ের ফিডব্যাক আগেই দেওয়া হয়েছে বা এটি বাতিল।')
        boolean allowed = accessService.isHrAdmin() || accessService.currentEmployee()?.id == iv.interviewer.id
        if (!allowed) return fail('শুধু ইন্টারভিউয়ার বা HR এই ফিডব্যাক দিতে পারে।')
        if (iv.scheduledAt.isAfter(LocalDateTime.now(AttendanceService.ZONE))) return fail('ইন্টারভিউয়ের সময় হওয়ার আগে ফিডব্যাক দেওয়া যায় না।')
        if (rating == null || rating < 1 || rating > 5) return fail('রেটিং ১ থেকে ৫ এর মধ্যে দিন।')
        if (!Interview.RECOMMENDATIONS.contains(recommendation)) return fail('সুপারিশ বেছে নিন (HIRE / HOLD / REJECT)।')
        if (!text?.trim() || text.trim().length() < 5) return fail('ফিডব্যাক লিখুন (অন্তত ৫ অক্ষর)।')

        iv.rating = rating
        iv.recommendation = recommendation
        iv.feedback = text.trim().take(1500)
        iv.feedbackAt = LocalDateTime.now(AttendanceService.ZONE)
        iv.feedbackBy = actor()
        iv.status = 'COMPLETED'
        iv.save(failOnError: true, flush: true)
        [ok: true, message: 'ফিডব্যাক সংরক্ষিত হয়েছে।']
    }

    // ================================================================== অফার
    Map saveOffer(JobOffer o) {
        Candidate c = o.candidate
        o.offeredBy = actor()
        o.status = 'SENT'
        o.validate()
        if (c != null) {
            if (!(c.currentStage in ['INTERVIEW', 'OFFER'])) return [ok: false, offer: o, message: 'অফার দিতে প্রার্থীকে ইন্টারভিউ ধাপ পেরোতে হবে।']
            if (JobOffer.findAllByCandidate(c).any { JobOffer x -> x.live }) return [ok: false, offer: o, message: 'এই প্রার্থীর একটি চলমান অফার আছে - আগে সেটি প্রত্যাহার করুন।']
            if (Employee.countByEmail(c.email) > 0) return [ok: false, offer: o, message: 'এই ইমেইলে আগেই একজন কর্মী আছে।']
        }
        if (o.joinDate != null && o.joinDate.isBefore(today())) o.errors.rejectValue('joinDate', 'invalid', 'যোগদানের তারিখ অতীতে হতে পারে না।')
        if (o.validUntil != null) {
            if (o.validUntil.isBefore(today())) o.errors.rejectValue('validUntil', 'invalid', 'অফারের মেয়াদ অতীতে হতে পারে না।')
            else if (o.joinDate != null && o.validUntil.isAfter(o.joinDate)) o.errors.rejectValue('validUntil', 'invalid', 'অফারের মেয়াদ যোগদানের তারিখের আগে শেষ হতে হবে।')
        }
        if (o.hasErrors()) return [ok: false, offer: o, message: FIX_FORM]

        if (c.currentStage == 'INTERVIEW') {
            Map moved = moveStage(c, 'OFFER')
            if (!moved.ok) return [ok: false, offer: o, message: moved.message]
        }
        o.save(failOnError: true, flush: true)
        [ok: true, offer: o, message: 'অফার পাঠানো হয়েছে (ধাপ: অফার)।']
    }

    Map respondOffer(JobOffer o, boolean accept, String note) {
        if (o.status != 'SENT') return fail('শুধু পাঠানো অফারে উত্তর রেকর্ড করা যায়।')
        if (accept && o.expired) return fail('অফারের মেয়াদ শেষ - নতুন অফার দিন।')
        if (!accept && !note?.trim()) return fail('প্রত্যাখ্যানের কারণ লিখুন।')
        o.status = accept ? 'ACCEPTED' : 'DECLINED'
        o.decidedAt = LocalDateTime.now(AttendanceService.ZONE)
        o.decisionNote = note?.trim()?.take(255)
        o.save(failOnError: true, flush: true)
        if (!accept) {
            Candidate c = o.candidate
            c.currentStage = 'REJECTED'
            c.rejectReason = ('অফার প্রত্যাখ্যান (প্রার্থী): ' + note.trim()).take(255)
            c.stageChangedAt = LocalDateTime.now(AttendanceService.ZONE)
            c.save(failOnError: true, flush: true)
        }
        [ok: true, message: accept ? 'অফার গৃহীত - এখন কর্মীতে রূপান্তর করা যাবে।' : 'অফার প্রত্যাখ্যাত হয়েছে, প্রার্থী বাদ পড়েছে।']
    }

    Map withdrawOffer(JobOffer o, String note) {
        if (!o.live) return fail('শুধু চলমান অফার প্রত্যাহার করা যায়।')
        if (o.candidate.employee != null) return fail('প্রার্থী ইতিমধ্যে কর্মী হয়ে গেছে।')
        if (!note?.trim()) return fail('প্রত্যাহারের কারণ লিখুন।')
        o.status = 'WITHDRAWN'
        o.decidedAt = LocalDateTime.now(AttendanceService.ZONE)
        o.decisionNote = note.trim().take(255)
        o.save(failOnError: true, flush: true)
        [ok: true, message: 'অফার প্রত্যাহার করা হয়েছে।']
    }

    JobOffer currentOffer(Candidate c) {
        JobOffer.findAllByCandidate(c, [sort: 'id', order: 'desc']).find { JobOffer o -> o.live } ?:
                JobOffer.findByCandidate(c, [sort: 'id', order: 'desc'])
    }

    // ================================================================== কর্মীতে রূপান্তর
    Map convertToEmployee(Candidate c, Employee manager, Grade grade) {
        if (c.employee != null) return fail('এই প্রার্থী আগেই কর্মীতে রূপান্তরিত হয়েছে।')
        if (c.currentStage != 'OFFER') return fail('শুধু অফার ধাপের প্রার্থীকে রূপান্তর করা যায়।')
        JobOffer o = JobOffer.findByCandidateAndStatus(c, 'ACCEPTED')
        if (o == null) return fail('গৃহীত (ACCEPTED) অফার ছাড়া রূপান্তর করা যায় না।')
        if (Employee.countByEmail(c.email) > 0) return fail('এই ইমেইলে আগেই একজন কর্মী আছে।')

        String type = o.probationMonths > 0 ? 'PROBATION' : o.employeeType
        Employee emp = new Employee(name: c.name, email: c.email, phone: c.phone, joinDate: o.joinDate, salary: o.salary,
                department: o.department, designation: o.designation, branch: o.branch, grade: grade,
                reportingManager: manager, employeeType: type, status: 'ACTIVE')
        if (!emp.save(flush: true)) {
            return fail('কর্মী তৈরি করা যায়নি: ' + emp.errors.allErrors.collect { it.defaultMessage }.join('; '))
        }

        String by = actor()
        employmentService.logJoined(emp, by)
        salaryService.currentStructure(emp)                       // যোগদানের দিন থেকে বেতন কাঠামো
        int tasks = onboardingService.startOnboarding(emp)
        if (o.probationMonths > 0) onboardingService.startProbation(emp, o.joinDate, o.probationMonths, o.employeeType)

        CandidateResume cv = CandidateResume.findByCandidate(c)
        if (cv != null) {
            new EmployeeDocument(employee: emp, docType: 'CV', title: "CV - ${c.name}".toString(), fileName: cv.fileName,
                    contentType: cv.contentType, fileSize: cv.fileSize, fileData: cv.fileData, uploadedBy: by).save(failOnError: true)
        }

        c.employee = emp
        c.currentStage = 'HIRED'
        c.stageChangedAt = LocalDateTime.now(AttendanceService.ZONE)
        c.save(failOnError: true, flush: true)
        // সব পদ পূরণ হলে চাহিদা (ও এর বিজ্ঞপ্তি) বন্ধ
        ManpowerRequisition req = c.jobPosting.requisition
        String extra = ''
        if (req.status == 'APPROVED' && req.hiredCount() >= req.vacancies) {
            closeRequisitionInternal(req)
            extra = ' সব পদ পূরণ হওয়ায় চাহিদা বন্ধ হয়েছে।'
        }
        [ok: true, employee: emp, message: "'${emp.name}' এখন কর্মী। অনবোর্ডিং চেকলিস্টে ${tasks}টি কাজ তৈরি হয়েছে।${extra}".toString()]
    }

    // ================================================================== বোর্ড ও পরিসংখ্যান
    /** Kanban এর তথ্য: ধাপ -> প্রার্থী (প্রত্যাখ্যাত বাদে), পরের ইন্টারভিউ, সাম্প্রতিক প্রত্যাখ্যাতদের তালিকা (সর্বোচ্চ ৫০) */
    Map boardData(JobPosting posting) {
        List<Candidate> list = Candidate.createCriteria().list {
            if (posting) eq('jobPosting', posting)
            ne('currentStage', 'REJECTED')
            order('stageChangedAt', 'asc')
        } as List<Candidate>
        Map<String, List<Candidate>> columns = Candidate.BOARD_STAGES.collectEntries { String s -> [(s): [] as List<Candidate>] }
        list.each { Candidate c -> columns[c.currentStage] << c }

        Map<Long, Interview> next = [:]
        if (list) {
            Interview.findAllByCandidateInListAndStatus(list, 'SCHEDULED', [sort: 'scheduledAt', order: 'desc']).each { Interview i ->
                next[i.candidate.id] = i                          // সবচেয়ে কাছেরটা শেষে বসে
            }
        }
        List<Candidate> rejected = Candidate.createCriteria().list(max: 50) {
            if (posting) eq('jobPosting', posting)
            eq('currentStage', 'REJECTED')
            order('stageChangedAt', 'desc')
        } as List<Candidate>
        [columns: columns, nextInterview: next, rejected: rejected, total: list.size()]
    }

    Map overview() {
        [pendingRequisitions: ManpowerRequisition.countByStatus('PENDING'),
         openPostings       : JobPosting.countByStatus('OPEN'),
         activeCandidates   : Candidate.countByCurrentStageInList(Candidate.MOVABLE_STAGES),
         acceptedOffers     : JobOffer.countByStatus('ACCEPTED')]
    }
}

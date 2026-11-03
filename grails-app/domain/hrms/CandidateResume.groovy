package hrms

/** Day 7: প্রার্থীর সিভি (আলাদা টেবিলে, যাতে প্রার্থীর তালিকা হালকা থাকে) */
class CandidateResume {

    Candidate candidate
    String fileName
    String contentType
    Long fileSize
    byte[] fileData

    static constraints = {
        candidate nullable: false, unique: true
        fileName blank: false, maxSize: 255
        contentType nullable: true, maxSize: 100
        fileSize nullable: true
        fileData nullable: false, maxSize: Candidate.RESUME_MAX_BYTES
    }

    static mapping = {
        table 'hrms_candidate_resume'
        fileData sqlType: 'blob'
    }
}

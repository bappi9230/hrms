package hrms

import java.time.LocalDate

/** কর্মীর নথি (NID, সার্টিফিকেট, চুক্তিপত্র ইত্যাদি) - মেয়াদ শেষের তারিখসহ */
class EmployeeDocument {

    static final List<String> TYPES = ['NID', 'PASSPORT', 'CERTIFICATE', 'CONTRACT', 'CV', 'OTHER']
    static final List<String> ALLOWED_EXTENSIONS = ['pdf', 'jpg', 'jpeg', 'png', 'doc', 'docx']
    static final int MAX_BYTES = 2 * 1024 * 1024   // 2 MB (application.yml এর multipart সীমার সাথে মিল রাখা)
    static final int EXPIRING_DAYS = 30            // এত দিনের মধ্যে মেয়াদ শেষ হলে "শিগগির শেষ" ধরা হবে

    Employee employee
    String docType = 'OTHER'
    String title
    String fileName
    String contentType
    Long fileSize
    byte[] fileData
    LocalDate expiryDate
    String uploadedBy
    Date dateCreated

    static constraints = {
        employee nullable: false
        docType inList: TYPES
        title blank: false, maxSize: 150
        fileName blank: false, maxSize: 255
        contentType nullable: true, maxSize: 100
        fileSize nullable: true
        fileData nullable: false, maxSize: MAX_BYTES
        expiryDate nullable: true
        uploadedBy nullable: true, maxSize: 50
    }

    static mapping = {
        fileData sqlType: 'blob'
    }

    /** NONE (মেয়াদ নেই) | EXPIRED | EXPIRING | VALID */
    static String expiryStatusOf(LocalDate date) {
        if (date == null) return 'NONE'
        LocalDate today = LocalDate.now()
        if (date.isBefore(today)) return 'EXPIRED'
        if (!date.isAfter(today.plusDays(EXPIRING_DAYS))) return 'EXPIRING'
        'VALID'
    }
}

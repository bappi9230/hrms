package hrms

import grails.plugin.springsecurity.annotation.Secured
import org.springframework.web.multipart.MultipartFile

import java.time.LocalDate
import java.time.format.DateTimeParseException

/** কর্মীর নথি আপলোড / ডাউনলোড / মোছা */
@Secured(['ROLE_ADMIN', 'ROLE_HR'])
class DocumentController {

    static allowedMethods = [upload: 'POST', delete: 'POST']

    def accessService

    /** id = কর্মীর id */
    def upload(Long id) {
        Employee employee = id ? Employee.get(id) : null
        if (!employee) {
            flash.error = 'কর্মী পাওয়া যায়নি।'
            redirect(controller: 'employee', action: 'index')
            return
        }

        String error = null
        MultipartFile file = request.getFile('file')
        String ext = file?.originalFilename ? file.originalFilename.toLowerCase().tokenize('.').last() : ''
        LocalDate expiry = null

        if (!file || file.empty) {
            error = 'ফাইল নির্বাচন করুন।'
        } else if (!EmployeeDocument.ALLOWED_EXTENSIONS.contains(ext)) {
            error = "ফাইল শুধু ${EmployeeDocument.ALLOWED_EXTENSIONS.join(', ')} হতে পারে।".toString()
        } else if (file.size > EmployeeDocument.MAX_BYTES) {
            error = 'ফাইলের সাইজ সর্বোচ্চ ২ MB হতে পারে।'
        }
        if (!error && params.expiryDate) {
            try {
                expiry = LocalDate.parse(params.expiryDate as String)
            } catch (DateTimeParseException ignored) {
                error = 'মেয়াদ শেষের তারিখ সঠিক নয়।'
            }
        }

        if (!error) {
            String docType = EmployeeDocument.TYPES.contains(params.docType) ? params.docType : 'OTHER'
            String cleanName = new File(file.originalFilename).name.take(255)
            String title = params.title?.toString()?.trim() ?: cleanName
            EmployeeDocument doc = new EmployeeDocument(
                    employee: employee, docType: docType, title: title.take(150),
                    fileName: cleanName, contentType: file.contentType, fileSize: file.size,
                    fileData: file.bytes, expiryDate: expiry, uploadedBy: accessService.currentUser()?.username)
            if (doc.save(flush: true)) {
                flash.success = "নথি '${doc.title}' আপলোড হয়েছে।".toString()
            } else {
                error = doc.errors.allErrors.collect { g.message(error: it) }.join('; ')
            }
        }
        if (error) flash.error = error
        redirect(controller: 'employee', action: 'show', id: employee.id, fragment: 'tab-documents')
    }

    /** HR/Admin সবার নথি, কর্মী শুধু নিজের নথি নামাতে পারে */
    @Secured(['isAuthenticated()'])
    def download(Long id) {
        EmployeeDocument doc = id ? EmployeeDocument.get(id) : null
        if (!doc) {
            response.sendError(404)
            return
        }
        boolean allowed = accessService.isHrAdmin() || accessService.currentEmployee()?.id == doc.employee.id
        if (!allowed) {
            redirect(controller: 'auth', action: 'denied')
            return
        }
        response.contentType = doc.contentType ?: 'application/octet-stream'
        response.setHeader('Content-Disposition',
                "attachment; filename*=UTF-8''" + URLEncoder.encode(doc.fileName, 'UTF-8').replace('+', '%20'))
        response.setHeader('X-Content-Type-Options', 'nosniff')
        response.outputStream << doc.fileData
        response.outputStream.flush()
    }

    def delete(Long id) {
        EmployeeDocument doc = id ? EmployeeDocument.get(id) : null
        if (!doc) {
            flash.error = 'নথি পাওয়া যায়নি।'
            redirect(controller: 'employee', action: 'index')
            return
        }
        Long employeeId = doc.employee.id
        String title = doc.title
        doc.delete(flush: true)
        flash.success = "নথি '${title}' মুছে ফেলা হয়েছে।".toString()
        redirect(controller: 'employee', action: 'show', id: employeeId, fragment: 'tab-documents')
    }
}

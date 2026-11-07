package hrms

import grails.plugin.springsecurity.annotation.Secured

import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * Day 8: সম্পদ রেজিস্টার। দেখা: Admin/HR/হিসাব বিভাগ; বরাদ্দ-ফেরত-সম্পাদনা: Admin/HR; প্রত্যেক কর্মী নিজের সম্পদ (mine) দেখে।
 */
@Secured(['isAuthenticated()'])
class AssetController {

    static allowedMethods = [save: 'POST', update: 'POST', issue: 'POST', receive: 'POST', changeStatus: 'POST', delete: 'POST']

    def accessService
    def assetService

    private static LocalDate today() { LocalDate.now(AttendanceService.ZONE) }

    @Secured(['ROLE_ADMIN', 'ROLE_HR', 'ROLE_ACCOUNTANT'])
    def index() {
        String status = Asset.STATUSES.contains(params.status) ? params.status : null
        String type = Asset.TYPES.contains(params.type) ? params.type : null
        String q = params.q?.toString()?.trim()
        List<Asset> assets = Asset.createCriteria().list {
            if (status) eq('status', status)
            if (type) eq('assetType', type)
            if (q) {
                or {
                    ilike('assetTag', "%${q}%")
                    ilike('name', "%${q}%")
                    ilike('serialNo', "%${q}%")
                }
            }
            order('assetTag', 'asc')
        } as List<Asset>
        Map<Long, AssetAllocation> holders = [:]
        if (assets) {
            AssetAllocation.findAllByStatusAndAssetInList('ISSUED', assets).each { AssetAllocation al -> holders[al.asset.id] = al }
        }
        Map<String, Integer> counts = Asset.STATUSES.collectEntries { String s -> [(s): Asset.countByStatus(s)] }
        [assets: assets, holders: holders, counts: counts, status: status, type: type, q: q, types: Asset.TYPES, statuses: Asset.STATUSES,
         canManage: accessService.isHrAdmin()]
    }

    /** কার কাছে কী আছে (সব ISSUED বরাদ্দ, রেজিস্টারের ও হাতে দেওয়া দুটোই) */
    @Secured(['ROLE_ADMIN', 'ROLE_HR', 'ROLE_ACCOUNTANT'])
    def holders() {
        List<AssetAllocation> issued = AssetAllocation.findAllByStatus('ISSUED', [sort: 'issuedDate', order: 'desc'])
        Map<Employee, List<AssetAllocation>> byEmployee = issued.groupBy { it.employee }
        List<Employee> employees = byEmployee.keySet().sort { Employee e -> e.name }
        [byEmployee: byEmployee, employees: employees, total: issued.size(), canManage: accessService.isHrAdmin()]
    }

    def mine() {
        Employee me = accessService.currentEmployee()
        [allocations: me ? AssetAllocation.findAllByEmployee(me, [sort: 'issuedDate', order: 'desc']) : [], hasEmployee: me != null]
    }

    @Secured(['ROLE_ADMIN', 'ROLE_HR', 'ROLE_ACCOUNTANT'])
    def show(Long id) {
        Asset a = id ? Asset.get(id) : null
        if (!a) { notFound(); return }
        [asset: a, history: AssetAllocation.findAllByAsset(a, [sort: 'issuedDate', order: 'desc']), current: assetService.currentAllocation(a),
         employees: accessService.isHrAdmin() ? Employee.findAllByStatusNotEqual('INACTIVE', [sort: 'name']) : [],
         canManage: accessService.isHrAdmin(), today: today()]
    }

    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def create() {
        render(view: 'form', model: [asset: new Asset(), types: Asset.TYPES])
    }

    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def save() {
        Asset a = new Asset()
        bind(a)
        Map r = assetService.saveAsset(a)
        if (r.ok) {
            flash.success = r.message
            redirect(action: 'show', id: a.id)
        } else {
            flash.error = r.message
            render(view: 'form', model: [asset: a, types: Asset.TYPES])
        }
    }

    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def edit(Long id) {
        Asset a = id ? Asset.get(id) : null
        if (!a) { notFound(); return }
        render(view: 'form', model: [asset: a, types: Asset.TYPES])
    }

    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def update(Long id) {
        Asset a = id ? Asset.get(id) : null
        if (!a) { notFound(); return }
        bind(a)
        Map r = assetService.saveAsset(a)
        if (r.ok) {
            flash.success = r.message
            redirect(action: 'show', id: a.id)
        } else {
            flash.error = r.message
            a.discard()
            render(view: 'form', model: [asset: a, types: Asset.TYPES])
        }
    }

    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def issue(Long id) {
        Asset a = id ? Asset.get(id) : null
        if (!a) { notFound(); return }
        Employee emp = params.long('employee') ? Employee.get(params.long('employee')) : null
        Map r = assetService.issue(a, emp, parseDate(params.issuedDate as String), params.remarks as String)
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'show', id: id)
    }

    /** id = বরাদ্দ (AssetAllocation) */
    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def receive(Long id) {
        AssetAllocation al = id ? AssetAllocation.get(id) : null
        if (!al || !al.asset) { notFound(); return }
        Map r = assetService.receive(al, parseDate(params.returnedDate as String), params.remarks as String, params.maintenance == 'true')
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'show', id: al.asset.id)
    }

    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def changeStatus(Long id) {
        Asset a = id ? Asset.get(id) : null
        if (!a) { notFound(); return }
        Map r = assetService.setStatus(a, params.target as String)
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: 'show', id: id)
    }

    @Secured(['ROLE_ADMIN', 'ROLE_HR'])
    def delete(Long id) {
        Asset a = id ? Asset.get(id) : null
        if (!a) { notFound(); return }
        Map r = assetService.deleteAsset(a)
        flash[r.ok ? 'success' : 'error'] = r.message
        redirect(action: r.ok ? 'index' : 'show', id: r.ok ? null : id)
    }

    // ------------------------------------------------------------------ helpers
    private void bind(Asset a) {
        if (a.id == null) a.assetTag = params.assetTag?.toString()?.trim()
        a.assetType = params.assetType
        a.name = params.name as String
        a.serialNo = params.serialNo as String
        a.purchaseDate = parseDate(params.purchaseDate as String)
        try { a.purchaseCost = params.purchaseCost?.toString()?.trim() ? new BigDecimal(params.purchaseCost.toString().trim()) : null } catch (NumberFormatException ignored) { a.purchaseCost = null }
        a.remarks = params.remarks as String
    }

    private static LocalDate parseDate(String s) {
        try { s ? LocalDate.parse(s) : null } catch (DateTimeParseException ignored) { null }
    }

    private void notFound() {
        flash.error = 'সম্পদ পাওয়া যায়নি।'
        redirect(action: 'index')
    }
}

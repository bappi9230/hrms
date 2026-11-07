<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>${asset.assetTag}</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <div>
        <h1 class="page-title mb-1">${asset.assetTag} - ${asset.name}</h1>
        <hrms:d8Badge kind="asset" status="${asset.status}"/>
    </div>
    <div class="d-flex gap-2">
        <g:if test="${canManage}"><g:link action="edit" id="${asset.id}" class="btn btn-outline-primary"><i class="bi bi-pencil"></i> সম্পাদনা</g:link></g:if>
        <g:link action="index" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> রেজিস্টার</g:link>
    </div>
</div>

<div class="row g-3">
    <div class="col-lg-5">
        <div class="card mb-3">
            <table class="table table-sm mb-0">
                <tbody>
                <tr><td>ধরন</td><td class="text-end"><hrms:d8Name value="${asset.assetType}"/></td></tr>
                <tr><td>সিরিয়াল</td><td class="text-end">${asset.serialNo ?: '—'}</td></tr>
                <tr><td>ক্রয়ের তারিখ</td><td class="text-end"><hrms:date value="${asset.purchaseDate}"/></td></tr>
                <tr><td>ক্রয়মূল্য</td><td class="text-end"><hrms:money value="${asset.purchaseCost}"/></td></tr>
                <tr><td>মন্তব্য</td><td class="text-end">${asset.remarks ?: '—'}</td></tr>
                <tr><td>বর্তমানে কার কাছে</td><td class="text-end fw-semibold">${current?.employee?.name ?: '—'}</td></tr>
                </tbody>
            </table>
        </div>

        <g:if test="${canManage}">
            <g:if test="${asset.status == 'AVAILABLE'}">
                <div class="card mb-3">
                    <div class="card-header bg-white fw-semibold">কর্মীকে বরাদ্দ</div>
                    <div class="card-body">
                        <g:form action="issue" id="${asset.id}" method="POST" class="row g-2">
                            <div class="col-12">
                                <select name="employee" class="form-select" required>
                                    <option value="">— কর্মী নির্বাচন —</option>
                                    <g:each in="${employees}" var="e"><option value="${e.id}">${e.name}</option></g:each>
                                </select>
                            </div>
                            <div class="col-md-5"><input type="date" name="issuedDate" value="${today}" max="${today}" class="form-control"></div>
                            <div class="col-md-7"><input type="text" name="remarks" maxlength="255" class="form-control" placeholder="মন্তব্য (ঐচ্ছিক)"></div>
                            <div class="col-12"><button type="submit" class="btn btn-primary w-100">বরাদ্দ করুন</button></div>
                        </g:form>
                    </div>
                </div>
            </g:if>
            <g:if test="${asset.status == 'ASSIGNED' && current}">
                <div class="card mb-3">
                    <div class="card-header bg-white fw-semibold">ফেরত নিন</div>
                    <div class="card-body">
                        <g:form action="receive" id="${current.id}" method="POST" class="row g-2">
                            <div class="col-md-5"><input type="date" name="returnedDate" value="${today}" min="${current.issuedDate}" max="${today}" class="form-control"></div>
                            <div class="col-md-7"><input type="text" name="remarks" maxlength="255" class="form-control" placeholder="অবস্থা / মন্তব্য"></div>
                            <div class="col-12 form-check ms-2">
                                <input type="checkbox" class="form-check-input" id="maint" name="maintenance" value="true">
                                <label class="form-check-label" for="maint">মেরামত দরকার (MAINTENANCE এ যাবে)</label>
                            </div>
                            <div class="col-12"><button type="submit" class="btn btn-outline-primary w-100">ফেরত নেওয়া হয়েছে</button></div>
                        </g:form>
                    </div>
                </div>
            </g:if>
            <div class="d-flex flex-wrap gap-2">
                <g:if test="${asset.status == 'AVAILABLE'}">
                    <g:form action="changeStatus" id="${asset.id}" method="POST"><input type="hidden" name="target" value="MAINTENANCE"><button class="btn btn-outline-warning" type="submit">মেরামতে পাঠান</button></g:form>
                </g:if>
                <g:if test="${asset.status == 'MAINTENANCE'}">
                    <g:form action="changeStatus" id="${asset.id}" method="POST"><input type="hidden" name="target" value="AVAILABLE"><button class="btn btn-outline-success" type="submit">মেরামত শেষ - খালি করুন</button></g:form>
                </g:if>
                <g:if test="${asset.status in ['AVAILABLE', 'MAINTENANCE']}">
                    <g:form action="changeStatus" id="${asset.id}" method="POST" onsubmit="return confirm('সম্পদটি বাতিল (RETIRED) করবেন? আর ফেরানো যাবে না।');"><input type="hidden" name="target" value="RETIRED"><button class="btn btn-outline-secondary" type="submit">বাতিল (RETIRE)</button></g:form>
                </g:if>
                <g:if test="${!history}">
                    <g:form action="delete" id="${asset.id}" method="POST" onsubmit="return confirm('সম্পদ মুছে ফেলবেন?');"><button class="btn btn-outline-danger" type="submit">মুছুন</button></g:form>
                </g:if>
            </div>
        </g:if>
    </div>

    <div class="col-lg-7">
        <div class="card">
            <div class="card-header bg-white fw-semibold">বরাদ্দের ইতিহাস</div>
            <div class="table-responsive">
                <table class="table table-sm align-middle mb-0">
                    <thead class="table-light"><tr><th>কর্মী</th><th>বরাদ্দ</th><th>ফেরত</th><th>মন্তব্য</th></tr></thead>
                    <tbody>
                    <g:each in="${history}" var="h">
                        <tr>
                            <td>${h.employee.name}</td>
                            <td><hrms:date value="${h.issuedDate}"/></td>
                            <td>${h.returnedDate ? '' : ''}<hrms:date value="${h.returnedDate}"/></td>
                            <td class="small">${h.remarks ?: ''}</td>
                        </tr>
                    </g:each>
                    <g:if test="${!history}"><tr><td colspan="4" class="text-center text-muted py-3">এখনো কাউকে দেওয়া হয়নি।</td></tr></g:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
</body>
</html>

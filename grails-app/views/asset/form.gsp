<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>${asset.id ? 'সম্পদ সম্পাদনা' : 'নতুন সম্পদ'}</title>
</head>
<body>
<h1 class="page-title">${asset.id ? 'সম্পদ সম্পাদনা - ' + asset.assetTag : 'নতুন সম্পদ'}</h1>

<div class="card" style="max-width:700px;">
    <div class="card-body">
        <g:form action="${asset.id ? 'update' : 'save'}" id="${asset.id}" method="POST" class="row g-3">
            <g:if test="${!asset.id}">
                <div class="col-md-4">
                    <label class="form-label" for="assetTag">ট্যাগ</label>
                    <input type="text" id="assetTag" name="assetTag" value="${asset.assetTag}" maxlength="30" class="form-control" placeholder="ফাঁকা = স্বয়ংক্রিয়">
                </div>
            </g:if>
            <div class="col-md-4">
                <label class="form-label" for="assetType">ধরন <span class="text-danger">*</span></label>
                <select id="assetType" name="assetType" class="form-select"><g:each in="${types}" var="t"><option value="${t}" ${asset.assetType == t ? 'selected' : ''}><hrms:d8Name value="${t}"/></option></g:each></select>
            </div>
            <div class="col-md-8">
                <label class="form-label" for="name">নাম / মডেল <span class="text-danger">*</span></label>
                <input type="text" id="name" name="name" value="${asset.name}" maxlength="120" class="form-control" required>
            </div>
            <div class="col-md-4">
                <label class="form-label" for="serialNo">সিরিয়াল নম্বর</label>
                <input type="text" id="serialNo" name="serialNo" value="${asset.serialNo}" maxlength="80" class="form-control">
            </div>
            <div class="col-md-6">
                <label class="form-label" for="purchaseDate">ক্রয়ের তারিখ</label>
                <input type="date" id="purchaseDate" name="purchaseDate" value="${asset.purchaseDate}" class="form-control">
            </div>
            <div class="col-md-6">
                <label class="form-label" for="purchaseCost">ক্রয়মূল্য (৳)</label>
                <input type="number" id="purchaseCost" name="purchaseCost" value="${asset.purchaseCost}" min="0" step="0.01" class="form-control">
            </div>
            <div class="col-12">
                <label class="form-label" for="remarks">মন্তব্য</label>
                <input type="text" id="remarks" name="remarks" value="${asset.remarks}" maxlength="255" class="form-control">
            </div>
            <div class="col-12">
                <button type="submit" class="btn btn-primary"><i class="bi bi-check-lg"></i> সংরক্ষণ</button>
                <g:link action="${asset.id ? 'show' : 'index'}" id="${asset.id}" class="btn btn-secondary">বাতিল</g:link>
            </div>
        </g:form>
    </div>
</div>
</body>
</html>

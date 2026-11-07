<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>চাকরি-ছাড়া শুরু (HR)</title>
</head>
<body>
<div class="d-flex justify-content-between align-items-center mb-3">
    <h1 class="page-title mb-0">HR এর পক্ষ থেকে চাকরি-ছাড়া শুরু</h1>
    <g:link action="index" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> ফিরে যান</g:link>
</div>
<div class="alert alert-info small">চাকরিচ্যুতি, অবসর বা চুক্তি শেষের ক্ষেত্রে কেস সরাসরি "গৃহীত" হয়ে ক্লিয়ারেন্স ধাপে যায়।</div>
<div class="card" style="max-width: 720px;">
    <div class="card-body">
        <g:form action="start" method="POST">
            <div class="mb-3">
                <label class="form-label">কর্মী</label>
                <select name="employee" class="form-select" required>
                    <option value="">-- বেছে নিন --</option>
                    <g:each in="${employees}" var="e"><option value="${e.id}" ${form.employee == e.id.toString() ? 'selected' : ''}>${e.name} (${e.department})</option></g:each>
                </select>
            </div>
            <div class="row g-3 mb-3">
                <div class="col-md-6">
                    <label class="form-label">ধরন</label>
                    <select name="exitType" class="form-select" required>
                        <g:each in="${types.findAll { it != 'RESIGNATION' }}" var="t"><option value="${t}" ${form.exitType == t ? 'selected' : ''}><hrms:d9Name value="${t}"/></option></g:each>
                    </select>
                </div>
                <div class="col-md-6">
                    <label class="form-label">কারণের ধরন</label>
                    <select name="reasonCategory" class="form-select" required>
                        <g:each in="${reasons}" var="r"><option value="${r}" ${form.reasonCategory == r ? 'selected' : ''}><hrms:d9Name value="${r}"/></option></g:each>
                    </select>
                </div>
            </div>
            <div class="mb-3">
                <label class="form-label">বিস্তারিত কারণ</label>
                <textarea name="reason" rows="3" maxlength="1000" class="form-control" required>${form.reason}</textarea>
            </div>
            <div class="mb-3">
                <label class="form-label">শেষ কর্মদিবস</label>
                <input type="date" name="lastDay" value="${form.lastDay}" min="${tomorrow}" class="form-control" required>
            </div>
            <button type="submit" class="btn btn-danger" onclick="return confirm('কেস শুরু করবেন? কর্মীকে জানানো হবে।');">কেস শুরু করুন</button>
        </g:form>
    </div>
</div>
</body>
</html>

<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>নতুন শৃঙ্খলা কেস</title>
</head>
<body>
<div class="d-flex justify-content-between align-items-center mb-3">
    <h1 class="page-title mb-0">নতুন শৃঙ্খলা কেস</h1>
    <g:link action="index" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> ফিরে যান</g:link>
</div>
<div class="card" style="max-width: 720px;">
    <div class="card-body">
        <g:form action="save" method="POST">
            <div class="mb-3">
                <label class="form-label">কর্মী</label>
                <select name="employee" class="form-select" required>
                    <option value="">-- বেছে নিন --</option>
                    <g:each in="${employees}" var="e"><option value="${e.id}" ${form.employee == e.id.toString() ? 'selected' : ''}>${e.name} (${e.department})</option></g:each>
                </select>
            </div>
            <div class="row g-3 mb-3">
                <div class="col-md-6">
                    <label class="form-label">কেসের ধরন</label>
                    <select name="caseType" class="form-select" required>
                        <g:each in="${types}" var="t">
                            <g:if test="${isHr || !(t in ['SUSPENSION', 'FINAL_WARNING'])}"><option value="${t}" ${form.caseType == t ? 'selected' : ''}><hrms:d9Name value="${t}"/></option></g:if>
                        </g:each>
                    </select>
                </div>
                <div class="col-md-6">
                    <label class="form-label">ঘটনার তারিখ</label>
                    <input type="date" name="incidentDate" value="${form.incidentDate}" max="${today}" class="form-control" required>
                </div>
            </div>
            <div class="mb-3">
                <label class="form-label">বিষয়</label>
                <input type="text" name="subject" value="${form.subject}" maxlength="150" class="form-control" required>
            </div>
            <div class="mb-3">
                <label class="form-label">বিস্তারিত</label>
                <textarea name="description" rows="5" maxlength="2000" class="form-control" required>${form.description}</textarea>
            </div>
            <button type="submit" class="btn btn-primary" onclick="return confirm('কেস খুলবেন? কর্মীকে জানানো হবে।');">কেস খুলুন</button>
        </g:form>
    </div>
</div>
</body>
</html>

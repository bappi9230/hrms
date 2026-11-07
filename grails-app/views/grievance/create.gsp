<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>নতুন অভিযোগ</title>
</head>
<body>
<div class="d-flex justify-content-between align-items-center mb-3">
    <h1 class="page-title mb-0">নতুন অভিযোগ</h1>
    <g:link action="index" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> ফিরে যান</g:link>
</div>
<div class="card" style="max-width: 720px;">
    <div class="card-body">
        <g:form action="save" method="POST">
            <div class="mb-3">
                <label class="form-label">ধরন</label>
                <select name="category" class="form-select" required>
                    <g:each in="${categories}" var="c"><option value="${c}" ${form.category == c ? 'selected' : ''}><hrms:d9Name value="${c}"/></option></g:each>
                </select>
            </div>
            <div class="mb-3">
                <label class="form-label">বিষয়</label>
                <input type="text" name="subject" value="${form.subject}" maxlength="150" class="form-control" required>
            </div>
            <div class="mb-3">
                <label class="form-label">বিস্তারিত</label>
                <textarea name="description" rows="5" maxlength="2000" class="form-control" required>${form.description}</textarea>
            </div>
            <button type="submit" class="btn btn-primary">অভিযোগ জমা দিন</button>
        </g:form>
    </div>
</div>
</body>
</html>

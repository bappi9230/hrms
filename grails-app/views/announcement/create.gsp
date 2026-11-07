<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>নতুন ঘোষণা</title>
</head>
<body>
<div class="d-flex justify-content-between align-items-center mb-3">
    <h1 class="page-title mb-0">নতুন ঘোষণা</h1>
    <g:link action="index" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> ফিরে যান</g:link>
</div>
<div class="card" style="max-width: 720px;">
    <div class="card-body">
        <g:form action="save" method="POST">
            <div class="mb-3">
                <label class="form-label">শিরোনাম</label>
                <input type="text" name="title" value="${form.title}" maxlength="150" class="form-control" required>
            </div>
            <div class="mb-3">
                <label class="form-label">বিস্তারিত</label>
                <textarea name="body" rows="5" maxlength="4000" class="form-control" required>${form.body}</textarea>
            </div>
            <div class="row g-3 mb-3">
                <div class="col-md-4">
                    <label class="form-label">ধরন</label>
                    <select name="category" class="form-select">
                        <g:each in="${categories}" var="c"><option value="${c}" ${form.category == c ? 'selected' : ''}><hrms:d9Name value="${c}"/></option></g:each>
                    </select>
                </div>
                <div class="col-md-4">
                    <label class="form-label">প্রকাশের তারিখ</label>
                    <input type="date" name="publishDate" value="${form.publishDate}" class="form-control">
                </div>
                <div class="col-md-4">
                    <label class="form-label">মেয়াদ শেষ (ঐচ্ছিক)</label>
                    <input type="date" name="expiryDate" value="${form.expiryDate}" min="${today}" class="form-control">
                </div>
            </div>
            <div class="form-check mb-2"><input class="form-check-input" type="checkbox" name="pinned" id="pinned" ${form.pinned ? 'checked' : ''}><label class="form-check-label" for="pinned">সবার ওপরে আটকে রাখুন</label></div>
            <div class="form-check mb-3"><input class="form-check-input" type="checkbox" name="notify" id="notify" ${form.notify ? 'checked' : ''}><label class="form-check-label" for="notify">সবাইকে নোটিফিকেশন পাঠান</label></div>
            <button type="submit" class="btn btn-primary">প্রকাশ করুন</button>
        </g:form>
    </div>
</div>
</body>
</html>

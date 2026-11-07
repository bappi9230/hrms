<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>পদত্যাগের আবেদন</title>
</head>
<body>
<div class="d-flex justify-content-between align-items-center mb-3">
    <h1 class="page-title mb-0">পদত্যাগের আবেদন</h1>
    <g:link action="index" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> ফিরে যান</g:link>
</div>

<g:if test="${live}">
    <div class="alert alert-warning">আপনার একটি চাকরি-ছাড়ার অনুরোধ আগেই চালু আছে। নতুন আবেদন করা যাবে না।</div>
</g:if>
<div class="alert alert-info small">
    আপনার নোটিস পিরিয়ড <strong>${notice} দিন</strong>। শেষ কর্মদিবস আজ থেকে কমপক্ষে ${notice} দিন পরে (<hrms:date value="${minDay}"/> বা তার পরে) হতে হবে।
    কম নোটিস দিলে HR মওকুফ না করলে চূড়ান্ত নিষ্পত্তিতে কর্তন হতে পারে।
</div>

<div class="card" style="max-width: 720px;">
    <div class="card-body">
        <g:form action="save" method="POST">
            <div class="mb-3">
                <label class="form-label">কারণের ধরন</label>
                <select name="reasonCategory" class="form-select" required>
                    <g:each in="${reasons.findAll { !(it in ['MISCONDUCT', 'PERFORMANCE', 'CONTRACT_END']) }}" var="r">
                        <option value="${r}" ${form.reasonCategory == r ? 'selected' : ''}><hrms:d9Name value="${r}"/></option>
                    </g:each>
                </select>
            </div>
            <div class="mb-3">
                <label class="form-label">বিস্তারিত কারণ</label>
                <textarea name="reason" rows="4" maxlength="1000" class="form-control" required>${form.reason}</textarea>
            </div>
            <div class="mb-3">
                <label class="form-label">প্রস্তাবিত শেষ কর্মদিবস</label>
                <input type="date" name="lastDay" value="${form.lastDay ?: minDay}" class="form-control" required>
            </div>
            <button type="submit" class="btn btn-danger" ${live ? 'disabled' : ''} onclick="return confirm('পদত্যাগের আবেদন জমা দেবেন?');"><i class="bi bi-send"></i> আবেদন জমা দিন</button>
        </g:form>
    </div>
</div>
</body>
</html>

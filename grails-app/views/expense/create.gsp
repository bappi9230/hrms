<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>নতুন খরচের দাবি</title>
</head>
<body>
<h1 class="page-title">নতুন খরচের দাবি</h1>
<g:render template="/expense/tabs" model="[active: 'expense']"/>

<div class="card" style="max-width:760px;">
    <div class="card-body">
        <g:uploadForm action="save" method="POST" class="row g-3">
            <div class="col-md-5">
                <label class="form-label" for="category">ধরন <span class="text-danger">*</span></label>
                <select id="category" name="category" class="form-select" required>
                    <g:each in="${categories}" var="c"><option value="${c}" ${claim.category == c ? 'selected' : ''}><hrms:d8Name value="${c}"/></option></g:each>
                </select>
            </div>
            <div class="col-md-7">
                <label class="form-label" for="title">বিবরণ <span class="text-danger">*</span></label>
                <input type="text" id="title" name="title" value="${claim.title}" maxlength="150" class="form-control" placeholder="যেমন: ক্লায়েন্ট মিটিংয়ে যাতায়াত" required>
            </div>
            <div class="col-md-6">
                <label class="form-label" for="expenseDate">খরচের তারিখ <span class="text-danger">*</span></label>
                <input type="date" id="expenseDate" name="expenseDate" value="${claim.expenseDate}" min="${minDate}" max="${today}" class="form-control" required>
            </div>
            <div class="col-md-6">
                <label class="form-label" for="amount">পরিমাণ (৳) <span class="text-danger">*</span></label>
                <input type="number" id="amount" name="amount" value="${claim.amount}" min="1" max="500000" step="0.01" class="form-control" required>
            </div>
            <g:if test="${travels}">
                <div class="col-12">
                    <label class="form-label" for="travel">অনুমোদিত ভ্রমণের সাথে যুক্ত করুন (ঐচ্ছিক)</label>
                    <select id="travel" name="travel" class="form-select">
                        <option value="">— যুক্ত নয় —</option>
                        <g:each in="${travels}" var="t"><option value="${t.id}" ${claim.travel?.id == t.id ? 'selected' : ''}>${t.destination} (<hrms:date value="${t.fromDate}"/> - <hrms:date value="${t.toDate}"/>)</option></g:each>
                    </select>
                </div>
            </g:if>
            <div class="col-12">
                <label class="form-label" for="description">অতিরিক্ত তথ্য</label>
                <textarea id="description" name="description" rows="2" maxlength="500" class="form-control">${claim.description}</textarea>
            </div>
            <div class="col-12">
                <label class="form-label" for="receipt">রসিদ (PDF/JPG/PNG, সর্বোচ্চ ২ MB)</label>
                <input type="file" id="receipt" name="receipt" accept=".pdf,.jpg,.jpeg,.png" class="form-control">
                <div class="form-text">${receiptAbove.intValue()} ৳ এর বেশি খরচে রসিদ বাধ্যতামূলক। দাবি আগে আপনার ম্যানেজার, পরে হিসাব বিভাগ অনুমোদন করবে। খরচ হওয়ার ৯০ দিনের মধ্যে দাবি করতে হবে।</div>
            </div>
            <div class="col-12">
                <button type="submit" class="btn btn-primary"><i class="bi bi-send"></i> দাবি জমা দিন</button>
                <g:link action="index" class="btn btn-secondary">বাতিল</g:link>
            </div>
        </g:uploadForm>
    </div>
</div>
</body>
</html>

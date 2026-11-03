<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>নতুন বিজ্ঞপ্তি</title>
</head>
<body>
<h1 class="page-title">নতুন চাকরির বিজ্ঞপ্তি</h1>
<g:render template="/layouts/errors" model="[bean: posting]"/>

<g:if test="${!requisitions}">
    <div class="alert alert-warning">
        বিজ্ঞপ্তি দেওয়ার মতো কোনো অনুমোদিত চাহিদা নেই (পদ খালি আছে এমন)। আগে <g:link controller="requisition" action="index">জনবল চাহিদা</g:link> অনুমোদন করুন।
    </div>
</g:if>
<g:else>
    <div class="card" style="max-width: 820px;">
        <div class="card-body">
            <g:form action="savePosting" method="POST" class="row g-3">
                <div class="col-12">
                    <label class="form-label" for="requisition">অনুমোদিত চাহিদা <span class="text-danger">*</span></label>
                    <select name="requisition" id="requisition" class="form-select" required>
                        <option value="">— বেছে নিন —</option>
                        <g:each in="${requisitions}" var="r">
                            <option value="${r.id}" ${posting.requisition?.id == r.id ? 'selected' : ''}>#${r.id} ${r.title} — ${r.department} (খালি ${r.remaining()}টি)</option>
                        </g:each>
                    </select>
                </div>
                <div class="col-md-8">
                    <label class="form-label" for="title">বিজ্ঞপ্তির শিরোনাম <span class="text-danger">*</span></label>
                    <input type="text" name="title" id="title" maxlength="120" class="form-control" value="${posting.title}" required>
                </div>
                <div class="col-md-4">
                    <label class="form-label" for="deadline">আবেদনের শেষ তারিখ</label>
                    <input type="date" name="deadline" id="deadline" class="form-control" value="${posting.deadline}">
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="location">কর্মস্থল</label>
                    <input type="text" name="location" id="location" maxlength="120" class="form-control" value="${posting.location}" placeholder="যেমন: Head Office, Motijheel">
                </div>
                <div class="col-12">
                    <label class="form-label" for="description">কাজের বিবরণ <span class="text-danger">*</span></label>
                    <textarea name="description" id="description" rows="5" maxlength="3000" class="form-control" required>${posting.description}</textarea>
                </div>
                <div class="col-12">
                    <label class="form-label" for="requirements">যোগ্যতা ও অভিজ্ঞতা</label>
                    <textarea name="requirements" id="requirements" rows="4" maxlength="2000" class="form-control">${posting.requirements}</textarea>
                </div>
                <div class="col-12 d-flex gap-2">
                    <button type="submit" class="btn btn-primary"><i class="bi bi-megaphone"></i> প্রকাশ করুন</button>
                    <g:link action="postings" class="btn btn-outline-secondary">বাতিল</g:link>
                </div>
            </g:form>
        </div>
    </div>
</g:else>
</body>
</html>

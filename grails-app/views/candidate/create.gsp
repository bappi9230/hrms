<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>নতুন প্রার্থী</title>
</head>
<body>
<h1 class="page-title">নতুন প্রার্থী</h1>
<g:render template="/layouts/errors" model="[bean: candidate]"/>

<g:if test="${!postings}">
    <div class="alert alert-warning">
        আবেদন নেওয়া হচ্ছে এমন কোনো খোলা বিজ্ঞপ্তি নেই। আগে <g:link controller="recruitment" action="postings">বিজ্ঞপ্তি</g:link> প্রকাশ করুন।
    </div>
</g:if>
<g:else>
    <div class="card" style="max-width: 820px;">
        <div class="card-body">
            <g:uploadForm action="save" method="POST" class="row g-3">
                <div class="col-12">
                    <label class="form-label" for="jobPosting">কোন বিজ্ঞপ্তিতে আবেদন <span class="text-danger">*</span></label>
                    <select name="jobPosting" id="jobPosting" class="form-select" required>
                        <option value="">— বেছে নিন —</option>
                        <g:each in="${postings}" var="p"><option value="${p.id}" ${candidate.jobPosting?.id == p.id ? 'selected' : ''}>${p.title}</option></g:each>
                    </select>
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="name">নাম <span class="text-danger">*</span></label>
                    <input type="text" name="name" id="name" maxlength="100" class="form-control" value="${candidate.name}" required>
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="email">ইমেইল <span class="text-danger">*</span></label>
                    <input type="email" name="email" id="email" maxlength="120" class="form-control" value="${candidate.email}" required>
                </div>
                <div class="col-md-4">
                    <label class="form-label" for="phone">ফোন</label>
                    <input type="text" name="phone" id="phone" maxlength="20" class="form-control" value="${candidate.phone}">
                </div>
                <div class="col-md-4">
                    <label class="form-label" for="source">সূত্র</label>
                    <select name="source" id="source" class="form-select">
                        <g:each in="${sources}" var="s"><option value="${s}" ${candidate.source == s ? 'selected' : ''}><hrms:sourceName value="${s}"/></option></g:each>
                    </select>
                </div>
                <div class="col-md-2">
                    <label class="form-label" for="experienceYears">অভিজ্ঞতা (বছর)</label>
                    <input type="number" min="0" max="60" step="0.5" name="experienceYears" id="experienceYears" class="form-control" value="${candidate.experienceYears}">
                </div>
                <div class="col-md-2">
                    <label class="form-label" for="expectedSalary">প্রত্যাশিত বেতন</label>
                    <input type="number" min="0" step="1" name="expectedSalary" id="expectedSalary" class="form-control" value="${candidate.expectedSalary?.toBigInteger()}">
                </div>
                <div class="col-12">
                    <label class="form-label" for="resume">সিভি (PDF / DOC / DOCX, সর্বোচ্চ ২ MB)</label>
                    <input type="file" name="resume" id="resume" class="form-control" accept=".pdf,.doc,.docx">
                </div>
                <div class="col-12">
                    <label class="form-label" for="note">নোট</label>
                    <textarea name="note" id="note" rows="2" maxlength="500" class="form-control">${candidate.note}</textarea>
                </div>
                <div class="col-12 d-flex gap-2">
                    <button type="submit" class="btn btn-primary"><i class="bi bi-check-lg"></i> যোগ করুন</button>
                    <g:link controller="recruitment" action="index" class="btn btn-outline-secondary">বাতিল</g:link>
                </div>
            </g:uploadForm>
        </div>
    </div>
</g:else>
</body>
</html>

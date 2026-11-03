<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>ঋণ / অগ্রিমের আবেদন</title>
</head>
<body>
<h1 class="page-title">ঋণ / অগ্রিমের আবেদন</h1>

<div class="row">
    <div class="col-lg-7">
        <div class="card">
            <div class="card-body">
                <g:form action="save" method="POST" class="row g-3">
                    <div class="col-md-8">
                        <label class="form-label" for="employee">কর্মী <span class="text-danger">*</span></label>
                        <select name="employee" id="employee" class="form-select" required>
                            <option value="">— বেছে নিন —</option>
                            <g:each in="${employees}" var="e"><option value="${e.id}" ${form.employee == e.id.toString() ? 'selected' : ''}>${e.name}</option></g:each>
                        </select>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label" for="loanType">ধরন <span class="text-danger">*</span></label>
                        <select name="loanType" id="loanType" class="form-select">
                            <option value="LOAN" ${form.loanType != 'ADVANCE' ? 'selected' : ''}>ঋণ</option>
                            <option value="ADVANCE" ${form.loanType == 'ADVANCE' ? 'selected' : ''}>বেতন অগ্রিম</option>
                        </select>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label" for="amount">টাকা (৳) <span class="text-danger">*</span></label>
                        <input type="number" min="1" step="1" name="amount" id="amount" class="form-control" value="${form.amount}" required>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label" for="installments">কিস্তির সংখ্যা <span class="text-danger">*</span></label>
                        <input type="number" min="1" max="60" step="1" name="installments" id="installments" class="form-control" value="${form.installments ?: 6}" required>
                    </div>
                    <div class="col-md-4"></div>
                    <div class="col-md-4">
                        <label class="form-label" for="startMonth">প্রথম কিস্তি: মাস</label>
                        <select name="startMonth" id="startMonth" class="form-select">
                            <g:each in="${1..12}" var="m"><option value="${m}" ${m == defaultMonth ? 'selected' : ''}><hrms:monthName value="${m}"/></option></g:each>
                        </select>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label" for="startYear">বছর</label>
                        <input type="number" min="2000" max="2100" name="startYear" id="startYear" class="form-control" value="${defaultYear}" required>
                    </div>
                    <div class="col-12">
                        <label class="form-label" for="reason">কারণ</label>
                        <input type="text" maxlength="255" name="reason" id="reason" class="form-control" value="${form.reason}">
                    </div>
                    <div class="col-12 d-flex gap-2">
                        <button type="submit" class="btn btn-primary"><i class="bi bi-check-lg"></i> আবেদন জমা</button>
                        <g:link action="index" class="btn btn-outline-secondary">বাতিল</g:link>
                    </div>
                </g:form>
            </div>
        </div>
    </div>
    <div class="col-lg-5">
        <div class="card">
            <div class="card-header bg-white fw-semibold">নিয়ম</div>
            <div class="card-body small">
                <ul class="mb-0 ps-3">
                    <li>ঋণের সীমা: কর্মীর gross এর <strong>${maxLoan} গুণ</strong>; অগ্রিম: <strong>${maxAdvance} গুণ</strong> (সর্বোচ্চ ৩ কিস্তি)।</li>
                    <li>কিস্তি gross এর <strong>${maxInstallmentPct}%</strong> এর বেশি হবে না।</li>
                    <li>একই ধরনের একটি ঋণ/অগ্রিম চলতে থাকলে নতুন নয়।</li>
                    <li>সুদ নেই। সমান কিস্তি; ভগ্নাংশ শেষ কিস্তিতে।</li>
                    <li>Admin অনুমোদন করলে কিস্তি প্রতি মাসের পে-রোলে আপনা-আপনি কাটা হয়।</li>
                </ul>
            </div>
        </div>
    </div>
</div>
</body>
</html>

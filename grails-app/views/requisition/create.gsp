<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>নতুন জনবল চাহিদা</title>
</head>
<body>
<h1 class="page-title">নতুন জনবল চাহিদা</h1>
<g:render template="/layouts/errors" model="[bean: requisition]"/>

<div class="row">
    <div class="col-lg-8">
        <div class="card">
            <div class="card-body">
                <g:form action="save" method="POST" class="row g-3">
                    <div class="col-12">
                        <label class="form-label" for="title">পদের শিরোনাম <span class="text-danger">*</span></label>
                        <input type="text" name="title" id="title" maxlength="120" class="form-control" value="${requisition.title}" required placeholder="যেমন: Software Engineer (Backend)">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label" for="department">বিভাগ <span class="text-danger">*</span></label>
                        <select name="department" id="department" class="form-select" required>
                            <option value="">— বেছে নিন —</option>
                            <g:each in="${departments}" var="d"><option value="${d.id}" ${requisition.department?.id == d.id ? 'selected' : ''}>${d.name}</option></g:each>
                        </select>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label" for="designation">পদবি <span class="text-danger">*</span></label>
                        <select name="designation" id="designation" class="form-select" required>
                            <option value="">— বেছে নিন —</option>
                            <g:each in="${designations}" var="d"><option value="${d.id}" ${requisition.designation?.id == d.id ? 'selected' : ''}>${d.title}</option></g:each>
                        </select>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label" for="branch">শাখা</label>
                        <select name="branch" id="branch" class="form-select">
                            <option value="">— যেকোনো —</option>
                            <g:each in="${branches}" var="b"><option value="${b.id}" ${requisition.branch?.id == b.id ? 'selected' : ''}>${b.name}</option></g:each>
                        </select>
                    </div>
                    <div class="col-md-3">
                        <label class="form-label" for="vacancies">পদসংখ্যা <span class="text-danger">*</span></label>
                        <input type="number" min="1" max="50" name="vacancies" id="vacancies" class="form-control" value="${requisition.vacancies ?: 1}" required>
                    </div>
                    <div class="col-md-3">
                        <label class="form-label" for="employeeType">কর্মীর ধরন</label>
                        <select name="employeeType" id="employeeType" class="form-select">
                            <g:each in="${types}" var="t"><option value="${t}" ${requisition.employeeType == t ? 'selected' : ''}><hrms:label value="${t}"/></option></g:each>
                        </select>
                    </div>
                    <div class="col-md-3">
                        <label class="form-label" for="minSalary">বেতন (সর্বনিম্ন ৳)</label>
                        <input type="number" min="0" step="1" name="minSalary" id="minSalary" class="form-control" value="${requisition.minSalary?.toBigInteger()}">
                    </div>
                    <div class="col-md-3">
                        <label class="form-label" for="maxSalary">বেতন (সর্বোচ্চ ৳)</label>
                        <input type="number" min="0" step="1" name="maxSalary" id="maxSalary" class="form-control" value="${requisition.maxSalary?.toBigInteger()}">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label" for="expectedJoinDate">প্রত্যাশিত যোগদান</label>
                        <input type="date" name="expectedJoinDate" id="expectedJoinDate" class="form-control" value="${requisition.expectedJoinDate}">
                    </div>
                    <div class="col-12">
                        <label class="form-label" for="justification">কেন দরকার <span class="text-danger">*</span></label>
                        <textarea name="justification" id="justification" rows="3" maxlength="500" class="form-control" required>${requisition.justification}</textarea>
                    </div>
                    <div class="col-12 d-flex gap-2">
                        <button type="submit" class="btn btn-primary"><i class="bi bi-check-lg"></i> চাহিদা জমা</button>
                        <g:link action="index" class="btn btn-outline-secondary">বাতিল</g:link>
                    </div>
                </g:form>
            </div>
        </div>
    </div>
    <div class="col-lg-4">
        <div class="card">
            <div class="card-header bg-white fw-semibold">প্রক্রিয়া</div>
            <div class="card-body small">
                <ol class="mb-0 ps-3">
                    <li>চাহিদা জমা দিলে HR/Admin এর অনুমোদনের অপেক্ষায় থাকে।</li>
                    <li>অনুমোদিত হলে HR চাকরির বিজ্ঞপ্তি দেয়।</li>
                    <li>প্রার্থী বাছাই, ইন্টারভিউ ও অফার শেষে কর্মী হিসেবে যোগ হয়।</li>
                    <li>সব পদ পূরণ হলে চাহিদা আপনা-আপনি বন্ধ হয়।</li>
                </ol>
            </div>
        </div>
    </div>
</div>
</body>
</html>

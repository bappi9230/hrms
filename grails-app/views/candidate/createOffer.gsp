<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>অফার - ${candidate.name}</title>
</head>
<body>
<h1 class="page-title">অফার: ${candidate.name}</h1>
<g:render template="/layouts/errors" model="[bean: offer]"/>

<div class="card" style="max-width: 820px;">
    <div class="card-body">
        <g:form action="saveOffer" id="${candidate.id}" method="POST" class="row g-3">
            <div class="col-md-4">
                <label class="form-label" for="department">বিভাগ <span class="text-danger">*</span></label>
                <select name="department" id="department" class="form-select" required>
                    <g:each in="${departments}" var="d"><option value="${d.id}" ${offer.department?.id == d.id ? 'selected' : ''}>${d.name}</option></g:each>
                </select>
            </div>
            <div class="col-md-4">
                <label class="form-label" for="designation">পদবি <span class="text-danger">*</span></label>
                <select name="designation" id="designation" class="form-select" required>
                    <g:each in="${designations}" var="d"><option value="${d.id}" ${offer.designation?.id == d.id ? 'selected' : ''}>${d.title}</option></g:each>
                </select>
            </div>
            <div class="col-md-4">
                <label class="form-label" for="branch">শাখা</label>
                <select name="branch" id="branch" class="form-select">
                    <option value="">— নেই —</option>
                    <g:each in="${branches}" var="b"><option value="${b.id}" ${offer.branch?.id == b.id ? 'selected' : ''}>${b.name}</option></g:each>
                </select>
            </div>
            <div class="col-md-4">
                <label class="form-label" for="salary">মাসিক বেতন - gross (৳) <span class="text-danger">*</span></label>
                <input type="number" min="1" step="1" name="salary" id="salary" class="form-control" value="${offer.salary?.toBigInteger()}" required>
            </div>
            <div class="col-md-4">
                <label class="form-label" for="joinDate">যোগদানের তারিখ <span class="text-danger">*</span></label>
                <input type="date" name="joinDate" id="joinDate" class="form-control" value="${offer.joinDate}" required>
            </div>
            <div class="col-md-4">
                <label class="form-label" for="validUntil">অফারের মেয়াদ <span class="text-danger">*</span></label>
                <input type="date" name="validUntil" id="validUntil" class="form-control" value="${offer.validUntil}" required>
            </div>
            <div class="col-md-4">
                <label class="form-label" for="probationMonths">পরীক্ষাকাল (মাস, 0 = নেই)</label>
                <input type="number" min="0" max="12" name="probationMonths" id="probationMonths" class="form-control" value="${offer.probationMonths}" required>
            </div>
            <div class="col-md-4">
                <label class="form-label" for="employeeType">পরীক্ষাকালের পর কর্মীর ধরন</label>
                <select name="employeeType" id="employeeType" class="form-select">
                    <g:each in="${types}" var="t"><option value="${t}" ${offer.employeeType == t ? 'selected' : ''}><hrms:label value="${t}"/></option></g:each>
                </select>
            </div>
            <div class="col-12">
                <label class="form-label" for="terms">অতিরিক্ত শর্ত</label>
                <textarea name="terms" id="terms" rows="3" maxlength="1500" class="form-control">${offer.terms}</textarea>
            </div>
            <div class="col-12 d-flex gap-2">
                <button type="submit" class="btn btn-primary"><i class="bi bi-envelope-paper"></i> অফার পাঠান</button>
                <g:link action="show" id="${candidate.id}" class="btn btn-outline-secondary">বাতিল</g:link>
            </div>
        </g:form>
    </div>
</div>
</body>
</html>

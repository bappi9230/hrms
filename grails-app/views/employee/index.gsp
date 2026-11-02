<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>কর্মী তালিকা</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="page-title mb-0">কর্মী তালিকা <span class="badge text-bg-secondary fs-6">${employeeCount}</span></h1>
    <div class="d-flex flex-wrap gap-2">
        <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_HR">
            <g:link action="importForm" class="btn btn-outline-secondary"><i class="bi bi-upload"></i> CSV ইমপোর্ট</g:link>
            <g:link action="create" class="btn btn-primary"><i class="bi bi-plus-lg"></i> নতুন কর্মী</g:link>
        </sec:ifAnyGranted>
        <g:link action="exportCsv" params="${filterParams}" class="btn btn-outline-success"><i class="bi bi-download"></i> CSV এক্সপোর্ট</g:link>
    </div>
</div>

<%-- সার্চ ও ফিল্টার --%>
<div class="card mb-3">
    <div class="card-body">
        <g:form action="index" method="GET" class="row g-2 align-items-end">
            <div class="col-md-3">
                <label class="form-label small mb-1" for="q">খুঁজুন</label>
                <input type="text" id="q" name="q" value="${params.q}" class="form-control" placeholder="নাম, ইমেইল, ফোন, NID">
            </div>
            <div class="col-md-2">
                <label class="form-label small mb-1" for="dept">বিভাগ</label>
                <g:select name="dept" id="dept" from="${departments}" optionKey="id" value="${params.dept}"
                          noSelection="['': 'সব']" class="form-select"/>
            </div>
            <div class="col-md-2">
                <label class="form-label small mb-1" for="branch">শাখা</label>
                <g:select name="branch" id="branch" from="${branches}" optionKey="id" value="${params.branch}"
                          noSelection="['': 'সব']" class="form-select"/>
            </div>
            <div class="col-md-2">
                <label class="form-label small mb-1" for="status">স্ট্যাটাস</label>
                <g:select name="status" id="status" from="${statuses}" value="${params.status}"
                          noSelection="['': 'সব']" class="form-select"/>
            </div>
            <div class="col-md-2">
                <label class="form-label small mb-1" for="type">ধরন</label>
                <g:select name="type" id="type" from="${types}" value="${params.type}"
                          noSelection="['': 'সব']" class="form-select"/>
            </div>
            <div class="col-md-1 d-flex gap-1">
                <button type="submit" class="btn btn-primary" title="খুঁজুন"><i class="bi bi-search"></i></button>
                <g:link action="index" class="btn btn-outline-secondary" title="রিসেট"><i class="bi bi-x-lg"></i></g:link>
            </div>
        </g:form>
    </div>
</div>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
            <tr>
                <th style="width:60px;"></th>
                <g:sortableColumn property="name" title="নাম" params="${filterParams}"/>
                <g:sortableColumn property="email" title="ইমেইল" params="${filterParams}"/>
                <th>বিভাগ</th>
                <th>পদবি</th>
                <th>শাখা</th>
                <g:sortableColumn property="employeeType" title="ধরন" params="${filterParams}"/>
                <g:sortableColumn property="status" title="স্ট্যাটাস" params="${filterParams}"/>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${employeeList}" var="e">
                <tr>
                    <td>
                        <g:if test="${e.hasPhoto}">
                            <img class="avatar avatar-sm" src="${createLink(action: 'photo', id: e.id)}" alt="">
                        </g:if>
                        <g:else><span class="avatar avatar-sm"><hrms:initials name="${e.name}"/></span></g:else>
                    </td>
                    <td><g:link action="show" id="${e.id}" class="fw-semibold text-decoration-none">${e.name}</g:link></td>
                    <td>${e.email}</td>
                    <td>${e.department}</td>
                    <td>${e.designation}</td>
                    <td>${e.branch ?: '—'}</td>
                    <td><hrms:label value="${e.employeeType}"/></td>
                    <td><hrms:statusBadge status="${e.status}"/></td>
                    <td class="text-end">
                        <g:link action="show" id="${e.id}" class="btn btn-sm btn-outline-primary"><i class="bi bi-eye"></i></g:link>
                        <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_HR">
                            <g:link action="edit" id="${e.id}" class="btn btn-sm btn-outline-secondary"><i class="bi bi-pencil"></i></g:link>
                        </sec:ifAnyGranted>
                    </td>
                </tr>
            </g:each>
            <g:if test="${!employeeList}">
                <tr><td colspan="9" class="text-center text-muted py-4">কোনো কর্মী পাওয়া যায়নি।</td></tr>
            </g:if>
            </tbody>
        </table>
    </div>
    <div class="card-footer bg-white">
        <g:paginate total="${employeeCount}" params="${filterParams}" class="pagination mb-0"/>
    </div>
</div>
</body>
</html>
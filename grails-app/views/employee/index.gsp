<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>কর্মী তালিকা</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="page-title mb-0">কর্মী তালিকা <span class="badge text-bg-secondary fs-6">${employeeCount}</span></h1>
    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_HR">
        <g:link action="create" class="btn btn-primary"><i class="bi bi-plus-lg"></i> নতুন কর্মী</g:link>
    </sec:ifAnyGranted>
</div>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
            <tr>
                <th style="width:60px;"></th>
                <g:sortableColumn property="name" title="নাম"/>
                <g:sortableColumn property="email" title="ইমেইল"/>
                <th>বিভাগ</th>
                <th>পদবি</th>
                <g:sortableColumn property="employeeType" title="ধরন"/>
                <g:sortableColumn property="status" title="স্ট্যাটাস"/>
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
                <tr><td colspan="8" class="text-center text-muted py-4">কোনো কর্মী নেই।</td></tr>
            </g:if>
            </tbody>
        </table>
    </div>
    <div class="card-footer bg-white">
        <g:paginate total="${employeeCount}" class="pagination mb-0"/>
    </div>
</div>
</body>
</html>
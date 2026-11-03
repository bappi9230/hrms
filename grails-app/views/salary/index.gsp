<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>বেতন কাঠামো</title>
</head>
<body>
<h1 class="page-title">বেতন কাঠামো</h1>
<g:render template="tabs" model="[active: 'index']"/>

<div class="card">
    <div class="card-header bg-white d-flex flex-wrap justify-content-between align-items-center gap-2">
        <span class="fw-semibold">বর্তমান কাঠামো (মাসিক) <span class="badge text-bg-secondary">${rows.size()}</span></span>
        <g:form action="index" method="GET" class="d-flex gap-2">
            <input type="text" name="q" value="${q}" class="form-control form-control-sm" placeholder="নাম দিয়ে খুঁজুন">
            <button type="submit" class="btn btn-sm btn-outline-secondary"><i class="bi bi-search"></i></button>
        </g:form>
    </div>
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
            <tr><th>কর্মী</th><th class="text-end">বেসিক</th><th class="text-end">বাড়ি ভাড়া</th><th class="text-end">চিকিৎসা</th>
                <th class="text-end">যাতায়াত</th><th class="text-end">Gross</th><th class="text-center">খাত</th><th style="width:100px;"></th></tr>
            </thead>
            <tbody>
            <g:each in="${rows}" var="row">
                <tr>
                    <td>
                        <div class="fw-semibold">${row.employee.name}
                            <g:if test="${row.mismatch}"><i class="bi bi-exclamation-triangle-fill text-warning" title="প্রোফাইলের বেতন কাঠামোর সাথে মেলে না"></i></g:if></div>
                        <div class="small text-muted">${row.employee.designation?.title} &middot; ${row.employee.department?.name}</div>
                    </td>
                    <td class="text-end"><hrms:money value="${row.structure.basic}"/></td>
                    <td class="text-end"><hrms:money value="${row.structure.houseRent}"/></td>
                    <td class="text-end"><hrms:money value="${row.structure.medical}"/></td>
                    <td class="text-end"><hrms:money value="${row.structure.conveyance}"/></td>
                    <td class="text-end fw-semibold"><hrms:money value="${row.structure.gross}"/></td>
                    <td class="text-center"><span class="badge text-bg-light border">${row.headCount}</span></td>
                    <td class="text-end"><g:link action="show" id="${row.employee.id}" class="btn btn-sm btn-outline-primary"><i class="bi bi-pencil-square"></i> খুলুন</g:link></td>
                </tr>
            </g:each>
            <g:if test="${!rows}"><tr><td colspan="8" class="text-center text-muted py-4">কোনো কর্মী পাওয়া যায়নি।</td></tr></g:if>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>

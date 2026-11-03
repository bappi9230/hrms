<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>জনবল চাহিদা</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="page-title mb-0">জনবল চাহিদা <span class="badge text-bg-secondary fs-6">${requisitions.size()}</span></h1>
    <div class="d-flex gap-2">
        <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_HR">
            <g:link controller="recruitment" action="index" class="btn btn-outline-secondary"><i class="bi bi-kanban"></i> নিয়োগ বোর্ড</g:link>
        </sec:ifAnyGranted>
        <g:link action="create" class="btn btn-primary"><i class="bi bi-plus-lg"></i> নতুন চাহিদা</g:link>
    </div>
</div>

<ul class="nav nav-pills mb-3 gap-1">
    <li class="nav-item"><g:link action="index" class="nav-link ${!status ? 'active' : ''}">সব</g:link></li>
    <g:each in="${['PENDING': 'অপেক্ষমাণ', 'APPROVED': 'অনুমোদিত', 'REJECTED': 'প্রত্যাখ্যাত', 'CLOSED': 'বন্ধ', 'CANCELLED': 'প্রত্যাহার']}" var="s">
        <li class="nav-item"><g:link action="index" params="[status: s.key]" class="nav-link ${status == s.key ? 'active' : ''}">${s.value}</g:link></li>
    </g:each>
</ul>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
            <tr><th>#</th><th>পদ</th><th>বিভাগ</th><th class="text-center">পদসংখ্যা</th><th>প্রত্যাশিত যোগদান</th><th>আবেদনকারী</th><th>অবস্থা</th><th></th></tr>
            </thead>
            <tbody>
            <g:each in="${requisitions}" var="r">
                <tr>
                    <td>${r.id}</td>
                    <td><div class="fw-semibold">${r.title}</div><div class="small text-muted">${r.designation}</div></td>
                    <td>${r.department}</td>
                    <td class="text-center">${hired[r.id]} / ${r.vacancies}</td>
                    <td><hrms:date value="${r.expectedJoinDate}"/></td>
                    <td>${r.requestedBy ?: '—'}</td>
                    <td><hrms:recruitBadge kind="requisition" status="${r.status}"/></td>
                    <td class="text-end"><g:link action="show" id="${r.id}" class="btn btn-sm btn-outline-secondary"><i class="bi bi-eye"></i></g:link></td>
                </tr>
            </g:each>
            <g:if test="${!requisitions}"><tr><td colspan="8" class="text-center text-muted py-4">কোনো চাহিদা নেই।</td></tr></g:if>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>

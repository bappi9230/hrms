<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>ছুটি অনুমোদন</title>
</head>
<body>
<h1 class="page-title">ছুটি</h1>
<g:render template="tabs" model="[active: 'approvals']"/>

<div class="card mb-4">
    <div class="card-header bg-white fw-semibold">অনুমোদনের অপেক্ষায় <span class="badge text-bg-warning text-dark">${pending.size()}</span></div>
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
            <tr><th>কর্মী</th><th>ধরন</th><th>সময়কাল</th><th>দিন</th><th>কারণ</th><th>ধাপ</th><th style="min-width:260px;">সিদ্ধান্ত</th></tr>
            </thead>
            <tbody>
            <g:each in="${pending}" var="r">
                <tr>
                    <td><g:link action="show" id="${r.id}" class="fw-semibold text-decoration-none">${r.employee.name}</g:link></td>
                    <td>${r.leaveType.name}</td>
                    <td class="text-nowrap"><hrms:date value="${r.fromDate}"/> – <hrms:date value="${r.toDate}"/></td>
                    <td>${r.days}</td>
                    <td class="small">${r.reason}</td>
                    <td class="small"><hrms:stageName stage="${r.currentStage()}"/> (${r.currentLevel}/${r.stageList().size()})</td>
                    <td>
                        <g:form action="approve" id="${r.id}" method="POST" class="d-flex gap-1">
                            <input type="text" name="remarks" class="form-control form-control-sm" maxlength="500" placeholder="মন্তব্য">
                            <button type="submit" class="btn btn-sm btn-success" title="অনুমোদন"><i class="bi bi-check-lg"></i></button>
                            <button type="submit" class="btn btn-sm btn-danger" title="বাতিল (মন্তব্য লাগবে)" formaction="${createLink(action: 'reject', id: r.id)}"><i class="bi bi-x-lg"></i></button>
                        </g:form>
                    </td>
                </tr>
            </g:each>
            <g:if test="${!pending}"><tr><td colspan="7" class="text-center text-muted py-4">অপেক্ষমাণ কোনো আবেদন নেই।</td></tr></g:if>
            </tbody>
        </table>
    </div>
</div>

<div class="card">
    <div class="card-header bg-white fw-semibold">আমার সাম্প্রতিক সিদ্ধান্ত</div>
    <ul class="list-group list-group-flush">
        <g:each in="${recent}" var="a">
            <li class="list-group-item d-flex justify-content-between align-items-center">
                <span>
                    <g:link action="show" id="${a.leaveRequest.id}" class="text-decoration-none">${a.leaveRequest.employee.name}</g:link>
                    &middot; ${a.leaveRequest.leaveType.name} &middot; <hrms:date value="${a.leaveRequest.fromDate}"/> – <hrms:date value="${a.leaveRequest.toDate}"/>
                </span>
                <span class="badge ${a.decision == 'APPROVED' ? 'text-bg-success' : 'text-bg-danger'}">${a.decision == 'APPROVED' ? 'অনুমোদন' : 'বাতিল'}</span>
            </li>
        </g:each>
        <g:if test="${!recent}"><li class="list-group-item text-muted">কিছু নেই।</li></g:if>
    </ul>
</div>
</body>
</html>

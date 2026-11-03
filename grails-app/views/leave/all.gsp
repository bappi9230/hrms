<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>সব ছুটির আবেদন</title>
</head>
<body>
<h1 class="page-title">ছুটি</h1>
<g:render template="tabs" model="[active: 'all']"/>

<div class="card">
    <div class="card-header bg-white">
        <g:form action="all" method="GET" class="d-flex gap-2 align-items-center">
            <label class="small mb-0" for="status">অবস্থা</label>
            <g:select name="status" id="status" from="${statuses}" value="${status}" noSelection="['': 'সব']" class="form-select form-select-sm w-auto"/>
            <button type="submit" class="btn btn-sm btn-primary"><i class="bi bi-funnel"></i></button>
        </g:form>
    </div>
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
            <tr><th>কর্মী</th><th>ধরন</th><th>শুরু</th><th>শেষ</th><th>দিন</th><th>অবস্থা</th><th>আবেদন</th><th></th></tr>
            </thead>
            <tbody>
            <g:each in="${items}" var="r">
                <tr>
                    <td>${r.employee.name}</td>
                    <td>${r.leaveType.name}</td>
                    <td><hrms:date value="${r.fromDate}"/></td>
                    <td><hrms:date value="${r.toDate}"/></td>
                    <td>${r.days}</td>
                    <td><hrms:leaveStatus status="${r.status}"/></td>
                    <td class="small text-muted">${r.appliedAt?.toString()?.replace('T', ' ')?.take(16)}</td>
                    <td class="text-end"><g:link action="show" id="${r.id}" class="btn btn-sm btn-outline-secondary"><i class="bi bi-eye"></i></g:link></td>
                </tr>
            </g:each>
            <g:if test="${!items}"><tr><td colspan="8" class="text-center text-muted py-4">কোনো আবেদন নেই।</td></tr></g:if>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>

<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>ছুটির ব্যালেন্স</title>
</head>
<body>
<h1 class="page-title">ছুটি</h1>
<g:render template="tabs" model="[active: 'balances']"/>

<div class="card">
    <div class="card-header bg-white d-flex justify-content-between align-items-center">
        <span class="fw-semibold">সব কর্মীর ব্যালেন্স — ${year} <span class="small text-muted">(বাকি / অর্জিত+আগের)</span></span>
        <div class="btn-group btn-group-sm">
            <g:link action="balances" params="[year: year - 1]" class="btn btn-outline-secondary"><i class="bi bi-chevron-left"></i> ${year - 1}</g:link>
            <g:link action="balances" params="[year: year + 1]" class="btn btn-outline-secondary">${year + 1} <i class="bi bi-chevron-right"></i></g:link>
        </div>
    </div>
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
            <tr>
                <th>কর্মী</th>
                <g:each in="${types}" var="t"><th class="text-center">${t.name}</th></g:each>
            </tr>
            </thead>
            <tbody>
            <g:each in="${rows}" var="row">
                <tr>
                    <td><g:link controller="employee" action="show" id="${row.employee.id}" fragment="tab-leave" class="text-decoration-none fw-semibold">${row.employee.name}</g:link></td>
                    <g:each in="${types}" var="t">
                        <g:set var="b" value="${row.byType[t.id]}"/>
                        <td class="text-center">
                            <g:if test="${!b}"><span class="text-muted">—</span></g:if>
                            <g:elseif test="${b.limited}">
                                <strong><g:formatNumber number="${b.remaining}" maxFractionDigits="1"/></strong>
                                <span class="small text-muted">/ <g:formatNumber number="${b.accrued + b.carried}" maxFractionDigits="1"/></span>
                            </g:elseif>
                            <g:else><span class="small text-muted">ব্যবহৃত ${b.used}</span></g:else>
                        </td>
                    </g:each>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>

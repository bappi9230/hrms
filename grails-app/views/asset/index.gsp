<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>সম্পদ রেজিস্টার</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="page-title mb-0">খরচ ও সম্পদ</h1>
    <g:if test="${canManage}"><g:link action="create" class="btn btn-primary"><i class="bi bi-plus-lg"></i> নতুন সম্পদ</g:link></g:if>
</div>
<g:render template="/expense/tabs" model="[active: 'asset']"/>

<div class="row g-3 mb-3">
    <g:each in="${statuses}" var="s">
        <div class="col-6 col-md-3">
            <g:link action="index" params="[status: s]" class="text-decoration-none">
                <div class="card stat-card ${s == 'AVAILABLE' ? 'success' : (s == 'MAINTENANCE' ? 'warning' : '')}">
                    <div class="card-body py-2"><div class="small text-muted"><hrms:d8Badge kind="asset" status="${s}"/></div><div class="stat-value">${counts[s]}</div></div>
                </div>
            </g:link>
        </div>
    </g:each>
</div>

<div class="d-flex flex-wrap gap-2 mb-3">
    <g:form action="index" method="GET" class="d-flex gap-2 flex-grow-1">
        <input type="text" name="q" value="${q}" class="form-control" style="max-width:260px;" placeholder="ট্যাগ / নাম / সিরিয়াল">
        <select name="type" class="form-select" style="max-width:170px;">
            <option value="">সব ধরন</option>
            <g:each in="${types}" var="t"><option value="${t}" ${type == t ? 'selected' : ''}><hrms:d8Name value="${t}"/></option></g:each>
        </select>
        <select name="status" class="form-select" style="max-width:170px;">
            <option value="">সব অবস্থা</option>
            <g:each in="${statuses}" var="s"><option value="${s}" ${status == s ? 'selected' : ''}>${s}</option></g:each>
        </select>
        <button type="submit" class="btn btn-outline-primary"><i class="bi bi-search"></i></button>
        <g:link action="index" class="btn btn-outline-secondary">রিসেট</g:link>
    </g:form>
    <g:link action="holders" class="btn btn-outline-secondary"><i class="bi bi-people"></i> কার কাছে কী আছে</g:link>
</div>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light"><tr><th>ট্যাগ</th><th>সম্পদ</th><th>সিরিয়াল</th><th>ক্রয়</th><th>অবস্থা</th><th>কার কাছে</th><th></th></tr></thead>
            <tbody>
            <g:each in="${assets}" var="a">
                <tr>
                    <td class="fw-semibold">${a.assetTag}</td>
                    <td>${a.name}<div class="small text-muted"><hrms:d8Name value="${a.assetType}"/></div></td>
                    <td>${a.serialNo ?: '—'}</td>
                    <td><hrms:date value="${a.purchaseDate}"/><g:if test="${a.purchaseCost}"><div class="small text-muted"><hrms:money value="${a.purchaseCost}"/></div></g:if></td>
                    <td><hrms:d8Badge kind="asset" status="${a.status}"/></td>
                    <td>${holders[a.id]?.employee?.name ?: '—'}</td>
                    <td class="text-end"><g:link action="show" id="${a.id}" class="btn btn-sm btn-outline-primary">দেখুন</g:link></td>
                </tr>
            </g:each>
            <g:if test="${!assets}"><tr><td colspan="7" class="text-center text-muted py-4">কোনো সম্পদ নেই।</td></tr></g:if>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>

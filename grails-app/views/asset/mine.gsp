<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>আমার সম্পদ</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="page-title mb-0">খরচ ও সম্পদ</h1>
</div>
<g:render template="/expense/tabs" model="[active: 'asset']"/>

<div class="card">
    <div class="table-responsive">
        <table class="table align-middle mb-0">
            <thead class="table-light"><tr><th>সম্পদ</th><th>সিরিয়াল</th><th>বরাদ্দ</th><th>অবস্থা</th></tr></thead>
            <tbody>
            <g:each in="${allocations}" var="a">
                <tr>
                    <td><div class="fw-semibold">${a.assetName}</div><div class="small text-muted"><hrms:d8Name value="${a.assetType}"/></div></td>
                    <td>${a.serialNo ?: '—'}</td>
                    <td><hrms:date value="${a.issuedDate}"/></td>
                    <td><hrms:recruitBadge kind="asset" status="${a.status}"/><g:if test="${a.returnedDate}"><div class="small text-muted">ফেরত: <hrms:date value="${a.returnedDate}"/></div></g:if></td>
                </tr>
            </g:each>
            <g:if test="${!allocations}"><tr><td colspan="4" class="text-center text-muted py-4">${hasEmployee ? 'আপনার নামে কোনো সম্পদ বরাদ্দ নেই।' : 'আপনার লগইনের সাথে কর্মী প্রোফাইল যুক্ত নেই।'}</td></tr></g:if>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>

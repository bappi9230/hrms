<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>কার কাছে কী আছে</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="page-title mb-0">খরচ ও সম্পদ</h1>
</div>
<g:render template="/expense/tabs" model="[active: 'asset']"/>

<div class="d-flex justify-content-between align-items-center mb-3">
    <div><span class="fw-semibold">কার কাছে কী আছে</span> <span class="badge text-bg-secondary">${total}</span></div>
    <g:link action="index" class="btn btn-outline-secondary btn-sm"><i class="bi bi-arrow-left"></i> রেজিস্টার</g:link>
</div>

<div class="row g-3">
    <g:each in="${employees}" var="e">
        <div class="col-md-6 col-xl-4">
            <div class="card h-100">
                <div class="card-header bg-white fw-semibold">${e.name} <span class="small text-muted fw-normal">${e.department}</span></div>
                <ul class="list-group list-group-flush">
                    <g:each in="${byEmployee[e]}" var="al">
                        <li class="list-group-item small">
                            <div class="fw-semibold">
                                <g:if test="${al.asset}"><g:link action="show" id="${al.asset.id}" class="text-decoration-none">${al.asset.assetTag}</g:link> - </g:if>${al.assetName}
                            </div>
                            <div class="text-muted"><hrms:d8Name value="${al.assetType}"/><g:if test="${al.serialNo}"> &middot; ${al.serialNo}</g:if> &middot; <hrms:date value="${al.issuedDate}"/> থেকে</div>
                            <g:if test="${!al.asset}"><div class="text-muted fst-italic">অনবোর্ডিং থেকে হাতে দেওয়া (রেজিস্টারে নেই)</div></g:if>
                        </li>
                    </g:each>
                </ul>
            </div>
        </div>
    </g:each>
    <g:if test="${!employees}"><div class="col-12"><div class="alert alert-light border text-center">এখন কারো কাছে কোনো সম্পদ বরাদ্দ নেই।</div></div></g:if>
</div>
</body>
</html>

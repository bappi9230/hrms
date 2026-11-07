<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>অনুমোদন ইনবক্স</title>
</head>
<body>
<div class="d-flex justify-content-between align-items-center mb-3">
    <h1 class="page-title mb-0">অনুমোদন ইনবক্স <g:if test="${total}"><span class="badge text-bg-danger fs-6">${total}</span></g:if></h1>
</div>
<g:if test="${!total}"><div class="alert alert-success"><i class="bi bi-check-circle"></i> আপনার সিদ্ধান্তের অপেক্ষায় কোনো কাজ নেই।</div></g:if>
<g:each in="${groups}" var="grp">
    <div class="card mb-3">
        <div class="card-header bg-white fw-semibold">${grp.key} <span class="badge text-bg-secondary">${grp.value.size()}</span></div>
        <div class="list-group list-group-flush">
            <g:each in="${grp.value}" var="it2">
                <g:link controller="${it2.controller}" action="${it2.action}" id="${it2.id}" params="${it2.params ?: [:]}" class="list-group-item list-group-item-action d-flex justify-content-between align-items-center">
                    <span><i class="bi ${it2.icon} me-2"></i><strong>${it2.title}</strong><span class="text-muted small ms-2">${it2.detail}</span></span>
                    <i class="bi bi-chevron-right text-muted"></i>
                </g:link>
            </g:each>
        </div>
    </div>
</g:each>
</body>
</html>

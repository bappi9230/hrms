<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>নোটিফিকেশন</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="page-title mb-0">নোটিফিকেশন <g:if test="${unread}"><span class="badge text-bg-danger fs-6">${unread}</span></g:if></h1>
    <div class="d-flex gap-2">
        <div class="btn-group btn-group-sm">
            <g:link action="index" class="btn btn-outline-secondary ${!unreadOnly ? 'active' : ''}">সব</g:link>
            <g:link action="index" params="[filter: 'unread']" class="btn btn-outline-secondary ${unreadOnly ? 'active' : ''}">না-পড়া</g:link>
        </div>
        <g:if test="${unread}"><g:form action="markAll" method="POST"><button type="submit" class="btn btn-sm btn-outline-primary">সব পড়া হয়েছে</button></g:form></g:if>
    </div>
</div>
<div class="card">
    <div class="list-group list-group-flush">
        <g:each in="${notifications}" var="n">
            <g:link action="open" id="${n.id}" class="list-group-item list-group-item-action ${n.seen ? '' : 'bg-light'}">
                <div class="d-flex justify-content-between">
                    <div>
                        <i class="bi ${n.type == 'WARNING' ? 'bi-exclamation-triangle text-warning' : (n.type == 'SUCCESS' ? 'bi-check-circle text-success' : (n.type == 'APPROVAL' ? 'bi-hourglass-split text-primary' : 'bi-info-circle text-secondary'))}"></i>
                        <span class="${n.seen ? '' : 'fw-semibold'}">${n.title}</span>
                        <g:if test="${n.message}"><div class="small text-muted ms-4">${n.message}</div></g:if>
                    </div>
                    <div class="small text-muted text-nowrap ms-2"><hrms:dt value="${n.dateCreated}"/></div>
                </div>
            </g:link>
        </g:each>
        <g:if test="${!notifications}"><div class="list-group-item text-muted text-center py-4">কোনো নোটিফিকেশন নেই।</div></g:if>
    </div>
</div>
</body>
</html>

<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>প্রশিক্ষণ</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="page-title mb-0">প্রশিক্ষণ</h1>
    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_HR">
        <g:link action="create" class="btn btn-primary"><i class="bi bi-plus-lg"></i> নতুন কোর্স</g:link>
    </sec:ifAnyGranted>
</div>
<g:render template="tabs" model="[active: 'index']"/>

<div class="d-flex flex-wrap gap-2 mb-3">
    <div class="btn-group btn-group-sm">
        <g:link action="index" params="[when: 'upcoming', category: category]" class="btn btn-outline-primary ${when == 'upcoming' ? 'active' : ''}">আসন্ন ও চলমান</g:link>
        <g:link action="index" params="[when: 'ended', category: category]" class="btn btn-outline-primary ${when == 'ended' ? 'active' : ''}">শেষ হয়েছে</g:link>
        <g:link action="index" params="[when: 'all', category: category]" class="btn btn-outline-primary ${when == 'all' ? 'active' : ''}">সব</g:link>
    </div>
    <div class="btn-group btn-group-sm">
        <g:link action="index" params="[when: when]" class="btn btn-outline-secondary ${!category ? 'active' : ''}">সব ধরন</g:link>
        <g:each in="${categories}" var="c">
            <g:link action="index" params="[when: when, category: c]" class="btn btn-outline-secondary ${category == c ? 'active' : ''}"><hrms:d8Name value="${c}"/></g:link>
        </g:each>
    </div>
</div>

<div class="row g-3">
    <g:each in="${courses}" var="c">
        <g:set var="my" value="${mine[c.id]}"/>
        <g:set var="left" value="${seatsLeft[c.id]}"/>
        <div class="col-md-6 col-xl-4">
            <div class="card h-100">
                <div class="card-body d-flex flex-column">
                    <div class="d-flex justify-content-between mb-1">
                        <span class="badge text-bg-light border"><hrms:d8Name value="${c.category}"/></span>
                        <span>
                            <g:if test="${c.status == 'CANCELLED'}"><hrms:d8Badge kind="course" status="CANCELLED"/></g:if>
                            <g:else><hrms:d8Badge kind="phase" status="${c.phase(today)}"/></g:else>
                        </span>
                    </div>
                    <h2 class="h6 fw-bold mb-1"><g:link action="show" id="${c.id}" class="text-decoration-none">${c.title}</g:link></h2>
                    <div class="small text-muted mb-2">${c.trainer ?: '—'} &middot; <hrms:d8Name value="${c.trainingMode}"/><g:if test="${c.location}"> &middot; ${c.location}</g:if></div>
                    <div class="small mb-1"><i class="bi bi-calendar3"></i> <hrms:date value="${c.startDate}"/><g:if test="${c.endDate != c.startDate}"> - <hrms:date value="${c.endDate}"/></g:if> &middot; ${c.hours} ঘণ্টা</div>
                    <div class="small mb-3"><i class="bi bi-people"></i> ${taken[c.id]} জন<g:if test="${c.capacity > 0}"> / ${c.capacity} আসন</g:if></div>
                    <div class="mt-auto d-flex gap-2 align-items-center">
                        <g:if test="${my && my.status == 'ENROLLED'}">
                            <span class="badge text-bg-primary">আপনি নথিভুক্ত</span>
                        </g:if>
                        <g:elseif test="${my && my.status == 'COMPLETED'}">
                            <g:link action="certificate" id="${my.id}" class="btn btn-sm btn-success"><i class="bi bi-award"></i> সার্টিফিকেট</g:link>
                        </g:elseif>
                        <g:elseif test="${me && c.status == 'OPEN' && !c.endDate.isBefore(today) && (left == null || left > 0)}">
                            <g:form action="enroll" id="${c.id}" method="POST" class="d-inline">
                                <input type="hidden" name="back" value="index">
                                <button type="submit" class="btn btn-sm btn-primary">নথিভুক্ত হন</button>
                            </g:form>
                        </g:elseif>
                        <g:elseif test="${me && c.status == 'OPEN' && !c.endDate.isBefore(today) && left == 0}">
                            <span class="small text-danger">আসন খালি নেই</span>
                        </g:elseif>
                        <g:link action="show" id="${c.id}" class="btn btn-sm btn-outline-secondary ms-auto">বিস্তারিত</g:link>
                    </div>
                </div>
            </div>
        </div>
    </g:each>
    <g:if test="${!courses}"><div class="col-12"><div class="alert alert-light border text-center">কোনো কোর্স নেই।</div></div></g:if>
</div>
</body>
</html>

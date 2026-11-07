<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>ঘোষণা</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="page-title mb-0">কোম্পানির ঘোষণা</h1>
    <g:if test="${hr}"><g:link action="create" class="btn btn-primary"><i class="bi bi-plus-lg"></i> নতুন ঘোষণা</g:link></g:if>
</div>

<g:each in="${current}" var="a">
    <div class="card mb-3 ${a.pinned ? 'border-warning' : ''}">
        <div class="card-body">
            <div class="d-flex justify-content-between align-items-start gap-2">
                <div>
                    <g:if test="${a.pinned}"><i class="bi bi-pin-angle-fill text-warning"></i></g:if>
                    <span class="fw-semibold fs-5">${a.title}</span>
                    <hrms:d9Badge kind="announcement" status="${a.category}"/>
                </div>
                <g:if test="${hr}">
                    <div class="d-flex gap-1">
                        <g:form action="toggle" id="${a.id}" method="POST"><button type="submit" class="btn btn-sm btn-outline-secondary">বন্ধ করুন</button></g:form>
                        <g:form action="remove" id="${a.id}" method="POST" onsubmit="return confirm('ঘোষণা মুছে ফেলবেন?');"><button type="submit" class="btn btn-sm btn-outline-danger"><i class="bi bi-trash"></i></button></g:form>
                    </div>
                </g:if>
            </div>
            <div class="mt-2" style="white-space: pre-line;">${a.body}</div>
            <div class="small text-muted mt-2">প্রকাশ: <hrms:date value="${a.publishDate}"/> · ${a.postedBy}<g:if test="${a.expiryDate}"> · মেয়াদ: <hrms:date value="${a.expiryDate}"/></g:if></div>
        </div>
    </div>
</g:each>
<g:if test="${!current}"><div class="alert alert-light border">এখন কোনো চালু ঘোষণা নেই।</div></g:if>

<g:if test="${hr && others}">
    <h2 class="h5 mt-4">বন্ধ / মেয়াদোত্তীর্ণ / ভবিষ্যতের ঘোষণা</h2>
    <div class="card">
        <div class="table-responsive">
            <table class="table align-middle mb-0">
                <thead class="table-light"><tr><th>শিরোনাম</th><th>প্রকাশ</th><th>মেয়াদ</th><th>অবস্থা</th><th></th></tr></thead>
                <tbody>
                <g:each in="${others}" var="a">
                    <tr>
                        <td>${a.title}</td>
                        <td><hrms:date value="${a.publishDate}"/></td>
                        <td><hrms:date value="${a.expiryDate}"/></td>
                        <td>
                            <g:if test="${!a.active}"><span class="badge text-bg-secondary">বন্ধ</span></g:if>
                            <g:elseif test="${a.publishDate.isAfter(today)}"><span class="badge text-bg-info text-dark">আসছে</span></g:elseif>
                            <g:else><span class="badge text-bg-light text-dark border">মেয়াদ শেষ</span></g:else>
                        </td>
                        <td class="text-end">
                            <div class="d-flex gap-1 justify-content-end">
                                <g:form action="toggle" id="${a.id}" method="POST"><button type="submit" class="btn btn-sm btn-outline-secondary">${a.active ? 'বন্ধ করুন' : 'চালু করুন'}</button></g:form>
                                <g:form action="remove" id="${a.id}" method="POST" onsubmit="return confirm('ঘোষণা মুছে ফেলবেন?');"><button type="submit" class="btn btn-sm btn-outline-danger"><i class="bi bi-trash"></i></button></g:form>
                            </div>
                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>
    </div>
</g:if>
</body>
</html>

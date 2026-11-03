<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>চাকরির বিজ্ঞপ্তি</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="page-title mb-0">চাকরির বিজ্ঞপ্তি <span class="badge text-bg-secondary fs-6">${postings.size()}</span></h1>
    <div class="d-flex gap-2">
        <g:link action="index" class="btn btn-outline-secondary"><i class="bi bi-kanban"></i> বোর্ড</g:link>
        <g:link action="createPosting" class="btn btn-primary"><i class="bi bi-plus-lg"></i> নতুন বিজ্ঞপ্তি</g:link>
    </div>
</div>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
            <tr><th>#</th><th>শিরোনাম</th><th>চাহিদা</th><th>স্থান</th><th>শেষ তারিখ</th><th class="text-center">প্রার্থী</th><th>অবস্থা</th><th></th></tr>
            </thead>
            <tbody>
            <g:each in="${postings}" var="p">
                <tr>
                    <td>${p.id}</td>
                    <td class="fw-semibold">${p.title}</td>
                    <td><g:link controller="requisition" action="show" id="${p.requisition.id}" class="text-decoration-none">#${p.requisition.id}</g:link>
                        <span class="small text-muted">${p.requisition.hiredCount()}/${p.requisition.vacancies} পূরণ</span></td>
                    <td>${p.location ?: '—'}</td>
                    <td><hrms:date value="${p.deadline}"/>
                        <g:if test="${p.status == 'OPEN' && !p.acceptingApplications}"><span class="badge text-bg-warning text-dark">মেয়াদ পার</span></g:if></td>
                    <td class="text-center"><g:link action="index" params="[posting: p.id]" class="text-decoration-none">${counts[p.id] ?: 0}</g:link></td>
                    <td><hrms:recruitBadge kind="posting" status="${p.status}"/></td>
                    <td class="text-end text-nowrap">
                        <g:if test="${p.status == 'OPEN'}">
                            <g:form action="closePosting" id="${p.id}" method="POST" class="d-inline" onsubmit="return confirm('বিজ্ঞপ্তি বন্ধ করবেন?');">
                                <button type="submit" class="btn btn-sm btn-outline-secondary">বন্ধ করুন</button>
                            </g:form>
                        </g:if>
                        <g:else>
                            <g:form action="reopenPosting" id="${p.id}" method="POST" class="d-inline">
                                <button type="submit" class="btn btn-sm btn-outline-primary">আবার খুলুন</button>
                            </g:form>
                        </g:else>
                    </td>
                </tr>
            </g:each>
            <g:if test="${!postings}"><tr><td colspan="8" class="text-center text-muted py-4">কোনো বিজ্ঞপ্তি নেই। অনুমোদিত চাহিদা থেকে বিজ্ঞপ্তি দিন।</td></tr></g:if>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>

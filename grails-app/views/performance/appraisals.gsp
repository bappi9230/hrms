<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>মূল্যায়ন</title>
</head>
<body>
<h1 class="page-title">পারফরম্যান্স</h1>
<g:render template="tabs" model="[active: 'appraisals']"/>

<g:if test="${!cycle}">
    <div class="alert alert-info">এখনো কোনো মূল্যায়ন চক্র চালু হয়নি।
        <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_HR"><g:link action="cycles">চক্র তৈরি করুন</g:link></sec:ifAnyGranted>
    </div>
</g:if>
<g:else>
    <div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
        <div>
            <span class="fw-semibold">${cycle.name}</span> <hrms:d8Badge kind="cycle" status="${cycle.status}"/>
            <div class="small text-muted"><hrms:date value="${cycle.startDate}"/> - <hrms:date value="${cycle.endDate}"/> &middot; চূড়ান্ত তারিখ <hrms:date value="${cycle.dueDate}"/></div>
        </div>
        <g:if test="${cycles.size() > 1}">
            <div class="dropdown">
                <button class="btn btn-sm btn-outline-secondary dropdown-toggle" data-bs-toggle="dropdown">চক্র বদলান</button>
                <ul class="dropdown-menu dropdown-menu-end">
                    <g:each in="${cycles}" var="c"><li><g:link action="appraisals" params="[cycle: c.id]" class="dropdown-item ${c.id == cycle.id ? 'active' : ''}">${c.name}</g:link></li></g:each>
                </ul>
            </div>
        </g:if>
    </div>

    <div class="card">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                <tr><th>কর্মী</th><th>মূল্যায়নকারী</th><th>নিজের রেটিং</th><th>ম্যানেজারের রেটিং</th><th>লক্ষ্য অর্জন</th><th>অবস্থা</th><th></th></tr>
                </thead>
                <tbody>
                <g:each in="${appraisals}" var="a">
                    <tr>
                        <td class="fw-semibold">${a.employee.name}<g:if test="${a.employee.id == myId}"> <span class="badge text-bg-light border">আমি</span></g:if></td>
                        <td>${a.reviewer ?: 'HR / Admin'}</td>
                        <td><hrms:stars value="${a.selfRating}"/></td>
                        <td><hrms:stars value="${a.managerRating}"/></td>
                        <td>${a.goalScore != null ? a.goalScore + '%' : '—'}</td>
                        <td><hrms:d8Badge kind="appraisal" status="${a.status}"/></td>
                        <td class="text-end">
                            <g:link action="appraisal" id="${a.id}" class="btn btn-sm ${canReview[a.id] || (a.employee.id == myId && a.status == 'PENDING_SELF' && cycle.status == 'OPEN') ? 'btn-primary' : 'btn-outline-secondary'}">
                                ${a.employee.id == myId && a.status == 'PENDING_SELF' && cycle.status == 'OPEN' ? 'মূল্যায়ন করুন' : (canReview[a.id] ? 'রিভিউ দিন' : 'দেখুন')}
                            </g:link>
                        </td>
                    </tr>
                </g:each>
                <g:if test="${!appraisals}"><tr><td colspan="7" class="text-center text-muted py-4">এই চক্রে আপনার দেখার মতো কোনো মূল্যায়ন নেই।</td></tr></g:if>
                </tbody>
            </table>
        </div>
    </div>
</g:else>
</body>
</html>

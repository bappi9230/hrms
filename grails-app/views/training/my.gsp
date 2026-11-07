<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>আমার প্রশিক্ষণ</title>
</head>
<body>
<h1 class="page-title">প্রশিক্ষণ</h1>
<g:render template="tabs" model="[active: 'my']"/>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light"><tr><th>কোর্স</th><th>তারিখ</th><th>ঘণ্টা</th><th>অবস্থা</th><th>স্কোর</th><th></th></tr></thead>
            <tbody>
            <g:each in="${enrollments}" var="e">
                <tr>
                    <td><g:link action="show" id="${e.course.id}" class="fw-semibold text-decoration-none">${e.course.title}</g:link><div class="small text-muted"><hrms:d8Name value="${e.course.category}"/></div></td>
                    <td><hrms:date value="${e.course.startDate}"/><g:if test="${e.course.endDate != e.course.startDate}"> - <hrms:date value="${e.course.endDate}"/></g:if></td>
                    <td>${e.course.hours}</td>
                    <td><hrms:d8Badge kind="enroll" status="${e.status}"/><g:if test="${e.completedDate}"><div class="small text-muted"><hrms:date value="${e.completedDate}"/></div></g:if></td>
                    <td>${e.score != null ? e.score : '—'}</td>
                    <td class="text-end">
                        <g:if test="${e.status == 'COMPLETED'}">
                            <g:link action="certificate" id="${e.id}" class="btn btn-sm btn-success"><i class="bi bi-award"></i> সার্টিফিকেট</g:link>
                        </g:if>
                        <g:if test="${e.status == 'ENROLLED' && e.course.startDate.isAfter(today)}">
                            <g:form action="cancelEnrollment" id="${e.id}" method="POST" class="d-inline" onsubmit="return confirm('নথিভুক্তি বাতিল করবেন?');">
                                <input type="hidden" name="back" value="my">
                                <button type="submit" class="btn btn-sm btn-outline-danger">বাতিল</button>
                            </g:form>
                        </g:if>
                    </td>
                </tr>
            </g:each>
            <g:if test="${!enrollments}"><tr><td colspan="6" class="text-center text-muted py-4">আপনি এখনো কোনো কোর্সে নথিভুক্ত হননি। <g:link action="index">কোর্স ক্যাটালগ দেখুন</g:link></td></tr></g:if>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>

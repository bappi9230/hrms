<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>ইমেইল লগ</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="page-title mb-0">ইমেইল লগ</h1>
    <g:link action="index" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> অডিট ট্রেইল</g:link>
</div>
<g:if test="${!configured}"><div class="alert alert-warning small">মেইল সার্ভার ঠিক করা নেই (<code>SPRING_MAIL_HOST</code>), তাই ইমেইল পাঠানো হয় না - শুধু এখানে জমা থাকে।</div></g:if>
<div class="card">
    <div class="table-responsive">
        <table class="table table-sm align-middle mb-0">
            <thead class="table-light"><tr><th>সময়</th><th>প্রাপক</th><th>বিষয়</th><th>অবস্থা</th></tr></thead>
            <tbody>
            <g:each in="${rows}" var="r">
                <tr>
                    <td class="text-nowrap"><hrms:dt value="${r.dateCreated}"/></td>
                    <td>${r.toAddress}</td>
                    <td>${r.subject}<g:if test="${r.error}"><div class="small text-muted">${r.error}</div></g:if></td>
                    <td><hrms:d9Badge kind="email" status="${r.status}"/></td>
                </tr>
            </g:each>
            <g:if test="${!rows}"><tr><td colspan="4" class="text-center text-muted py-4">কোনো ইমেইল নেই।</td></tr></g:if>
            </tbody>
        </table>
    </div>
    <div class="card-footer bg-white d-flex justify-content-between small">
        <span>মোট ${total}টি</span>
        <span>
            <g:if test="${offset > 0}"><g:link action="emails" params="[offset: Math.max(0, offset - page)]">&laquo; আগের</g:link></g:if>
            <g:if test="${offset + page < total}"> <g:link action="emails" params="[offset: offset + page]">পরের &raquo;</g:link></g:if>
        </span>
    </div>
</div>
</body>
</html>

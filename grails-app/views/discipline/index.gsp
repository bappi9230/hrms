<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>শৃঙ্খলা</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="page-title mb-0">শৃঙ্খলা সংক্রান্ত কেস</h1>
    <g:if test="${canRaise}"><g:link action="create" class="btn btn-primary"><i class="bi bi-plus-lg"></i> নতুন কেস</g:link></g:if>
</div>
<div class="alert alert-secondary small py-2"><i class="bi bi-lock"></i> এই তথ্য গোপনীয়: শুধু HR/Admin, কেস যিনি খুলেছেন এবং সংশ্লিষ্ট কর্মী দেখতে পান।</div>
<div class="btn-group btn-group-sm mb-3">
    <g:link action="index" class="btn btn-outline-secondary ${!status ? 'active' : ''}">সব</g:link>
    <g:each in="${['OPEN', 'RESPONDED', 'CLOSED']}" var="s"><g:link action="index" params="[status: s]" class="btn btn-outline-secondary ${status == s ? 'active' : ''}"><hrms:d9Badge kind="discipline" status="${s}"/></g:link></g:each>
</div>
<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light"><tr><th>#</th><th>কর্মী</th><th>ধরন</th><th>বিষয়</th><th>ঘটনার তারিখ</th><th>অবস্থা</th><th></th></tr></thead>
            <tbody>
            <g:each in="${cases}" var="c">
                <tr>
                    <td>${c.id}</td>
                    <td>${c.employee.name}</td>
                    <td><hrms:d9Name value="${c.caseType}"/></td>
                    <td>${c.subject}</td>
                    <td><hrms:date value="${c.incidentDate}"/></td>
                    <td><hrms:d9Badge kind="discipline" status="${c.status}"/></td>
                    <td class="text-end"><g:link action="show" id="${c.id}" class="btn btn-sm btn-outline-primary">দেখুন</g:link></td>
                </tr>
            </g:each>
            <g:if test="${!cases}"><tr><td colspan="7" class="text-center text-muted py-4">কোনো কেস নেই।</td></tr></g:if>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>

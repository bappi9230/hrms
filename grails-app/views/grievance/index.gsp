<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>অভিযোগ</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="page-title mb-0">অভিযোগ</h1>
    <g:if test="${hasEmployee}"><g:link action="create" class="btn btn-primary"><i class="bi bi-plus-lg"></i> নতুন অভিযোগ</g:link></g:if>
</div>
<div class="alert alert-secondary small py-2"><i class="bi bi-lock"></i> অভিযোগ গোপনীয়: শুধু অভিযোগকারী ও HR/Admin দেখতে পান।</div>
<div class="btn-group btn-group-sm mb-3">
    <g:link action="index" class="btn btn-outline-secondary ${!status ? 'active' : ''}">সব</g:link>
    <g:each in="${['OPEN', 'IN_REVIEW', 'RESOLVED', 'REJECTED']}" var="s"><g:link action="index" params="[status: s]" class="btn btn-outline-secondary ${status == s ? 'active' : ''}"><hrms:d9Badge kind="grievance" status="${s}"/></g:link></g:each>
</div>
<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light"><tr><th>#</th><g:if test="${isHr}"><th>কর্মী</th></g:if><th>ধরন</th><th>বিষয়</th><th>জমার তারিখ</th><th>অবস্থা</th><th></th></tr></thead>
            <tbody>
            <g:each in="${grievances}" var="gr">
                <tr>
                    <td>${gr.id}</td>
                    <g:if test="${isHr}"><td>${gr.employee.name}</td></g:if>
                    <td><hrms:d9Name value="${gr.category}"/></td>
                    <td>${gr.subject}</td>
                    <td><hrms:date value="${gr.dateCreated}"/></td>
                    <td><hrms:d9Badge kind="grievance" status="${gr.status}"/></td>
                    <td class="text-end"><g:link action="show" id="${gr.id}" class="btn btn-sm btn-outline-primary">দেখুন</g:link></td>
                </tr>
            </g:each>
            <g:if test="${!grievances}"><tr><td colspan="7" class="text-center text-muted py-4">কোনো অভিযোগ নেই।</td></tr></g:if>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>

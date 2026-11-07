<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>খরচের দাবি</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="page-title mb-0">খরচ ও সম্পদ</h1>
    <g:if test="${hasEmployee}"><g:link action="create" class="btn btn-primary"><i class="bi bi-plus-lg"></i> নতুন দাবি</g:link></g:if>
</div>
<g:render template="/expense/tabs" model="[active: 'expense']"/>

<div class="d-flex flex-wrap gap-2 mb-3">
    <div class="btn-group btn-group-sm">
        <g:if test="${hasEmployee}"><g:link action="index" params="[scope: 'mine']" class="btn btn-outline-primary ${scope == 'mine' ? 'active' : ''}">আমার দাবি</g:link></g:if>
        <g:if test="${approver}">
            <g:link action="index" params="[scope: 'approvals']" class="btn btn-outline-primary ${scope == 'approvals' ? 'active' : ''}">অনুমোদন / পরিশোধ <g:if test="${pendingCount}"><span class="badge text-bg-danger">${pendingCount}</span></g:if></g:link>
        </g:if>
        <g:if test="${seesAll}"><g:link action="index" params="[scope: 'all']" class="btn btn-outline-primary ${scope == 'all' ? 'active' : ''}">সব দাবি</g:link></g:if>
    </div>
    <g:if test="${scope != 'approvals'}">
        <div class="btn-group btn-group-sm">
            <g:link action="index" params="[scope: scope]" class="btn btn-outline-secondary ${!status ? 'active' : ''}">সব অবস্থা</g:link>
            <g:each in="${['PENDING_MANAGER', 'PENDING_ACCOUNTS', 'APPROVED', 'PAID', 'REJECTED']}" var="s">
                <g:link action="index" params="[scope: scope, status: s]" class="btn btn-outline-secondary ${status == s ? 'active' : ''}"><hrms:d8Badge kind="claim" status="${s}"/></g:link>
            </g:each>
        </div>
    </g:if>
</div>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
            <tr><th>#</th><th>কর্মী</th><th>বিবরণ</th><th>তারিখ</th><th class="text-end">পরিমাণ</th><th>রসিদ</th><th>অবস্থা</th><th></th></tr>
            </thead>
            <tbody>
            <g:each in="${claims}" var="c">
                <tr>
                    <td>${c.id}</td>
                    <td>${c.employee.name}</td>
                    <td><div class="fw-semibold">${c.title}</div><div class="small text-muted"><hrms:d8Name value="${c.category}"/></div></td>
                    <td><hrms:date value="${c.expenseDate}"/></td>
                    <td class="text-end"><hrms:money value="${c.amount}"/></td>
                    <td><g:if test="${c.hasReceipt}"><i class="bi bi-paperclip"></i></g:if><g:else><span class="text-muted">—</span></g:else></td>
                    <td><hrms:d8Badge kind="claim" status="${c.status}"/></td>
                    <td class="text-end"><g:link action="show" id="${c.id}" class="btn btn-sm btn-outline-primary">${scope == 'approvals' ? (c.status == 'APPROVED' ? 'পরিশোধ' : 'রিভিউ') : 'দেখুন'}</g:link></td>
                </tr>
            </g:each>
            <g:if test="${!claims}"><tr><td colspan="8" class="text-center text-muted py-4">কোনো দাবি নেই।</td></tr></g:if>
            </tbody>
            <g:if test="${claims}">
                <tfoot><tr class="table-light"><td colspan="4" class="text-end small text-muted">বাতিল/প্রত্যাখ্যাত বাদে মোট</td><td class="text-end fw-semibold"><hrms:money value="${total}"/></td><td colspan="3"></td></tr></tfoot>
            </g:if>
        </table>
    </div>
</div>
</body>
</html>

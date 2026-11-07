<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>চাকরি ছাড়া</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="page-title mb-0">চাকরি ছাড়া</h1>
    <div class="d-flex gap-2">
        <g:if test="${hasEmployee}"><g:link action="apply" class="btn btn-primary"><i class="bi bi-box-arrow-right"></i> পদত্যাগের আবেদন</g:link></g:if>
        <g:if test="${canStart}"><g:link action="startForm" class="btn btn-outline-danger"><i class="bi bi-person-x"></i> HR এর পক্ষ থেকে শুরু</g:link></g:if>
    </div>
</div>

<div class="d-flex flex-wrap gap-2 mb-3">
    <div class="btn-group btn-group-sm">
        <g:if test="${hasEmployee}"><g:link action="index" params="[scope: 'mine']" class="btn btn-outline-primary ${scope == 'mine' ? 'active' : ''}">আমার</g:link></g:if>
        <g:if test="${isManager}"><g:link action="index" params="[scope: 'team']" class="btn btn-outline-primary ${scope == 'team' ? 'active' : ''}">আমার টিম</g:link></g:if>
        <g:if test="${approver}">
            <g:link action="index" params="[scope: 'approvals']" class="btn btn-outline-primary ${scope == 'approvals' ? 'active' : ''}">অপেক্ষমাণ <g:if test="${pendingCount}"><span class="badge text-bg-danger">${pendingCount}</span></g:if></g:link>
        </g:if>
        <g:if test="${seesAll}"><g:link action="index" params="[scope: 'all']" class="btn btn-outline-primary ${scope == 'all' ? 'active' : ''}">সব কেস</g:link></g:if>
    </div>
    <g:if test="${scope != 'approvals'}">
        <div class="btn-group btn-group-sm">
            <g:link action="index" params="[scope: scope]" class="btn btn-outline-secondary ${!status ? 'active' : ''}">সব অবস্থা</g:link>
            <g:each in="${['PENDING_MANAGER', 'PENDING_HR', 'ACCEPTED', 'COMPLETED', 'REJECTED', 'WITHDRAWN']}" var="s">
                <g:link action="index" params="[scope: scope, status: s]" class="btn btn-outline-secondary ${status == s ? 'active' : ''}"><hrms:d9Badge kind="separation" status="${s}"/></g:link>
            </g:each>
        </div>
    </g:if>
</div>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
            <tr><th>#</th><th>কর্মী</th><th>ধরন</th><th>কারণ</th><th>নোটিস</th><th>শেষ কর্মদিবস</th><th>অবস্থা</th><th></th></tr>
            </thead>
            <tbody>
            <g:each in="${separations}" var="s">
                <tr>
                    <td>${s.id}</td>
                    <td>${s.employee.name}<div class="small text-muted">${s.employee.department}</div></td>
                    <td><hrms:d9Name value="${s.exitType}"/></td>
                    <td><hrms:d9Name value="${s.reasonCategory}"/></td>
                    <td><hrms:date value="${s.noticeDate}"/></td>
                    <td><hrms:date value="${s.lastWorkingDay ?: s.requestedLastDay}"/></td>
                    <td><hrms:d9Badge kind="separation" status="${s.status}"/></td>
                    <td class="text-end"><g:link action="show" id="${s.id}" class="btn btn-sm btn-outline-primary">দেখুন</g:link></td>
                </tr>
            </g:each>
            <g:if test="${!separations}"><tr><td colspan="8" class="text-center text-muted py-4">কোনো কেস নেই।</td></tr></g:if>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>

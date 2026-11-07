<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>সুবিধা</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="page-title mb-0">খরচ ও সম্পদ</h1>
    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_HR">
        <g:link controller="master" action="index" params="[type: 'benefitplan']" class="btn btn-outline-secondary"><i class="bi bi-gear"></i> সুবিধার প্ল্যান</g:link>
    </sec:ifAnyGranted>
</div>
<g:render template="/expense/tabs" model="[active: 'benefit']"/>

<div class="btn-group btn-group-sm mb-3">
    <g:if test="${hasEmployee}"><g:link action="index" params="[scope: 'mine']" class="btn btn-outline-primary ${scope == 'mine' ? 'active' : ''}">আমার সুবিধা</g:link></g:if>
    <g:if test="${seesAll}"><g:link action="index" params="[scope: 'all']" class="btn btn-outline-primary ${scope == 'all' ? 'active' : ''}">সবার সুবিধা</g:link></g:if>
</div>

<g:if test="${canManage && scope == 'all'}">
<div class="card mb-3">
    <div class="card-header bg-white fw-semibold">কর্মীকে সুবিধা দিন</div>
    <div class="card-body">
        <g:form action="enroll" method="POST" class="row g-2">
            <div class="col-md-3">
                <select name="employee" class="form-select" required>
                    <option value="">— কর্মী —</option>
                    <g:each in="${employees}" var="e"><option value="${e.id}">${e.name}</option></g:each>
                </select>
            </div>
            <div class="col-md-3">
                <select name="plan" class="form-select" required>
                    <option value="">— প্ল্যান —</option>
                    <g:each in="${plans}" var="p"><option value="${p.id}">${p.name}<g:if test="${p.minServiceMonths}"> (${p.minServiceMonths} মাস+)</g:if></option></g:each>
                </select>
            </div>
            <div class="col-md-2"><input type="date" name="startDate" value="${today}" class="form-control"></div>
            <div class="col-md-3"><input type="text" name="remarks" maxlength="255" class="form-control" placeholder="মন্তব্য (ঐচ্ছিক)"></div>
            <div class="col-md-1"><button type="submit" class="btn btn-primary w-100">যোগ</button></div>
        </g:form>
    </div>
</div>
</g:if>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light"><tr><g:if test="${scope == 'all'}"><th>কর্মী</th></g:if><th>সুবিধা</th><th>সরবরাহকারী</th><th class="text-end">মাসিক মূল্য</th><th>শুরু</th><th>শেষ</th><th>অবস্থা</th><g:if test="${canManage && scope == 'all'}"><th></th></g:if></tr></thead>
            <tbody>
            <g:each in="${enrollments}" var="b">
                <tr>
                    <g:if test="${scope == 'all'}"><td>${b.employee.name}</td></g:if>
                    <td><div class="fw-semibold">${b.plan.name}</div><div class="small text-muted"><hrms:d8Name value="${b.plan.benefitType}"/></div></td>
                    <td>${b.plan.provider ?: '—'}</td>
                    <td class="text-end"><hrms:money value="${b.plan.monthlyValue}"/></td>
                    <td><hrms:date value="${b.startDate}"/></td>
                    <td><hrms:date value="${b.endDate}"/></td>
                    <td><hrms:d8Badge kind="benefit" status="${b.status}"/></td>
                    <g:if test="${canManage && scope == 'all'}">
                        <td class="text-end">
                            <g:if test="${b.status == 'ACTIVE'}">
                                <g:form action="endBenefit" id="${b.id}" method="POST" class="d-inline" onsubmit="return confirm('সুবিধা আজ থেকে শেষ করবেন?');">
                                    <button type="submit" class="btn btn-sm btn-outline-danger">শেষ করুন</button>
                                </g:form>
                            </g:if>
                        </td>
                    </g:if>
                </tr>
            </g:each>
            <g:if test="${!enrollments}"><tr><td colspan="8" class="text-center text-muted py-4">কোনো সুবিধা নেই।</td></tr></g:if>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>

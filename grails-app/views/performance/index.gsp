<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>লক্ষ্য / KPI</title>
</head>
<body>
<h1 class="page-title">পারফরম্যান্স</h1>
<g:render template="tabs" model="[active: 'index']"/>

<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <div class="d-flex flex-wrap gap-2">
        <g:if test="${wide && me}">
            <div class="btn-group btn-group-sm">
                <g:link action="index" params="[scope: 'mine']" class="btn btn-outline-primary ${scope == 'mine' ? 'active' : ''}">আমার লক্ষ্য</g:link>
                <g:link action="index" params="[scope: 'team']" class="btn btn-outline-primary ${scope == 'team' ? 'active' : ''}">দলের লক্ষ্য</g:link>
            </div>
        </g:if>
        <div class="btn-group btn-group-sm">
            <g:link action="index" params="[scope: scope]" class="btn btn-outline-secondary ${!status ? 'active' : ''}">সব</g:link>
            <g:link action="index" params="[scope: scope, status: 'ACTIVE']" class="btn btn-outline-secondary ${status == 'ACTIVE' ? 'active' : ''}">চলমান</g:link>
            <g:link action="index" params="[scope: scope, status: 'COMPLETED']" class="btn btn-outline-secondary ${status == 'COMPLETED' ? 'active' : ''}">অর্জিত</g:link>
            <g:link action="index" params="[scope: scope, status: 'CANCELLED']" class="btn btn-outline-secondary ${status == 'CANCELLED' ? 'active' : ''}">বাতিল</g:link>
        </div>
    </div>
    <g:if test="${assignable}">
        <button class="btn btn-primary" type="button" data-bs-toggle="collapse" data-bs-target="#goalForm"><i class="bi bi-plus-lg"></i> নতুন লক্ষ্য</button>
    </g:if>
</div>

<g:if test="${assignable}">
<div class="collapse mb-3" id="goalForm">
    <div class="card">
        <div class="card-body">
            <g:form action="saveGoal" method="POST" class="row g-3">
                <g:if test="${assignable.size() > 1}">
                    <div class="col-md-4">
                        <label class="form-label" for="goalEmployee">কর্মী <span class="text-danger">*</span></label>
                        <select name="employee" id="goalEmployee" class="form-select" required>
                            <g:each in="${assignable}" var="e"><option value="${e.id}" ${me?.id == e.id ? 'selected' : ''}>${e.name}${me?.id == e.id ? ' (আমি)' : ''}</option></g:each>
                        </select>
                    </div>
                </g:if>
                <g:else><input type="hidden" name="employee" value="${assignable[0].id}"></g:else>
                <div class="col-md-8">
                    <label class="form-label" for="goalTitle">লক্ষ্য / KPI <span class="text-danger">*</span></label>
                    <input type="text" id="goalTitle" name="title" maxlength="150" class="form-control" placeholder="যেমন: মাসে ২০টি বাগ ফিক্স" required>
                </div>
                <div class="col-12">
                    <label class="form-label" for="goalDesc">বিবরণ</label>
                    <input type="text" id="goalDesc" name="description" maxlength="500" class="form-control">
                </div>
                <div class="col-md-3">
                    <label class="form-label" for="goalTarget">লক্ষ্যমাত্রা <span class="text-danger">*</span></label>
                    <input type="number" id="goalTarget" name="targetValue" value="100" min="0.01" step="any" class="form-control" required>
                </div>
                <div class="col-md-3">
                    <label class="form-label" for="goalUnit">একক</label>
                    <input type="text" id="goalUnit" name="unit" value="%" maxlength="20" class="form-control" placeholder="%, টি, ৳ ...">
                </div>
                <div class="col-md-3">
                    <label class="form-label" for="goalWeight">Weight (১-১০০) <span class="text-danger">*</span></label>
                    <input type="number" id="goalWeight" name="weight" value="10" min="1" max="100" class="form-control" required>
                </div>
                <div class="col-md-3">
                    <label class="form-label" for="goalDue">শেষ তারিখ <span class="text-danger">*</span></label>
                    <input type="date" id="goalDue" name="dueDate" min="${today}" class="form-control" required>
                </div>
                <div class="col-12 small text-muted">একজন কর্মীর একই বছরের সব সচল লক্ষ্যের weight এর যোগফল সর্বোচ্চ ১০০।</div>
                <div class="col-12">
                    <button type="submit" class="btn btn-primary"><i class="bi bi-check-lg"></i> সংরক্ষণ</button>
                </div>
            </g:form>
        </div>
    </div>
</div>
</g:if>

<g:if test="${scope == 'mine'}">
    <div class="alert alert-light border py-2 small">${today.year} সালের জন্য আপনার লক্ষ্যের মোট weight: <strong>${usedWeight}</strong> / ১০০</div>
</g:if>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
            <tr>
                <g:if test="${scope == 'team'}"><th>কর্মী</th></g:if>
                <th>লক্ষ্য</th><th style="min-width:150px;">অগ্রগতি</th><th class="text-center">Weight</th><th>শেষ তারিখ</th><th>অবস্থা</th><th style="min-width:230px;"></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${goals}" var="g">
                <tr>
                    <g:if test="${scope == 'team'}"><td class="fw-semibold">${g.employee.name}</td></g:if>
                    <td><div class="fw-semibold">${g.title}</div><g:if test="${g.description}"><div class="small text-muted">${g.description}</div></g:if></td>
                    <td>
                        <hrms:progressBar value="${g.progressPercent()}"/>
                        <div class="small text-muted"><g:formatNumber number="${g.currentValue}" maxFractionDigits="2"/> / <g:formatNumber number="${g.targetValue}" maxFractionDigits="2"/> ${g.unit} (${g.progressPercent()}%)</div>
                    </td>
                    <td class="text-center">${g.weight}</td>
                    <td>
                        <hrms:date value="${g.dueDate}"/>
                        <g:if test="${g.status == 'ACTIVE' && g.dueDate.isBefore(today)}"><div class="small text-danger">সময় পেরিয়েছে</div></g:if>
                    </td>
                    <td><hrms:d8Badge kind="goal" status="${g.status}"/></td>
                    <td class="text-end">
                        <g:if test="${canEdit[g.id] && g.status != 'CANCELLED'}">
                            <g:form action="updateGoal" id="${g.id}" method="POST" class="d-inline-flex gap-1">
                                <input type="hidden" name="scope" value="${scope}">
                                <input type="number" name="currentValue" value="${g.currentValue}" min="0" step="any" class="form-control form-control-sm" style="width:90px;" aria-label="বর্তমান মান">
                                <button type="submit" class="btn btn-sm btn-outline-primary">হালনাগাদ</button>
                            </g:form>
                            <g:if test="${g.status == 'ACTIVE'}">
                                <g:form action="cancelGoal" id="${g.id}" method="POST" class="d-inline" onsubmit="return confirm('লক্ষ্যটি বাতিল করবেন?');">
                                    <input type="hidden" name="scope" value="${scope}">
                                    <button type="submit" class="btn btn-sm btn-outline-danger" title="বাতিল"><i class="bi bi-x-lg"></i></button>
                                </g:form>
                            </g:if>
                        </g:if>
                    </td>
                </tr>
            </g:each>
            <g:if test="${!goals}"><tr><td colspan="7" class="text-center text-muted py-4">কোনো লক্ষ্য নেই।</td></tr></g:if>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>

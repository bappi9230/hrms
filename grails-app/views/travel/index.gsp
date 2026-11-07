<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>ভ্রমণের অনুরোধ</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="page-title mb-0">খরচ ও সম্পদ</h1>
    <g:if test="${me}"><button class="btn btn-primary" type="button" data-bs-toggle="collapse" data-bs-target="#travelForm"><i class="bi bi-plus-lg"></i> নতুন ভ্রমণ অনুরোধ</button></g:if>
</div>
<g:render template="/expense/tabs" model="[active: 'travel']"/>

<g:if test="${me}">
<div class="collapse mb-3" id="travelForm">
    <div class="card">
        <div class="card-body">
            <g:form action="save" method="POST" class="row g-3">
                <div class="col-md-6">
                    <label class="form-label" for="destination">গন্তব্য <span class="text-danger">*</span></label>
                    <input type="text" id="destination" name="destination" maxlength="120" class="form-control" required>
                </div>
                <div class="col-md-3">
                    <label class="form-label" for="fromDate">যাত্রা <span class="text-danger">*</span></label>
                    <input type="date" id="fromDate" name="fromDate" min="${today}" class="form-control" required>
                </div>
                <div class="col-md-3">
                    <label class="form-label" for="toDate">ফেরা <span class="text-danger">*</span></label>
                    <input type="date" id="toDate" name="toDate" min="${today}" class="form-control" required>
                </div>
                <div class="col-md-4">
                    <label class="form-label" for="travelMode">মাধ্যম</label>
                    <select id="travelMode" name="travelMode" class="form-select"><g:each in="${modes}" var="m"><option value="${m}"><hrms:d8Name value="${m}"/></option></g:each></select>
                </div>
                <div class="col-md-4">
                    <label class="form-label" for="estimatedCost">আনুমানিক খরচ (৳)</label>
                    <input type="number" id="estimatedCost" name="estimatedCost" min="0" step="0.01" class="form-control">
                </div>
                <div class="col-12">
                    <label class="form-label" for="purpose">উদ্দেশ্য <span class="text-danger">*</span></label>
                    <textarea id="purpose" name="purpose" rows="2" maxlength="500" class="form-control" required></textarea>
                </div>
                <div class="col-12">
                    <button type="submit" class="btn btn-primary"><i class="bi bi-send"></i> অনুরোধ জমা দিন</button>
                    <span class="small text-muted ms-2">ম্যানেজার (না থাকলে HR) অনুমোদন করবেন। অনুমোদিত ছুটির দিনে ভ্রমণ চলবে না।</span>
                </div>
            </g:form>
        </div>
    </div>
</div>
</g:if>

<div class="btn-group btn-group-sm mb-3">
    <g:if test="${me}"><g:link action="index" params="[scope: 'mine']" class="btn btn-outline-primary ${scope == 'mine' ? 'active' : ''}">আমার অনুরোধ</g:link></g:if>
    <g:if test="${approver}"><g:link action="index" params="[scope: 'approvals']" class="btn btn-outline-primary ${scope == 'approvals' ? 'active' : ''}">অনুমোদনের অপেক্ষায়</g:link></g:if>
    <g:if test="${seesAll}"><g:link action="index" params="[scope: 'all']" class="btn btn-outline-primary ${scope == 'all' ? 'active' : ''}">সব অনুরোধ</g:link></g:if>
</div>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light"><tr><th>কর্মী</th><th>গন্তব্য ও উদ্দেশ্য</th><th>তারিখ</th><th>মাধ্যম</th><th class="text-end">আনুমানিক খরচ</th><th>অবস্থা</th><th style="min-width:260px;"></th></tr></thead>
            <tbody>
            <g:each in="${travels}" var="t">
                <tr>
                    <td>${t.employee.name}</td>
                    <td><div class="fw-semibold">${t.destination}</div><div class="small text-muted">${t.purpose}</div></td>
                    <td class="text-nowrap"><hrms:date value="${t.fromDate}"/> - <hrms:date value="${t.toDate}"/></td>
                    <td><hrms:d8Name value="${t.travelMode}"/></td>
                    <td class="text-end"><hrms:money value="${t.estimatedCost}"/></td>
                    <td>
                        <hrms:d8Badge kind="travel" status="${t.status}"/>
                        <g:if test="${t.decisionNote}"><div class="small text-muted">${t.decisionNote}</div></g:if>
                        <g:if test="${claimCount[t.id]}"><div class="small text-muted">${claimCount[t.id]}টি খরচের দাবি</div></g:if>
                    </td>
                    <td class="text-end">
                        <g:if test="${canAct[t.id]}">
                            <g:form action="approve" id="${t.id}" method="POST" class="d-inline">
                                <input type="hidden" name="scope" value="${scope}">
                                <button type="submit" class="btn btn-sm btn-success"><i class="bi bi-check-lg"></i></button>
                            </g:form>
                            <g:form action="reject" id="${t.id}" method="POST" class="d-inline-flex gap-1">
                                <input type="hidden" name="scope" value="${scope}">
                                <input type="text" name="note" maxlength="255" class="form-control form-control-sm" style="width:130px;" placeholder="কারণ" required>
                                <button type="submit" class="btn btn-sm btn-outline-danger"><i class="bi bi-x-lg"></i></button>
                            </g:form>
                        </g:if>
                        <g:if test="${canCancel[t.id]}">
                            <g:form action="cancel" id="${t.id}" method="POST" class="d-inline" onsubmit="return confirm('অনুরোধ প্রত্যাহার করবেন?');">
                                <input type="hidden" name="scope" value="${scope}">
                                <button type="submit" class="btn btn-sm btn-outline-secondary">প্রত্যাহার</button>
                            </g:form>
                        </g:if>
                        <g:if test="${t.status == 'APPROVED' && me && t.employee.id == me.id}">
                            <g:link controller="expense" action="create" class="btn btn-sm btn-outline-primary" title="এই ভ্রমণের খরচ দাবি করুন">খরচ</g:link>
                        </g:if>
                    </td>
                </tr>
            </g:each>
            <g:if test="${!travels}"><tr><td colspan="7" class="text-center text-muted py-4">কোনো অনুরোধ নেই।</td></tr></g:if>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>

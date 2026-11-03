<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>অনবোর্ডিং</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="page-title mb-0">অনবোর্ডিং ও পরীক্ষাকাল</h1>
    <g:link controller="master" action="index" params="[type: 'onboardingtemplate']" class="btn btn-outline-secondary"><i class="bi bi-list-check"></i> চেকলিস্ট টেমপ্লেট</g:link>
</div>

<g:if test="${noChecklist}">
    <div class="alert alert-warning">
        <div class="fw-semibold mb-1">গত ৩০ দিনে যোগ দিয়েছেন কিন্তু চেকলিস্ট নেই:</div>
        <g:each in="${noChecklist}" var="e">
            <g:form action="start" id="${e.id}" method="POST" class="d-inline">
                <button type="submit" class="btn btn-sm btn-outline-dark me-1 mb-1"><i class="bi bi-plus"></i> ${e.name}</button>
            </g:form>
        </g:each>
    </div>
</g:if>

<div class="row g-3">
    <div class="col-xl-7">
        <div class="card">
            <div class="card-header bg-white fw-semibold">অনবোর্ডিং চেকলিস্ট</div>
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light"><tr><th>কর্মী</th><th>যোগদান</th><th style="width: 38%;">অগ্রগতি</th><th></th></tr></thead>
                    <tbody>
                    <g:each in="${employees}" var="e">
                        <g:set var="p" value="${progress[e.id]}"/>
                        <tr>
                            <td><div class="fw-semibold">${e.name}</div><div class="small text-muted">${e.designation} &middot; ${e.department}</div></td>
                            <td><hrms:date value="${e.joinDate}"/></td>
                            <td>
                                <div class="progress thin mb-1"><div class="progress-bar ${p.percent == 100 ? 'bg-success' : ''}" style="width: ${p.percent}%"></div></div>
                                <span class="small text-muted">${p.done}/${p.total} কাজ (${p.percent}%)</span>
                            </td>
                            <td class="text-end"><g:link action="show" id="${e.id}" class="btn btn-sm btn-outline-secondary"><i class="bi bi-eye"></i></g:link></td>
                        </tr>
                    </g:each>
                    <g:if test="${!employees}"><tr><td colspan="4" class="text-center text-muted py-4">কোনো অনবোর্ডিং চেকলিস্ট নেই।</td></tr></g:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <div class="col-xl-5">
        <div class="card">
            <div class="card-header bg-white fw-semibold">চলমান পরীক্ষাকাল</div>
            <div class="table-responsive">
                <table class="table table-sm align-middle mb-0">
                    <thead class="table-light"><tr><th>কর্মী</th><th>শেষ</th><th>অবস্থা</th><th></th></tr></thead>
                    <tbody>
                    <g:each in="${probations}" var="r">
                        <tr>
                            <td>${r.employee.name}</td>
                            <td><hrms:date value="${r.endDate}"/></td>
                            <td><hrms:recruitBadge kind="probation" status="${r.phase()}"/></td>
                            <td class="text-end"><g:link action="show" id="${r.employee.id}" fragment="probation" class="btn btn-sm btn-outline-secondary"><i class="bi bi-eye"></i></g:link></td>
                        </tr>
                    </g:each>
                    <g:if test="${!probations}"><tr><td colspan="4" class="text-center text-muted py-3">কেউ পরীক্ষাকালে নেই।</td></tr></g:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
</body>
</html>

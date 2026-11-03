<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>বেতন শিট ${run}</title>
    <style>
        .salary-sheet { font-size: .82rem; }
        .salary-sheet th, .salary-sheet td { white-space: nowrap; }
        .salary-sheet tfoot td { background: #eef3fb; font-weight: 600; }
    </style>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <div>
        <h1 class="page-title mb-1">বেতন শিট — <hrms:monthName value="${run.payMonth}"/> ${run.payYear}</h1>
        <hrms:payrollStatus status="${run.status}"/>
        <span class="small text-muted ms-2">কর্মদিবস ${run.workingDays} &middot; চালিয়েছেন ${run.generatedBy ?: '—'}
            <g:if test="${run.status == 'FINALIZED'}">&middot; চূড়ান্ত করেছেন ${run.finalizedBy}</g:if></span>
    </div>
    <div class="d-flex flex-wrap gap-2">
        <g:link action="index" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> তালিকা</g:link>
        <g:link action="exportCsv" id="${run.id}" class="btn btn-outline-primary"><i class="bi bi-filetype-csv"></i> CSV</g:link>
        <g:if test="${run.status == 'DRAFT'}">
            <g:form action="generate" method="POST" class="d-inline" onsubmit="return confirm('নতুন করে হিসাব করবেন? হাতে দেওয়া OT ও সমন্বয় ধরে রাখা হবে।');">
                <g:hiddenField name="payYear" value="${run.payYear}"/>
                <g:hiddenField name="payMonth" value="${run.payMonth}"/>
                <button type="submit" class="btn btn-outline-primary"><i class="bi bi-arrow-repeat"></i> আবার হিসাব</button>
            </g:form>
            <g:form action="finalizeRun" id="${run.id}" method="POST" class="d-inline" onsubmit="return confirm('চূড়ান্ত করলে আর বদলানো যাবে না। চূড়ান্ত করবেন?');">
                <button type="submit" class="btn btn-success"><i class="bi bi-lock"></i> চূড়ান্ত করুন</button>
            </g:form>
            <g:form action="delete" id="${run.id}" method="POST" class="d-inline" onsubmit="return confirm('এই পে-রোল মুছে ফেলবেন?');">
                <button type="submit" class="btn btn-outline-danger"><i class="bi bi-trash"></i></button>
            </g:form>
        </g:if>
        <g:else>
            <sec:ifAnyGranted roles="ROLE_ADMIN">
                <g:form action="reopen" id="${run.id}" method="POST" class="d-inline" onsubmit="return confirm('আবার DRAFT করবেন?');">
                    <button type="submit" class="btn btn-outline-warning"><i class="bi bi-unlock"></i> পুনরায় খুলুন</button>
                </g:form>
            </sec:ifAnyGranted>
        </g:else>
    </div>
</div>

<div class="row g-3 mb-3">
    <div class="col-6 col-xl-3"><div class="card stat-card"><div class="card-body"><div class="small text-muted">কর্মী</div><div class="stat-value">${run.employeeCount}</div></div></div></div>
    <div class="col-6 col-xl-3"><div class="card stat-card"><div class="card-body"><div class="small text-muted">মোট আয় (gross)</div><div class="stat-value fs-4"><hrms:money value="${run.totalGross}"/></div></div></div></div>
    <div class="col-6 col-xl-3"><div class="card stat-card danger"><div class="card-body"><div class="small text-muted">মোট কর্তন</div><div class="stat-value fs-4"><hrms:money value="${run.totalDeduction}"/></div></div></div></div>
    <div class="col-6 col-xl-3"><div class="card stat-card success"><div class="card-body"><div class="small text-muted">মোট নেট বেতন</div><div class="stat-value fs-4"><hrms:money value="${run.totalNet}"/></div></div></div></div>
</div>

<g:if test="${warnings}">
    <div class="alert alert-warning py-2"><i class="bi bi-exclamation-triangle"></i> ${warnings} জন কর্মীর লাইনে সতর্কবার্তা আছে (⚠ চিহ্ন) - বিস্তারিত দেখে যাচাই করুন।</div>
</g:if>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover table-sm align-middle mb-0 salary-sheet">
            <thead class="table-light">
            <tr>
                <th>কর্মী</th>
                <th class="text-center">উপস্থিত/কর্মদিবস</th>
                <th class="text-end">বেসিক</th>
                <th class="text-end">বাড়ি ভাড়া</th>
                <th class="text-end">চিকিৎসা</th>
                <th class="text-end">যাতায়াত</th>
                <th class="text-end">ভাতা</th>
                <th class="text-end">OT</th>
                <th class="text-end">মোট আয়</th>
                <th class="text-end">অনুপস্থিতি/দেরি কর্তন</th>
                <th class="text-end">অন্য কর্তন</th>
                <th class="text-end">সমন্বয়</th>
                <th class="text-end">নেট বেতন</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${lines}" var="l">
                <tr>
                    <td>
                        <div class="fw-semibold">${l.employeeName}<g:if test="${l.remarks}"> <i class="bi bi-exclamation-triangle-fill text-warning" title="${l.remarks}"></i></g:if></div>
                        <div class="text-muted">${l.departmentName ?: '—'}</div>
                    </td>
                    <td class="text-center">${l.presentDays + l.paidLeaveDays + l.holidayDays}/${l.workingDays}
                        <g:if test="${l.absentDays + l.unpaidLeaveDays > 0}"><span class="badge text-bg-danger">${l.absentDays + l.unpaidLeaveDays} অনুপঃ</span></g:if>
                        <g:if test="${l.lateDays > 0}"><span class="badge text-bg-warning text-dark">${l.lateDays} দেরি</span></g:if>
                    </td>
                    <td class="text-end"><g:formatNumber number="${l.basic}" format="#,##0"/></td>
                    <td class="text-end"><g:formatNumber number="${l.houseRent}" format="#,##0"/></td>
                    <td class="text-end"><g:formatNumber number="${l.medical}" format="#,##0"/></td>
                    <td class="text-end"><g:formatNumber number="${l.conveyance}" format="#,##0"/></td>
                    <td class="text-end"><g:formatNumber number="${l.otherAllowance}" format="#,##0"/></td>
                    <td class="text-end"><g:formatNumber number="${l.overtimeAmount}" format="#,##0"/><g:if test="${l.overtimeHours > 0}"><div class="text-muted">${l.overtimeHours} ঘণ্টা</div></g:if></td>
                    <td class="text-end fw-semibold"><g:formatNumber number="${l.grossEarnings}" format="#,##0"/></td>
                    <td class="text-end text-danger"><g:formatNumber number="${l.absentDeduction + l.lateDeduction}" format="#,##0"/></td>
                    <td class="text-end text-danger"><g:formatNumber number="${l.otherDeduction}" format="#,##0"/></td>
                    <td class="text-end"><hrms:signedMoney value="${l.adjustment}"/></td>
                    <td class="text-end fw-bold"><g:formatNumber number="${l.netPay}" format="#,##0"/></td>
                    <td class="text-end"><g:link action="line" id="${l.id}" class="btn btn-sm btn-outline-secondary" title="বিস্তারিত"><i class="bi bi-eye"></i></g:link></td>
                </tr>
            </g:each>
            <g:if test="${!lines}">
                <tr><td colspan="14" class="text-center text-muted py-4">কোনো কর্মী নেই।</td></tr>
            </g:if>
            </tbody>
            <g:if test="${lines}">
                <tfoot>
                <tr>
                    <td colspan="2">মোট</td>
                    <td class="text-end"><g:formatNumber number="${totals.basic}" format="#,##0"/></td>
                    <td class="text-end"><g:formatNumber number="${totals.houseRent}" format="#,##0"/></td>
                    <td class="text-end"><g:formatNumber number="${totals.medical}" format="#,##0"/></td>
                    <td class="text-end"><g:formatNumber number="${totals.conveyance}" format="#,##0"/></td>
                    <td class="text-end"><g:formatNumber number="${totals.allowance}" format="#,##0"/></td>
                    <td class="text-end"><g:formatNumber number="${totals.overtime}" format="#,##0"/></td>
                    <td class="text-end"><g:formatNumber number="${run.totalGross}" format="#,##0"/></td>
                    <td colspan="2" class="text-end text-danger"><g:formatNumber number="${run.totalDeduction}" format="#,##0"/></td>
                    <td class="text-end"><hrms:signedMoney value="${totals.adjustment}"/></td>
                    <td class="text-end"><g:formatNumber number="${run.totalNet}" format="#,##0"/></td>
                    <td></td>
                </tr>
                </tfoot>
            </g:if>
        </table>
    </div>
</div>
</body>
</html>

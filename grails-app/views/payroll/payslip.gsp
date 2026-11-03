<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>পে-স্লিপ - ${line.employeeName}</title>
    <style>
        .payslip { max-width: 820px; margin: 0 auto; background: #fff; border: 1px solid #dee2e6; border-radius: .5rem; padding: 2rem; }
        .payslip h2 { font-size: 1.4rem; margin-bottom: .25rem; }
        .payslip .ps-head { border-bottom: 2px solid #212529; padding-bottom: .75rem; margin-bottom: 1rem; }
        .payslip table td { padding: .3rem .5rem; }
        .payslip .ps-total td { border-top: 1px solid #212529; font-weight: 600; }
        .payslip .ps-net { border: 2px solid #212529; border-radius: .4rem; padding: .75rem 1rem; margin-top: 1rem; }
        @media print {
            .hrms-topbar, .hrms-sidebar, .hrms-footer, .hrms-subnav, .toast-container, .no-print { display: none !important; }
            .hrms-shell, .hrms-main { display: block !important; margin: 0 !important; padding: 0 !important; }
            .payslip { border: 0; padding: 0; max-width: 100%; }
            body { background: #fff !important; }
        }
    </style>
</head>
<body>
<div class="d-flex justify-content-between align-items-center mb-3 no-print">
    <g:if test="${self}">
        <g:link controller="payslip" action="index" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> ফিরে যান</g:link>
    </g:if>
    <g:else>
        <g:link controller="payroll" action="line" id="${line.id}" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> ফিরে যান</g:link>
    </g:else>
    <div class="d-flex gap-2">
        <g:if test="${self}">
            <g:link controller="payslip" action="pdf" id="${line.id}" target="_blank" class="btn btn-outline-primary"><i class="bi bi-file-earmark-pdf"></i> PDF ডাউনলোড</g:link>
        </g:if>
        <g:else>
            <g:link controller="payroll" action="payslipPdf" id="${line.id}" target="_blank" class="btn btn-outline-primary"><i class="bi bi-file-earmark-pdf"></i> PDF ডাউনলোড</g:link>
        </g:else>
        <button type="button" class="btn btn-primary" onclick="window.print()"><i class="bi bi-printer"></i> প্রিন্ট</button>
    </div>
</div>

<div class="payslip">
    <div class="ps-head d-flex justify-content-between align-items-end">
        <div>
            <h2>পে-স্লিপ</h2>
            <div class="text-muted"><hrms:monthName value="${run.payMonth}"/> ${run.payYear}</div>
        </div>
        <div class="text-end small text-muted">
            <g:if test="${run.status != 'FINALIZED'}"><span class="badge text-bg-warning text-dark">খসড়া</span></g:if>
        </div>
    </div>

    <div class="row mb-3">
        <div class="col-6">
            <div><strong>নাম:</strong> ${line.employeeName}</div>
            <div><strong>পদবি:</strong> ${line.designationTitle ?: '—'}</div>
            <div><strong>বিভাগ:</strong> ${line.departmentName ?: '—'}</div>
        </div>
        <div class="col-6">
            <div><strong>কর্মী আইডি:</strong> ${line.employee.id}</div>
            <div><strong>যোগদান:</strong> <hrms:date value="${line.employee.joinDate}"/></div>
            <div><strong>কর্মদিবস / উপস্থিত:</strong> ${line.workingDays} / ${line.presentDays}</div>
        </div>
    </div>

    <div class="row g-4">
        <div class="col-6">
            <table class="table table-sm mb-0">
                <thead><tr><th>আয়</th><th class="text-end">টাকা</th></tr></thead>
                <tbody>
                <tr><td>বেসিক</td><td class="text-end"><hrms:money value="${line.basic}"/></td></tr>
                <tr><td>বাড়ি ভাড়া</td><td class="text-end"><hrms:money value="${line.houseRent}"/></td></tr>
                <tr><td>চিকিৎসা ভাতা</td><td class="text-end"><hrms:money value="${line.medical}"/></td></tr>
                <tr><td>যাতায়াত ভাতা</td><td class="text-end"><hrms:money value="${line.conveyance}"/></td></tr>
                <g:each in="${earnings}" var="i"><tr><td>${i.headName}</td><td class="text-end"><hrms:money value="${i.amount}"/></td></tr></g:each>
                <g:if test="${line.overtimeAmount != 0}"><tr><td>ওভারটাইম (${line.overtimeHours} ঘণ্টা)</td><td class="text-end"><hrms:money value="${line.overtimeAmount}"/></td></tr></g:if>
                <tr class="ps-total"><td>মোট আয়</td><td class="text-end"><hrms:money value="${line.grossEarnings}"/></td></tr>
                </tbody>
            </table>
        </div>
        <div class="col-6">
            <table class="table table-sm mb-0">
                <thead><tr><th>কর্তন</th><th class="text-end">টাকা</th></tr></thead>
                <tbody>
                <g:if test="${line.absentDeduction != 0}"><tr><td>অনুপস্থিতি / বিনা বেতন ছুটি</td><td class="text-end"><hrms:money value="${line.absentDeduction}"/></td></tr></g:if>
                <g:if test="${line.lateDeduction != 0}"><tr><td>দেরি</td><td class="text-end"><hrms:money value="${line.lateDeduction}"/></td></tr></g:if>
                <g:each in="${deductions}" var="i"><tr><td>${i.headName}</td><td class="text-end"><hrms:money value="${i.amount}"/></td></tr></g:each>
                <g:if test="${line.adjustment != 0}"><tr><td>সমন্বয় <span class="small text-muted">${line.adjustmentNote}</span></td><td class="text-end"><hrms:signedMoney value="${line.adjustment}"/></td></tr></g:if>
                <tr class="ps-total"><td>মোট কর্তন</td><td class="text-end"><hrms:money value="${line.totalDeduction}"/></td></tr>
                </tbody>
            </table>
        </div>
    </div>

    <div class="ps-net d-flex justify-content-between align-items-center">
        <span class="fw-semibold">নেট বেতন</span>
        <span class="fs-4 fw-bold"><hrms:money value="${line.netPay}"/></span>
    </div>

    <g:if test="${line.pfEmployer > 0 || line.gratuityProvision > 0}">
        <div class="small text-muted mt-3">
            <g:if test="${line.pfEmployer > 0}">কোম্পানির PF অংশ (কাটা হয় না): <hrms:money value="${line.pfEmployer}"/>. </g:if>
            <g:if test="${line.gratuityProvision > 0}">এ মাসের গ্র্যাচুইটি প্রভিশন (কাটা হয় না): <hrms:money value="${line.gratuityProvision}"/>.</g:if>
        </div>
    </g:if>

    <div class="small text-muted mt-4">এটি কম্পিউটারে তৈরি পে-স্লিপ, স্বাক্ষরের প্রয়োজন নেই।</div>
</div>
</body>
</html>

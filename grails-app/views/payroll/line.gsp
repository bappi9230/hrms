<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>${line.employeeName} - বেতনের হিসাব</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <div>
        <h1 class="page-title mb-1">${line.employeeName}</h1>
        <span class="text-muted">${line.designationTitle ?: '—'} &middot; ${line.departmentName ?: '—'} &middot;
            <hrms:monthName value="${run.payMonth}"/> ${run.payYear}</span>
        <hrms:payrollStatus status="${run.status}"/>
    </div>
    <div class="d-flex gap-2">
        <g:link action="payslip" id="${line.id}" class="btn btn-outline-primary"><i class="bi bi-file-earmark-text"></i> পে-স্লিপ</g:link>
        <g:link action="show" id="${run.id}" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> বেতন শিট</g:link>
    </div>
</div>

<g:if test="${line.remarks}">
    <div class="alert alert-warning"><i class="bi bi-exclamation-triangle"></i> ${line.remarks}</div>
</g:if>

<div class="row g-3">
    <div class="col-lg-6">
        <div class="card mb-3">
            <div class="card-header bg-white fw-semibold">আয়</div>
            <table class="table table-sm mb-0">
                <tbody>
                <tr><td>বেসিক</td><td class="text-end"><hrms:money value="${line.basic}"/></td></tr>
                <tr><td>বাড়ি ভাড়া</td><td class="text-end"><hrms:money value="${line.houseRent}"/></td></tr>
                <tr><td>চিকিৎসা ভাতা</td><td class="text-end"><hrms:money value="${line.medical}"/></td></tr>
                <tr><td>যাতায়াত ভাতা</td><td class="text-end"><hrms:money value="${line.conveyance}"/></td></tr>
                <g:each in="${earnings}" var="i"><tr><td>${i.headName}</td><td class="text-end"><hrms:money value="${i.amount}"/></td></tr></g:each>
                <tr><td>ওভারটাইম <span class="small text-muted">(${line.overtimeHours} ঘণ্টা)</span></td><td class="text-end"><hrms:money value="${line.overtimeAmount}"/></td></tr>
                <tr class="table-light fw-semibold"><td>মোট আয়</td><td class="text-end"><hrms:money value="${line.grossEarnings}"/></td></tr>
                </tbody>
            </table>
        </div>
        <div class="card mb-3">
            <div class="card-header bg-white fw-semibold">কর্তন</div>
            <table class="table table-sm mb-0">
                <tbody>
                <tr><td>অনুপস্থিতি / বিনা বেতনের ছুটি <span class="small text-muted">(${line.absentDays + line.unpaidLeaveDays} দিন × gross÷${dayDivisor})</span></td><td class="text-end"><hrms:money value="${line.absentDeduction}"/></td></tr>
                <tr><td>দেরি <span class="small text-muted">(${line.lateDays} দিন দেরি → ${line.lateDeductionDays} দিনের বেতন)</span></td><td class="text-end"><hrms:money value="${line.lateDeduction}"/></td></tr>
                <g:each in="${deductions}" var="i"><tr><td>${i.headName}</td><td class="text-end"><hrms:money value="${i.amount}"/></td></tr></g:each>
                <tr class="table-light fw-semibold"><td>মোট কর্তন</td><td class="text-end"><hrms:money value="${line.totalDeduction}"/></td></tr>
                </tbody>
            </table>
        </div>
        <div class="card stat-card success">
            <div class="card-body d-flex justify-content-between align-items-center">
                <div>
                    <div class="small text-muted">নেট বেতন</div>
                    <div class="small text-muted">মোট আয় − কর্তন<g:if test="${line.adjustment != 0}"> ± সমন্বয় (<hrms:signedMoney value="${line.adjustment}"/>)</g:if></div>
                </div>
                <div class="stat-value"><hrms:money value="${line.netPay}"/></div>
            </div>
        </div>
    </div>

    <div class="col-lg-6">
        <div class="card mb-3">
            <div class="card-header bg-white fw-semibold">উপস্থিতির সারসংক্ষেপ</div>
            <div class="card-body">
                <div class="row text-center g-2">
                    <div class="col-4"><div class="fs-4 fw-bold">${line.workingDays}</div><div class="small text-muted">কর্মদিবস</div></div>
                    <div class="col-4"><div class="fs-4 fw-bold text-success">${line.presentDays}</div><div class="small text-muted">উপস্থিত</div></div>
                    <div class="col-4"><div class="fs-4 fw-bold text-warning">${line.lateDays}</div><div class="small text-muted">দেরি</div></div>
                    <div class="col-4"><div class="fs-4 fw-bold">${line.paidLeaveDays}</div><div class="small text-muted">বেতনসহ ছুটি</div></div>
                    <div class="col-4"><div class="fs-4 fw-bold">${line.unpaidLeaveDays}</div><div class="small text-muted">বিনা বেতন ছুটি</div></div>
                    <div class="col-4"><div class="fs-4 fw-bold text-danger">${line.absentDays}</div><div class="small text-muted">অনুপস্থিত</div></div>
                </div>
                <div class="small text-muted mt-3">কাঠামোর পূর্ণ মাসের gross: <hrms:money value="${line.structureGross}"/></div>
            </div>
        </div>

        <g:if test="${run.status == 'DRAFT'}">
            <div class="card">
                <div class="card-header bg-white fw-semibold">ওভারটাইম ও সমন্বয় (হাতে)</div>
                <div class="card-body">
                    <g:form action="updateLine" id="${line.id}" method="POST">
                        <div class="mb-3">
                            <label class="form-label" for="overtimeHours">OT ঘণ্টা (খালি রাখলে অনুমোদিত OT থেকে)</label>
                            <input type="number" step="0.5" min="0" max="300" name="overtimeHours" id="overtimeHours" class="form-control"
                                   value="${line.overtimeHoursManual}" placeholder="স্বয়ংক্রিয়: ${line.overtimeHoursManual == null ? line.overtimeHours : '—'}">
                        </div>
                        <div class="mb-3">
                            <label class="form-label" for="adjustment">সমন্বয় (৳, + যোগ / − বিয়োগ)</label>
                            <input type="number" step="1" name="adjustment" id="adjustment" class="form-control" value="${line.adjustment != 0 ? line.adjustment : ''}" placeholder="0">
                        </div>
                        <div class="mb-3">
                            <label class="form-label" for="adjustmentNote">সমন্বয়ের কারণ <span class="text-muted small">(সমন্বয় দিলে বাধ্যতামূলক)</span></label>
                            <input type="text" name="adjustmentNote" id="adjustmentNote" maxlength="255" class="form-control" value="${line.adjustmentNote}">
                        </div>
                        <button type="submit" class="btn btn-primary"><i class="bi bi-check-lg"></i> সংরক্ষণ ও আবার হিসাব</button>
                    </g:form>
                </div>
            </div>
        </g:if>
        <g:elseif test="${line.adjustment != 0 || line.overtimeHoursManual != null}">
            <div class="card"><div class="card-body small">
                <g:if test="${line.overtimeHoursManual != null}">OT ঘণ্টা হাতে দেওয়া: ${line.overtimeHoursManual}<br></g:if>
                <g:if test="${line.adjustment != 0}">সমন্বয়: <hrms:signedMoney value="${line.adjustment}"/> — ${line.adjustmentNote}</g:if>
            </div></div>
        </g:elseif>
    </div>
</div>
</body>
</html>

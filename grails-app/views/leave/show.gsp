<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>ছুটির আবেদন</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="page-title mb-0">ছুটির আবেদন <hrms:leaveStatus status="${leave.status}"/></h1>
    <g:link action="index" class="btn btn-outline-secondary btn-sm"><i class="bi bi-arrow-left"></i> ফিরে যান</g:link>
</div>

<div class="row g-3">
    <div class="col-lg-7">
        <div class="card mb-3">
            <div class="card-body">
                <dl class="row mb-0">
                    <dt class="col-sm-4">কর্মী</dt><dd class="col-sm-8">${leave.employee.name} <span class="text-muted small">(${leave.employee.department})</span></dd>
                    <dt class="col-sm-4">ছুটির ধরন</dt><dd class="col-sm-8">${leave.leaveType.name}</dd>
                    <dt class="col-sm-4">সময়কাল</dt><dd class="col-sm-8"><hrms:date value="${leave.fromDate}"/> – <hrms:date value="${leave.toDate}"/> (${leave.days} কর্মদিবস)</dd>
                    <dt class="col-sm-4">কারণ</dt><dd class="col-sm-8">${leave.reason}</dd>
                    <dt class="col-sm-4">আবেদনের সময়</dt><dd class="col-sm-8">${leave.appliedAt?.toString()?.replace('T', ' ')?.take(16)}</dd>
                    <dt class="col-sm-4">অনুমোদনের ধাপ</dt>
                    <dd class="col-sm-8">
                        <g:each in="${leave.stageList()}" var="s" status="i">
                            <span class="badge ${leave.status == 'APPROVED' || (leave.status == 'PENDING' && i + 1 < leave.currentLevel) ? 'text-bg-success' : (leave.status == 'PENDING' && i + 1 == leave.currentLevel ? 'text-bg-warning text-dark' : 'text-bg-light border')}">
                                ${i + 1}. <hrms:stageName stage="${s}"/></span>
                        </g:each>
                    </dd>
                </dl>
            </div>
        </div>

        <g:if test="${canDecide}">
            <div class="card border-primary mb-3">
                <div class="card-header bg-white fw-semibold">আপনার সিদ্ধান্ত (<hrms:stageName stage="${leave.currentStage()}"/> ধাপ)</div>
                <div class="card-body">
                    <g:form action="approve" id="${leave.id}" method="POST" name="decideForm">
                        <g:hiddenField name="back" value="show"/>
                        <div class="mb-3">
                            <label class="form-label" for="remarks">মন্তব্য <span class="text-muted small">(বাতিলের সময় বাধ্যতামূলক)</span></label>
                            <textarea name="remarks" id="remarks" rows="2" maxlength="500" class="form-control"></textarea>
                        </div>
                        <button type="submit" class="btn btn-success"><i class="bi bi-check-lg"></i> অনুমোদন</button>
                        <button type="submit" class="btn btn-danger" formaction="${createLink(action: 'reject', id: leave.id)}"><i class="bi bi-x-lg"></i> বাতিল</button>
                    </g:form>
                </div>
            </div>
        </g:if>

        <g:if test="${canCancel}">
            <g:form action="cancel" id="${leave.id}" method="POST" onsubmit="return confirm('এই আবেদন বাতিল/প্রত্যাহার করবেন?');">
                <g:hiddenField name="back" value="show"/>
                <button type="submit" class="btn btn-outline-danger"><i class="bi bi-slash-circle"></i> আবেদন প্রত্যাহার</button>
            </g:form>
        </g:if>
    </div>

    <div class="col-lg-5">
        <div class="card mb-3">
            <div class="card-header bg-white fw-semibold">অনুমোদনের ইতিহাস</div>
            <ul class="list-group list-group-flush">
                <g:each in="${leave.approvals}" var="a">
                    <li class="list-group-item">
                        <div class="d-flex justify-content-between">
                            <span><strong>${a.actedByName ?: a.actedBy}</strong> <span class="text-muted small">(<hrms:stageName stage="${a.stage}"/>)</span></span>
                            <span class="badge ${a.decision == 'APPROVED' ? 'text-bg-success' : 'text-bg-danger'}">${a.decision == 'APPROVED' ? 'অনুমোদন' : 'বাতিল'}</span>
                        </div>
                        <g:if test="${a.remarks}"><div class="small mt-1">${a.remarks}</div></g:if>
                        <div class="small text-muted">${a.actedAt?.toString()?.replace('T', ' ')?.take(16)}</div>
                    </li>
                </g:each>
                <g:if test="${!leave.approvals}"><li class="list-group-item text-muted">এখনো কোনো সিদ্ধান্ত হয়নি।</li></g:if>
                <g:if test="${leave.status == 'CANCELLED'}"><li class="list-group-item text-muted small">প্রত্যাহার করেছেন: ${leave.cancelledBy}</li></g:if>
            </ul>
        </div>
        <div class="card">
            <div class="card-header bg-white fw-semibold">${leave.leaveType.name} — ${leave.fromDate.year} সালের ব্যালেন্স</div>
            <div class="card-body small">
                <g:if test="${balance.limited}">
                    অর্জিত <g:formatNumber number="${balance.accrued}" maxFractionDigits="1"/>
                    + আগের বছরের <g:formatNumber number="${balance.carried}" maxFractionDigits="1"/>
                    &minus; ব্যবহৃত <g:formatNumber number="${balance.used}" maxFractionDigits="1"/>
                    = বাকি <strong><g:formatNumber number="${balance.remaining}" maxFractionDigits="1"/></strong> দিন
                    (অপেক্ষমাণ <g:formatNumber number="${balance.pending}" maxFractionDigits="1"/>)
                </g:if>
                <g:else>সীমাহীন ছুটি; ব্যবহৃত ${balance.used} দিন।</g:else>
            </div>
        </div>
    </div>
</div>
</body>
</html>

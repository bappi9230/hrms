<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>আমার ছুটি</title>
</head>
<body>
<h1 class="page-title">ছুটি</h1>
<g:render template="tabs" model="[active: 'index']"/>

<g:if test="${!me}">
    <div class="alert alert-warning">এই অ্যাকাউন্টের সাথে কোনো কর্মীর প্রোফাইল যুক্ত নেই, তাই নিজের ছুটির তথ্য নেই।
        <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_HR">উপরের ট্যাব থেকে সবার আবেদন ও ব্যালেন্স দেখুন।</sec:ifAnyGranted></div>
</g:if>
<g:else>
    <div class="d-flex justify-content-between align-items-center mb-2">
        <h5 class="mb-0">ছুটির ব্যালেন্স — ${year}</h5>
        <div class="btn-group btn-group-sm">
            <g:link action="index" params="[year: year - 1]" class="btn btn-outline-secondary"><i class="bi bi-chevron-left"></i> ${year - 1}</g:link>
            <g:link action="index" params="[year: year + 1]" class="btn btn-outline-secondary">${year + 1} <i class="bi bi-chevron-right"></i></g:link>
        </div>
    </div>

    <div class="row g-3 mb-4">
        <g:each in="${balances}" var="b">
            <div class="col-sm-6 col-xl-3">
                <div class="card h-100 stat-card ${b.limited ? (b.available <= 0 ? 'danger' : 'success') : ''}">
                    <div class="card-body">
                        <div class="d-flex justify-content-between">
                            <div class="fw-semibold">${b.type.name}</div>
                            <span class="badge text-bg-light border">${b.type.code}</span>
                        </div>
                        <g:if test="${b.limited}">
                            <div class="stat-value"><g:formatNumber number="${b.remaining}" maxFractionDigits="1"/> <span class="fs-6 text-muted">দিন বাকি</span></div>
                            <div class="progress my-2" style="height:6px;">
                                <div class="progress-bar" style="width:${(b.accrued + b.carried) > 0 ? Math.min(100, (b.used * 100 / (b.accrued + b.carried)) as int) : 0}%"></div>
                            </div>
                            <div class="small text-muted">
                                অর্জিত <g:formatNumber number="${b.accrued}" maxFractionDigits="1"/>
                                <g:if test="${b.carried > 0}">+ আগের বছরের <g:formatNumber number="${b.carried}" maxFractionDigits="1"/></g:if>
                                &middot; ব্যবহৃত <g:formatNumber number="${b.used}" maxFractionDigits="1"/>
                                <g:if test="${b.pending > 0}">&middot; অপেক্ষমাণ <g:formatNumber number="${b.pending}" maxFractionDigits="1"/></g:if>
                            </div>
                        </g:if>
                        <g:else>
                            <div class="stat-value">∞ <span class="fs-6 text-muted">সীমাহীন</span></div>
                            <div class="small text-muted">ব্যবহৃত ${b.used} দিন<g:if test="${!b.type.paid}"> &middot; বিনা বেতন</g:if></div>
                        </g:else>
                    </div>
                </div>
            </div>
        </g:each>
        <g:if test="${!balances}"><div class="col-12 text-muted">কোনো ছুটির ধরন নির্ধারণ করা নেই।</div></g:if>
    </div>

    <div class="card">
        <div class="card-header bg-white d-flex justify-content-between align-items-center">
            <span class="fw-semibold">আমার আবেদনসমূহ</span>
            <g:link action="apply" class="btn btn-sm btn-primary"><i class="bi bi-plus-lg"></i> নতুন আবেদন</g:link>
        </div>
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                <tr><th>ধরন</th><th>শুরু</th><th>শেষ</th><th>দিন</th><th>অবস্থা</th><th>অপেক্ষায়</th><th style="width:110px;"></th></tr>
                </thead>
                <tbody>
                <g:each in="${requests}" var="r">
                    <tr>
                        <td>${r.leaveType.name}</td>
                        <td><hrms:date value="${r.fromDate}"/></td>
                        <td><hrms:date value="${r.toDate}"/></td>
                        <td>${r.days}</td>
                        <td><hrms:leaveStatus status="${r.status}"/></td>
                        <td class="small text-muted">
                            <g:if test="${r.status == 'PENDING'}"><hrms:stageName stage="${r.currentStage()}"/> (${r.currentLevel}/${r.stageList().size()})</g:if>
                            <g:else>—</g:else>
                        </td>
                        <td class="text-end">
                            <g:link action="show" id="${r.id}" class="btn btn-sm btn-outline-secondary" title="বিস্তারিত"><i class="bi bi-eye"></i></g:link>
                        </td>
                    </tr>
                </g:each>
                <g:if test="${!requests}"><tr><td colspan="7" class="text-center text-muted py-4">এখনো কোনো আবেদন নেই।</td></tr></g:if>
                </tbody>
            </table>
        </div>
    </div>
</g:else>
</body>
</html>

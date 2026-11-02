<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>উপস্থিতি</title>
</head>
<body>
<h1 class="page-title">উপস্থিতি</h1>

<g:if test="${!me}">
    <div class="alert alert-warning">এই অ্যাকাউন্টের সাথে কোনো কর্মীর প্রোফাইল যুক্ত নেই, তাই নিজের চেক-ইন করা যাবে না।</div>
</g:if>
<g:else>
    <div class="row g-3 mb-3">
        <div class="col-lg-5">
            <div class="card h-100">
                <div class="card-body">
                    <h5 class="card-title mb-1">আজ — <hrms:date value="${today}"/></h5>
                    <p class="text-muted small mb-3">
                        <g:if test="${shift}">শিফট: ${shift.name} (${shift.startTime} – ${shift.endTime}), গ্রেস ${shift.graceMinutes} মিনিট</g:if>
                        <g:else>কোনো শিফট নির্ধারণ করা নেই।</g:else>
                    </p>

                    <dl class="row mb-3">
                        <dt class="col-5">চেক-ইন</dt>
                        <dd class="col-7">${todayRecord?.checkIn ?: '—'}</dd>
                        <dt class="col-5">চেক-আউট</dt>
                        <dd class="col-7">${todayRecord?.checkOut ?: '—'}</dd>
                        <dt class="col-5">অবস্থা</dt>
                        <dd class="col-7">
                            <g:if test="${todayRecord}">
                                <span class="badge ${todayRecord.status == 'LATE' ? 'text-bg-warning' : 'text-bg-success'}">${todayRecord.status}</span>
                                <g:if test="${todayRecord.lateMinutes}"><span class="small text-muted">(${todayRecord.lateMinutes} মিনিট দেরি)</span></g:if>
                                <g:if test="${todayRecord.earlyOut}"><span class="badge text-bg-secondary">সময়ের আগে বের</span></g:if>
                            </g:if>
                            <g:else>—</g:else>
                        </dd>
                    </dl>

                    <div class="d-flex gap-2">
                        <g:form action="checkIn" method="POST">
                            <button type="submit" class="btn btn-success" ${todayRecord?.checkIn ? 'disabled' : ''}>
                                <i class="bi bi-box-arrow-in-right"></i> চেক-ইন
                            </button>
                        </g:form>
                        <g:form action="checkOut" method="POST">
                            <button type="submit" class="btn btn-outline-danger" ${(!todayRecord?.checkIn || todayRecord?.checkOut) ? 'disabled' : ''}>
                                <i class="bi bi-box-arrow-right"></i> চেক-আউট
                            </button>
                        </g:form>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-lg-7">
            <div class="card h-100">
                <div class="card-body">
                    <h5 class="card-title">এই মাসের সারসংক্ষেপ (${month})</h5>
                    <div class="row text-center g-2">
                        <div class="col-3"><div class="fs-3 fw-semibold">${summary.total}</div><div class="small text-muted">মোট দিন</div></div>
                        <div class="col-3"><div class="fs-3 fw-semibold text-success">${summary.present}</div><div class="small text-muted">উপস্থিত</div></div>
                        <div class="col-3"><div class="fs-3 fw-semibold text-warning">${summary.late}</div><div class="small text-muted">দেরি</div></div>
                        <div class="col-3"><div class="fs-3 fw-semibold text-danger">${summary.absent}</div><div class="small text-muted">অনুপস্থিত</div></div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="card mb-4">
        <div class="card-header">আমার এই মাসের উপস্থিতি</div>
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                <tr><th>তারিখ</th><th>চেক-ইন</th><th>চেক-আউট</th><th>অবস্থা</th><th>দেরি (মিনিট)</th><th>আর্লি-আউট</th></tr>
                </thead>
                <tbody>
                <g:each in="${records}" var="r">
                    <tr>
                        <td><hrms:date value="${r.attDate}"/></td>
                        <td>${r.checkIn ?: '—'}</td>
                        <td>${r.checkOut ?: '—'}</td>
                        <td><span class="badge ${r.status == 'LATE' ? 'text-bg-warning' : (r.status == 'ABSENT' ? 'text-bg-danger' : 'text-bg-success')}">${r.status}</span></td>
                        <td>${r.lateMinutes ?: '—'}</td>
                        <td>${r.earlyOut ? 'হ্যাঁ' : '—'}</td>
                    </tr>
                </g:each>
                <g:if test="${!records}">
                    <tr><td colspan="6" class="text-center text-muted py-4">এই মাসে কোনো রেকর্ড নেই।</td></tr>
                </g:if>
                </tbody>
            </table>
        </div>
    </div>
</g:else>

<sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_HR,ROLE_MANAGER">
    <div class="card">
        <div class="card-header">আজকের উপস্থিতি (টিম)</div>
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                <tr><th>কর্মী</th><th>চেক-ইন</th><th>চেক-আউট</th><th>অবস্থা</th><th>দেরি (মিনিট)</th></tr>
                </thead>
                <tbody>
                <g:each in="${teamToday}" var="r">
                    <tr>
                        <td>${r.employee.name}</td>
                        <td>${r.checkIn ?: '—'}</td>
                        <td>${r.checkOut ?: '—'}</td>
                        <td><span class="badge ${r.status == 'LATE' ? 'text-bg-warning' : (r.status == 'ABSENT' ? 'text-bg-danger' : 'text-bg-success')}">${r.status}</span></td>
                        <td>${r.lateMinutes ?: '—'}</td>
                    </tr>
                </g:each>
                <g:if test="${!teamToday}">
                    <tr><td colspan="5" class="text-center text-muted py-4">আজ এখনো কেউ চেক-ইন করেনি।</td></tr>
                </g:if>
                </tbody>
            </table>
        </div>
    </div>
</sec:ifAnyGranted>
</body>
</html>

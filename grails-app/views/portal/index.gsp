<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>আমার পোর্টাল</title>
</head>
<body>
<div class="mb-3">
    <h1 class="page-title mb-1">স্বাগতম, <sec:username/></h1>
    <div class="text-muted small"><hrms:date value="${today}"/><g:if test="${me}"> · ${me.designation} · ${me.department}</g:if></div>
</div>

<g:if test="${!me}">
    <div class="alert alert-info">আপনার লগইনের সাথে কোনো কর্মী প্রোফাইল যুক্ত নেই, তাই ব্যক্তিগত তথ্য দেখানো হচ্ছে না।</div>
</g:if>

<%-- জরুরি: আমার করণীয় --%>
<g:if test="${caseWaiting}">
    <g:each in="${caseWaiting}" var="cw">
        <div class="alert alert-warning d-flex justify-content-between align-items-center">
            <span><i class="bi bi-shield-exclamation"></i> একটি শৃঙ্খলা নোটিসে আপনার জবাব চাওয়া হয়েছে: <strong>${cw.subject}</strong></span>
            <g:link controller="discipline" action="show" id="${cw.id}" class="btn btn-sm btn-warning">জবাব দিন</g:link>
        </div>
    </g:each>
</g:if>
<g:if test="${mySeparation}">
    <div class="alert alert-secondary d-flex justify-content-between align-items-center">
        <span><i class="bi bi-box-arrow-right"></i> আপনার চাকরি-ছাড়ার কেস চালু আছে: <hrms:d9Badge kind="separation" status="${mySeparation.status}"/></span>
        <g:link controller="offboarding" action="show" id="${mySeparation.id}" class="btn btn-sm btn-outline-secondary">দেখুন</g:link>
    </div>
</g:if>

<div class="row g-3 mb-3">
    <%-- আজকের উপস্থিতি --%>
    <g:if test="${me}">
        <div class="col-md-6 col-xl-3">
            <div class="card h-100">
                <div class="card-body">
                    <div class="text-muted small">আজকের উপস্থিতি</div>
                    <g:if test="${todayRecord}">
                        <div class="fs-5 fw-semibold">${todayRecord.status == 'LATE' ? 'দেরিতে উপস্থিত' : (todayRecord.status == 'PRESENT' ? 'উপস্থিত' : todayRecord.status)}</div>
                        <div class="small text-muted">${todayRecord.checkIn ?: '—'} - ${todayRecord.checkOut ?: '—'}</div>
                    </g:if>
                    <g:else><div class="fs-5 fw-semibold text-muted">এখনো চেক-ইন হয়নি</div></g:else>
                    <g:link controller="attendance" action="index" class="small">উপস্থিতি পাতা &raquo;</g:link>
                    <g:if test="${summary}"><div class="small text-muted mt-2">এই মাসে: উপস্থিত ${summary.present}, দেরি ${summary.late}, অনুপস্থিত ${summary.absent}</div></g:if>
                </div>
            </div>
        </div>
        <div class="col-md-6 col-xl-3">
            <div class="card h-100">
                <div class="card-body">
                    <div class="text-muted small">সর্বশেষ পে-স্লিপ</div>
                    <g:if test="${payslip}">
                        <div class="fs-5 fw-semibold"><hrms:monthName value="${payslip.payrollRun.payMonth}"/> ${payslip.payrollRun.payYear}</div>
                        <div class="small text-muted">নিট: <hrms:money value="${payslip.netPay}"/></div>
                        <g:link controller="payslip" action="show" id="${payslip.id}" class="small">দেখুন &raquo;</g:link>
                    </g:if>
                    <g:else><div class="text-muted">এখনো কোনো পে-স্লিপ নেই।</div></g:else>
                </div>
            </div>
        </div>
        <div class="col-md-6 col-xl-3">
            <div class="card h-100">
                <div class="card-body">
                    <div class="text-muted small">আমার অপেক্ষমাণ আবেদন</div>
                    <ul class="list-unstyled mb-0 small mt-1">
                        <li><g:link controller="leave" action="index">ছুটি: ${myPendingLeave}</g:link></li>
                        <li><g:link controller="expense" action="index">খরচের দাবি: ${myClaims}</g:link></li>
                        <li><g:link controller="performance" action="index">চালু লক্ষ্য: ${myGoals}</g:link></li>
                        <li><g:link controller="training" action="my">প্রশিক্ষণ: ${myCourses}</g:link></li>
                        <li><g:link controller="asset" action="mine">আমার সম্পদ: ${myAssets}</g:link></li>
                    </ul>
                </div>
            </div>
        </div>
        <div class="col-md-6 col-xl-3">
            <div class="card h-100">
                <div class="card-body">
                    <div class="text-muted small mb-2">দ্রুত কাজ</div>
                    <div class="d-grid gap-2">
                        <g:link controller="leave" action="apply" class="btn btn-sm btn-outline-primary"><i class="bi bi-airplane"></i> ছুটির আবেদন</g:link>
                        <g:link controller="expense" action="create" class="btn btn-sm btn-outline-primary"><i class="bi bi-receipt-cutoff"></i> খরচের দাবি</g:link>
                        <g:link controller="grievance" action="create" class="btn btn-sm btn-outline-secondary"><i class="bi bi-megaphone"></i> অভিযোগ জানান</g:link>
                    </div>
                </div>
            </div>
        </div>
    </g:if>
</div>

<%-- ছুটির ব্যালেন্স --%>
<g:if test="${balances}">
    <div class="card mb-3">
        <div class="card-header bg-white fw-semibold">আমার ছুটির ব্যালেন্স (${today.year})</div>
        <div class="card-body">
            <div class="row g-3">
                <g:each in="${balances}" var="b">
                    <div class="col-6 col-md-3">
                        <div class="border rounded p-2 text-center">
                            <div class="small text-muted">${b.type.name}</div>
                            <div class="fs-4 fw-semibold"><g:formatNumber number="${b.available}" maxFractionDigits="1"/></div>
                            <div class="small text-muted">ব্যবহৃত ${b.used} · অপেক্ষমাণ ${b.pending}</div>
                        </div>
                    </div>
                </g:each>
            </div>
        </div>
    </div>
</g:if>

<div class="row g-3">
    <%-- ম্যানেজারের টিম + অনুমোদন --%>
    <div class="col-lg-6">
        <g:if test="${teamSize != null}">
            <div class="card mb-3">
                <div class="card-header bg-white fw-semibold">আমার টিম</div>
                <div class="card-body">
                    <div class="row text-center">
                        <div class="col"><div class="fs-3 fw-semibold">${teamSize}</div><div class="small text-muted">সদস্য</div></div>
                        <div class="col"><div class="fs-3 fw-semibold text-success">${teamPresent}</div><div class="small text-muted">আজ উপস্থিত</div></div>
                        <div class="col"><div class="fs-3 fw-semibold text-warning">${teamOnLeave}</div><div class="small text-muted">ছুটিতে</div></div>
                    </div>
                </div>
            </div>
        </g:if>
        <div class="card">
            <div class="card-header bg-white fw-semibold d-flex justify-content-between">
                <span>আমার সিদ্ধান্তের অপেক্ষায় <g:if test="${approvalTotal}"><span class="badge text-bg-danger">${approvalTotal}</span></g:if></span>
                <g:link controller="inbox" action="index" class="small">সব দেখুন &raquo;</g:link>
            </div>
            <div class="list-group list-group-flush">
                <g:each in="${approvals}" var="it2">
                    <g:link controller="${it2.controller}" action="${it2.action}" id="${it2.id}" params="${it2.params ?: [:]}" class="list-group-item list-group-item-action">
                        <i class="bi ${it2.icon} me-1"></i> ${it2.title} <span class="small text-muted">${it2.detail}</span>
                    </g:link>
                </g:each>
                <g:if test="${!approvals}"><div class="list-group-item text-muted small">কোনো অপেক্ষমাণ কাজ নেই।</div></g:if>
            </div>
        </div>
    </div>

    <%-- ঘোষণা --%>
    <div class="col-lg-6">
        <div class="card">
            <div class="card-header bg-white fw-semibold d-flex justify-content-between">
                <span>কোম্পানির নোটিশ</span>
                <g:link controller="announcement" action="index" class="small">সব &raquo;</g:link>
            </div>
            <div class="list-group list-group-flush">
                <g:each in="${announcements}" var="a">
                    <div class="list-group-item">
                        <div><g:if test="${a.pinned}"><i class="bi bi-pin-angle-fill text-warning"></i></g:if> <strong>${a.title}</strong> <hrms:d9Badge kind="announcement" status="${a.category}"/></div>
                        <div class="small text-muted">${a.body.take(140)}${a.body.size() > 140 ? '…' : ''}</div>
                        <div class="small text-muted"><hrms:date value="${a.publishDate}"/></div>
                    </div>
                </g:each>
                <g:if test="${!announcements}"><div class="list-group-item text-muted small">এখন কোনো নোটিশ নেই।</div></g:if>
            </div>
        </div>
    </div>
</div>
</body>
</html>

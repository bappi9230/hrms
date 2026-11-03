<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>PF ও গ্র্যাচুইটি</title>
</head>
<body>
<div class="d-flex justify-content-between align-items-center mb-3">
    <h1 class="page-title mb-0">PF ও গ্র্যাচুইটি</h1>
    <g:link action="index" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> পে-রোল</g:link>
</div>

<div class="card mb-4">
    <div class="card-header bg-white fw-semibold">প্রভিডেন্ট ফান্ড জমা (চূড়ান্ত পে-রোল থেকে)</div>
    <div class="small text-muted px-3 pt-2">কর্মী ${pfEmployeePct}% + কোম্পানি ${pfEmployerPct}% (বেসিকের)। শুধু FULL_TIME কর্মী।</div>
    <div class="table-responsive">
        <table class="table table-sm align-middle mb-0">
            <thead class="table-light"><tr><th>কর্মী</th><th class="text-end">মাস</th><th class="text-end">কর্মীর অংশ</th><th class="text-end">কোম্পানির অংশ</th><th class="text-end">মোট জমা</th></tr></thead>
            <tbody>
            <g:each in="${pf}" var="r">
                <tr><td>${r.name}</td><td class="text-end">${r.months}</td><td class="text-end"><hrms:money value="${r.employee}"/></td>
                    <td class="text-end"><hrms:money value="${r.employer}"/></td><td class="text-end fw-semibold"><hrms:money value="${r.total}"/></td></tr>
            </g:each>
            <g:if test="${!pf}"><tr><td colspan="5" class="text-center text-muted py-3">এখনো কোনো চূড়ান্ত পে-রোলে PF জমা হয়নি।</td></tr></g:if>
            </tbody>
        </table>
    </div>
</div>

<div class="card">
    <div class="card-header bg-white fw-semibold">গ্র্যাচুইটি অবস্থা (আজকের তারিখে)</div>
    <div class="small text-muted px-3 pt-2">পাওনা হয় ${eligibleYears} বছর পূর্ণ হলে: শেষ বেসিক × পূর্ণ বছর (বছরে ৩০ দিনের মজুরি)। ১ বছর পূর্ণ FULL_TIME কর্মীর জন্য মাসে বেসিক ÷ ১২ প্রভিশন ধরা হয়।</div>
    <div class="table-responsive">
        <table class="table table-sm align-middle mb-0">
            <thead class="table-light"><tr><th>কর্মী</th><th>চাকরির বয়স</th><th class="text-end">বর্তমান বেসিক</th><th>অবস্থা</th><th class="text-end">পাওনা (এখন)</th><th class="text-end">প্রভিশন (হিসাব)</th></tr></thead>
            <tbody>
            <g:each in="${gratuity}" var="r">
                <tr>
                    <td>${r.employee.name}</td>
                    <td>${r.years} বছর ${r.months} মাস</td>
                    <td class="text-end"><hrms:money value="${r.basic}"/></td>
                    <td>
                        <g:if test="${r.eligible}"><span class="badge text-bg-success">যোগ্য</span></g:if>
                        <g:else><span class="badge text-bg-secondary">${eligibleYears} বছর হয়নি</span></g:else>
                        <g:if test="${!r.fullTime}"><span class="badge text-bg-light border">FULL_TIME নয়</span></g:if>
                    </td>
                    <td class="text-end"><g:if test="${r.eligible}"><hrms:money value="${r.payable}"/></g:if><g:else>—</g:else></td>
                    <td class="text-end"><g:if test="${r.provisionSoFar > 0}"><hrms:money value="${r.provisionSoFar}"/></g:if><g:else>—</g:else></td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>

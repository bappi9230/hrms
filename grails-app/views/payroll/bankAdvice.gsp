<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>ব্যাংক / ওয়ালেট শিট ${run}</title>
    <style>
        .advice { background: #fff; border: 1px solid #dee2e6; border-radius: .5rem; padding: 1.5rem; }
        .advice table td, .advice table th { font-size: .85rem; }
        @media print {
            .hrms-topbar, .hrms-sidebar, .hrms-footer, .hrms-subnav, .toast-container, .no-print { display: none !important; }
            .hrms-shell, .hrms-main { display: block !important; margin: 0 !important; padding: 0 !important; }
            .advice { border: 0; padding: 0; }
            body { background: #fff !important; }
        }
    </style>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2 no-print">
    <div>
        <h1 class="page-title mb-1">পেমেন্ট শিট — <hrms:monthName value="${run.payMonth}"/> ${run.payYear}</h1>
        <span class="small text-muted">চূড়ান্ত পে-রোল থেকে; নেট বেতন > ০ এমন কর্মী</span>
    </div>
    <div class="d-flex flex-wrap gap-2">
        <g:link action="show" id="${run.id}" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> বেতন শিট</g:link>
        <g:link action="bankCsv" id="${run.id}" class="btn btn-outline-primary ${sheet.bank ? '' : 'disabled'}"><i class="bi bi-filetype-csv"></i> ব্যাংক CSV</g:link>
        <g:link action="walletCsv" id="${run.id}" class="btn btn-outline-primary ${sheet.wallet ? '' : 'disabled'}"><i class="bi bi-filetype-csv"></i> bKash/Nagad CSV</g:link>
        <button type="button" class="btn btn-primary" onclick="window.print()"><i class="bi bi-printer"></i> প্রিন্ট</button>
    </div>
</div>

<g:if test="${sheet.missing}">
    <div class="alert alert-warning no-print">
        <i class="bi bi-exclamation-triangle"></i>
        <strong>${sheet.missing.size()} জনের</strong> পেমেন্ট তথ্য নেই বা অসম্পূর্ণ (মোট <hrms:money value="${sheet.missingTotal}"/>) - নিচে আলাদা তালিকা।
        <g:link controller="paymentInfo" action="index">পেমেন্ট তথ্য দিন</g:link> তারপর পাতাটি আবার খুলুন।
    </div>
</g:if>

<div class="advice mb-4">
    <div class="mb-3">
        <div class="fw-bold fs-5">${company}</div>
        <div class="text-muted small">তারিখ: <g:formatDate date="${new Date()}" format="dd-MM-yyyy"/></div>
    </div>
    <p class="mb-2">ব্যবস্থাপক,<br>
        <g:if test="${companyBank}">${companyBank}</g:if><g:else>[ব্যাংকের নাম]</g:else></p>
    <p class="fw-semibold mb-2">বিষয়: <hrms:monthName value="${run.payMonth}"/> ${run.payYear} মাসের বেতন পরিশোধের অনুরোধ</p>
    <p class="mb-3">জনাব, আমাদের হিসাব নং <strong><g:if test="${companyAccount}">${companyAccount}</g:if><g:else>[কোম্পানির হিসাব নং]</g:else></strong>
        থেকে মোট <strong><hrms:money value="${sheet.bankTotal}"/></strong> কেটে নিয়ে নিচের কর্মীদের হিসাবে জমা দেওয়ার জন্য অনুরোধ করা হলো।</p>

    <div class="table-responsive">
        <table class="table table-sm table-bordered align-middle mb-0">
            <thead class="table-light">
            <tr><th>#</th><th>হিসাবের নাম</th><th>হিসাব নং</th><th>ব্যাংক</th><th>শাখা</th><th>রাউটিং</th><th class="text-end">টাকা</th></tr>
            </thead>
            <tbody>
            <g:each in="${sheet.bank}" var="r" status="i">
                <tr>
                    <td>${i + 1}</td>
                    <td>${r.info.accountName ?: r.name}</td>
                    <td>${r.info.accountNo}</td>
                    <td>${r.info.bankName}</td>
                    <td>${r.info.bankBranch ?: '—'}</td>
                    <td>${r.info.routingNo ?: '—'}</td>
                    <td class="text-end"><hrms:money value="${r.amount}"/></td>
                </tr>
            </g:each>
            <g:if test="${!sheet.bank}"><tr><td colspan="7" class="text-center text-muted">ব্যাংকে পরিশোধের কেউ নেই।</td></tr></g:if>
            </tbody>
            <g:if test="${sheet.bank}">
                <tfoot><tr class="fw-bold"><td colspan="6" class="text-end">মোট</td><td class="text-end"><hrms:money value="${sheet.bankTotal}"/></td></tr></tfoot>
            </g:if>
        </table>
    </div>
    <div class="mt-5 d-flex justify-content-between small">
        <span>প্রস্তুতকারক ____________</span><span>যাচাইকারী ____________</span><span>অনুমোদনকারী ____________</span>
    </div>
</div>

<div class="card mb-3">
    <div class="card-header bg-white fw-semibold">bKash / Nagad (মোট <hrms:money value="${sheet.walletTotal}"/>)</div>
    <div class="table-responsive">
        <table class="table table-sm align-middle mb-0">
            <thead class="table-light"><tr><th>#</th><th>কর্মী</th><th>ওয়ালেট</th><th>নম্বর</th><th class="text-end">টাকা</th></tr></thead>
            <tbody>
            <g:each in="${sheet.wallet}" var="r" status="i">
                <tr><td>${i + 1}</td><td>${r.name}</td><td>${r.info.method == 'BKASH' ? 'bKash' : 'Nagad'}</td><td>${r.info.walletNo}</td><td class="text-end"><hrms:money value="${r.amount}"/></td></tr>
            </g:each>
            <g:if test="${!sheet.wallet}"><tr><td colspan="5" class="text-center text-muted">কেউ নেই।</td></tr></g:if>
            </tbody>
        </table>
    </div>
</div>

<div class="row g-3">
    <div class="col-lg-6">
        <div class="card h-100">
            <div class="card-header bg-white fw-semibold">নগদে পরিশোধ (মোট <hrms:money value="${sheet.cashTotal}"/>)</div>
            <ul class="list-group list-group-flush">
                <g:each in="${sheet.cash}" var="r"><li class="list-group-item d-flex justify-content-between">${r.name}<span><hrms:money value="${r.amount}"/></span></li></g:each>
                <g:if test="${!sheet.cash}"><li class="list-group-item text-muted">কেউ নেই।</li></g:if>
            </ul>
        </div>
    </div>
    <div class="col-lg-6">
        <div class="card h-100 border-warning">
            <div class="card-header bg-white fw-semibold text-warning-emphasis">পেমেন্ট তথ্য নেই/অসম্পূর্ণ (মোট <hrms:money value="${sheet.missingTotal}"/>)</div>
            <ul class="list-group list-group-flush">
                <g:each in="${sheet.missing}" var="r">
                    <li class="list-group-item d-flex justify-content-between">
                        <g:link controller="paymentInfo" action="edit" id="${r.line.employee.id}">${r.name}</g:link>
                        <span><hrms:money value="${r.amount}"/></span>
                    </li>
                </g:each>
                <g:if test="${!sheet.missing}"><li class="list-group-item text-muted">সবার তথ্য আছে।</li></g:if>
            </ul>
        </div>
    </div>
</div>
</body>
</html>

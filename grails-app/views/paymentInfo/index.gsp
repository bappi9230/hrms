<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>পেমেন্ট তথ্য</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="page-title mb-0">বেতন পাওয়ার মাধ্যম</h1>
    <g:link controller="payroll" action="index" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> পে-রোল</g:link>
</div>

<g:if test="${missing}">
    <div class="alert alert-warning py-2"><i class="bi bi-exclamation-triangle"></i> ${missing} জনের তথ্য নেই বা অসম্পূর্ণ - ব্যাংক/ওয়ালেট শিটে এরা "তথ্য নেই" তালিকায় যাবে।</div>
</g:if>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light"><tr><th>কর্মী</th><th>মাধ্যম</th><th>বিবরণ</th><th>অবস্থা</th><th></th></tr></thead>
            <tbody>
            <g:each in="${employees}" var="e">
                <g:set var="p" value="${infos[e.id]}"/>
                <tr>
                    <td class="fw-semibold">${e.name}</td>
                    <td>${p ? [BANK: 'ব্যাংক', BKASH: 'bKash', NAGAD: 'Nagad', CASH: 'নগদ'][p.method] : '—'}</td>
                    <td class="small">
                        <g:if test="${p?.method == 'BANK'}">${p.bankName} ${p.bankBranch ? '— ' + p.bankBranch : ''}<br>${p.accountNo}</g:if>
                        <g:elseif test="${p && (p.method == 'BKASH' || p.method == 'NAGAD')}">${p.walletNo}</g:elseif>
                    </td>
                    <td>
                        <g:if test="${p && p.complete}"><span class="badge text-bg-success">সম্পূর্ণ</span></g:if>
                        <g:else><span class="badge text-bg-warning text-dark">তথ্য নেই</span></g:else>
                    </td>
                    <td class="text-end"><g:link action="edit" id="${e.id}" class="btn btn-sm btn-outline-secondary"><i class="bi bi-pencil"></i></g:link></td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>

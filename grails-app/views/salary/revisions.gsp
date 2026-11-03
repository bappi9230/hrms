<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>ইনক্রিমেন্ট / সংশোধনের লগ</title>
</head>
<body>
<h1 class="page-title">বেতন কাঠামো</h1>
<g:render template="tabs" model="[active: 'revisions']"/>

<div class="card">
    <div class="card-header bg-white fw-semibold">সব ইনক্রিমেন্ট ও সংশোধন (সর্বশেষ ${items.size()}টি)</div>
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
            <tr><th>কার্যকর তারিখ</th><th>কর্মী</th><th>ধরন</th><th class="text-end">আগের gross</th><th class="text-end">নতুন gross</th><th class="text-end">পরিবর্তন</th><th>কারণ</th><th>করেছেন</th></tr>
            </thead>
            <tbody>
            <g:each in="${items}" var="r">
                <tr>
                    <td><hrms:date value="${r.effectiveDate}"/></td>
                    <td><g:link action="show" id="${r.employee.id}" class="text-decoration-none fw-semibold">${r.employee.name}</g:link></td>
                    <td><span class="badge ${r.revisionType == 'INCREMENT' ? 'text-bg-success' : 'text-bg-secondary'}">${r.revisionType == 'INCREMENT' ? 'ইনক্রিমেন্ট' : 'সংশোধন'}</span></td>
                    <td class="text-end"><hrms:money value="${r.oldGross}"/></td>
                    <td class="text-end fw-semibold"><hrms:money value="${r.newGross}"/></td>
                    <td class="text-end"><hrms:signedMoney value="${r.changeAmount}"/>
                        <g:if test="${r.changePercent != null}"><span class="small text-muted">(${r.changePercent}%)</span></g:if></td>
                    <td class="small">${r.remarks ?: '—'}</td>
                    <td class="small text-muted">${r.createdBy ?: '—'}</td>
                </tr>
            </g:each>
            <g:if test="${!items}"><tr><td colspan="8" class="text-center text-muted py-4">এখনো কোনো ইনক্রিমেন্ট বা সংশোধন নেই।</td></tr></g:if>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>

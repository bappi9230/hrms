<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>ঋণ ও অগ্রিম</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="page-title mb-0">ঋণ ও বেতন অগ্রিম <span class="badge text-bg-secondary fs-6">${loans.size()}</span></h1>
    <div class="d-flex gap-2">
        <g:link controller="payroll" action="index" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> পে-রোল</g:link>
        <g:link action="create" class="btn btn-primary"><i class="bi bi-plus-lg"></i> নতুন আবেদন</g:link>
    </div>
</div>

<ul class="nav nav-pills mb-3 gap-1">
    <li class="nav-item"><g:link action="index" class="nav-link ${!status ? 'active' : ''}">সব</g:link></li>
    <g:each in="${['PENDING': 'অপেক্ষমাণ', 'ACTIVE': 'চলমান', 'CLOSED': 'শোধ হয়েছে', 'REJECTED': 'প্রত্যাখ্যাত', 'CANCELLED': 'প্রত্যাহার']}" var="s">
        <li class="nav-item"><g:link action="index" params="[status: s.key]" class="nav-link ${status == s.key ? 'active' : ''}">${s.value}</g:link></li>
    </g:each>
</ul>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
            <tr><th>#</th><th>কর্মী</th><th>ধরন</th><th class="text-end">পরিমাণ</th><th class="text-end">কিস্তি</th><th>শুরু</th><th class="text-end">বাকি</th><th>অবস্থা</th><th></th></tr>
            </thead>
            <tbody>
            <g:each in="${loans}" var="l">
                <tr>
                    <td>${l.id}</td>
                    <td class="fw-semibold">${l.employee.name}</td>
                    <td>${l.loanType == 'LOAN' ? 'ঋণ' : 'অগ্রিম'}</td>
                    <td class="text-end"><hrms:money value="${l.amount}"/></td>
                    <td class="text-end">${l.installments} × <hrms:money value="${l.installmentAmount}"/></td>
                    <td><hrms:monthName value="${l.startMonth}"/> ${l.startYear}</td>
                    <td class="text-end"><g:if test="${l.status == 'ACTIVE'}"><hrms:money value="${outstanding[l.id]}"/></g:if><g:else>—</g:else></td>
                    <td><hrms:loanStatus status="${l.status}"/></td>
                    <td class="text-end"><g:link action="show" id="${l.id}" class="btn btn-sm btn-outline-secondary"><i class="bi bi-eye"></i></g:link></td>
                </tr>
            </g:each>
            <g:if test="${!loans}"><tr><td colspan="9" class="text-center text-muted py-4">কোনো ঋণ/অগ্রিম নেই।</td></tr></g:if>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>

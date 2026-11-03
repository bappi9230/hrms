<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>ঋণ #${loan.id}</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <div>
        <h1 class="page-title mb-1">${loan.loanType == 'LOAN' ? 'ঋণ' : 'অগ্রিম'} #${loan.id} — ${loan.employee.name}</h1>
        <hrms:loanStatus status="${loan.status}"/>
    </div>
    <g:link action="index" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> তালিকা</g:link>
</div>

<div class="row g-3">
    <div class="col-lg-5">
        <div class="card mb-3">
            <table class="table table-sm mb-0">
                <tbody>
                <tr><td>পরিমাণ</td><td class="text-end fw-semibold"><hrms:money value="${loan.amount}"/></td></tr>
                <tr><td>কিস্তি</td><td class="text-end">${loan.installments} × <hrms:money value="${loan.installmentAmount}"/></td></tr>
                <tr><td>প্রথম কিস্তি</td><td class="text-end"><hrms:monthName value="${loan.startMonth}"/> ${loan.startYear}</td></tr>
                <tr><td>বাকি (ভবিষ্যৎ কিস্তি)</td><td class="text-end"><hrms:money value="${outstanding}"/></td></tr>
                <tr><td>কারণ</td><td class="text-end">${loan.reason ?: '—'}</td></tr>
                <tr><td>আবেদন করেছেন</td><td class="text-end">${loan.requestedBy ?: '—'}</td></tr>
                <tr><td>সিদ্ধান্ত</td><td class="text-end">${loan.decidedBy ?: '—'}<g:if test="${loan.decidedAt}"> <span class="small text-muted">${loan.decidedAt.toString().replace('T', ' ').take(16)}</span></g:if></td></tr>
                <tr><td>মন্তব্য</td><td class="text-end">${loan.decisionNote ?: '—'}</td></tr>
                </tbody>
            </table>
        </div>

        <g:if test="${loan.status == 'PENDING'}">
            <div class="card mb-3">
                <div class="card-header bg-white fw-semibold">অপেক্ষমাণ</div>
                <div class="card-body">
                    <sec:ifAnyGranted roles="ROLE_ADMIN">
                        <g:form action="approve" id="${loan.id}" method="POST" class="mb-2">
                            <input type="text" name="note" maxlength="255" class="form-control mb-2" placeholder="মন্তব্য (ঐচ্ছিক)">
                            <button type="submit" class="btn btn-success w-100"><i class="bi bi-check-lg"></i> অনুমোদন</button>
                        </g:form>
                        <g:form action="reject" id="${loan.id}" method="POST" class="mb-2">
                            <input type="text" name="note" maxlength="255" class="form-control mb-2" placeholder="প্রত্যাখ্যানের কারণ (বাধ্যতামূলক)">
                            <button type="submit" class="btn btn-outline-danger w-100"><i class="bi bi-x-lg"></i> প্রত্যাখ্যান</button>
                        </g:form>
                    </sec:ifAnyGranted>
                    <sec:ifNotGranted roles="ROLE_ADMIN"><div class="small text-muted mb-2">Admin এর অনুমোদনের অপেক্ষায়।</div></sec:ifNotGranted>
                    <g:form action="cancel" id="${loan.id}" method="POST" onsubmit="return confirm('আবেদন প্রত্যাহার করবেন?');">
                        <button type="submit" class="btn btn-outline-secondary w-100">আবেদন প্রত্যাহার</button>
                    </g:form>
                </div>
            </div>
        </g:if>
        <g:if test="${loan.status == 'ACTIVE'}">
            <sec:ifAnyGranted roles="ROLE_ADMIN">
                <div class="card">
                    <div class="card-header bg-white fw-semibold">আগাম নিষ্পত্তি</div>
                    <div class="card-body">
                        <div class="small text-muted mb-2">বাকি কিস্তি পে-রোলের বাইরে (নগদ/ব্যাংকে) শোধ হলে।</div>
                        <g:form action="settle" id="${loan.id}" method="POST" onsubmit="return confirm('বাকি সব কিস্তি নিষ্পত্তি (SETTLED) করবেন?');">
                            <input type="text" name="note" maxlength="150" class="form-control mb-2" placeholder="কীভাবে শোধ হলো">
                            <button type="submit" class="btn btn-outline-warning w-100">বাকি কিস্তি নিষ্পত্তি</button>
                        </g:form>
                    </div>
                </div>
            </sec:ifAnyGranted>
        </g:if>
    </div>

    <div class="col-lg-7">
        <div class="card">
            <div class="card-header bg-white fw-semibold">কিস্তির তালিকা</div>
            <div class="table-responsive">
                <table class="table table-sm align-middle mb-0">
                    <thead class="table-light"><tr><th>#</th><th>মাস</th><th class="text-end">টাকা</th><th>অবস্থা</th></tr></thead>
                    <tbody>
                    <g:each in="${installments}" var="i">
                        <tr>
                            <td>${i.seqNo}</td>
                            <td><hrms:monthName value="${i.dueMonth}"/> ${i.dueYear}</td>
                            <td class="text-end"><hrms:money value="${i.amount}"/></td>
                            <td>
                                <g:if test="${i.status == 'PAID'}"><span class="badge text-bg-success">কাটা হয়েছে</span></g:if>
                                <g:elseif test="${i.status == 'SETTLED'}"><span class="badge text-bg-info">নিষ্পত্তি</span></g:elseif>
                                <g:else><span class="badge text-bg-warning text-dark">বাকি</span><g:if test="${i.payrollRun}"> <span class="small text-muted">(খসড়া ${i.payrollRun} এ ধরা)</span></g:if></g:else>
                            </td>
                        </tr>
                    </g:each>
                    <g:if test="${!installments}"><tr><td colspan="4" class="text-center text-muted py-3">অনুমোদনের পর কিস্তির তালিকা তৈরি হবে।</td></tr></g:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
</body>
</html>

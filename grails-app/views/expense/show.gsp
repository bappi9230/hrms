<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>দাবি #${claim.id}</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <div>
        <h1 class="page-title mb-1">${claim.title}</h1>
        <hrms:d8Badge kind="claim" status="${claim.status}"/>
        <span class="small text-muted ms-1">দাবি #${claim.id}</span>
    </div>
    <g:link action="index" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> তালিকা</g:link>
</div>

<%-- অনুমোদনের ধাপ: প্রগ্রেস --%>
<div class="card mb-3">
    <div class="card-body">
        <div class="d-flex justify-content-between text-center small">
            <g:each in="${steps}" var="step">
                <div class="flex-fill">
                    <i class="bi ${step.done ? 'bi-check-circle-fill text-success' : 'bi-circle text-muted'} fs-4"></i>
                    <div>${step.label}</div>
                </div>
            </g:each>
        </div>
        <g:if test="${claim.status in ['REJECTED', 'CANCELLED']}"><div class="text-center mt-2"><hrms:d8Badge kind="claim" status="${claim.status}"/></div></g:if>
    </div>
</div>

<div class="row g-3">
    <div class="col-lg-6">
        <div class="card mb-3">
            <table class="table table-sm mb-0">
                <tbody>
                <tr><td>কর্মী</td><td class="text-end">${claim.employee.name}</td></tr>
                <tr><td>ধরন</td><td class="text-end"><hrms:d8Name value="${claim.category}"/></td></tr>
                <tr><td>খরচের তারিখ</td><td class="text-end"><hrms:date value="${claim.expenseDate}"/></td></tr>
                <tr><td>পরিমাণ</td><td class="text-end fw-semibold"><hrms:money value="${claim.amount}"/></td></tr>
                <g:if test="${claim.travel}"><tr><td>ভ্রমণ</td><td class="text-end">${claim.travel.destination} (<hrms:date value="${claim.travel.fromDate}"/>)</td></tr></g:if>
                <tr><td>রসিদ</td><td class="text-end">
                    <g:if test="${claim.hasReceipt}"><g:link action="receipt" id="${claim.id}"><i class="bi bi-paperclip"></i> ${claim.receiptName}</g:link> <span class="small text-muted"><hrms:fileSize value="${claim.receiptSize}"/></span></g:if>
                    <g:else>—</g:else>
                </td></tr>
                <tr><td>জমার তারিখ</td><td class="text-end"><hrms:date value="${claim.dateCreated}"/></td></tr>
                </tbody>
            </table>
        </div>
        <g:if test="${claim.description}"><div class="card"><div class="card-body" style="white-space: pre-line;">${claim.description}</div></div></g:if>
    </div>

    <div class="col-lg-6">
        <div class="card mb-3">
            <div class="card-header bg-white fw-semibold">সিদ্ধান্তের ইতিহাস</div>
            <ul class="list-group list-group-flush small">
                <li class="list-group-item"><strong>ম্যানেজার:</strong>
                    <g:if test="${claim.managerBy}">${claim.managerBy} - <hrms:dateTime value="${claim.managerAt}"/><g:if test="${claim.managerNote}"><br>"${claim.managerNote}"</g:if></g:if>
                    <g:elseif test="${claim.employee.reportingManager == null}">ম্যানেজার নেই - এই ধাপ বাদ</g:elseif>
                    <g:else>অপেক্ষায়</g:else>
                </li>
                <li class="list-group-item"><strong>হিসাব বিভাগ:</strong>
                    <g:if test="${claim.accountsBy}">${claim.accountsBy} - <hrms:dateTime value="${claim.accountsAt}"/><g:if test="${claim.accountsNote}"><br>"${claim.accountsNote}"</g:if></g:if>
                    <g:else>—</g:else>
                </li>
                <li class="list-group-item"><strong>পরিশোধ:</strong>
                    <g:if test="${claim.paidBy}">${claim.paidBy} - <hrms:date value="${claim.paidDate}"/> (রেফ: ${claim.paymentRef})</g:if>
                    <g:else>—</g:else>
                </li>
            </ul>
        </div>

        <g:if test="${canManagerAct || canAccountsAct}">
            <div class="card mb-3">
                <div class="card-header bg-white fw-semibold">${canManagerAct ? 'ম্যানেজারের সিদ্ধান্ত' : 'হিসাব বিভাগের সিদ্ধান্ত'}</div>
                <div class="card-body">
                    <g:form action="approve" id="${claim.id}" method="POST" class="mb-2">
                        <input type="text" name="note" maxlength="255" class="form-control mb-2" placeholder="মন্তব্য (ঐচ্ছিক)">
                        <button type="submit" class="btn btn-success w-100"><i class="bi bi-check-lg"></i> অনুমোদন</button>
                    </g:form>
                    <g:form action="reject" id="${claim.id}" method="POST">
                        <input type="text" name="note" maxlength="255" class="form-control mb-2" placeholder="প্রত্যাখ্যানের কারণ (বাধ্যতামূলক)">
                        <button type="submit" class="btn btn-outline-danger w-100"><i class="bi bi-x-lg"></i> প্রত্যাখ্যান</button>
                    </g:form>
                </div>
            </div>
        </g:if>

        <g:if test="${canPay}">
            <div class="card mb-3">
                <div class="card-header bg-white fw-semibold">পরিশোধ</div>
                <div class="card-body">
                    <g:form action="pay" id="${claim.id}" method="POST" class="row g-2">
                        <div class="col-md-5"><input type="date" name="paidDate" value="${today}" min="${claim.expenseDate}" max="${today}" class="form-control" required></div>
                        <div class="col-md-7"><input type="text" name="paymentRef" maxlength="80" class="form-control" placeholder="চেক / bKash / ব্যাংক ট্রানজ্যাকশন নং" required></div>
                        <div class="col-12"><button type="submit" class="btn btn-primary w-100"><i class="bi bi-cash-coin"></i> পরিশোধিত হিসেবে রেকর্ড</button></div>
                    </g:form>
                </div>
            </div>
        </g:if>

        <g:if test="${canCancel}">
            <g:form action="cancel" id="${claim.id}" method="POST" onsubmit="return confirm('দাবি প্রত্যাহার করবেন?');">
                <button type="submit" class="btn btn-outline-secondary w-100">দাবি প্রত্যাহার</button>
            </g:form>
        </g:if>
    </div>
</div>
</body>
</html>

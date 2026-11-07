<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>চাকরি-ছাড়া #${separation.id}</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <div>
        <h1 class="page-title mb-1">${separation.employee.name} - <hrms:d9Name value="${separation.exitType}"/></h1>
        <hrms:d9Badge kind="separation" status="${separation.status}"/>
        <span class="small text-muted ms-1">কেস #${separation.id}<g:if test="${separation.letterNo}"> · চিঠি নং ${separation.letterNo}</g:if></span>
    </div>
    <g:link action="index" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> তালিকা</g:link>
</div>

<g:set var="st" value="${separation.status}"/>
<div class="card mb-3">
    <div class="card-body">
        <div class="d-flex justify-content-between text-center small">
            <div class="flex-fill"><i class="bi bi-check-circle-fill text-success fs-4"></i><div>জমা</div></div>
            <div class="flex-fill"><i class="bi ${st in ['PENDING_HR', 'ACCEPTED', 'COMPLETED'] ? 'bi-check-circle-fill text-success' : 'bi-circle text-muted'} fs-4"></i><div>ম্যানেজার</div></div>
            <div class="flex-fill"><i class="bi ${st in ['ACCEPTED', 'COMPLETED'] ? 'bi-check-circle-fill text-success' : 'bi-circle text-muted'} fs-4"></i><div>HR গ্রহণ</div></div>
            <div class="flex-fill"><i class="bi ${progress?.allDone || st == 'COMPLETED' ? 'bi-check-circle-fill text-success' : 'bi-circle text-muted'} fs-4"></i><div>ক্লিয়ারেন্স</div></div>
            <div class="flex-fill"><i class="bi ${settlement?.status == 'PAID' ? 'bi-check-circle-fill text-success' : 'bi-circle text-muted'} fs-4"></i><div>নিষ্পত্তি</div></div>
            <div class="flex-fill"><i class="bi ${st == 'COMPLETED' ? 'bi-check-circle-fill text-success' : 'bi-circle text-muted'} fs-4"></i><div>সম্পন্ন</div></div>
        </div>
        <g:if test="${st in ['REJECTED', 'WITHDRAWN']}"><div class="text-center mt-2"><hrms:d9Badge kind="separation" status="${st}"/></div></g:if>
    </div>
</div>

<div class="row g-3">
    <div class="col-lg-6">
        <div class="card mb-3">
            <table class="table table-sm mb-0">
                <tbody>
                <tr><td>কর্মী</td><td class="text-end">${separation.employee.name} (${separation.employee.department})</td></tr>
                <tr><td>কারণের ধরন</td><td class="text-end"><hrms:d9Name value="${separation.reasonCategory}"/></td></tr>
                <tr><td>নোটিসের তারিখ</td><td class="text-end"><hrms:date value="${separation.noticeDate}"/></td></tr>
                <tr><td>চাওয়া শেষ দিন</td><td class="text-end"><hrms:date value="${separation.requestedLastDay}"/></td></tr>
                <tr><td>চূড়ান্ত শেষ কর্মদিবস</td><td class="text-end fw-semibold"><hrms:date value="${separation.lastWorkingDay}"/></td></tr>
                <tr><td>নোটিস পিরিয়ড</td><td class="text-end">${separation.noticeDays} দিন<g:if test="${separation.noticeWaived}"> (মওকুফ)</g:if></td></tr>
                <tr><td>শুরু করেছেন</td><td class="text-end">${separation.initiatedBy ?: '—'}</td></tr>
                </tbody>
            </table>
        </div>
        <div class="card mb-3"><div class="card-body" style="white-space: pre-line;">${separation.reason}</div></div>

        <div class="card mb-3">
            <div class="card-header bg-white fw-semibold">সিদ্ধান্তের ইতিহাস</div>
            <ul class="list-group list-group-flush small">
                <li class="list-group-item"><strong>ম্যানেজার:</strong>
                    <g:if test="${separation.managerBy}">${separation.managerBy} - <hrms:dt value="${separation.managerAt}"/><g:if test="${separation.managerNote}"><br>"${separation.managerNote}"</g:if></g:if>
                    <g:else>—</g:else></li>
                <li class="list-group-item"><strong>HR:</strong>
                    <g:if test="${separation.hrBy}">${separation.hrBy} - <hrms:dt value="${separation.hrAt}"/><g:if test="${separation.hrNote}"><br>"${separation.hrNote}"</g:if></g:if>
                    <g:else>—</g:else></li>
                <g:if test="${separation.exitInterviewNote}"><li class="list-group-item"><strong>এক্সিট নোট:</strong> ${separation.exitInterviewNote}</li></g:if>
            </ul>
        </div>

        <g:if test="${canManagerAct}">
            <div class="card mb-3">
                <div class="card-header bg-white fw-semibold">ম্যানেজারের সিদ্ধান্ত</div>
                <div class="card-body">
                    <g:form action="managerDecision" id="${separation.id}" method="POST">
                        <input type="text" name="note" maxlength="255" class="form-control mb-2" placeholder="মন্তব্য (প্রত্যাখ্যানে বাধ্যতামূলক)">
                        <div class="d-flex gap-2">
                            <button type="submit" name="decision" value="approve" class="btn btn-success flex-fill">HR এর কাছে পাঠান</button>
                            <button type="submit" name="decision" value="reject" class="btn btn-outline-danger flex-fill">প্রত্যাখ্যান</button>
                        </div>
                    </g:form>
                </div>
            </div>
        </g:if>

        <g:if test="${canHrAct}">
            <div class="card mb-3">
                <div class="card-header bg-white fw-semibold">HR এর সিদ্ধান্ত</div>
                <div class="card-body">
                    <g:form action="hrDecision" id="${separation.id}" method="POST">
                        <div class="mb-2">
                            <label class="form-label small mb-1">চূড়ান্ত শেষ কর্মদিবস</label>
                            <input type="date" name="lastDay" value="${separation.requestedLastDay}" class="form-control">
                            <div class="form-text">নোটিস অনুযায়ী ন্যূনতম <hrms:date value="${minLastDay}"/>; এর আগে দিলে নোটিস মওকুফ টিক দিন।</div>
                        </div>
                        <div class="form-check mb-2"><input class="form-check-input" type="checkbox" name="waive" id="waive"><label class="form-check-label" for="waive">কম নোটিস মওকুফ</label></div>
                        <input type="text" name="note" maxlength="255" class="form-control mb-2" placeholder="মন্তব্য (প্রত্যাখ্যানে বাধ্যতামূলক)">
                        <div class="d-flex gap-2">
                            <button type="submit" name="decision" value="approve" class="btn btn-success flex-fill">গ্রহণ করুন</button>
                            <button type="submit" name="decision" value="reject" class="btn btn-outline-danger flex-fill">প্রত্যাখ্যান</button>
                        </div>
                    </g:form>
                </div>
            </div>
        </g:if>

        <g:if test="${canWithdraw}">
            <g:form action="withdraw" id="${separation.id}" method="POST" onsubmit="return confirm('পদত্যাগ প্রত্যাহার করবেন?');">
                <button type="submit" class="btn btn-outline-secondary w-100 mb-3">আবেদন প্রত্যাহার</button>
            </g:form>
        </g:if>
        <g:if test="${canCancel}">
            <div class="card mb-3 border-danger">
                <div class="card-body">
                    <g:form action="cancelExit" id="${separation.id}" method="POST" onsubmit="return confirm('এই চাকরি-ছাড়ার কেস বাতিল করবেন?');">
                        <input type="text" name="note" maxlength="255" class="form-control mb-2" placeholder="বাতিলের কারণ">
                        <button type="submit" class="btn btn-outline-danger w-100">কেস বাতিল করুন</button>
                    </g:form>
                </div>
            </div>
        </g:if>
    </div>

    <div class="col-lg-6">
        <%-- ক্লিয়ারেন্স --%>
        <g:if test="${items}">
            <div class="card mb-3" id="clearance">
                <div class="card-header bg-white fw-semibold d-flex justify-content-between">
                    <span>ক্লিয়ারেন্স চেকলিস্ট</span>
                    <span class="small text-muted">${progress.done}/${progress.total}</span>
                </div>
                <ul class="list-group list-group-flush">
                    <g:each in="${items}" var="i">
                        <li class="list-group-item">
                            <div class="d-flex justify-content-between align-items-start gap-2">
                                <div>
                                    <i class="bi ${i.done ? 'bi-check-square-fill text-success' : 'bi-square text-muted'}"></i>
                                    <span class="${i.done ? 'text-decoration-line-through text-muted' : ''}">${i.title}</span>
                                    <span class="badge text-bg-light border"><hrms:d9Name value="${i.ownerRole}"/></span>
                                    <g:if test="${i.done}"><div class="small text-muted">${i.doneBy} - <hrms:dt value="${i.doneAt}"/><g:if test="${i.remarks}"> · ${i.remarks}</g:if></div></g:if>
                                </div>
                                <g:if test="${canTick[i.id]}">
                                    <g:if test="${!i.done}">
                                        <g:form action="tick" id="${i.id}" method="POST" class="d-flex gap-1">
                                            <input type="text" name="remarks" maxlength="255" class="form-control form-control-sm" placeholder="মন্তব্য" style="width:120px;">
                                            <button type="submit" class="btn btn-sm btn-success"><i class="bi bi-check-lg"></i></button>
                                        </g:form>
                                    </g:if>
                                    <g:elseif test="${st == 'ACCEPTED'}">
                                        <g:form action="reopenItem" id="${i.id}" method="POST"><button type="submit" class="btn btn-sm btn-outline-secondary">আবার খুলুন</button></g:form>
                                    </g:elseif>
                                </g:if>
                            </div>
                        </li>
                    </g:each>
                </ul>
            </div>
        </g:if>

        <%-- চূড়ান্ত নিষ্পত্তি --%>
        <g:if test="${st in ['ACCEPTED', 'COMPLETED']}">
            <div class="card mb-3" id="settlement">
                <div class="card-header bg-white fw-semibold d-flex justify-content-between">
                    <span>চূড়ান্ত নিষ্পত্তি</span>
                    <g:if test="${settlement}"><hrms:d9Badge kind="settlement" status="${settlement.status}"/></g:if>
                </div>
                <g:if test="${settlement}">
                    <table class="table table-sm mb-0">
                        <tbody>
                        <tr><td>বকেয়া বেতন (${settlement.salaryDays} দিন)</td><td class="text-end"><hrms:money value="${settlement.unpaidSalary}"/></td></tr>
                        <tr><td>ছুটি নগদায়ন (${settlement.leaveDays} দিন)</td><td class="text-end"><hrms:money value="${settlement.leaveEncashment}"/></td></tr>
                        <tr><td>গ্র্যাচুইটি (${settlement.serviceYears} বছর)</td><td class="text-end"><hrms:money value="${settlement.gratuity}"/></td></tr>
                        <tr><td>অনুমোদিত খরচের দাবি</td><td class="text-end"><hrms:money value="${settlement.pendingClaims}"/></td></tr>
                        <tr><td>ঋণ/অগ্রিম কর্তন</td><td class="text-end text-danger">- <hrms:money value="${settlement.loanDeduction}"/></td></tr>
                        <tr><td>কম নোটিস কর্তন (${settlement.noticeShortDays} দিন)</td><td class="text-end text-danger">- <hrms:money value="${settlement.noticeDeduction}"/></td></tr>
                        <tr><td>হাতে সমন্বয়<g:if test="${settlement.adjustmentNote}"><div class="small text-muted">${settlement.adjustmentNote}</div></g:if></td><td class="text-end"><hrms:signedMoney value="${settlement.otherAdjustment}"/></td></tr>
                        <tr class="table-light fw-semibold"><td>নিট প্রদেয়</td><td class="text-end"><hrms:signedMoney value="${settlement.netPayable}"/></td></tr>
                        </tbody>
                    </table>
                    <g:if test="${settlement.notes}"><div class="card-body small text-muted border-top">${settlement.notes}</div></g:if>
                    <g:if test="${settlement.status == 'PAID'}"><div class="card-body small border-top">পরিশোধ: ${settlement.paidBy} - <hrms:date value="${settlement.paidDate}"/> (রেফ: ${settlement.paymentRef})</div></g:if>
                </g:if>
                <g:else><div class="card-body text-muted small">এখনো হিসাব তৈরি হয়নি।</div></g:else>

                <g:if test="${canPrepare}">
                    <div class="card-body border-top">
                        <g:form action="prepare" id="${separation.id}" method="POST"><button type="submit" class="btn btn-outline-primary w-100"><i class="bi bi-calculator"></i> ${settlement ? 'হিসাব আবার করুন' : 'হিসাব তৈরি করুন'}</button></g:form>
                    </div>
                </g:if>
                <g:if test="${canAdjust}">
                    <div class="card-body border-top">
                        <g:form action="adjust" id="${separation.id}" method="POST" class="row g-2">
                            <div class="col-5"><input type="number" step="0.01" name="adjustment" value="${settlement.otherAdjustment}" class="form-control" placeholder="+ পাওনা / - কর্তন"></div>
                            <div class="col-7"><input type="text" name="note" maxlength="255" value="${settlement.adjustmentNote}" class="form-control" placeholder="সমন্বয়ের কারণ"></div>
                            <div class="col-12"><button type="submit" class="btn btn-outline-secondary w-100">সমন্বয় সংরক্ষণ</button></div>
                        </g:form>
                    </div>
                </g:if>
                <g:if test="${canApproveSettlement}">
                    <div class="card-body border-top">
                        <g:form action="approveSettlement" id="${separation.id}" method="POST"><button type="submit" class="btn btn-success w-100">নিষ্পত্তি অনুমোদন (হিসাব বিভাগ)</button></g:form>
                    </div>
                </g:if>
                <g:if test="${canPay}">
                    <div class="card-body border-top">
                        <g:form action="pay" id="${separation.id}" method="POST" class="row g-2">
                            <div class="col-5"><input type="date" name="paidDate" value="${today}" max="${today}" class="form-control" required></div>
                            <div class="col-7"><input type="text" name="paymentRef" maxlength="80" class="form-control" placeholder="চেক / ব্যাংক রেফারেন্স" required></div>
                            <div class="col-12"><button type="submit" class="btn btn-primary w-100"><i class="bi bi-cash-coin"></i> পরিশোধিত হিসেবে রেকর্ড</button></div>
                        </g:form>
                    </div>
                </g:if>
            </div>
        </g:if>

        <g:if test="${canComplete}">
            <div class="card mb-3 border-success">
                <div class="card-header bg-white fw-semibold">কেস সম্পন্ন করুন</div>
                <div class="card-body">
                    <p class="small text-muted">সব ক্লিয়ারেন্স, নিষ্পত্তি পরিশোধ ও শেষ কর্মদিবস পার হলে কর্মী INACTIVE হবে এবং লগইন বন্ধ হবে।</p>
                    <g:form action="complete" id="${separation.id}" method="POST" onsubmit="return confirm('কেস সম্পন্ন করবেন? কর্মীর লগইন বন্ধ হয়ে যাবে।');">
                        <textarea name="exitNote" rows="2" maxlength="1000" class="form-control mb-2" placeholder="এক্সিট ইন্টারভিউয়ের সারসংক্ষেপ (ঐচ্ছিক)"></textarea>
                        <button type="submit" class="btn btn-success w-100"><i class="bi bi-check2-all"></i> কেস সম্পন্ন</button>
                    </g:form>
                </div>
            </div>
        </g:if>

        <%-- চিঠি --%>
        <g:if test="${st in ['ACCEPTED', 'COMPLETED'] && canLetter}">
            <div>
                <div class="card">
                    <div class="card-header bg-white fw-semibold">চিঠি (প্রিন্টযোগ্য)</div>
                    <div class="list-group list-group-flush">
                        <g:link action="letter" id="${separation.id}" params="[kind: 'acceptance']" class="list-group-item list-group-item-action"><i class="bi bi-file-earmark-text"></i> পদত্যাগ গ্রহণের চিঠি</g:link>
                        <g:if test="${st == 'COMPLETED'}">
                            <g:link action="letter" id="${separation.id}" params="[kind: 'relieving']" class="list-group-item list-group-item-action"><i class="bi bi-file-earmark-text"></i> রিলিভিং লেটার</g:link>
                            <g:link action="letter" id="${separation.id}" params="[kind: 'experience']" class="list-group-item list-group-item-action"><i class="bi bi-file-earmark-text"></i> অভিজ্ঞতার সনদ</g:link>
                        </g:if>
                    </div>
                </div>
            </div>
        </g:if>
    </div>
</div>
</body>
</html>

<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>${employee.name} - বেতন কাঠামো</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <div>
        <h1 class="page-title mb-1">${employee.name} — বেতন কাঠামো</h1>
        <span class="text-muted">${employee.designation?.title} &middot; ${employee.department?.name} &middot; যোগদান <hrms:date value="${employee.joinDate}"/></span>
    </div>
    <g:link action="index" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> তালিকা</g:link>
</div>

<g:if test="${mismatch}">
    <div class="alert alert-warning d-flex flex-wrap justify-content-between align-items-center gap-2">
        <span><i class="bi bi-exclamation-triangle"></i> প্রোফাইলের বেতন (<hrms:money value="${employee.salary}"/>) বর্তমান কাঠামোর gross (<hrms:money value="${current.gross}"/>) এর সাথে মেলে না। পে-রোল কাঠামো ধরে হিসাব করে।</span>
        <g:form action="syncToProfile" id="${employee.id}" method="POST" onsubmit="return confirm('প্রোফাইলের বেতন অনুযায়ী ডিফল্ট ভাগে নতুন কাঠামো (আজ থেকে) বানাবেন?');">
            <button type="submit" class="btn btn-sm btn-warning">প্রোফাইলের বেতন অনুযায়ী ঠিক করুন</button>
        </g:form>
    </div>
</g:if>

<div class="row g-3">
    <div class="col-lg-7">
        <div class="card mb-3">
            <div class="card-header bg-white d-flex justify-content-between align-items-center">
                <span class="fw-semibold">বর্তমান কাঠামো <span class="small text-muted">(কার্যকর <hrms:date value="${current.effectiveFrom}"/>)</span></span>
                <span class="fs-5 fw-bold text-primary"><hrms:money value="${current.gross}"/></span>
            </div>
            <div class="card-body">
                <g:form action="saveStructure" id="${employee.id}" method="POST">
                    <div class="row g-3">
                        <div class="col-sm-6">
                            <label class="form-label" for="basic">বেসিক (৳)</label>
                            <input type="number" min="1" step="1" id="basic" name="basic" value="${current.basic.setScale(0, java.math.RoundingMode.HALF_UP)}" class="form-control sal-part" required>
                        </div>
                        <div class="col-sm-6">
                            <label class="form-label" for="houseRent">বাড়ি ভাড়া (৳)</label>
                            <input type="number" min="0" step="1" id="houseRent" name="houseRent" value="${current.houseRent.setScale(0, java.math.RoundingMode.HALF_UP)}" class="form-control sal-part">
                        </div>
                        <div class="col-sm-6">
                            <label class="form-label" for="medical">চিকিৎসা ভাতা (৳)</label>
                            <input type="number" min="0" step="1" id="medical" name="medical" value="${current.medical.setScale(0, java.math.RoundingMode.HALF_UP)}" class="form-control sal-part">
                        </div>
                        <div class="col-sm-6">
                            <label class="form-label" for="conveyance">যাতায়াত ভাতা (৳)</label>
                            <input type="number" min="0" step="1" id="conveyance" name="conveyance" value="${current.conveyance.setScale(0, java.math.RoundingMode.HALF_UP)}" class="form-control sal-part">
                        </div>
                        <div class="col-sm-6">
                            <label class="form-label" for="effectiveFrom">কার্যকর তারিখ</label>
                            <input type="date" id="effectiveFrom" name="effectiveFrom" value="${today}" class="form-control" required>
                        </div>
                        <div class="col-sm-6">
                            <label class="form-label">নতুন Gross (যোগফল)</label>
                            <div class="form-control bg-light fw-bold" id="grossSum">—</div>
                        </div>
                        <div class="col-12">
                            <label class="form-label" for="remarks">মন্তব্য</label>
                            <input type="text" id="remarks" name="remarks" maxlength="255" class="form-control">
                        </div>
                    </div>
                    <div class="small text-muted mt-2">একই তারিখে আগে থেকে কাঠামো থাকলে সেটিই হালনাগাদ হবে। বেতনে বড় পরিবর্তনের জন্য নিচের "ইনক্রিমেন্ট / সংশোধন" ব্যবহার করুন - সেটি লগে থাকে।</div>
                    <button type="submit" class="btn btn-primary mt-3"><i class="bi bi-check-lg"></i> কাঠামো সংরক্ষণ</button>
                </g:form>
            </div>
        </div>

        <g:if test="${canRevise}">
            <div class="card mb-3">
                <div class="card-header bg-white fw-semibold">ইনক্রিমেন্ট / বেতন সংশোধন</div>
                <div class="card-body">
                    <g:form action="saveRevision" id="${employee.id}" method="POST">
                        <div class="row g-3">
                            <div class="col-sm-4">
                                <label class="form-label" for="revisionType">ধরন</label>
                                <select name="revisionType" id="revisionType" class="form-select">
                                    <option value="INCREMENT">ইনক্রিমেন্ট (বাড়ানো)</option>
                                    <option value="REVISION">সংশোধন (কমানোও যায়)</option>
                                </select>
                            </div>
                            <div class="col-sm-4">
                                <label class="form-label" for="effectiveDate">কার্যকর তারিখ</label>
                                <input type="date" name="effectiveDate" id="effectiveDate" value="${today}" class="form-control" required>
                            </div>
                            <div class="col-sm-4">
                                <label class="form-label" for="newGross">নতুন Gross (৳)</label>
                                <input type="number" min="1" step="1" name="newGross" id="newGross" class="form-control" required>
                                <div class="form-text" id="revHint">বর্তমান: <hrms:money value="${current.gross}"/></div>
                            </div>
                            <div class="col-12">
                                <label class="form-label" for="revRemarks">কারণ</label>
                                <input type="text" name="remarks" id="revRemarks" maxlength="255" class="form-control" placeholder="যেমন: বার্ষিক ইনক্রিমেন্ট ২০২৬">
                            </div>
                        </div>
                        <div class="small text-muted mt-2">বর্তমান কাঠামোর অনুপাত ধরে বেসিক, বাড়ি ভাড়া, চিকিৎসা ও যাতায়াত নতুন করে ভাগ হবে। ভবিষ্যৎ তারিখ দিলে সেই তারিখ থেকে কার্যকর হবে।</div>
                        <button type="submit" class="btn btn-success mt-3"><i class="bi bi-graph-up-arrow"></i> প্রয়োগ করুন</button>
                    </g:form>
                </div>
            </div>
        </g:if>
    </div>

    <div class="col-lg-5">
        <div class="card mb-3">
            <div class="card-header bg-white fw-semibold">ভাতা ও কর্তনের খাত</div>
            <ul class="list-group list-group-flush">
                <g:each in="${assigned}" var="a">
                    <li class="list-group-item d-flex justify-content-between align-items-center">
                        <div>
                            <span class="badge ${a.head.headType == 'EARNING' ? 'text-bg-success' : 'text-bg-danger'}">${a.head.headType == 'EARNING' ? 'ভাতা' : 'কর্তন'}</span>
                            ${a.head.name}
                            <div class="small text-muted">
                                <g:if test="${a.amount != null}">নির্দিষ্ট <hrms:money value="${a.amount}"/></g:if>
                                <g:elseif test="${a.head.calcType == 'PERCENT_BASIC'}">বেসিকের ${a.head.defaultValue}%</g:elseif>
                                <g:else><hrms:money value="${a.head.defaultValue}"/></g:else>
                                <g:if test="${!a.head.active}"> &middot; নিষ্ক্রিয় (পে-রোলে ধরা হয় না)</g:if>
                            </div>
                        </div>
                        <g:form action="removeHead" id="${employee.id}" method="POST" onsubmit="return confirm('খাতটি সরাবেন?');">
                            <g:hiddenField name="assignmentId" value="${a.id}"/>
                            <button type="submit" class="btn btn-sm btn-outline-danger"><i class="bi bi-x-lg"></i></button>
                        </g:form>
                    </li>
                </g:each>
                <g:if test="${!assigned}"><li class="list-group-item text-muted">কোনো অতিরিক্ত খাত নেই।</li></g:if>
            </ul>
            <g:if test="${freeHeads}">
                <div class="card-body border-top">
                    <g:form action="assignHead" id="${employee.id}" method="POST" class="row g-2 align-items-end">
                        <div class="col-12">
                            <label class="form-label small mb-1" for="headId">খাত যোগ করুন</label>
                            <select name="headId" id="headId" class="form-select form-select-sm">
                                <g:each in="${freeHeads}" var="h"><option value="${h.id}">${h.name} (${h.headType == 'EARNING' ? 'ভাতা' : 'কর্তন'})</option></g:each>
                            </select>
                        </div>
                        <div class="col-8">
                            <label class="form-label small mb-1" for="amount">নির্দিষ্ট টাকা (ঐচ্ছিক)</label>
                            <input type="number" min="0" step="1" name="amount" id="amount" class="form-control form-control-sm" placeholder="খালি = খাতের ডিফল্ট">
                        </div>
                        <div class="col-4"><button type="submit" class="btn btn-sm btn-primary w-100">যোগ</button></div>
                    </g:form>
                </div>
            </g:if>
        </div>

        <div class="card mb-3">
            <div class="card-header bg-white fw-semibold">কাঠামোর ইতিহাস</div>
            <div class="table-responsive">
                <table class="table table-sm mb-0 align-middle">
                    <thead class="table-light"><tr><th>কার্যকর</th><th class="text-end">বেসিক</th><th class="text-end">Gross</th></tr></thead>
                    <tbody>
                    <g:each in="${history}" var="h">
                        <tr><td><hrms:date value="${h.effectiveFrom}"/></td><td class="text-end"><hrms:money value="${h.basic}"/></td><td class="text-end fw-semibold"><hrms:money value="${h.gross}"/></td></tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="card">
            <div class="card-header bg-white fw-semibold">ইনক্রিমেন্ট / সংশোধনের লগ</div>
            <ul class="list-group list-group-flush">
                <g:each in="${revisions}" var="r">
                    <li class="list-group-item">
                        <div class="d-flex justify-content-between">
                            <span><span class="badge ${r.revisionType == 'INCREMENT' ? 'text-bg-success' : 'text-bg-secondary'}">${r.revisionType == 'INCREMENT' ? 'ইনক্রিমেন্ট' : 'সংশোধন'}</span>
                                <hrms:date value="${r.effectiveDate}"/></span>
                            <span><hrms:signedMoney value="${r.changeAmount}"/></span>
                        </div>
                        <div class="small text-muted"><hrms:money value="${r.oldGross}"/> → <strong><hrms:money value="${r.newGross}"/></strong>
                            <g:if test="${r.changePercent != null}">(${r.changePercent}%)</g:if>
                            <g:if test="${r.remarks}"> &middot; ${r.remarks}</g:if></div>
                    </li>
                </g:each>
                <g:if test="${!revisions}"><li class="list-group-item text-muted">এখনো কোনো ইনক্রিমেন্ট/সংশোধন নেই।</li></g:if>
            </ul>
        </div>
    </div>
</div>

<script>
    (function () {
        var parts = document.querySelectorAll('.sal-part');
        var sum = document.getElementById('grossSum');
        function recalc() {
            var t = 0;
            parts.forEach(function (i) { t += parseFloat(i.value) || 0; });
            sum.textContent = t.toLocaleString('en-US') + ' ৳';
        }
        parts.forEach(function (i) { i.addEventListener('input', recalc); });
        recalc();

        var cur = ${current.gross.setScale(0, java.math.RoundingMode.HALF_UP)};
        var ng = document.getElementById('newGross');
        var hint = document.getElementById('revHint');
        if (ng && hint) {
            ng.addEventListener('input', function () {
                var v = parseFloat(ng.value);
                if (!v || !cur) { hint.textContent = 'বর্তমান: ' + cur.toLocaleString('en-US') + ' ৳'; return; }
                var diff = v - cur;
                var pct = (diff * 100 / cur).toFixed(1);
                hint.textContent = 'বর্তমান ' + cur.toLocaleString('en-US') + ' ৳ → পরিবর্তন ' + (diff >= 0 ? '+' : '') + diff.toLocaleString('en-US') + ' ৳ (' + pct + '%)';
            });
        }
    })();
</script>
</body>
</html>

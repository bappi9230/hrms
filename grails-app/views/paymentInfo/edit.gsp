<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>পেমেন্ট তথ্য - ${employee.name}</title>
</head>
<body>
<h1 class="page-title">পেমেন্ট তথ্য — ${employee.name}</h1>

<div class="card" style="max-width:720px;">
    <div class="card-body">
        <g:form action="save" id="${employee.id}" method="POST" class="row g-3">
            <div class="col-md-4">
                <label class="form-label" for="method">মাধ্যম <span class="text-danger">*</span></label>
                <select name="method" id="method" class="form-select">
                    <option value="BANK" ${info.method == 'BANK' ? 'selected' : ''}>ব্যাংক</option>
                    <option value="BKASH" ${info.method == 'BKASH' ? 'selected' : ''}>bKash</option>
                    <option value="NAGAD" ${info.method == 'NAGAD' ? 'selected' : ''}>Nagad</option>
                    <option value="CASH" ${info.method == 'CASH' ? 'selected' : ''}>নগদ</option>
                </select>
            </div>
            <div class="col-12"></div>

            <div class="col-md-6 pi-bank">
                <label class="form-label" for="bankName">ব্যাংকের নাম</label>
                <input type="text" name="bankName" id="bankName" maxlength="80" class="form-control" value="${info.bankName}">
            </div>
            <div class="col-md-6 pi-bank">
                <label class="form-label" for="bankBranch">শাখা</label>
                <input type="text" name="bankBranch" id="bankBranch" maxlength="80" class="form-control" value="${info.bankBranch}">
            </div>
            <div class="col-md-6 pi-bank">
                <label class="form-label" for="accountName">হিসাবের নাম <span class="small text-muted">(খালি = কর্মীর নাম)</span></label>
                <input type="text" name="accountName" id="accountName" maxlength="100" class="form-control" value="${info.accountName}">
            </div>
            <div class="col-md-6 pi-bank">
                <label class="form-label" for="accountNo">হিসাব নং</label>
                <input type="text" name="accountNo" id="accountNo" maxlength="30" class="form-control" value="${info.accountNo}">
            </div>
            <div class="col-md-6 pi-bank">
                <label class="form-label" for="routingNo">রাউটিং নং <span class="small text-muted">(৯ সংখ্যা, ঐচ্ছিক)</span></label>
                <input type="text" name="routingNo" id="routingNo" maxlength="9" class="form-control" value="${info.routingNo}">
            </div>

            <div class="col-md-6 pi-wallet">
                <label class="form-label" for="walletNo">মোবাইল নম্বর</label>
                <input type="text" name="walletNo" id="walletNo" maxlength="11" class="form-control" placeholder="01712345678" value="${info.walletNo}">
            </div>

            <div class="col-12 d-flex gap-2">
                <button type="submit" class="btn btn-primary"><i class="bi bi-check-lg"></i> সংরক্ষণ</button>
                <g:link action="index" class="btn btn-outline-secondary">বাতিল</g:link>
            </div>
        </g:form>
    </div>
</div>

<script>
    (function () {
        var sel = document.getElementById('method');
        function sync() {
            var m = sel.value;
            document.querySelectorAll('.pi-bank').forEach(function (el) { el.style.display = m === 'BANK' ? '' : 'none'; });
            document.querySelectorAll('.pi-wallet').forEach(function (el) { el.style.display = (m === 'BKASH' || m === 'NAGAD') ? '' : 'none'; });
        }
        sel.addEventListener('change', sync);
        sync();
    })();
</script>
</body>
</html>

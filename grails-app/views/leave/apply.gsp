<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>ছুটির আবেদন</title>
</head>
<body>
<h1 class="page-title">ছুটির আবেদন</h1>
<g:render template="tabs" model="[active: 'apply']"/>

<div class="row g-3">
    <div class="col-lg-7">
        <div class="card">
            <div class="card-body">
                <g:form action="save" method="POST">
                    <div class="mb-3">
                        <label class="form-label" for="leaveType">ছুটির ধরন <span class="text-danger">*</span></label>
                        <select name="leaveType" id="leaveType" class="form-select" required>
                            <option value="">— নির্বাচন করুন —</option>
                            <g:each in="${types}" var="t">
                                <option value="${t.id}" ${form?.leaveType == t.id.toString() ? 'selected' : ''}>${t.name}${t.paid ? '' : ' (বিনা বেতন)'}</option>
                            </g:each>
                        </select>
                    </div>
                    <div class="row g-3 mb-3">
                        <div class="col-md-6">
                            <label class="form-label" for="fromDate">শুরুর তারিখ <span class="text-danger">*</span></label>
                            <input type="date" name="fromDate" id="fromDate" value="${form?.fromDate}" class="form-control" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label" for="toDate">শেষের তারিখ <span class="text-danger">*</span></label>
                            <input type="date" name="toDate" id="toDate" value="${form?.toDate}" class="form-control" required>
                        </div>
                    </div>
                    <div class="alert alert-info py-2 small" id="daysInfo">তারিখ দিলে কর্মদিবসের সংখ্যা এখানে দেখাবে (শুক্রবার বাদ)।</div>
                    <div class="mb-3">
                        <label class="form-label" for="reason">কারণ <span class="text-danger">*</span></label>
                        <textarea name="reason" id="reason" rows="3" maxlength="500" class="form-control" required>${form?.reason}</textarea>
                    </div>
                    <button type="submit" class="btn btn-primary"><i class="bi bi-send"></i> আবেদন জমা দিন</button>
                    <g:link action="index" class="btn btn-secondary">বাতিল</g:link>
                </g:form>
            </div>
        </div>
    </div>
    <div class="col-lg-5">
        <div class="card">
            <div class="card-header bg-white fw-semibold">আমার ব্যালেন্স</div>
            <ul class="list-group list-group-flush">
                <g:each in="${balances}" var="b">
                    <li class="list-group-item d-flex justify-content-between">
                        <span>${b.type.name}</span>
                        <span class="fw-semibold">
                            <g:if test="${b.limited}"><g:formatNumber number="${b.available}" maxFractionDigits="1"/> দিন খালি</g:if>
                            <g:else>সীমাহীন</g:else>
                        </span>
                    </li>
                </g:each>
            </ul>
            <div class="card-footer small text-muted">খালি = অর্জিত + আগের বছরের − ব্যবহৃত − অপেক্ষমাণ।
                <g:if test="${me?.reportingManager}">আপনার আবেদন প্রথমে <strong>${me.reportingManager.name}</strong>-এর কাছে যাবে।</g:if>
                <g:else>আপনার কোনো রিপোর্টিং ম্যানেজার নেই, তাই আবেদন সরাসরি HR-এর কাছে যাবে।</g:else>
            </div>
        </div>
    </div>
</div>

<script>
    (function () {
        var from = document.getElementById('fromDate'), to = document.getElementById('toDate'), info = document.getElementById('daysInfo');
        function update() {
            if (!from.value || !to.value) return;
            var a = new Date(from.value + 'T00:00:00'), b = new Date(to.value + 'T00:00:00');
            if (b < a) { info.textContent = 'শেষের তারিখ শুরুর আগে হতে পারে না।'; return; }
            var n = 0;
            for (var d = new Date(a); d <= b; d.setDate(d.getDate() + 1)) { if (d.getDay() !== 5) n++; }
            info.textContent = 'মোট কর্মদিবস: ' + n + ' দিন (শুক্রবার বাদ)।';
        }
        from.addEventListener('change', function () { if (!to.value || to.value < from.value) to.value = from.value; update(); });
        to.addEventListener('change', update);
        update();
    })();
</script>
</body>
</html>

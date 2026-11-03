<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>বেতন</title>
</head>
<body>
<h1 class="page-title">বেতন (পে-রোল)</h1>

<div class="row g-3 mb-4">
    <div class="col-lg-5">
        <div class="card h-100">
            <div class="card-header bg-white fw-semibold">মাসের পে-রোল চালান</div>
            <div class="card-body">
                <g:form action="generate" method="POST" class="row g-2 align-items-end">
                    <div class="col-6">
                        <label class="form-label small mb-1" for="payMonth">মাস</label>
                        <select name="payMonth" id="payMonth" class="form-select">
                            <g:each in="${1..12}" var="m">
                                <option value="${m}" ${m == defaultMonth ? 'selected' : ''}><hrms:monthName value="${m}"/></option>
                            </g:each>
                        </select>
                    </div>
                    <div class="col-6">
                        <label class="form-label small mb-1" for="payYear">বছর</label>
                        <input type="number" name="payYear" id="payYear" class="form-control" value="${defaultYear}" min="2000" max="2100" required>
                    </div>
                    <div class="col-12 mt-3">
                        <button type="submit" class="btn btn-primary w-100"><i class="bi bi-play-fill"></i> পে-রোল চালান</button>
                    </div>
                </g:form>
                <div class="small text-muted mt-2">শুধু শেষ হয়ে যাওয়া মাসের জন্য চালানো যাবে। খসড়া (DRAFT) অবস্থায় বারবার চালানো যায়; হাতে দেওয়া OT ঘণ্টা ও সমন্বয় ধরে রাখা হয়।</div>
            </div>
        </div>
    </div>
    <div class="col-lg-7">
        <div class="card h-100">
            <div class="card-header bg-white fw-semibold">হিসাবের নিয়ম (Day 5 - সহজ সংস্করণ)</div>
            <div class="card-body small">
                <ul class="mb-0 ps-3">
                    <li><strong>কর্মদিবস:</strong> শুক্রবার বাদে প্রতিদিন (যোগদানের তারিখ থেকে)। মাসের মাঝে যোগ দিলে আয় দিনের অনুপাতে।</li>
                    <li><strong>অনুপস্থিতি / বিনা বেতনের ছুটি:</strong> প্রতিদিনের জন্য gross ÷ ${dayDivisor} কাটা হয়। উপস্থিতির রেকর্ড না থাকলে অনুপস্থিত ধরা হয়।</li>
                    <li><strong>দেরি:</strong> প্রতি ${lateRule} দিন দেরিতে ১ দিনের বেতন কাটা।</li>
                    <li><strong>ওভারটাইম:</strong> অনুমোদিত ঘণ্টা × (বেসিক ÷ ২০৮) × ${otMultiplier}। হিসাব বিভাগ লাইনে গিয়ে ঘণ্টা বদলাতে পারে।</li>
                    <li><strong>ভাতা/কর্তন খাত:</strong> কর্মীর বেতন কাঠামো পাতা থেকে বরাদ্দ করা খাত যোগ/বিয়োগ হয়। কর/PF/বোনাস Day 6 এ আসবে।</li>
                </ul>
            </div>
        </div>
    </div>
</div>

<div class="card">
    <div class="card-header bg-white fw-semibold">পে-রোলের তালিকা</div>
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
            <tr><th>মাস</th><th>অবস্থা</th><th class="text-end">কর্মী</th><th class="text-end">মোট আয়</th><th class="text-end">কর্তন</th><th class="text-end">নেট বেতন</th><th>চালিয়েছেন</th><th style="width:90px;"></th></tr>
            </thead>
            <tbody>
            <g:each in="${runs}" var="r">
                <tr>
                    <td class="fw-semibold"><hrms:monthName value="${r.payMonth}"/> ${r.payYear}</td>
                    <td><hrms:payrollStatus status="${r.status}"/></td>
                    <td class="text-end">${r.employeeCount}</td>
                    <td class="text-end"><hrms:money value="${r.totalGross}"/></td>
                    <td class="text-end"><hrms:money value="${r.totalDeduction}"/></td>
                    <td class="text-end fw-semibold"><hrms:money value="${r.totalNet}"/></td>
                    <td class="small text-muted">${r.generatedBy ?: '—'}</td>
                    <td class="text-end"><g:link action="show" id="${r.id}" class="btn btn-sm btn-outline-primary"><i class="bi bi-table"></i> শিট</g:link></td>
                </tr>
            </g:each>
            <g:if test="${!runs}">
                <tr><td colspan="8" class="text-center text-muted py-4">এখনো কোনো পে-রোল চালানো হয়নি।</td></tr>
            </g:if>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>

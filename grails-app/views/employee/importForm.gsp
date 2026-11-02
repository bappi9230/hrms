<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>কর্মী CSV ইমপোর্ট</title>
</head>
<body>
<div class="d-flex justify-content-between align-items-center mb-3">
    <h1 class="page-title mb-0">কর্মী CSV ইমপোর্ট</h1>
    <g:link action="index" class="btn btn-secondary"><i class="bi bi-arrow-left"></i> তালিকা</g:link>
</div>

<div class="card mb-3">
    <div class="card-body">
        <ol class="mb-3">
            <li>নিচের বাটন থেকে নমুনা ফাইল নামান (Excel এ খুলে এডিট করা যায়)।</li>
            <li>প্রতি লাইনে একজন কর্মী লিখুন। বিভাগ, পদবি, শাখা ও গ্রেডের নাম সিস্টেমে যেমন আছে হুবহু তেমন দিতে হবে।</li>
            <li>তারিখ <code>yyyy-MM-dd</code> ফরম্যাটে (যেমন 2026-01-15)। Excel এ Save As → <strong>CSV UTF-8</strong> বেছে নিন।</li>
            <li>ভুল লাইন বাদ যাবে, ঠিক লাইনগুলো ইমপোর্ট হবে; কোন লাইনে কী ভুল তা নিচে দেখানো হবে।</li>
        </ol>
        <g:link action="importTemplate" class="btn btn-outline-success mb-3"><i class="bi bi-file-earmark-arrow-down"></i> নমুনা CSV নামান</g:link>

        <g:uploadForm action="importCsv" method="POST" class="row g-2 align-items-end">
            <div class="col-md-8">
                <label for="file" class="form-label">CSV ফাইল (সর্বোচ্চ ২ MB)</label>
                <input type="file" class="form-control" id="file" name="file" accept=".csv,text/csv" required>
            </div>
            <div class="col-md-4">
                <button type="submit" class="btn btn-primary"><i class="bi bi-upload"></i> ইমপোর্ট করুন</button>
            </div>
        </g:uploadForm>
    </div>
</div>

<g:if test="${result}">
    <div class="card">
        <div class="card-header bg-white fw-semibold">ইমপোর্টের ফলাফল</div>
        <div class="card-body">
            <span class="badge text-bg-secondary">মোট: ${result.total}</span>
            <span class="badge text-bg-success">সফল: ${result.imported}</span>
            <span class="badge text-bg-danger">ব্যর্থ: ${result.failed}</span>

            <g:if test="${result.errors}">
                <div class="table-responsive mt-3">
                    <table class="table table-sm align-middle mb-0">
                        <thead class="table-light"><tr><th style="width:80px;">লাইন</th><th>ইমেইল</th><th>সমস্যা</th></tr></thead>
                        <tbody>
                        <g:each in="${result.errors}" var="err">
                            <tr><td>${err.line}</td><td>${err.email ?: '—'}</td><td class="text-danger">${err.message}</td></tr>
                        </g:each>
                        </tbody>
                    </table>
                </div>
            </g:if>
        </div>
    </div>
</g:if>
</body>
</html>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>সার্টিফিকেট ${enrollment.certificateNo}</title>
    <style>
        .certificate { max-width: 820px; margin: 0 auto; background: #fff; border: 6px double #1F3864; padding: 3rem 2rem; }
        @media print {
            .hrms-topbar, .hrms-sidebar, .hrms-footer, .hrms-subnav, .toast-container, .no-print { display: none !important; }
            .hrms-shell, .hrms-main { display: block !important; margin: 0 !important; padding: 0 !important; }
            body { background: #fff !important; }
        }
    </style>
</head>
<body>
<div class="d-flex justify-content-between align-items-center mb-3 no-print">
    <g:link action="my" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> ফিরে যান</g:link>
    <button type="button" class="btn btn-primary" onclick="window.print()"><i class="bi bi-printer"></i> প্রিন্ট / PDF</button>
</div>

<div class="certificate text-center">
    <div class="text-uppercase small text-muted mb-2" style="letter-spacing: .2em;">${company}</div>
    <h1 class="h2 mb-4" style="color:#1F3864;">সমাপনী সার্টিফিকেট</h1>
    <p class="mb-1">এই মর্মে প্রত্যয়ন করা যাচ্ছে যে</p>
    <p class="display-6 fw-bold my-3">${enrollment.employee.name}</p>
    <p class="mb-1">${enrollment.employee.designation} &middot; ${enrollment.employee.department}</p>
    <p class="my-4">
        <strong>${enrollment.course.title}</strong> প্রশিক্ষণ কোর্সটি
        (<hrms:date value="${enrollment.course.startDate}"/><g:if test="${enrollment.course.endDate != enrollment.course.startDate}"> থেকে <hrms:date value="${enrollment.course.endDate}"/></g:if>,
        মোট ${enrollment.course.hours} ঘণ্টা)
        সফলভাবে সম্পন্ন করেছেন।<g:if test="${enrollment.score != null}"> প্রাপ্ত স্কোর: <strong>${enrollment.score}/100</strong>।</g:if>
    </p>
    <div class="d-flex justify-content-between mt-5 pt-4 small">
        <div class="text-start">সার্টিফিকেট নং: <strong>${enrollment.certificateNo}</strong><br>তারিখ: <hrms:date value="${enrollment.completedDate}"/></div>
        <div class="text-end"><div style="border-top:1px solid #333; padding-top:.3rem; min-width:180px;">অনুমোদনকারী (HR)</div></div>
    </div>
</div>
</body>
</html>

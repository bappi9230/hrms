<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>চিঠি - ${separation.employee.name}</title>
    <style>
        .letter { max-width: 820px; margin: 0 auto; background: #fff; border: 1px solid #ddd; padding: 3rem 3rem; line-height: 1.9; }
        @media print {
            .hrms-topbar, .hrms-sidebar, .hrms-footer, .hrms-subnav, .toast-container, .no-print { display: none !important; }
            .hrms-shell, .hrms-main { display: block !important; margin: 0 !important; padding: 0 !important; }
            .letter { border: 0; padding: 0; }
            body { background: #fff !important; }
        }
    </style>
</head>
<body>
<div class="d-flex justify-content-between align-items-center mb-3 no-print">
    <g:link action="show" id="${separation.id}" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> ফিরে যান</g:link>
    <button type="button" class="btn btn-primary" onclick="window.print()"><i class="bi bi-printer"></i> প্রিন্ট / PDF</button>
</div>

<g:set var="emp" value="${separation.employee}"/>
<div class="letter">
    <div class="text-center mb-4">
        <div class="h4 mb-0" style="color:#1F3864;">${company}</div>
        <div class="small text-muted">মানবসম্পদ বিভাগ</div>
    </div>
    <div class="d-flex justify-content-between mb-4 small">
        <div><g:if test="${separation.letterNo}">স্মারক নং: ${separation.letterNo}</g:if></div>
        <div>তারিখ: <hrms:date value="${today}"/></div>
    </div>

    <g:if test="${kind == 'acceptance'}">
        <h5 class="text-center mb-4"><u>পদত্যাগপত্র গ্রহণের চিঠি</u></h5>
        <p>জনাব/জনাবা <strong>${emp.name}</strong>,<br>${emp.designation}, ${emp.department}।</p>
        <p>আপনার <hrms:date value="${separation.noticeDate}"/> তারিখের পদত্যাগপত্র কর্তৃপক্ষ গ্রহণ করেছে। আপনার শেষ কর্মদিবস নির্ধারিত হয়েছে
            <strong><hrms:date value="${separation.lastWorkingDay}"/></strong>।<g:if test="${separation.noticeWaived}"> কম নোটিস মওকুফ করা হয়েছে।</g:if></p>
        <p>শেষ কর্মদিবসের মধ্যে সব ক্লিয়ারেন্স ধাপ সম্পন্ন করে কোম্পানির সম্পদ ও দায়িত্ব হস্তান্তর করার অনুরোধ করা হলো। ক্লিয়ারেন্স শেষে আপনার চূড়ান্ত পাওনা নিষ্পত্তি করা হবে।</p>
    </g:if>
    <g:elseif test="${kind == 'relieving'}">
        <h5 class="text-center mb-4"><u>রিলিভিং লেটার</u></h5>
        <p>এই মর্মে জানানো যাচ্ছে যে, <strong>${emp.name}</strong>, ${emp.designation}, ${emp.department}, <hrms:date value="${emp.joinDate}"/> তারিখ থেকে
            <hrms:date value="${separation.lastWorkingDay}"/> তারিখ পর্যন্ত আমাদের প্রতিষ্ঠানে কর্মরত ছিলেন।</p>
        <p>তিনি <hrms:date value="${separation.lastWorkingDay}"/> তারিখের কর্মদিবস শেষে প্রতিষ্ঠানের দায়িত্ব থেকে অব্যাহতি পেয়েছেন। সব ক্লিয়ারেন্স ও আর্থিক নিষ্পত্তি সম্পন্ন হয়েছে এবং প্রতিষ্ঠানের প্রতি তাঁর আর কোনো দায় নেই।</p>
        <p>তাঁর ভবিষ্যৎ জীবনের জন্য শুভকামনা রইল।</p>
    </g:elseif>
    <g:else>
        <h5 class="text-center mb-4"><u>অভিজ্ঞতার সনদ</u></h5>
        <p>এই মর্মে প্রত্যয়ন করা যাচ্ছে যে, <strong>${emp.name}</strong> আমাদের প্রতিষ্ঠানে <strong>${emp.designation}</strong> পদে, ${emp.department} বিভাগে
            <hrms:date value="${emp.joinDate}"/> থেকে <hrms:date value="${separation.lastWorkingDay}"/> পর্যন্ত কর্মরত ছিলেন।</p>
        <p>কর্মকালে তিনি তাঁর দায়িত্ব নিষ্ঠার সাথে পালন করেছেন। আমরা তাঁর সার্বিক সাফল্য কামনা করি।</p>
    </g:else>

    <div class="mt-5 pt-4">
        <div style="border-top:1px solid #333; display:inline-block; min-width:200px; padding-top:.3rem;">অনুমোদনকারী (HR)</div>
        <div class="small text-muted">${company}</div>
    </div>
</div>
</body>
</html>

<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>অফার লেটার - ${candidate.name}</title>
</head>
<body>
<div class="d-flex justify-content-between align-items-center mb-3 no-print">
    <g:link action="show" id="${candidate.id}" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> ফিরে যান</g:link>
    <button type="button" class="btn btn-primary" onclick="window.print()"><i class="bi bi-printer"></i> প্রিন্ট</button>
</div>

<div class="offer-letter">
    <div class="d-flex justify-content-between border-bottom border-2 border-dark pb-3 mb-4">
        <div>
            <h2 class="h4 mb-0">${company}</h2>
            <div class="small text-muted">নিয়োগ প্রস্তাব (Offer of Employment)</div>
        </div>
        <div class="text-end small">
            <div>স্মারক: OFR-${offer.id}</div>
            <div>তারিখ: <hrms:date value="${offer.dateCreated}"/></div>
        </div>
    </div>

    <p>প্রিয় <strong>${candidate.name}</strong>,</p>
    <p>
        আপনার আবেদন, যোগ্যতা ও ইন্টারভিউয়ের ফলাফলের ভিত্তিতে আমরা আপনাকে <strong>${offer.designation}</strong> পদে
        <strong>${offer.department}</strong> বিভাগে<g:if test="${offer.branch}"> (${offer.branch})</g:if> নিয়োগের প্রস্তাব দিচ্ছি।
    </p>

    <table class="table table-bordered">
        <tbody>
        <tr><th style="width: 40%;">পদবি</th><td>${offer.designation}</td></tr>
        <tr><th>বিভাগ</th><td>${offer.department}</td></tr>
        <tr><th>মাসিক মোট বেতন (gross)</th><td><hrms:money value="${offer.salary}"/></td></tr>
        <tr><th>যোগদানের তারিখ</th><td><hrms:date value="${offer.joinDate}"/></td></tr>
        <tr><th>পরীক্ষাকাল</th><td>${offer.probationMonths ? offer.probationMonths + ' মাস' : 'প্রযোজ্য নয়'}</td></tr>
        <tr><th>পরীক্ষাকাল শেষে নিয়োগের ধরন</th><td><hrms:label value="${offer.employeeType}"/></td></tr>
        <tr><th>প্রস্তাবের মেয়াদ</th><td><hrms:date value="${offer.validUntil}"/> পর্যন্ত</td></tr>
        </tbody>
    </table>

    <g:if test="${offer.terms}">
        <p class="mb-1 fw-semibold">অতিরিক্ত শর্ত</p>
        <p style="white-space: pre-line;">${offer.terms}</p>
    </g:if>

    <p>
        এই প্রস্তাব গ্রহণ করলে যোগদানের দিন আপনার NID, শিক্ষাগত সনদ ও পাসপোর্ট সাইজ ছবির কপি সাথে আনতে অনুরোধ করা হলো।
        পরীক্ষাকালের মেয়াদ শেষে মূল্যায়নের ভিত্তিতে চাকরি স্থায়ী করা হবে।
    </p>

    <div class="row mt-5 pt-4">
        <div class="col-6">
            <div class="border-top border-dark pt-1 small" style="max-width: 220px;">অনুমোদিত স্বাক্ষর<br>${company}</div>
        </div>
        <div class="col-6 text-end">
            <div class="border-top border-dark pt-1 small ms-auto" style="max-width: 220px;">প্রার্থীর স্বাক্ষর ও তারিখ<br>${candidate.name}</div>
        </div>
    </div>
</div>
</body>
</html>

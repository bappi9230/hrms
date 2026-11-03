<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>আমার ইন্টারভিউ</title>
</head>
<body>
<h1 class="page-title">আমার ইন্টারভিউ <span class="badge text-bg-secondary fs-6">${interviews.size()}</span></h1>

<div class="card">
    <div class="table-responsive">
        <table class="table align-middle mb-0">
            <thead class="table-light"><tr><th>প্রার্থী</th><th>পদ</th><th>রাউন্ড</th><th>সময়</th><th>অবস্থা</th><th>ফলাফল</th></tr></thead>
            <tbody>
            <g:each in="${interviews}" var="iv">
                <tr>
                    <td class="fw-semibold">${iv.candidate.name}
                        <g:if test="${iv.candidate.hasResume}"><g:link action="resume" id="${iv.candidate.id}" class="ms-1" title="সিভি"><i class="bi bi-file-earmark-arrow-down"></i></g:link></g:if></td>
                    <td>${iv.candidate.jobPosting.title}</td>
                    <td>${iv.roundNo}. ${iv.roundName}</td>
                    <td><hrms:dateTime value="${iv.scheduledAt}"/><div class="small text-muted">${iv.mode}<g:if test="${iv.location}"> &middot; ${iv.location}</g:if></div></td>
                    <td><hrms:recruitBadge kind="interview" status="${iv.status}"/></td>
                    <td>
                        <g:if test="${iv.status == 'COMPLETED'}"><hrms:recruitBadge kind="recommend" status="${iv.recommendation}"/> ${iv.rating}/5</g:if>
                        <g:elseif test="${iv.status == 'SCHEDULED' && iv.scheduledAt.isAfter(now)}"><span class="small text-muted">সময় হলে ফিডব্যাক দিন</span></g:elseif>
                        <g:else>—</g:else>
                    </td>
                </tr>
                <g:if test="${iv.status == 'SCHEDULED' && !iv.scheduledAt.isAfter(now)}">
                    <tr>
                        <td colspan="6" class="bg-light">
                            <g:form action="feedback" id="${iv.id}" method="POST" class="row g-2">
                                <input type="hidden" name="back" value="mine">
                                <div class="col-md-2">
                                    <select name="rating" class="form-select form-select-sm" required>
                                        <option value="">রেটিং</option>
                                        <g:each in="${5..1}" var="n"><option value="${n}">${n} / 5</option></g:each>
                                    </select>
                                </div>
                                <div class="col-md-2">
                                    <select name="recommendation" class="form-select form-select-sm" required>
                                        <option value="">সুপারিশ</option>
                                        <option value="HIRE">HIRE</option><option value="HOLD">HOLD</option><option value="REJECT">REJECT</option>
                                    </select>
                                </div>
                                <div class="col-md-6"><input type="text" name="feedback" maxlength="1500" class="form-control form-control-sm" placeholder="ফিডব্যাক (অন্তত ৫ অক্ষর)" required></div>
                                <div class="col-md-2"><button type="submit" class="btn btn-sm btn-primary w-100">ফিডব্যাক জমা</button></div>
                            </g:form>
                        </td>
                    </tr>
                </g:if>
            </g:each>
            <g:if test="${!interviews}"><tr><td colspan="6" class="text-center text-muted py-4">আপনার নামে কোনো ইন্টারভিউ নেই।</td></tr></g:if>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>

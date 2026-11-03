<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>চাহিদা #${requisition.id}</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <div>
        <h1 class="page-title mb-1">${requisition.title}</h1>
        <hrms:recruitBadge kind="requisition" status="${requisition.status}"/>
        <span class="small text-muted ms-1">চাহিদা #${requisition.id}</span>
    </div>
    <g:link action="index" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> তালিকা</g:link>
</div>

<div class="row g-3">
    <div class="col-lg-6">
        <div class="card mb-3">
            <table class="table table-sm mb-0">
                <tbody>
                <tr><td>বিভাগ</td><td class="text-end">${requisition.department}</td></tr>
                <tr><td>পদবি</td><td class="text-end">${requisition.designation}</td></tr>
                <tr><td>শাখা</td><td class="text-end">${requisition.branch ?: '—'}</td></tr>
                <tr><td>পদসংখ্যা</td><td class="text-end fw-semibold">${hired} / ${requisition.vacancies} পূরণ</td></tr>
                <tr><td>কর্মীর ধরন</td><td class="text-end"><hrms:label value="${requisition.employeeType}"/></td></tr>
                <tr><td>বেতনের সীমা</td><td class="text-end"><hrms:money value="${requisition.minSalary}"/> – <hrms:money value="${requisition.maxSalary}"/></td></tr>
                <tr><td>প্রত্যাশিত যোগদান</td><td class="text-end"><hrms:date value="${requisition.expectedJoinDate}"/></td></tr>
                <tr><td>আবেদনকারী</td><td class="text-end">${requisition.requestedBy ?: '—'}</td></tr>
                <tr><td>সিদ্ধান্ত</td><td class="text-end">${requisition.decidedBy ?: '—'}<g:if test="${requisition.decidedAt}"> <span class="small text-muted"><hrms:dateTime value="${requisition.decidedAt}"/></span></g:if></td></tr>
                <tr><td>মন্তব্য</td><td class="text-end">${requisition.decisionNote ?: '—'}</td></tr>
                </tbody>
            </table>
        </div>
        <div class="card">
            <div class="card-header bg-white fw-semibold">কেন দরকার</div>
            <div class="card-body">${requisition.justification}</div>
        </div>
    </div>

    <div class="col-lg-6">
        <g:if test="${requisition.status == 'PENDING'}">
            <div class="card mb-3">
                <div class="card-header bg-white fw-semibold">অপেক্ষমাণ</div>
                <div class="card-body">
                    <g:if test="${canDecide}">
                        <g:form action="approve" id="${requisition.id}" method="POST" class="mb-2">
                            <input type="text" name="note" maxlength="255" class="form-control mb-2" placeholder="মন্তব্য (ঐচ্ছিক)">
                            <button type="submit" class="btn btn-success w-100"><i class="bi bi-check-lg"></i> অনুমোদন</button>
                        </g:form>
                        <g:form action="reject" id="${requisition.id}" method="POST" class="mb-2">
                            <input type="text" name="note" maxlength="255" class="form-control mb-2" placeholder="প্রত্যাখ্যানের কারণ (বাধ্যতামূলক)">
                            <button type="submit" class="btn btn-outline-danger w-100"><i class="bi bi-x-lg"></i> প্রত্যাখ্যান</button>
                        </g:form>
                    </g:if>
                    <g:else><div class="small text-muted mb-2">HR/Admin এর অনুমোদনের অপেক্ষায়।</div></g:else>
                    <g:if test="${canDecide || mine}">
                        <g:form action="cancel" id="${requisition.id}" method="POST" onsubmit="return confirm('চাহিদা প্রত্যাহার করবেন?');">
                            <button type="submit" class="btn btn-outline-secondary w-100">চাহিদা প্রত্যাহার</button>
                        </g:form>
                    </g:if>
                </div>
            </div>
        </g:if>

        <g:if test="${requisition.status == 'APPROVED' && canDecide}">
            <div class="card mb-3">
                <div class="card-body d-flex flex-wrap gap-2">
                    <g:link controller="recruitment" action="createPosting" params="[requisition: requisition.id]" class="btn btn-primary"><i class="bi bi-megaphone"></i> চাকরির বিজ্ঞপ্তি দিন</g:link>
                    <g:form action="close" id="${requisition.id}" method="POST" onsubmit="return confirm('চাহিদা ও এর সব বিজ্ঞপ্তি বন্ধ করবেন?');">
                        <button type="submit" class="btn btn-outline-secondary">চাহিদা বন্ধ করুন</button>
                    </g:form>
                </div>
            </div>
        </g:if>

        <div class="card">
            <div class="card-header bg-white fw-semibold">বিজ্ঞপ্তি</div>
            <div class="table-responsive">
                <table class="table table-sm align-middle mb-0">
                    <thead class="table-light"><tr><th>শিরোনাম</th><th>শেষ তারিখ</th><th>অবস্থা</th></tr></thead>
                    <tbody>
                    <g:each in="${postings}" var="p">
                        <tr>
                            <td>${p.title}</td>
                            <td><hrms:date value="${p.deadline}"/></td>
                            <td><hrms:recruitBadge kind="posting" status="${p.status}"/></td>
                        </tr>
                    </g:each>
                    <g:if test="${!postings}"><tr><td colspan="3" class="text-center text-muted py-3">এখনো কোনো বিজ্ঞপ্তি নেই।</td></tr></g:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
</body>
</html>

<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>মূল্যায়ন - ${appraisal.employee.name}</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <div>
        <h1 class="page-title mb-1">${appraisal.employee.name} - মূল্যায়ন</h1>
        <span class="fw-semibold">${appraisal.cycle.name}</span>
        <hrms:d8Badge kind="appraisal" status="${appraisal.status}"/>
        <span class="small text-muted ms-1"><hrms:date value="${appraisal.cycle.startDate}"/> - <hrms:date value="${appraisal.cycle.endDate}"/></span>
    </div>
    <g:link action="appraisals" params="[cycle: appraisal.cycle.id]" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> তালিকা</g:link>
</div>

<g:if test="${appraisal.cycle.status == 'CLOSED' && appraisal.status != 'COMPLETED'}">
    <div class="alert alert-warning">চক্র বন্ধ হয়ে গেছে, এই মূল্যায়ন অসম্পূর্ণ থেকে গেছে।</div>
</g:if>

<div class="row g-3 mb-3">
    <%-- ============ কর্মীর নিজের মূল্যায়ন ============ --%>
    <div class="col-lg-6">
        <div class="card h-100">
            <div class="card-header bg-white fw-semibold">কর্মীর নিজের মূল্যায়ন</div>
            <div class="card-body">
                <g:if test="${canSelf}">
                    <g:form action="submitSelf" id="${appraisal.id}" method="POST">
                        <label class="form-label d-block">রেটিং <span class="text-danger">*</span></label>
                        <div class="mb-3">
                            <g:each in="${[5, 4, 3, 2, 1]}" var="n">
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="rating" id="self${n}" value="${n}" required>
                                    <label class="form-check-label" for="self${n}"><hrms:stars value="${n}"/> ${ratingLabels[n]}</label>
                                </div>
                            </g:each>
                        </div>
                        <div class="mb-3">
                            <label class="form-label" for="selfComment">মন্তব্য - এই সময়ে কী অর্জন করেছেন <span class="text-danger">*</span></label>
                            <textarea id="selfComment" name="comment" rows="4" maxlength="1000" class="form-control" required></textarea>
                        </div>
                        <button type="submit" class="btn btn-primary" onclick="return confirm('জমা দেওয়ার পর আর বদলানো যাবে না। জমা দেবেন?');"><i class="bi bi-send"></i> ম্যানেজারের কাছে জমা দিন</button>
                    </g:form>
                </g:if>
                <g:elseif test="${appraisal.selfRating}">
                    <div class="mb-2 fs-5"><hrms:stars value="${appraisal.selfRating}"/> <span class="fs-6">${ratingLabels[appraisal.selfRating]}</span></div>
                    <p style="white-space: pre-line;">${appraisal.selfComment}</p>
                    <div class="small text-muted">জমা: <hrms:dateTime value="${appraisal.selfSubmittedAt}"/></div>
                </g:elseif>
                <g:else><div class="text-muted">কর্মী এখনো নিজের মূল্যায়ন জমা দেননি।</div></g:else>
            </div>
        </div>
    </div>

    <%-- ============ ম্যানেজারের মূল্যায়ন ============ --%>
    <div class="col-lg-6">
        <div class="card h-100">
            <div class="card-header bg-white fw-semibold">ম্যানেজারের মূল্যায়ন <span class="small text-muted fw-normal">(${appraisal.reviewer ?: 'HR / Admin'})</span></div>
            <div class="card-body">
                <g:if test="${canReview}">
                    <g:if test="${appraisal.status == 'PENDING_SELF'}">
                        <div class="alert alert-warning py-2 small">কর্মী নিজের মূল্যায়ন জমা দেননি, তবে চূড়ান্ত তারিখ পেরিয়েছে বলে আপনি রিভিউ দিতে পারেন।</div>
                    </g:if>
                    <g:form action="submitManager" id="${appraisal.id}" method="POST">
                        <label class="form-label d-block">রেটিং <span class="text-danger">*</span></label>
                        <div class="mb-3">
                            <g:each in="${[5, 4, 3, 2, 1]}" var="n">
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="rating" id="mgr${n}" value="${n}" required>
                                    <label class="form-check-label" for="mgr${n}"><hrms:stars value="${n}"/> ${ratingLabels[n]}</label>
                                </div>
                            </g:each>
                        </div>
                        <div class="mb-3">
                            <label class="form-label" for="mgrComment">মন্তব্য <span class="small text-muted">(১-২ বা ৫ রেটিং দিলে কারণ বাধ্যতামূলক)</span></label>
                            <textarea id="mgrComment" name="comment" rows="4" maxlength="1000" class="form-control"></textarea>
                        </div>
                        <button type="submit" class="btn btn-primary" onclick="return confirm('চূড়ান্ত করার পর আর বদলানো যাবে না। চূড়ান্ত করবেন?');"><i class="bi bi-check2-all"></i> মূল্যায়ন চূড়ান্ত করুন</button>
                    </g:form>
                </g:if>
                <g:elseif test="${appraisal.managerRating}">
                    <div class="mb-2 fs-5"><hrms:stars value="${appraisal.managerRating}"/> <span class="fs-6">${ratingLabels[appraisal.managerRating]}</span></div>
                    <p style="white-space: pre-line;">${appraisal.managerComment ?: '(মন্তব্য নেই)'}</p>
                    <div class="small text-muted">${appraisal.reviewedBy} &middot; <hrms:dateTime value="${appraisal.managerSubmittedAt}"/></div>
                </g:elseif>
                <g:else><div class="text-muted">ম্যানেজারের মূল্যায়ন বাকি।</div></g:else>
            </div>
        </div>
    </div>
</div>

<div class="card">
    <div class="card-header bg-white d-flex justify-content-between">
        <span class="fw-semibold">এই সময়ের লক্ষ্য / KPI</span>
        <span class="small">
            লক্ষ্য অর্জন (weighted):
            <strong>${appraisal.status == 'COMPLETED' ? (appraisal.goalScore != null ? appraisal.goalScore + '%' : '—') : (liveScore != null ? liveScore + '%' : '—')}</strong>
        </span>
    </div>
    <div class="table-responsive">
        <table class="table table-sm align-middle mb-0">
            <thead class="table-light"><tr><th>লক্ষ্য</th><th style="min-width:140px;">অগ্রগতি</th><th class="text-center">Weight</th><th>শেষ তারিখ</th><th>অবস্থা</th></tr></thead>
            <tbody>
            <g:each in="${goals}" var="g">
                <tr>
                    <td>${g.title}</td>
                    <td><hrms:progressBar value="${g.progressPercent()}"/><div class="small text-muted">${g.progressPercent()}%</div></td>
                    <td class="text-center">${g.weight}</td>
                    <td><hrms:date value="${g.dueDate}"/></td>
                    <td><hrms:d8Badge kind="goal" status="${g.status}"/></td>
                </tr>
            </g:each>
            <g:if test="${!goals}"><tr><td colspan="5" class="text-center text-muted py-3">এই সময়ে শেষ-তারিখ পড়ে এমন কোনো লক্ষ্য নেই।</td></tr></g:if>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>

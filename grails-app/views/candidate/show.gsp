<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>${candidate.name}</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <div>
        <h1 class="page-title mb-1">${candidate.name}</h1>
        <hrms:recruitBadge kind="stage" status="${candidate.currentStage}"/>
        <span class="small text-muted ms-1">${candidate.jobPosting.title}</span>
    </div>
    <div class="d-flex gap-2">
        <g:if test="${candidate.hasResume}">
            <g:link action="resume" id="${candidate.id}" class="btn btn-outline-secondary"><i class="bi bi-file-earmark-arrow-down"></i> সিভি</g:link>
        </g:if>
        <g:link controller="recruitment" action="index" params="[posting: candidate.jobPosting.id]" class="btn btn-outline-secondary"><i class="bi bi-kanban"></i> বোর্ড</g:link>
    </div>
</div>

<div class="row g-3">
    <%-- ============ বাম: তথ্য, ধাপ, প্রত্যাখ্যান ============ --%>
    <div class="col-lg-4">
        <div class="card mb-3">
            <table class="table table-sm mb-0">
                <tbody>
                <tr><td>ইমেইল</td><td class="text-end">${candidate.email}</td></tr>
                <tr><td>ফোন</td><td class="text-end">${candidate.phone ?: '—'}</td></tr>
                <tr><td>সূত্র</td><td class="text-end"><hrms:sourceName value="${candidate.source}"/></td></tr>
                <tr><td>অভিজ্ঞতা</td><td class="text-end">${candidate.experienceYears ?: '—'} বছর</td></tr>
                <tr><td>প্রত্যাশিত বেতন</td><td class="text-end"><hrms:money value="${candidate.expectedSalary}"/></td></tr>
                <tr><td>পদ (চাহিদা)</td><td class="text-end"><g:link controller="requisition" action="show" id="${requisition.id}" class="text-decoration-none">#${requisition.id} ${requisition.designation}</g:link></td></tr>
                <tr><td>আবেদনের তারিখ</td><td class="text-end"><hrms:date value="${candidate.dateCreated}"/></td></tr>
                <tr><td>ধাপে আছে</td><td class="text-end"><hrms:dateTime value="${candidate.stageChangedAt}"/></td></tr>
                <g:if test="${candidate.note}"><tr><td>নোট</td><td class="text-end">${candidate.note}</td></tr></g:if>
                <g:if test="${candidate.rejectReason}"><tr class="table-danger"><td>প্রত্যাখ্যানের কারণ</td><td class="text-end">${candidate.rejectReason}</td></tr></g:if>
                <g:if test="${candidate.employee}"><tr class="table-success"><td>কর্মী</td><td class="text-end"><g:link controller="employee" action="show" id="${candidate.employee.id}" class="text-decoration-none">${candidate.employee.name}</g:link></td></tr></g:if>
                </tbody>
            </table>
        </div>

        <g:if test="${!(candidate.currentStage in ['HIRED', 'REJECTED'])}">
            <div class="card mb-3">
                <div class="card-header bg-white fw-semibold">ধাপ বদল</div>
                <div class="card-body d-flex flex-wrap gap-2">
                    <g:each in="${stages}" var="s">
                        <g:if test="${s != candidate.currentStage}">
                            <g:form controller="recruitment" action="move" id="${candidate.id}" method="POST" class="d-inline">
                                <input type="hidden" name="stage" value="${s}">
                                <button type="submit" class="btn btn-sm btn-outline-primary"><hrms:recruitBadge kind="stage" status="${s}"/></button>
                            </g:form>
                        </g:if>
                    </g:each>
                </div>
            </div>
            <div class="card mb-3">
                <div class="card-header bg-white fw-semibold text-danger">প্রত্যাখ্যান</div>
                <div class="card-body">
                    <g:form action="reject" id="${candidate.id}" method="POST" onsubmit="return confirm('প্রার্থীকে প্রত্যাখ্যান করবেন? চলমান ইন্টারভিউ/অফারও বাতিল হবে।');">
                        <input type="text" name="reason" maxlength="255" class="form-control mb-2" placeholder="কারণ (বাধ্যতামূলক)" required>
                        <button type="submit" class="btn btn-outline-danger w-100">প্রত্যাখ্যান</button>
                    </g:form>
                </div>
            </div>
        </g:if>
        <g:if test="${candidate.currentStage == 'REJECTED'}">
            <g:form action="reopen" id="${candidate.id}" method="POST" class="mb-3">
                <button type="submit" class="btn btn-outline-primary w-100"><i class="bi bi-arrow-counterclockwise"></i> আবার খুলুন (বাছাই ধাপে)</button>
            </g:form>
        </g:if>
    </div>

    <%-- ============ ডান: ইন্টারভিউ, অফার, রূপান্তর ============ --%>
    <div class="col-lg-8">
        <div class="card mb-3" id="interviews">
            <div class="card-header bg-white fw-semibold">ইন্টারভিউ</div>
            <div class="table-responsive">
                <table class="table table-sm align-middle mb-0">
                    <thead class="table-light"><tr><th>রাউন্ড</th><th>সময়</th><th>ইন্টারভিউয়ার</th><th>ধরন</th><th>অবস্থা</th><th>ফলাফল</th><th></th></tr></thead>
                    <tbody>
                    <g:each in="${interviews}" var="iv">
                        <tr>
                            <td>${iv.roundNo}. ${iv.roundName}</td>
                            <td><hrms:dateTime value="${iv.scheduledAt}"/><div class="small text-muted">${iv.durationMinutes} মিনিট</div></td>
                            <td>${iv.interviewer.name}</td>
                            <td>${iv.mode}<g:if test="${iv.location}"><div class="small text-muted">${iv.location}</div></g:if></td>
                            <td><hrms:recruitBadge kind="interview" status="${iv.status}"/></td>
                            <td>
                                <g:if test="${iv.status == 'COMPLETED'}">
                                    <hrms:recruitBadge kind="recommend" status="${iv.recommendation}"/> <span class="small">${iv.rating}/5</span>
                                    <div class="small text-muted" style="max-width: 260px;">${iv.feedback}</div>
                                </g:if>
                                <g:else>—</g:else>
                            </td>
                            <td class="text-end">
                                <g:if test="${iv.status == 'SCHEDULED'}">
                                    <g:form action="cancelInterview" id="${iv.id}" method="POST" class="d-inline" onsubmit="return confirm('ইন্টারভিউ বাতিল করবেন?');">
                                        <button type="submit" class="btn btn-sm btn-outline-secondary" title="বাতিল"><i class="bi bi-x-lg"></i></button>
                                    </g:form>
                                </g:if>
                            </td>
                        </tr>
                        <g:if test="${iv.status == 'SCHEDULED'}">
                            <tr>
                                <td colspan="7" class="bg-light">
                                    <details>
                                        <summary class="small">ফিডব্যাক দিন</summary>
                                        <g:form action="feedback" id="${iv.id}" method="POST" class="row g-2 mt-1">
                                            <div class="col-md-2">
                                                <select name="rating" class="form-select form-select-sm" required>
                                                    <option value="">রেটিং</option>
                                                    <g:each in="${5..1}" var="n"><option value="${n}">${n} / 5</option></g:each>
                                                </select>
                                            </div>
                                            <div class="col-md-3">
                                                <select name="recommendation" class="form-select form-select-sm" required>
                                                    <option value="">সুপারিশ</option>
                                                    <g:each in="${recommendations}" var="r"><option value="${r}">${r}</option></g:each>
                                                </select>
                                            </div>
                                            <div class="col-md-5"><input type="text" name="feedback" maxlength="1500" class="form-control form-control-sm" placeholder="ফিডব্যাক" required></div>
                                            <div class="col-md-2"><button type="submit" class="btn btn-sm btn-primary w-100">জমা</button></div>
                                        </g:form>
                                    </details>
                                </td>
                            </tr>
                        </g:if>
                    </g:each>
                    <g:if test="${!interviews}"><tr><td colspan="7" class="text-center text-muted py-3">কোনো ইন্টারভিউ নেই।</td></tr></g:if>
                    </tbody>
                </table>
            </div>

            <g:if test="${candidate.currentStage in ['SCREENING', 'INTERVIEW']}">
                <div class="card-body border-top">
                    <div class="fw-semibold mb-2">নতুন ইন্টারভিউ নির্ধারণ</div>
                    <g:form action="scheduleInterview" id="${candidate.id}" method="POST" class="row g-2">
                        <div class="col-md-4"><input type="text" name="roundName" maxlength="60" class="form-control" value="${defaultRoundName}" placeholder="রাউন্ডের নাম" required></div>
                        <div class="col-md-4"><input type="datetime-local" name="scheduledAt" class="form-control" value="${defaultInterviewAt}" required></div>
                        <div class="col-md-2"><input type="number" name="durationMinutes" min="15" max="240" step="5" class="form-control" value="45" title="মিনিট" required></div>
                        <div class="col-md-2">
                            <select name="mode" class="form-select"><g:each in="${modes}" var="m"><option value="${m}">${m}</option></g:each></select>
                        </div>
                        <div class="col-md-5">
                            <select name="interviewer" class="form-select" required>
                                <option value="">— ইন্টারভিউয়ার —</option>
                                <g:each in="${employees}" var="e"><option value="${e.id}">${e.name} (${e.designation})</option></g:each>
                            </select>
                        </div>
                        <div class="col-md-5"><input type="text" name="location" maxlength="150" class="form-control" placeholder="কক্ষ / মিটিং লিংক / ফোন"></div>
                        <div class="col-md-2"><button type="submit" class="btn btn-primary w-100">নির্ধারণ</button></div>
                    </g:form>
                    <div class="small text-muted mt-2">সময় ৭ দিনের বেশি আগের হলে নেওয়া হবে না; ইন্টারভিউয়ের সময় হওয়ার পর ফিডব্যাক দেওয়া যায়।</div>
                </div>
            </g:if>
        </div>

        <%-- ---------- অফার ---------- --%>
        <g:if test="${offer || candidate.currentStage in ['INTERVIEW', 'OFFER']}">
            <div class="card mb-3" id="offer">
                <div class="card-header bg-white fw-semibold">অফার</div>
                <g:if test="${offer}">
                    <table class="table table-sm mb-0">
                        <tbody>
                        <tr><td>অবস্থা</td><td class="text-end">
                            <hrms:recruitBadge kind="offer" status="${offer.status}"/>
                            <g:if test="${offer.expired}"><span class="badge text-bg-danger">মেয়াদোত্তীর্ণ</span></g:if>
                        </td></tr>
                        <tr><td>পদ / বিভাগ</td><td class="text-end">${offer.designation} &middot; ${offer.department}</td></tr>
                        <tr><td>মাসিক বেতন (gross)</td><td class="text-end fw-semibold"><hrms:money value="${offer.salary}"/></td></tr>
                        <tr><td>যোগদান</td><td class="text-end"><hrms:date value="${offer.joinDate}"/></td></tr>
                        <tr><td>পরীক্ষাকাল</td><td class="text-end">${offer.probationMonths ? offer.probationMonths + ' মাস' : 'নেই'} &rarr; <hrms:label value="${offer.employeeType}"/></td></tr>
                        <tr><td>অফারের মেয়াদ</td><td class="text-end"><hrms:date value="${offer.validUntil}"/></td></tr>
                        <g:if test="${offer.decisionNote}"><tr><td>মন্তব্য</td><td class="text-end">${offer.decisionNote}</td></tr></g:if>
                        </tbody>
                    </table>
                    <div class="card-body border-top d-flex flex-wrap gap-2">
                        <g:link action="offerLetter" id="${offer.id}" target="_blank" class="btn btn-outline-secondary"><i class="bi bi-printer"></i> অফার লেটার</g:link>
                        <g:if test="${offer.status == 'SENT'}">
                            <g:form action="respondOffer" id="${offer.id}" method="POST" class="d-inline">
                                <input type="hidden" name="decision" value="ACCEPT">
                                <button type="submit" class="btn btn-success"><i class="bi bi-check-lg"></i> প্রার্থী গ্রহণ করেছেন</button>
                            </g:form>
                        </g:if>
                    </div>
                    <g:if test="${offer.status == 'SENT'}">
                        <div class="card-body border-top">
                            <g:form action="respondOffer" id="${offer.id}" method="POST" class="row g-2">
                                <input type="hidden" name="decision" value="DECLINE">
                                <div class="col-md-9"><input type="text" name="note" maxlength="255" class="form-control" placeholder="প্রার্থী প্রত্যাখ্যান করলে কারণ" required></div>
                                <div class="col-md-3"><button type="submit" class="btn btn-outline-danger w-100">প্রত্যাখ্যান</button></div>
                            </g:form>
                        </div>
                    </g:if>
                    <g:if test="${offer.live && !candidate.employee}">
                        <div class="card-body border-top">
                            <g:form action="withdrawOffer" id="${offer.id}" method="POST" class="row g-2" onsubmit="return confirm('অফার প্রত্যাহার করবেন?');">
                                <div class="col-md-9"><input type="text" name="note" maxlength="255" class="form-control" placeholder="প্রত্যাহারের কারণ" required></div>
                                <div class="col-md-3"><button type="submit" class="btn btn-outline-warning w-100">অফার প্রত্যাহার</button></div>
                            </g:form>
                        </div>
                    </g:if>
                </g:if>

                <g:if test="${(!offer || !offer.live) && candidate.currentStage in ['INTERVIEW', 'OFFER']}">
                    <div class="card-body border-top">
                        <g:if test="${recommended}">
                            <g:link action="createOffer" id="${candidate.id}" class="btn btn-primary"><i class="bi bi-envelope-paper"></i> অফার তৈরি করুন</g:link>
                        </g:if>
                        <g:else><div class="small text-muted">অফার দিতে অন্তত একটি সম্পন্ন ইন্টারভিউয়ে HIRE/HOLD সুপারিশ লাগবে।</div></g:else>
                    </div>
                </g:if>
            </div>
        </g:if>

        <%-- ---------- কর্মীতে রূপান্তর ---------- --%>
        <g:if test="${offer?.status == 'ACCEPTED' && candidate.currentStage == 'OFFER' && !candidate.employee}">
            <div class="card border-success mb-3" id="convert">
                <div class="card-header bg-success text-white fw-semibold">কর্মীতে রূপান্তর</div>
                <div class="card-body">
                    <p class="small text-muted mb-3">রূপান্তরে কর্মীর প্রোফাইল, চাকরির ইতিহাস, বেতন কাঠামো, অনবোর্ডিং চেকলিস্ট, পরীক্ষাকাল এবং (সিভি থাকলে) নথি আপনা-আপনি তৈরি হবে।</p>
                    <g:form action="convert" id="${candidate.id}" method="POST" class="row g-2" onsubmit="return confirm('এই প্রার্থীকে কর্মী হিসেবে যোগ করবেন?');">
                        <div class="col-md-5">
                            <label class="form-label small mb-1" for="manager">রিপোর্টিং ম্যানেজার</label>
                            <select name="manager" id="manager" class="form-select">
                                <option value="">— নেই —</option>
                                <g:each in="${employees}" var="e"><option value="${e.id}">${e.name}</option></g:each>
                            </select>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label small mb-1" for="grade">গ্রেড</label>
                            <select name="grade" id="grade" class="form-select">
                                <option value="">— নেই —</option>
                                <g:each in="${grades}" var="g"><option value="${g.id}">${g.name}</option></g:each>
                            </select>
                        </div>
                        <div class="col-md-3 d-flex align-items-end"><button type="submit" class="btn btn-success w-100"><i class="bi bi-person-check"></i> রূপান্তর</button></div>
                    </g:form>
                </div>
            </div>
        </g:if>
    </div>
</div>
</body>
</html>

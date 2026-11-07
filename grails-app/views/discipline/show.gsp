<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>কেস #${c.id}</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <div>
        <h1 class="page-title mb-1">${c.subject}</h1>
        <hrms:d9Badge kind="discipline" status="${c.status}"/> <span class="badge text-bg-light border"><hrms:d9Name value="${c.caseType}"/></span>
        <span class="small text-muted ms-1">কেস #${c.id}</span>
    </div>
    <g:link action="index" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> তালিকা</g:link>
</div>

<div class="row g-3">
    <div class="col-lg-6">
        <div class="card mb-3">
            <table class="table table-sm mb-0"><tbody>
            <tr><td>কর্মী</td><td class="text-end">${c.employee.name} (${c.employee.department})</td></tr>
            <tr><td>ঘটনার তারিখ</td><td class="text-end"><hrms:date value="${c.incidentDate}"/></td></tr>
            <tr><td>খুলেছেন</td><td class="text-end">${c.raisedBy}</td></tr>
            <tr><td>খোলার সময়</td><td class="text-end"><hrms:dt value="${c.dateCreated}"/></td></tr>
            </tbody></table>
        </div>
        <div class="card"><div class="card-header bg-white fw-semibold">বিবরণ</div><div class="card-body" style="white-space: pre-line;">${c.description}</div></div>
    </div>

    <div class="col-lg-6">
        <div class="card mb-3">
            <div class="card-header bg-white fw-semibold">কর্মীর জবাব</div>
            <div class="card-body">
                <g:if test="${c.employeeResponse}">
                    <div style="white-space: pre-line;">${c.employeeResponse}</div>
                    <div class="small text-muted mt-1"><hrms:dt value="${c.respondedAt}"/></div>
                </g:if>
                <g:else><span class="text-muted">এখনো জবাব আসেনি।</span></g:else>
            </div>
        </div>

        <g:if test="${canRespond}">
            <div class="card mb-3 border-warning">
                <div class="card-header bg-white fw-semibold">আপনার জবাব দিন</div>
                <div class="card-body">
                    <g:form action="reply" id="${c.id}" method="POST">
                        <textarea name="response" rows="4" maxlength="2000" class="form-control mb-2" required></textarea>
                        <button type="submit" class="btn btn-primary w-100">জবাব জমা দিন</button>
                    </g:form>
                </div>
            </div>
        </g:if>

        <g:if test="${c.status == 'CLOSED'}">
            <div class="card mb-3">
                <div class="card-header bg-white fw-semibold">সিদ্ধান্ত</div>
                <div class="card-body">
                    <div><strong><hrms:d9Name value="${c.outcome}"/></strong></div>
                    <g:if test="${c.outcomeNote}"><div style="white-space: pre-line;">${c.outcomeNote}</div></g:if>
                    <div class="small text-muted mt-1">${c.closedBy} - <hrms:dt value="${c.closedAt}"/></div>
                </div>
            </div>
        </g:if>

        <g:if test="${waitingResponse && !own}"><div class="alert alert-warning small">কারণ দর্শানোর জবাবের অপেক্ষায় - জবাব না আসা পর্যন্ত সিদ্ধান্ত দেওয়া যাবে না।</div></g:if>

        <g:if test="${canClose}">
            <div class="card mb-3">
                <div class="card-header bg-white fw-semibold">HR এর সিদ্ধান্ত</div>
                <div class="card-body">
                    <g:form action="close" id="${c.id}" method="POST">
                        <select name="outcome" class="form-select mb-2" required>
                            <option value="">-- সিদ্ধান্ত --</option>
                            <g:each in="${outcomes}" var="o"><option value="${o}"><hrms:d9Name value="${o}"/></option></g:each>
                        </select>
                        <textarea name="note" rows="3" maxlength="500" class="form-control mb-2" placeholder="কারণ/বিবরণ (কোনো ব্যবস্থা না হলে ঐচ্ছিক)"></textarea>
                        <button type="submit" class="btn btn-danger w-100" onclick="return confirm('সিদ্ধান্ত দিয়ে কেস বন্ধ করবেন?');">কেস বন্ধ করুন</button>
                    </g:form>
                </div>
            </div>
        </g:if>
    </div>
</div>
</body>
</html>

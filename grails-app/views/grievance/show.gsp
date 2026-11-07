<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>অভিযোগ #${grv.id}</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <div>
        <h1 class="page-title mb-1">${grv.subject}</h1>
        <hrms:d9Badge kind="grievance" status="${grv.status}"/> <span class="badge text-bg-light border"><hrms:d9Name value="${grv.category}"/></span>
        <span class="small text-muted ms-1">#${grv.id} · ${grv.employee.name} · <hrms:date value="${grv.dateCreated}"/></span>
    </div>
    <g:link action="index" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> তালিকা</g:link>
</div>

<div class="row g-3">
    <div class="col-lg-6">
        <div class="card mb-3"><div class="card-header bg-white fw-semibold">বিবরণ</div><div class="card-body" style="white-space: pre-line;">${grv.description}</div></div>
        <g:if test="${grv.resolution}">
            <div class="card mb-3 border-${grv.status == 'RESOLVED' ? 'success' : 'danger'}">
                <div class="card-header bg-white fw-semibold">${grv.status == 'RESOLVED' ? 'সমাধান' : 'প্রত্যাখ্যানের কারণ'}</div>
                <div class="card-body" style="white-space: pre-line;">${grv.resolution}<div class="small text-muted mt-1">${grv.handledBy} - <hrms:dt value="${grv.resolvedAt}"/></div></div>
            </div>
        </g:if>
        <g:if test="${hr && open}">
            <div class="card mb-3">
                <div class="card-header bg-white fw-semibold">HR এর পদক্ষেপ</div>
                <div class="card-body">
                    <g:form action="update" id="${grv.id}" method="POST">
                        <textarea name="resolution" rows="3" maxlength="1000" class="form-control mb-2" placeholder="সমাধান / প্রত্যাখ্যানের কারণ (পর্যালোচনায় নিতে ঐচ্ছিক)"></textarea>
                        <div class="d-flex flex-wrap gap-2">
                            <g:if test="${grv.status == 'OPEN'}"><button type="submit" name="target" value="IN_REVIEW" class="btn btn-outline-primary flex-fill">পর্যালোচনায় নিন</button></g:if>
                            <button type="submit" name="target" value="RESOLVED" class="btn btn-success flex-fill">সমাধান হয়েছে</button>
                            <button type="submit" name="target" value="REJECTED" class="btn btn-outline-danger flex-fill">প্রত্যাখ্যান</button>
                        </div>
                    </g:form>
                </div>
            </div>
        </g:if>
    </div>

    <div class="col-lg-6">
        <div class="card">
            <div class="card-header bg-white fw-semibold">আলোচনা</div>
            <ul class="list-group list-group-flush">
                <g:each in="${notes}" var="n">
                    <li class="list-group-item ${n.fromHr ? 'bg-light' : ''}">
                        <div class="small"><strong>${n.author}</strong> <g:if test="${n.fromHr}"><span class="badge text-bg-primary">HR</span></g:if> · <span class="text-muted"><hrms:dt value="${n.dateCreated}"/></span></div>
                        <div style="white-space: pre-line;">${n.note}</div>
                    </li>
                </g:each>
                <g:if test="${!notes}"><li class="list-group-item text-muted small">এখনো কোনো মন্তব্য নেই।</li></g:if>
            </ul>
            <g:if test="${open || hr}">
                <div class="card-body border-top">
                    <g:form action="note" id="${grv.id}" method="POST">
                        <textarea name="note" rows="2" maxlength="1000" class="form-control mb-2" placeholder="মন্তব্য লিখুন" required></textarea>
                        <button type="submit" class="btn btn-sm btn-primary">মন্তব্য পাঠান</button>
                    </g:form>
                </div>
            </g:if>
        </div>
    </div>
</div>
</body>
</html>

<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>অডিট ট্রেইল</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="page-title mb-0">অডিট ট্রেইল</h1>
    <g:link action="emails" class="btn btn-outline-secondary"><i class="bi bi-envelope"></i> ইমেইল লগ</g:link>
</div>
<div class="card mb-3">
    <div class="card-body">
        <g:form action="index" method="GET" class="row g-2">
            <div class="col-md-3">
                <select name="actor" class="form-select">
                    <option value="">সব ব্যবহারকারী</option>
                    <g:each in="${actors}" var="a"><option value="${a}" ${f.actor == a ? 'selected' : ''}>${a}</option></g:each>
                </select>
            </div>
            <div class="col-md-3"><input type="text" name="act" value="${f.act}" class="form-control" placeholder="কাজ (যেমন SEPARATION)"></div>
            <div class="col-md-2"><input type="date" name="from" value="${f.from}" class="form-control"></div>
            <div class="col-md-2"><input type="date" name="to" value="${f.to}" class="form-control"></div>
            <div class="col-md-2"><button type="submit" class="btn btn-primary w-100"><i class="bi bi-search"></i> খুঁজুন</button></div>
        </g:form>
    </div>
</div>
<div class="card">
    <div class="table-responsive">
        <table class="table table-sm align-middle mb-0">
            <thead class="table-light"><tr><th>সময়</th><th>ব্যবহারকারী</th><th>কাজ</th><th>বিষয়</th><th>বিবরণ</th></tr></thead>
            <tbody>
            <g:each in="${rows}" var="r">
                <tr>
                    <td class="text-nowrap"><hrms:dt value="${r.dateCreated}"/></td>
                    <td>${r.actor}</td>
                    <td><code>${r.action}</code></td>
                    <td class="small">${r.entityType}<g:if test="${r.entityId}"> #${r.entityId}</g:if></td>
                    <td class="small">${r.summary}</td>
                </tr>
            </g:each>
            <g:if test="${!rows}"><tr><td colspan="5" class="text-center text-muted py-4">কোনো রেকর্ড নেই।</td></tr></g:if>
            </tbody>
        </table>
    </div>
    <div class="card-footer bg-white d-flex justify-content-between small">
        <span>মোট ${total}টি</span>
        <span>
            <g:if test="${offset > 0}"><g:link action="index" params="[actor: f.actor, act: f.act, from: f.from, to: f.to, offset: Math.max(0, offset - page)]">&laquo; আগের</g:link></g:if>
            <g:if test="${offset + page < total}"> <g:link action="index" params="[actor: f.actor, act: f.act, from: f.from, to: f.to, offset: offset + page]">পরের &raquo;</g:link></g:if>
        </span>
    </div>
</div>
</body>
</html>

<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>নিয়োগ - Kanban বোর্ড</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="page-title mb-0">নিয়োগ বোর্ড <span class="badge text-bg-secondary fs-6">${board.total}</span></h1>
    <div class="d-flex flex-wrap gap-2">
        <g:link controller="requisition" action="index" class="btn btn-outline-secondary"><i class="bi bi-person-lines-fill"></i> জনবল চাহিদা
            <g:if test="${overview.pendingRequisitions}"><span class="badge text-bg-warning">${overview.pendingRequisitions}</span></g:if>
        </g:link>
        <g:link action="postings" class="btn btn-outline-secondary"><i class="bi bi-megaphone"></i> বিজ্ঞপ্তি <span class="badge text-bg-light border">${overview.openPostings} খোলা</span></g:link>
        <g:link controller="candidate" action="create" params="${posting ? [posting: posting.id] : [:]}" class="btn btn-primary"><i class="bi bi-plus-lg"></i> নতুন প্রার্থী</g:link>
    </div>
</div>

<g:form action="index" method="GET" class="row g-2 mb-3">
    <div class="col-md-5">
        <select name="posting" class="form-select" onchange="this.form.submit()">
            <option value="">সব বিজ্ঞপ্তি</option>
            <g:each in="${postings}" var="p"><option value="${p.id}" ${posting?.id == p.id ? 'selected' : ''}>${p.title} (${p.status == 'OPEN' ? 'খোলা' : 'বন্ধ'})</option></g:each>
        </select>
    </div>
    <div class="col-md-7 small text-muted d-flex align-items-center">
        কার্ড টেনে এক ধাপ থেকে আরেক ধাপে নিন। "অফার" এ নিতে অন্তত একটি ইন্টারভিউয়ে HIRE/HOLD সুপারিশ লাগে; "নিয়োগ" হয় শুধু কর্মীতে রূপান্তরে।
    </div>
</g:form>

<div class="kanban" data-move-url="${createLink(action: 'move')}">
    <g:each in="${stages}" var="stage">
        <div class="kanban-col" data-stage="${stage}">
            <div class="kanban-head">
                <span><hrms:recruitBadge kind="stage" status="${stage}"/></span>
                <span class="badge text-bg-light border kanban-count">${board.columns[stage].size()}</span>
            </div>
            <div class="kanban-body">
                <g:each in="${board.columns[stage]}" var="c">
                    <div class="kanban-card ${stage == 'HIRED' ? 'readonly' : ''}" data-id="${c.id}" draggable="${stage == 'HIRED' ? 'false' : 'true'}">
                        <div class="name"><g:link controller="candidate" action="show" id="${c.id}" class="text-decoration-none">${c.name}</g:link></div>
                        <g:if test="${!posting}"><div class="small text-muted">${c.jobPosting.title}</div></g:if>
                        <div class="small text-muted"><hrms:sourceName value="${c.source}"/><g:if test="${c.expectedSalary}"> &middot; <hrms:money value="${c.expectedSalary}"/></g:if></div>
                        <g:if test="${board.nextInterview[c.id]}">
                            <div class="small mt-1"><i class="bi bi-calendar-event"></i> <hrms:dateTime value="${board.nextInterview[c.id].scheduledAt}"/></div>
                        </g:if>
                    </div>
                </g:each>
            </div>
        </div>
    </g:each>
</div>

<g:if test="${board.rejected}">
    <details class="mt-3">
        <summary class="text-muted">প্রত্যাখ্যাত প্রার্থী (${board.rejected.size()})</summary>
        <div class="card mt-2">
            <ul class="list-group list-group-flush small">
                <g:each in="${board.rejected}" var="c">
                    <li class="list-group-item d-flex justify-content-between gap-2">
                        <span><g:link controller="candidate" action="show" id="${c.id}" class="text-decoration-none fw-semibold">${c.name}</g:link> <span class="text-muted">&middot; ${c.jobPosting.title}</span></span>
                        <span class="text-muted">${c.rejectReason}</span>
                    </li>
                </g:each>
            </ul>
        </div>
    </details>
</g:if>

<script>
    (function () {
        var board = document.querySelector('.kanban');
        var url = board.getAttribute('data-move-url');

        function toast(msg, css) {
            var c = document.querySelector('.toast-container');
            var el = document.createElement('div');
            el.className = 'toast align-items-center text-bg-' + css + ' border-0';
            el.innerHTML = '<div class="d-flex"><div class="toast-body"></div></div>';
            el.querySelector('.toast-body').textContent = msg;
            c.appendChild(el);
            new bootstrap.Toast(el, {delay: 4500}).show();
        }

        function recount() {
            document.querySelectorAll('.kanban-col').forEach(function (col) {
                col.querySelector('.kanban-count').textContent = col.querySelectorAll('.kanban-card').length;
            });
        }

        document.querySelectorAll('.kanban-card[draggable=true]').forEach(function (card) {
            card.addEventListener('dragstart', function (e) {
                e.dataTransfer.setData('text/plain', card.getAttribute('data-id'));
                card.classList.add('dragging');
            });
            card.addEventListener('dragend', function () { card.classList.remove('dragging'); });
        });

        document.querySelectorAll('.kanban-col').forEach(function (col) {
            if (col.getAttribute('data-stage') === 'HIRED') { col.classList.add('no-drop'); return; }
            col.addEventListener('dragover', function (e) { e.preventDefault(); col.classList.add('drag-over'); });
            col.addEventListener('dragleave', function () { col.classList.remove('drag-over'); });
            col.addEventListener('drop', function (e) {
                e.preventDefault();
                col.classList.remove('drag-over');
                var id = e.dataTransfer.getData('text/plain');
                var card = document.querySelector('.kanban-card[data-id="' + id + '"]');
                if (!card || card.closest('.kanban-col') === col) { return; }
                var body = new URLSearchParams();
                body.append('id', id);
                body.append('stage', col.getAttribute('data-stage'));
                body.append('ajax', '1');
                fetch(url, {method: 'POST', headers: {'Content-Type': 'application/x-www-form-urlencoded'}, body: body, credentials: 'same-origin'})
                    .then(function (r) { return r.json(); })
                    .then(function (res) {
                        if (res.ok) {
                            col.querySelector('.kanban-body').appendChild(card);
                            recount();
                            toast(res.message, 'success');
                        } else {
                            card.classList.add('shake');
                            setTimeout(function () { card.classList.remove('shake'); }, 400);
                            toast(res.message, 'danger');
                        }
                    })
                    .catch(function () { toast('সংযোগে সমস্যা - পেজ রিফ্রেশ করে আবার চেষ্টা করুন।', 'danger'); });
            });
        });
    })();
</script>
</body>
</html>

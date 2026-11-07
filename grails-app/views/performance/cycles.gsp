<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>মূল্যায়ন চক্র</title>
</head>
<body>
<h1 class="page-title">পারফরম্যান্স</h1>
<g:render template="tabs" model="[active: 'cycles']"/>

<div class="row g-3">
    <div class="col-lg-8">
        <div class="card">
            <div class="table-responsive">
                <table class="table align-middle mb-0">
                    <thead class="table-light"><tr><th>চক্র</th><th>সময়কাল</th><th>চূড়ান্ত তারিখ</th><th>অগ্রগতি</th><th>অবস্থা</th><th></th></tr></thead>
                    <tbody>
                    <g:each in="${cycles}" var="c">
                        <g:set var="st" value="${stats[c.id]}"/>
                        <tr>
                            <td class="fw-semibold">${c.name}</td>
                            <td class="small"><hrms:date value="${c.startDate}"/> - <hrms:date value="${c.endDate}"/></td>
                            <td><hrms:date value="${c.dueDate}"/></td>
                            <td>
                                <g:if test="${st.total}">
                                    ${st.done} / ${st.total} সম্পন্ন
                                    <hrms:progressBar value="${st.total ? st.done * 100 / st.total : 0}"/>
                                    <g:if test="${st.self}"><div class="small text-muted">${st.self}টি ম্যানেজারের রিভিউয়ের অপেক্ষায়</div></g:if>
                                </g:if>
                                <g:else><span class="text-muted">—</span></g:else>
                            </td>
                            <td><hrms:d8Badge kind="cycle" status="${c.status}"/></td>
                            <td class="text-end text-nowrap">
                                <g:if test="${c.status == 'DRAFT'}">
                                    <g:form action="openCycle" id="${c.id}" method="POST" class="d-inline" onsubmit="return confirm('চক্র চালু করলে সব সক্রিয় কর্মীর মূল্যায়ন তৈরি হবে। চালু করবেন?');">
                                        <button type="submit" class="btn btn-sm btn-success">চালু করুন</button>
                                    </g:form>
                                    <g:form action="deleteCycle" id="${c.id}" method="POST" class="d-inline" onsubmit="return confirm('চক্র মুছবেন?');">
                                        <button type="submit" class="btn btn-sm btn-outline-danger" title="মুছুন"><i class="bi bi-trash"></i></button>
                                    </g:form>
                                </g:if>
                                <g:if test="${c.status == 'OPEN'}">
                                    <g:link action="appraisals" params="[cycle: c.id]" class="btn btn-sm btn-outline-primary">দেখুন</g:link>
                                    <g:form action="syncCycle" id="${c.id}" method="POST" class="d-inline"><button type="submit" class="btn btn-sm btn-outline-secondary" title="চক্র চলাকালে যোগ দেওয়া কর্মী যোগ করুন">নতুন কর্মী যোগ</button></g:form>
                                    <g:form action="closeCycle" id="${c.id}" method="POST" class="d-inline" onsubmit="return confirm('চক্র বন্ধ করলে আর কোনো মূল্যায়ন সম্পাদনা করা যাবে না। বন্ধ করবেন?');">
                                        <button type="submit" class="btn btn-sm btn-outline-dark">বন্ধ করুন</button>
                                    </g:form>
                                </g:if>
                                <g:if test="${c.status == 'CLOSED'}">
                                    <g:link action="appraisals" params="[cycle: c.id]" class="btn btn-sm btn-outline-secondary">দেখুন</g:link>
                                </g:if>
                            </td>
                        </tr>
                    </g:each>
                    <g:if test="${!cycles}"><tr><td colspan="6" class="text-center text-muted py-4">কোনো চক্র নেই।</td></tr></g:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    <div class="col-lg-4">
        <div class="card">
            <div class="card-header bg-white fw-semibold">নতুন চক্র</div>
            <div class="card-body">
                <g:form action="saveCycle" method="POST">
                    <div class="mb-3">
                        <label class="form-label" for="cName">নাম <span class="text-danger">*</span></label>
                        <input type="text" id="cName" name="name" maxlength="100" value="${defaultYear} বার্ষিক মূল্যায়ন" class="form-control" required>
                    </div>
                    <div class="row g-2 mb-3">
                        <div class="col-6">
                            <label class="form-label" for="cStart">সময়কাল শুরু <span class="text-danger">*</span></label>
                            <input type="date" id="cStart" name="startDate" value="${defaultYear}-01-01" class="form-control" required>
                        </div>
                        <div class="col-6">
                            <label class="form-label" for="cEnd">সময়কাল শেষ <span class="text-danger">*</span></label>
                            <input type="date" id="cEnd" name="endDate" value="${defaultYear}-12-31" class="form-control" required>
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label" for="cDue">মূল্যায়ন শেষ করার চূড়ান্ত তারিখ <span class="text-danger">*</span></label>
                        <input type="date" id="cDue" name="dueDate" value="${defaultYear + 1}-01-31" class="form-control" required>
                    </div>
                    <button type="submit" class="btn btn-primary w-100"><i class="bi bi-plus-lg"></i> চক্র তৈরি</button>
                </g:form>
                <div class="small text-muted mt-3">চক্র চালু করলে সময়কালের শেষ তারিখের আগে যোগ দেওয়া সব সক্রিয় কর্মীর মূল্যায়ন তৈরি হয়। দুটি চক্রের সময়কাল মিলতে পারে না।</div>
            </div>
        </div>
    </div>
</div>
</body>
</html>

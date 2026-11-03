<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>অনবোর্ডিং - ${employee.name}</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <div>
        <h1 class="page-title mb-1">${employee.name}</h1>
        <div class="text-muted">${employee.designation} &middot; ${employee.department} &middot; যোগদান <hrms:date value="${employee.joinDate}"/>
            &middot; <hrms:label value="${employee.employeeType}"/></div>
    </div>
    <div class="d-flex gap-2">
        <g:link controller="employee" action="show" id="${employee.id}" class="btn btn-outline-secondary"><i class="bi bi-person"></i> প্রোফাইল</g:link>
        <g:link action="index" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> তালিকা</g:link>
    </div>
</div>

<div class="row g-3">
    <%-- ============ চেকলিস্ট ============ --%>
    <div class="col-xl-7">
        <div class="card mb-3" id="checklist">
            <div class="card-header bg-white d-flex justify-content-between align-items-center">
                <span class="fw-semibold">চেকলিস্ট <span class="text-muted small">(${progress.done}/${progress.total})</span></span>
                <g:if test="${!tasks}">
                    <g:form action="start" id="${employee.id}" method="POST"><button type="submit" class="btn btn-sm btn-primary">টেমপ্লেট থেকে তৈরি করুন</button></g:form>
                </g:if>
            </div>
            <div class="card-body pb-0">
                <div class="progress thin mb-3"><div class="progress-bar ${progress.percent == 100 ? 'bg-success' : ''}" style="width: ${progress.percent}%"></div></div>
            </div>
            <div class="table-responsive">
                <table class="table table-sm align-middle mb-0">
                    <thead class="table-light"><tr><th>কাজ</th><th>দায়িত্ব</th><th>শেষ তারিখ</th><th></th></tr></thead>
                    <tbody>
                    <g:each in="${tasks}" var="t">
                        <tr class="${t.done ? 'table-success' : (t.overdue ? 'table-danger' : '')}">
                            <td>
                                <span class="${t.done ? 'text-decoration-line-through text-muted' : 'fw-semibold'}">${t.title}</span>
                                <div class="small text-muted"><hrms:label value="${t.category}"/><g:if test="${t.done}"> &middot; ${t.doneBy}, <hrms:dateTime value="${t.doneAt}"/></g:if><g:if test="${t.remarks}"> &middot; ${t.remarks}</g:if></div>
                            </td>
                            <td>${t.ownerRole}</td>
                            <td><hrms:date value="${t.dueDate}"/></td>
                            <td class="text-end text-nowrap">
                                <g:if test="${!t.done}">
                                    <g:form action="complete" id="${t.id}" method="POST" class="d-inline-flex gap-1">
                                        <input type="text" name="remarks" maxlength="255" class="form-control form-control-sm" placeholder="মন্তব্য" style="width: 110px;">
                                        <button type="submit" class="btn btn-sm btn-success" title="সম্পন্ন"><i class="bi bi-check-lg"></i></button>
                                    </g:form>
                                    <g:form action="deleteTask" id="${t.id}" method="POST" class="d-inline" onsubmit="return confirm('কাজটি মুছবেন?');">
                                        <button type="submit" class="btn btn-sm btn-outline-secondary" title="মুছুন"><i class="bi bi-trash"></i></button>
                                    </g:form>
                                </g:if>
                                <g:else>
                                    <g:form action="reopen" id="${t.id}" method="POST" class="d-inline">
                                        <button type="submit" class="btn btn-sm btn-outline-secondary" title="আবার বাকি">আবার বাকি</button>
                                    </g:form>
                                </g:else>
                            </td>
                        </tr>
                    </g:each>
                    <g:if test="${!tasks}"><tr><td colspan="4" class="text-center text-muted py-3">চেকলিস্টে কোনো কাজ নেই।</td></tr></g:if>
                    </tbody>
                </table>
            </div>
            <div class="card-body border-top">
                <g:form action="addTask" id="${employee.id}" method="POST" class="row g-2">
                    <div class="col-md-4"><input type="text" name="title" maxlength="150" class="form-control" placeholder="নতুন কাজ" required></div>
                    <div class="col-md-2"><select name="category" class="form-select"><g:each in="${categories}" var="c"><option value="${c}">${c}</option></g:each></select></div>
                    <div class="col-md-2"><select name="ownerRole" class="form-select"><g:each in="${owners}" var="o"><option value="${o}">${o}</option></g:each></select></div>
                    <div class="col-md-2"><input type="date" name="dueDate" class="form-control" value="${today.plusDays(7)}"></div>
                    <div class="col-md-2"><button type="submit" class="btn btn-outline-primary w-100">যোগ</button></div>
                </g:form>
            </div>
        </div>
    </div>

    <div class="col-xl-5">
        <%-- ============ পরীক্ষাকাল ============ --%>
        <div class="card mb-3" id="probation">
            <div class="card-header bg-white fw-semibold">পরীক্ষাকাল (Probation)</div>
            <g:if test="${probation}">
                <table class="table table-sm mb-0">
                    <tbody>
                    <tr><td>অবস্থা</td><td class="text-end"><hrms:recruitBadge kind="probation" status="${probation.phase()}"/></td></tr>
                    <tr><td>শুরু</td><td class="text-end"><hrms:date value="${probation.startDate}"/></td></tr>
                    <tr><td>শেষ</td><td class="text-end fw-semibold"><hrms:date value="${probation.endDate}"/></td></tr>
                    <tr><td>বাড়ানো হয়েছে</td><td class="text-end">${probation.extensions} / ${maxExtensions} বার</td></tr>
                    <tr><td>স্থায়ী হলে ধরন</td><td class="text-end"><hrms:label value="${probation.confirmType}"/></td></tr>
                    <g:if test="${probation.decidedBy}"><tr><td>সিদ্ধান্ত</td><td class="text-end">${probation.decidedBy}, <hrms:dateTime value="${probation.decidedAt}"/></td></tr></g:if>
                    <g:if test="${probation.reviewNote}"><tr><td>মন্তব্য</td><td class="text-end">${probation.reviewNote}</td></tr></g:if>
                    </tbody>
                </table>
                <g:if test="${probation.status == 'ONGOING'}">
                    <div class="card-body border-top">
                        <g:form action="confirmProbation" id="${probation.id}" method="POST" class="mb-2" onsubmit="return confirm('কর্মীকে স্থায়ী করবেন?');">
                            <div class="input-group">
                                <input type="text" name="note" maxlength="500" class="form-control" placeholder="মূল্যায়নের মন্তব্য (ঐচ্ছিক)">
                                <button type="submit" class="btn btn-success">স্থায়ী করুন</button>
                            </div>
                        </g:form>
                        <g:if test="${probation.extensions < maxExtensions}">
                            <g:form action="extendProbation" id="${probation.id}" method="POST" class="mb-2">
                                <div class="input-group">
                                    <select name="months" class="form-select" style="max-width: 110px;"><option value="1">১ মাস</option><option value="2">২ মাস</option><option value="3">৩ মাস</option></select>
                                    <input type="text" name="note" maxlength="500" class="form-control" placeholder="বাড়ানোর কারণ" required>
                                    <button type="submit" class="btn btn-outline-warning">মেয়াদ বাড়ান</button>
                                </div>
                            </g:form>
                        </g:if>
                        <g:form action="notConfirmProbation" id="${probation.id}" method="POST" onsubmit="return confirm('স্থায়ী না করার সিদ্ধান্ত নেবেন?');">
                            <div class="input-group">
                                <input type="text" name="note" maxlength="500" class="form-control" placeholder="স্থায়ী না করার কারণ" required>
                                <button type="submit" class="btn btn-outline-danger">স্থায়ী নয়</button>
                            </div>
                        </g:form>
                    </div>
                </g:if>
            </g:if>
            <g:else>
                <div class="card-body">
                    <div class="small text-muted mb-2">এই কর্মীর পরীক্ষাকালের রেকর্ড নেই।</div>
                    <g:form action="startProbation" id="${employee.id}" method="POST" class="input-group">
                        <input type="number" name="months" min="1" max="12" value="3" class="form-control" style="max-width: 90px;">
                        <span class="input-group-text">মাস</span>
                        <button type="submit" class="btn btn-outline-primary">পরীক্ষাকাল শুরু</button>
                    </g:form>
                </div>
            </g:else>
        </div>

        <%-- ============ সম্পদ ============ --%>
        <div class="card" id="assets">
            <div class="card-header bg-white fw-semibold">বরাদ্দকৃত সম্পদ</div>
            <div class="table-responsive">
                <table class="table table-sm align-middle mb-0">
                    <thead class="table-light"><tr><th>সম্পদ</th><th>বরাদ্দ</th><th>অবস্থা</th><th></th></tr></thead>
                    <tbody>
                    <g:each in="${assets}" var="a">
                        <tr>
                            <td><div class="fw-semibold">${a.assetName}</div><div class="small text-muted">${a.assetType}<g:if test="${a.serialNo}"> &middot; ${a.serialNo}</g:if></div></td>
                            <td><hrms:date value="${a.issuedDate}"/></td>
                            <td><hrms:recruitBadge kind="asset" status="${a.status}"/><g:if test="${a.returnedDate}"><div class="small text-muted"><hrms:date value="${a.returnedDate}"/></div></g:if></td>
                            <td class="text-end">
                                <g:if test="${a.status == 'ISSUED'}">
                                    <g:form action="returnAsset" id="${a.id}" method="POST" class="d-inline" onsubmit="return confirm('সম্পদটি ফেরত নেওয়া হয়েছে?');">
                                        <button type="submit" class="btn btn-sm btn-outline-secondary">ফেরত নিন</button>
                                    </g:form>
                                </g:if>
                            </td>
                        </tr>
                    </g:each>
                    <g:if test="${!assets}"><tr><td colspan="4" class="text-center text-muted py-3">কোনো সম্পদ বরাদ্দ নেই।</td></tr></g:if>
                    </tbody>
                </table>
            </div>
            <div class="card-body border-top">
                <g:form action="issueAsset" id="${employee.id}" method="POST" class="row g-2">
                    <div class="col-md-4"><select name="assetType" class="form-select"><g:each in="${assetTypes}" var="t"><option value="${t}">${t}</option></g:each></select></div>
                    <div class="col-md-8"><input type="text" name="assetName" maxlength="120" class="form-control" placeholder="নাম / মডেল (যেমন: Dell Latitude 5440)" required></div>
                    <div class="col-md-5"><input type="text" name="serialNo" maxlength="80" class="form-control" placeholder="সিরিয়াল / ট্যাগ"></div>
                    <div class="col-md-4"><input type="date" name="issuedDate" class="form-control" value="${today}" max="${today}"></div>
                    <div class="col-md-3"><button type="submit" class="btn btn-outline-primary w-100">বরাদ্দ</button></div>
                </g:form>
            </div>
        </div>
    </div>
</div>
</body>
</html>

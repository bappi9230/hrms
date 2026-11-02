<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>${employee.name}</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="page-title mb-0">কর্মী প্রোফাইল</h1>
    <div>
        <g:if test="${canEdit}">
            <g:link action="edit" id="${employee.id}" class="btn btn-primary"><i class="bi bi-pencil"></i> সম্পাদনা</g:link>
        </g:if>
        <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_HR,ROLE_MANAGER">
            <g:link action="index" class="btn btn-secondary"><i class="bi bi-arrow-left"></i> তালিকা</g:link>
        </sec:ifAnyGranted>
    </div>
</div>

<div class="row g-3">
    <div class="col-lg-4">
        <div class="card text-center">
            <div class="card-body">
                <g:if test="${employee.hasPhoto}">
                    <img class="avatar avatar-xl mb-3" src="${createLink(action: 'photo', id: employee.id)}" alt="${employee.name}">
                </g:if>
                <g:else>
                    <span class="avatar avatar-xl mb-3"><hrms:initials name="${employee.name}"/></span>
                </g:else>
                <h4 class="mb-0">${employee.name}</h4>
                <div class="text-muted mb-2">${employee.designation} &middot; ${employee.department}</div>
                <hrms:statusBadge status="${employee.status}"/>
                <span class="badge text-bg-info"><hrms:label value="${employee.employeeType}"/></span>
            </div>
            <ul class="list-group list-group-flush text-start small">
                <li class="list-group-item"><i class="bi bi-envelope me-2"></i>${employee.email}</li>
                <li class="list-group-item"><i class="bi bi-telephone me-2"></i>${employee.phone ?: '—'}</li>
                <li class="list-group-item"><i class="bi bi-calendar-event me-2"></i>যোগদান: <hrms:date value="${employee.joinDate}"/></li>
            </ul>
        </div>
    </div>

    <div class="col-lg-8">
        <div class="card">
            <div class="card-header bg-white">
                <ul class="nav nav-tabs card-header-tabs" role="tablist">
                    <li class="nav-item"><button class="nav-link active" data-bs-toggle="tab" data-bs-target="#tab-personal" type="button" role="tab">ব্যক্তিগত</button></li>
                    <li class="nav-item"><button class="nav-link" data-bs-toggle="tab" data-bs-target="#tab-job" type="button" role="tab">চাকরির তথ্য</button></li>
                    <g:if test="${canSeeSalary}">
                        <li class="nav-item"><button class="nav-link" data-bs-toggle="tab" data-bs-target="#tab-salary" type="button" role="tab">বেতন</button></li>
                    </g:if>
                    <g:if test="${canSeeDocs}">
                        <li class="nav-item"><button class="nav-link" data-bs-toggle="tab" data-bs-target="#tab-documents" type="button" role="tab">নথি <span class="badge text-bg-light border">${documents.size()}</span></button></li>
                    </g:if>
                    <li class="nav-item"><span class="nav-link disabled" title="Day 4-এ আসছে">ছুটি</span></li>
                    <li class="nav-item"><span class="nav-link disabled" title="Day 3-এ আসছে">উপস্থিতি</span></li>
                    <li class="nav-item"><button class="nav-link" data-bs-toggle="tab" data-bs-target="#tab-history" type="button" role="tab">ইতিহাস <span class="badge text-bg-light border">${history.size()}</span></button></li>
                </ul>
            </div>
            <div class="card-body tab-content">

                <div class="tab-pane fade show active" id="tab-personal" role="tabpanel">
                    <dl class="row profile-list mb-0">
                        <dt class="col-sm-4">জন্ম তারিখ</dt><dd class="col-sm-8"><hrms:date value="${employee.dateOfBirth}"/></dd>
                        <dt class="col-sm-4">লিঙ্গ</dt><dd class="col-sm-8">${employee.gender ? employee.gender.toLowerCase().capitalize() : '—'}</dd>
                        <dt class="col-sm-4">NID</dt><dd class="col-sm-8">${employee.nid ?: '—'}</dd>
                        <dt class="col-sm-4">বর্তমান ঠিকানা</dt><dd class="col-sm-8">${employee.presentAddress ?: '—'}</dd>
                        <dt class="col-sm-4">স্থায়ী ঠিকানা</dt><dd class="col-sm-8">${employee.permanentAddress ?: '—'}</dd>
                        <dt class="col-sm-4">জরুরি যোগাযোগ</dt>
                        <dd class="col-sm-8">
                            <g:if test="${employee.emergencyContactName}">
                                ${employee.emergencyContactName}
                                <g:if test="${employee.emergencyContactRelation}">(${employee.emergencyContactRelation})</g:if>
                                <br><span class="text-muted">${employee.emergencyContactPhone ?: ''}</span>
                            </g:if>
                            <g:else>—</g:else>
                        </dd>
                    </dl>
                </div>

                <div class="tab-pane fade" id="tab-job" role="tabpanel">
                    <dl class="row profile-list mb-0">
                        <dt class="col-sm-4">বিভাগ</dt><dd class="col-sm-8">${employee.department}</dd>
                        <dt class="col-sm-4">পদবি</dt><dd class="col-sm-8">${employee.designation}</dd>
                        <dt class="col-sm-4">শাখা</dt><dd class="col-sm-8">${employee.branch ?: '—'}</dd>
                        <dt class="col-sm-4">গ্রেড</dt><dd class="col-sm-8">${employee.grade ?: '—'}</dd>
                        <dt class="col-sm-4">কস্ট সেন্টার</dt><dd class="col-sm-8">${employee.costCenter ?: '—'}</dd>
                        <dt class="col-sm-4">রিপোর্টিং ম্যানেজার</dt>
                        <dd class="col-sm-8">
                            <g:if test="${employee.reportingManager}">
                                <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_HR">
                                    <g:link action="show" id="${employee.reportingManager.id}" class="text-decoration-none">${employee.reportingManager.name}</g:link>
                                </sec:ifAnyGranted>
                                <sec:ifNotGranted roles="ROLE_ADMIN,ROLE_HR">${employee.reportingManager.name}</sec:ifNotGranted>
                            </g:if>
                            <g:else>—</g:else>
                        </dd>
                        <dt class="col-sm-4">কর্মীর ধরন</dt><dd class="col-sm-8"><hrms:label value="${employee.employeeType}"/></dd>
                        <dt class="col-sm-4">যোগদান</dt><dd class="col-sm-8"><hrms:date value="${employee.joinDate}"/></dd>
                        <dt class="col-sm-4">স্ট্যাটাস</dt><dd class="col-sm-8"><hrms:statusBadge status="${employee.status}"/></dd>
                    </dl>
                </div>

                <g:if test="${canSeeSalary}">
                    <div class="tab-pane fade" id="tab-salary" role="tabpanel">
                        <dl class="row profile-list mb-0">
                            <dt class="col-sm-4">মাসিক বেতন</dt><dd class="col-sm-8 fs-5 fw-semibold"><hrms:money value="${employee.salary}"/></dd>
                            <dt class="col-sm-4">পদবির মূল বেতন</dt><dd class="col-sm-8"><hrms:money value="${employee.designation?.baseSalary}"/></dd>
                        </dl>
                        <div class="text-muted small">বেতন কাঠামো (বেসিক, বাড়িভাড়া, চিকিৎসা...) Day 5-এ আসছে।</div>
                    </div>
                </g:if>

                <%-- ========================= নথি ========================= --%>
                <g:if test="${canSeeDocs}">
                    <div class="tab-pane fade" id="tab-documents" role="tabpanel">
                        <g:if test="${canEdit}">
                            <g:uploadForm controller="document" action="upload" id="${employee.id}" method="POST" class="row g-2 align-items-end mb-3 pb-3 border-bottom">
                                <div class="col-md-3">
                                    <label class="form-label small mb-1" for="docType">ধরন</label>
                                    <g:select name="docType" id="docType" from="${docTypes}" class="form-select form-select-sm"/>
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label small mb-1" for="docTitle">শিরোনাম</label>
                                    <input type="text" name="title" id="docTitle" class="form-control form-control-sm" maxlength="150" placeholder="ফাঁকা থাকলে ফাইলের নাম">
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label small mb-1" for="docExpiry">মেয়াদ শেষ (ঐচ্ছিক)</label>
                                    <input type="date" name="expiryDate" id="docExpiry" class="form-control form-control-sm">
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label small mb-1" for="docFile">ফাইল (PDF/JPG/PNG/DOCX, ≤ ২ MB)</label>
                                    <input type="file" name="file" id="docFile" class="form-control form-control-sm" accept=".pdf,.jpg,.jpeg,.png,.doc,.docx" required>
                                </div>
                                <div class="col-12">
                                    <button type="submit" class="btn btn-sm btn-primary"><i class="bi bi-upload"></i> আপলোড</button>
                                </div>
                            </g:uploadForm>
                        </g:if>

                        <g:if test="${documents}">
                            <div class="table-responsive">
                                <table class="table table-sm align-middle mb-0">
                                    <thead class="table-light">
                                    <tr><th>শিরোনাম</th><th>ধরন</th><th>মেয়াদ শেষ</th><th>সাইজ</th><th>আপলোড</th><th></th></tr>
                                    </thead>
                                    <tbody>
                                    <g:each in="${documents}" var="d">
                                        <tr>
                                            <td>
                                                <g:link controller="document" action="download" id="${d.id}" class="text-decoration-none"><i class="bi bi-paperclip"></i> ${d.title}</g:link>
                                                <div class="small text-muted">${d.fileName}</div>
                                            </td>
                                            <td><hrms:label value="${d.docType}"/></td>
                                            <td>
                                                <hrms:date value="${d.expiryDate}"/>
                                                <g:if test="${d.expiryStatus == 'EXPIRED'}"><span class="badge text-bg-danger">মেয়াদোত্তীর্ণ</span></g:if>
                                                <g:elseif test="${d.expiryStatus == 'EXPIRING'}"><span class="badge text-bg-warning text-dark">শিগগির শেষ</span></g:elseif>
                                            </td>
                                            <td class="small"><hrms:fileSize value="${d.fileSize}"/></td>
                                            <td class="small text-muted"><hrms:date value="${d.dateCreated}"/><br>${d.uploadedBy ?: ''}</td>
                                            <td class="text-end">
                                                <g:if test="${canEdit}">
                                                    <g:form controller="document" action="delete" id="${d.id}" method="POST" onsubmit="return confirm('নথিটি মুছে ফেলবেন?');" class="d-inline">
                                                        <button type="submit" class="btn btn-sm btn-outline-danger"><i class="bi bi-trash"></i></button>
                                                    </g:form>
                                                </g:if>
                                            </td>
                                        </tr>
                                    </g:each>
                                    </tbody>
                                </table>
                            </div>
                        </g:if>
                        <g:else><div class="text-muted">কোনো নথি আপলোড করা হয়নি।</div></g:else>
                    </div>
                </g:if>

                <%-- ========================= ইতিহাস ========================= --%>
                <div class="tab-pane fade" id="tab-history" role="tabpanel">
                    <g:if test="${canEdit}">
                        <g:form action="recordChange" id="${employee.id}" method="POST" class="row g-2 align-items-end mb-3 pb-3 border-bottom">
                            <div class="col-md-3">
                                <label class="form-label small mb-1" for="eventType">ধরন</label>
                                <g:select name="eventType" id="eventType" from="${eventTypes}" class="form-select form-select-sm"/>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label small mb-1" for="effectiveDate">কার্যকর তারিখ <span class="text-danger">*</span></label>
                                <input type="date" name="effectiveDate" id="effectiveDate" class="form-control form-control-sm" value="${java.time.LocalDate.now()}" required>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label small mb-1" for="departmentId">নতুন বিভাগ</label>
                                <g:select name="departmentId" id="departmentId" from="${departments}" optionKey="id" noSelection="['': '-- অপরিবর্তিত --']" class="form-select form-select-sm"/>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label small mb-1" for="designationId">নতুন পদবি</label>
                                <g:select name="designationId" id="designationId" from="${designations}" optionKey="id" noSelection="['': '-- অপরিবর্তিত --']" class="form-select form-select-sm"/>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label small mb-1" for="branchId">নতুন শাখা</label>
                                <g:select name="branchId" id="branchId" from="${branches}" optionKey="id" noSelection="['': '-- অপরিবর্তিত --']" class="form-select form-select-sm"/>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label small mb-1" for="salary">নতুন বেতন (৳)</label>
                                <input type="number" name="salary" id="salary" min="0" step="0.01" class="form-control form-control-sm" placeholder="অপরিবর্তিত">
                            </div>
                            <div class="col-md-4">
                                <label class="form-label small mb-1" for="remarks">মন্তব্য</label>
                                <input type="text" name="remarks" id="remarks" maxlength="255" class="form-control form-control-sm">
                            </div>
                            <div class="col-md-2">
                                <button type="submit" class="btn btn-sm btn-primary w-100"><i class="bi bi-check-lg"></i> রেকর্ড করুন</button>
                            </div>
                        </g:form>
                    </g:if>

                    <g:if test="${history}">
                        <div class="table-responsive">
                            <table class="table table-sm align-middle mb-0">
                                <thead class="table-light">
                                <tr><th>তারিখ</th><th>ঘটনা</th><th>পরিবর্তন</th><th>মন্তব্য</th></tr>
                                </thead>
                                <tbody>
                                <g:each in="${history}" var="h">
                                    <tr>
                                        <td class="text-nowrap"><hrms:date value="${h.effectiveDate}"/></td>
                                        <td>
                                            <g:if test="${h.eventType == 'JOINED'}"><span class="badge text-bg-success">যোগদান</span></g:if>
                                            <g:elseif test="${h.eventType == 'PROMOTION'}"><span class="badge text-bg-primary">পদোন্নতি</span></g:elseif>
                                            <g:elseif test="${h.eventType == 'TRANSFER'}"><span class="badge text-bg-info">বদলি</span></g:elseif>
                                            <g:else><span class="badge text-bg-secondary">অন্যান্য</span></g:else>
                                        </td>
                                        <td class="small">
                                            <g:if test="${h.toDepartment}">বিভাগ: <g:if test="${h.fromDepartment}">${h.fromDepartment} → </g:if><strong>${h.toDepartment}</strong><br></g:if>
                                            <g:if test="${h.toDesignation}">পদবি: <g:if test="${h.fromDesignation}">${h.fromDesignation} → </g:if><strong>${h.toDesignation}</strong><br></g:if>
                                            <g:if test="${h.toBranch}">শাখা: <g:if test="${h.fromBranch}">${h.fromBranch} → </g:if><strong>${h.toBranch}</strong><br></g:if>
                                            <g:if test="${canSeeSalary && h.newSalary != null}">
                                                বেতন: <g:if test="${h.oldSalary != null}"><hrms:money value="${h.oldSalary}"/> → </g:if><strong><hrms:money value="${h.newSalary}"/></strong>
                                            </g:if>
                                        </td>
                                        <td class="small text-muted">${h.remarks ?: ''}<g:if test="${h.recordedBy}"><br>— ${h.recordedBy}</g:if></td>
                                    </tr>
                                </g:each>
                                </tbody>
                            </table>
                        </div>
                    </g:if>
                    <g:else><div class="text-muted">কোনো ইতিহাস নেই।</div></g:else>
                </div>
            </div>
        </div>
    </div>
</div>

<sec:ifAnyGranted roles="ROLE_ADMIN">
    <div class="mt-4">
        <g:form action="delete" id="${employee.id}" method="POST" onsubmit="return confirm('এই কর্মীকে স্থায়ীভাবে মুছে ফেলবেন?');">
            <button type="submit" class="btn btn-sm btn-outline-danger"><i class="bi bi-trash"></i> কর্মী মুছুন</button>
        </g:form>
    </div>
</sec:ifAnyGranted>
<script>
    // URL এ #tab-documents / #tab-history থাকলে সেই ট্যাব খুলে দেওয়া
    (function () {
        var hash = window.location.hash;
        if (!hash) return;
        var btn = document.querySelector('[data-bs-target="' + hash + '"]');
        if (btn) new bootstrap.Tab(btn).show();
    })();
</script>
</body>
</html>
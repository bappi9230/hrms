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
                    <li class="nav-item"><span class="nav-link disabled" title="Day 2-এ আসছে">নথি</span></li>
                    <li class="nav-item"><span class="nav-link disabled" title="Day 4-এ আসছে">ছুটি</span></li>
                    <li class="nav-item"><span class="nav-link disabled" title="Day 3-এ আসছে">উপস্থিতি</span></li>
                    <li class="nav-item"><span class="nav-link disabled" title="Day 2-এ আসছে">ইতিহাস</span></li>
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
</body>
</html>
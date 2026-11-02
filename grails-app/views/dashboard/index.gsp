<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>ড্যাশবোর্ড</title>
</head>
<body>
<h1 class="page-title">ড্যাশবোর্ড</h1>

<div class="row g-3 mb-4">
    <div class="col-6 col-lg-3">
        <div class="card stat-card"><div class="card-body">
            <div class="text-muted small">মোট কর্মী</div>
            <div class="stat-value">${totalEmployees}</div>
        </div></div>
    </div>
    <div class="col-6 col-lg-3">
        <div class="card stat-card success"><div class="card-body">
            <div class="text-muted small">আজ উপস্থিত</div>
            <div class="stat-value">${presentToday}</div>
        </div></div>
    </div>
    <div class="col-6 col-lg-3">
        <div class="card stat-card warning"><div class="card-body">
            <div class="text-muted small">ছুটিতে</div>
            <div class="stat-value">${onLeaveCount}</div>
        </div></div>
    </div>
    <div class="col-6 col-lg-3">
        <div class="card stat-card danger"><div class="card-body">
            <div class="text-muted small">অপেক্ষমাণ অনুমোদন</div>
            <div class="stat-value">${pendingCount}</div>
        </div></div>
    </div>
</div>

<div class="row g-3">
    <div class="col-lg-6">
        <div class="card h-100">
            <div class="card-header bg-white fw-semibold">বিভাগ অনুযায়ী কর্মী</div>
            <div class="card-body">
                <g:each in="${byDepartment}" var="row">
                    <div class="d-flex justify-content-between small mb-1">
                        <span>${row.department}</span><span>${row.count}</span>
                    </div>
                    <div class="progress mb-3" style="height: 8px;">
                        <div class="progress-bar" style="width: ${totalEmployees ? (row.count * 100 / totalEmployees) : 0}%"></div>
                    </div>
                </g:each>
                <g:if test="${!byDepartment}"><span class="text-muted">কোনো তথ্য নেই।</span></g:if>
            </div>
        </div>
    </div>
    <g:if test="${expiring}">
        <div class="col-12">
            <div class="card border-warning">
                <div class="card-header bg-white fw-semibold"><i class="bi bi-exclamation-triangle text-warning"></i> নথির মেয়াদ শেষ হচ্ছে / হয়ে গেছে</div>
                <ul class="list-group list-group-flush">
                    <g:each in="${expiring}" var="d">
                        <li class="list-group-item d-flex justify-content-between align-items-center">
                            <span>
                                <g:link controller="employee" action="show" id="${d.employee.id}" fragment="tab-documents" class="fw-semibold text-decoration-none">${d.employee.name}</g:link>
                                &middot; ${d.title}
                            </span>
                            <span>
                                <hrms:date value="${d.expiryDate}"/>
                                <g:if test="${d.expiryStatus == 'EXPIRED'}"><span class="badge text-bg-danger">মেয়াদোত্তীর্ণ</span></g:if>
                                <g:else><span class="badge text-bg-warning text-dark">শিগগির শেষ</span></g:else>
                            </span>
                        </li>
                    </g:each>
                </ul>
            </div>
        </div>
    </g:if>
    <div class="col-lg-6">
        <div class="card h-100">
            <div class="card-header bg-white fw-semibold">সাম্প্রতিক যোগদান</div>
            <ul class="list-group list-group-flush">
                <g:each in="${recentJoins}" var="e">
                    <li class="list-group-item d-flex align-items-center gap-3">
                        <g:if test="${e.hasPhoto}">
                            <img class="avatar avatar-sm" src="${createLink(controller: 'employee', action: 'photo', id: e.id)}" alt="">
                        </g:if>
                        <g:else><span class="avatar avatar-sm"><hrms:initials name="${e.name}"/></span></g:else>
                        <div class="flex-grow-1">
                            <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_HR,ROLE_MANAGER,ROLE_ACCOUNTANT">
                                <g:link controller="employee" action="show" id="${e.id}" class="fw-semibold text-decoration-none">${e.name}</g:link>
                            </sec:ifAnyGranted>
                            <sec:ifNotGranted roles="ROLE_ADMIN,ROLE_HR,ROLE_MANAGER,ROLE_ACCOUNTANT">
                                <span class="fw-semibold">${e.name}</span>
                            </sec:ifNotGranted>
                            <div class="small text-muted">${e.designation} &middot; ${e.department}</div>
                        </div>
                        <span class="small text-muted"><hrms:date value="${e.joinDate}"/></span>
                    </li>
                </g:each>
            </ul>
        </div>
    </div>
</div>
</body>
</html>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>বোনাস</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="page-title mb-0">বোনাস</h1>
    <g:link controller="payroll" action="index" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> পে-রোল</g:link>
</div>

<g:form action="index" method="GET" class="row g-2 align-items-end mb-3">
    <div class="col-auto">
        <label class="form-label small mb-1" for="month">পে-মাস</label>
        <select name="month" id="month" class="form-select">
            <g:each in="${1..12}" var="m"><option value="${m}" ${m == month ? 'selected' : ''}><hrms:monthName value="${m}"/></option></g:each>
        </select>
    </div>
    <div class="col-auto">
        <label class="form-label small mb-1" for="year">বছর</label>
        <input type="number" name="year" id="year" class="form-control" value="${year}" min="2000" max="2100" style="width:110px;">
    </div>
    <div class="col-auto"><button type="submit" class="btn btn-outline-primary">দেখুন</button></div>
    <g:if test="${locked}"><div class="col-auto"><span class="badge text-bg-success">এই মাসের পে-রোল চূড়ান্ত</span></div></g:if>
</g:form>

<g:unless test="${locked}">
<div class="row g-3 mb-4">
    <div class="col-lg-6">
        <div class="card h-100">
            <div class="card-header bg-white fw-semibold">উৎসব বোনাস - সবার জন্য একসাথে</div>
            <div class="card-body">
                <g:form action="generateFestival" method="POST" class="row g-2">
                    <g:hiddenField name="year" value="${year}"/>
                    <g:hiddenField name="month" value="${month}"/>
                    <div class="col-md-8">
                        <label class="form-label small mb-1" for="fTitle">নাম <span class="text-danger">*</span></label>
                        <input type="text" name="title" id="fTitle" maxlength="80" class="form-control" placeholder="Eid-ul-Fitr ${year}" required>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label small mb-1" for="fPct">বেসিকের % <span class="text-danger">*</span></label>
                        <input type="number" name="percent" id="fPct" min="1" max="500" step="1" class="form-control" value="100" required>
                    </div>
                    <div class="col-12 small text-muted">৩ মাসের কম চাকরি: নেই; ৩-১১ মাস: মাসের অনুপাতে (মাস ÷ ১২); ১২ মাস+: পূর্ণ। একই নামে আগে থাকলে আবার হয় না।</div>
                    <div class="col-12"><button type="submit" class="btn btn-primary"><i class="bi bi-gift"></i> বোনাস তৈরি</button></div>
                </g:form>
            </div>
        </div>
    </div>
    <div class="col-lg-6">
        <div class="card h-100">
            <div class="card-header bg-white fw-semibold">একজনের বোনাস (পারফরম্যান্স/অন্য)</div>
            <div class="card-body">
                <g:form action="add" method="POST" class="row g-2">
                    <g:hiddenField name="year" value="${year}"/>
                    <g:hiddenField name="month" value="${month}"/>
                    <div class="col-md-6">
                        <label class="form-label small mb-1" for="aEmp">কর্মী <span class="text-danger">*</span></label>
                        <select name="employee" id="aEmp" class="form-select" required>
                            <option value="">— বেছে নিন —</option>
                            <g:each in="${employees}" var="e"><option value="${e.id}">${e.name}</option></g:each>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label small mb-1" for="aType">ধরন</label>
                        <select name="bonusType" id="aType" class="form-select">
                            <option value="PERFORMANCE">পারফরম্যান্স</option>
                            <option value="FESTIVAL">উৎসব</option>
                            <option value="OTHER">অন্য</option>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label small mb-1" for="aTitle">শিরোনাম <span class="text-danger">*</span></label>
                        <input type="text" name="title" id="aTitle" maxlength="80" class="form-control" placeholder="Q3 performance" required>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label small mb-1" for="aAmount">টাকা <span class="text-danger">*</span></label>
                        <input type="number" name="amount" id="aAmount" min="1" step="1" class="form-control" required>
                    </div>
                    <div class="col-12"><input type="text" name="note" maxlength="255" class="form-control" placeholder="মন্তব্য (ঐচ্ছিক)"></div>
                    <div class="col-12"><button type="submit" class="btn btn-primary"><i class="bi bi-plus-lg"></i> যোগ করুন</button></div>
                </g:form>
            </div>
        </div>
    </div>
</div>
</g:unless>

<div class="card">
    <div class="card-header bg-white fw-semibold d-flex justify-content-between">
        <span><hrms:monthName value="${month}"/> ${year} এর বোনাস</span>
        <span>মোট (বাতিল বাদে): <hrms:money value="${total}"/></span>
    </div>
    <div class="table-responsive">
        <table class="table table-sm align-middle mb-0">
            <thead class="table-light"><tr><th>কর্মী</th><th>ধরন</th><th>শিরোনাম</th><th class="text-end">টাকা</th><th>নোট</th><th>অবস্থা</th><th></th></tr></thead>
            <tbody>
            <g:each in="${bonuses}" var="b">
                <tr class="${b.status == 'CANCELLED' ? 'text-muted' : ''}">
                    <td class="fw-semibold">${b.employee.name}</td>
                    <td>${b.bonusType == 'FESTIVAL' ? 'উৎসব' : (b.bonusType == 'PERFORMANCE' ? 'পারফরম্যান্স' : 'অন্য')}</td>
                    <td>${b.title}</td>
                    <td class="text-end"><hrms:money value="${b.amount}"/></td>
                    <td class="small">${b.note ?: ''}</td>
                    <td>
                        <g:if test="${b.status == 'PAID'}"><span class="badge text-bg-success">দেওয়া হয়েছে</span></g:if>
                        <g:elseif test="${b.status == 'CANCELLED'}"><span class="badge text-bg-secondary">বাতিল</span></g:elseif>
                        <g:else><span class="badge text-bg-warning text-dark">পরিকল্পিত</span></g:else>
                    </td>
                    <td class="text-end">
                        <g:if test="${b.status == 'PLANNED'}">
                            <g:form action="cancel" id="${b.id}" method="POST" class="d-inline" onsubmit="return confirm('বোনাসটি বাতিল করবেন?');">
                                <button type="submit" class="btn btn-sm btn-outline-danger" title="বাতিল"><i class="bi bi-x-lg"></i></button>
                            </g:form>
                        </g:if>
                    </td>
                </tr>
            </g:each>
            <g:if test="${!bonuses}"><tr><td colspan="7" class="text-center text-muted py-4">এই মাসে কোনো বোনাস নেই।</td></tr></g:if>
            </tbody>
        </table>
    </div>
</div>
<div class="small text-muted mt-2">বোনাস যোগ/বাতিলের পর এই মাসের খসড়া পে-রোলে "আবার হিসাব" চালান।</div>
</body>
</html>

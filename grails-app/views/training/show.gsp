<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>${course.title}</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <div>
        <h1 class="page-title mb-1">${course.title}</h1>
        <span class="badge text-bg-light border"><hrms:d8Name value="${course.category}"/></span>
        <g:if test="${course.status == 'CANCELLED'}"><hrms:d8Badge kind="course" status="CANCELLED"/></g:if>
        <g:else><hrms:d8Badge kind="phase" status="${course.phase(today)}"/></g:else>
    </div>
    <div class="d-flex gap-2">
        <g:if test="${hr && course.status == 'OPEN'}"><g:link action="edit" id="${course.id}" class="btn btn-outline-primary"><i class="bi bi-pencil"></i> সম্পাদনা</g:link></g:if>
        <g:link action="index" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> ক্যাটালগ</g:link>
    </div>
</div>

<div class="row g-3">
    <div class="col-lg-5">
        <div class="card mb-3">
            <table class="table table-sm mb-0">
                <tbody>
                <tr><td>প্রশিক্ষক</td><td class="text-end">${course.trainer ?: '—'}</td></tr>
                <tr><td>ধরন</td><td class="text-end"><hrms:d8Name value="${course.trainingMode}"/></td></tr>
                <tr><td>স্থান</td><td class="text-end">${course.location ?: '—'}</td></tr>
                <tr><td>তারিখ</td><td class="text-end"><hrms:date value="${course.startDate}"/> - <hrms:date value="${course.endDate}"/></td></tr>
                <tr><td>মোট ঘণ্টা</td><td class="text-end">${course.hours}</td></tr>
                <tr><td>আসন</td><td class="text-end">${taken}<g:if test="${course.capacity > 0}"> / ${course.capacity} (খালি ${seatsLeft})</g:if><g:else> (সীমাহীন)</g:else></td></tr>
                </tbody>
            </table>
        </div>
        <g:if test="${course.description}">
            <div class="card mb-3"><div class="card-header bg-white fw-semibold">বিবরণ</div><div class="card-body" style="white-space: pre-line;">${course.description}</div></div>
        </g:if>

        <%-- আমার অবস্থা --%>
        <g:if test="${mine && mine.status == 'COMPLETED'}">
            <div class="alert alert-success d-flex justify-content-between align-items-center mb-3">
                <span>আপনি কোর্সটি সম্পন্ন করেছেন (${mine.certificateNo}).</span>
                <g:link action="certificate" id="${mine.id}" class="btn btn-sm btn-success"><i class="bi bi-award"></i> সার্টিফিকেট</g:link>
            </div>
        </g:if>
        <g:elseif test="${mine && mine.status == 'ENROLLED'}">
            <div class="alert alert-primary d-flex justify-content-between align-items-center mb-3">
                <span>আপনি এই কোর্সে নথিভুক্ত আছেন।</span>
                <g:if test="${course.startDate.isAfter(today)}">
                    <g:form action="cancelEnrollment" id="${mine.id}" method="POST" onsubmit="return confirm('নথিভুক্তি বাতিল করবেন?');"><button type="submit" class="btn btn-sm btn-outline-danger">বাতিল</button></g:form>
                </g:if>
            </div>
        </g:elseif>
        <g:elseif test="${course.status == 'OPEN' && !course.endDate.isBefore(today) && (seatsLeft == null || seatsLeft > 0)}">
            <g:if test="${mine == null || mine.status in ['CANCELLED', 'FAILED']}">
                <g:form action="enroll" id="${course.id}" method="POST" class="mb-3">
                    <button type="submit" class="btn btn-primary w-100"><i class="bi bi-box-arrow-in-right"></i> নথিভুক্ত হন</button>
                </g:form>
            </g:if>
        </g:elseif>
    </div>

    <div class="col-lg-7">
        <g:if test="${hr}">
            <div class="card mb-3">
                <div class="card-header bg-white fw-semibold">নথিভুক্ত কর্মী (${enrollments.size()})</div>
                <div class="table-responsive">
                    <table class="table table-sm align-middle mb-0">
                        <thead class="table-light"><tr><th>কর্মী</th><th>অবস্থা</th><th>স্কোর</th><th style="min-width:230px;"></th></tr></thead>
                        <tbody>
                        <g:each in="${enrollments}" var="e">
                            <tr>
                                <td>${e.employee.name}</td>
                                <td><hrms:d8Badge kind="enroll" status="${e.status}"/><g:if test="${e.certificateNo}"><div class="small text-muted">${e.certificateNo}</div></g:if></td>
                                <td>${e.score != null ? e.score : '—'}</td>
                                <td class="text-end">
                                    <g:if test="${e.status == 'ENROLLED' && course.status == 'OPEN'}">
                                        <g:if test="${!course.startDate.isAfter(today)}">
                                            <g:form action="result" id="${e.id}" method="POST" class="d-inline-flex gap-1">
                                                <input type="number" name="score" min="0" max="100" class="form-control form-control-sm" style="width:70px;" placeholder="স্কোর">
                                                <button type="submit" name="result" value="PASS" class="btn btn-sm btn-success">পাস</button>
                                                <button type="submit" name="result" value="FAIL" class="btn btn-sm btn-outline-danger">ফেল</button>
                                            </g:form>
                                        </g:if>
                                        <g:form action="cancelEnrollment" id="${e.id}" method="POST" class="d-inline" onsubmit="return confirm('নথিভুক্তি বাতিল করবেন?');">
                                            <button type="submit" class="btn btn-sm btn-outline-secondary" title="নথিভুক্তি বাতিল"><i class="bi bi-x-lg"></i></button>
                                        </g:form>
                                    </g:if>
                                    <g:if test="${e.status == 'COMPLETED'}"><g:link action="certificate" id="${e.id}" class="btn btn-sm btn-outline-success"><i class="bi bi-award"></i></g:link></g:if>
                                </td>
                            </tr>
                        </g:each>
                        <g:if test="${!enrollments}"><tr><td colspan="4" class="text-center text-muted py-3">কেউ নথিভুক্ত হননি।</td></tr></g:if>
                        </tbody>
                    </table>
                </div>
                <g:if test="${course.startDate.isAfter(today)}"><div class="card-footer small text-muted">ফল দেওয়ার বাটন কোর্স শুরুর দিন থেকে দেখা যাবে।</div></g:if>
            </div>

            <g:if test="${course.status == 'OPEN' && !course.endDate.isBefore(today)}">
                <div class="card mb-3">
                    <div class="card-header bg-white fw-semibold">কর্মীকে নথিভুক্ত করুন</div>
                    <div class="card-body">
                        <g:form action="enroll" id="${course.id}" method="POST" class="d-flex gap-2">
                            <select name="employee" class="form-select" required>
                                <option value="">— কর্মী নির্বাচন —</option>
                                <g:each in="${candidates}" var="emp"><option value="${emp.id}">${emp.name}</option></g:each>
                            </select>
                            <button type="submit" class="btn btn-primary text-nowrap">যোগ করুন</button>
                        </g:form>
                    </div>
                </div>
            </g:if>

            <g:if test="${course.status == 'OPEN'}">
                <div class="d-flex gap-2">
                    <g:form action="cancelCourse" id="${course.id}" method="POST" onsubmit="return confirm('কোর্স বাতিল করলে সব নথিভুক্তি বাতিল হবে। নিশ্চিত?');"><button type="submit" class="btn btn-outline-danger">কোর্স বাতিল</button></g:form>
                    <g:if test="${!enrollments}"><g:form action="deleteCourse" id="${course.id}" method="POST" onsubmit="return confirm('কোর্স মুছে ফেলবেন?');"><button type="submit" class="btn btn-outline-secondary">মুছুন</button></g:form></g:if>
                </div>
            </g:if>
        </g:if>
    </div>
</div>
</body>
</html>

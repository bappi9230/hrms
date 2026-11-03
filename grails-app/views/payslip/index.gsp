<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>আমার পে-স্লিপ</title>
</head>
<body>
<h1 class="page-title">আমার পে-স্লিপ</h1>

<g:if test="${!me}">
    <div class="alert alert-info">আপনার ইউজারের সাথে কোনো কর্মী যুক্ত নেই।</div>
</g:if>
<g:else>
<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light"><tr><th>মাস</th><th class="text-end">মোট আয়</th><th class="text-end">কর্তন</th><th class="text-end">নেট বেতন</th><th></th></tr></thead>
            <tbody>
            <g:each in="${lines}" var="l">
                <tr>
                    <td class="fw-semibold"><hrms:monthName value="${l.payrollRun.payMonth}"/> ${l.payrollRun.payYear}</td>
                    <td class="text-end"><hrms:money value="${l.grossEarnings}"/></td>
                    <td class="text-end"><hrms:money value="${l.totalDeduction}"/></td>
                    <td class="text-end fw-semibold"><hrms:money value="${l.netPay}"/></td>
                    <td class="text-end text-nowrap">
                        <g:link action="show" id="${l.id}" class="btn btn-sm btn-outline-secondary"><i class="bi bi-eye"></i> দেখুন</g:link>
                        <g:link action="pdf" id="${l.id}" target="_blank" class="btn btn-sm btn-outline-primary"><i class="bi bi-file-earmark-pdf"></i> PDF</g:link>
                    </td>
                </tr>
            </g:each>
            <g:if test="${!lines}"><tr><td colspan="5" class="text-center text-muted py-4">এখনো কোনো চূড়ান্ত পে-স্লিপ নেই।</td></tr></g:if>
            </tbody>
        </table>
    </div>
</div>
</g:else>
</body>
</html>

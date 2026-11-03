<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>দলের ছুটির ক্যালেন্ডার</title>
    <style>
        .leave-cal th, .leave-cal td { text-align: center; padding: .25rem .2rem; font-size: .8rem; min-width: 30px; }
        .leave-cal td.name { text-align: left; white-space: nowrap; position: sticky; left: 0; background: #fff; min-width: 140px; }
        .leave-cal .off { background: #f1f3f8; }
        .leave-cal .lv-approved { background: #198754; color: #fff; }
        .leave-cal .lv-pending { background: #ffc107; color: #000; }
    </style>
</head>
<body>
<h1 class="page-title">ছুটি</h1>
<g:render template="tabs" model="[active: 'calendar']"/>

<div class="card">
    <div class="card-header bg-white d-flex flex-wrap justify-content-between align-items-center gap-2">
        <span class="fw-semibold">${month.year}-<g:formatNumber number="${month.monthValue}" minIntegerDigits="2"/></span>
        <div class="d-flex align-items-center gap-3">
            <span class="small"><span class="badge lv-approved" style="background:#198754">&nbsp;</span> অনুমোদিত
                <span class="badge" style="background:#ffc107">&nbsp;</span> অপেক্ষমাণ
                <span class="badge" style="background:#e3e7f0">&nbsp;</span> সাপ্তাহিক ছুটি</span>
            <div class="btn-group btn-group-sm">
                <g:link action="calendar" params="[month: prev]" class="btn btn-outline-secondary"><i class="bi bi-chevron-left"></i></g:link>
                <g:link action="calendar" class="btn btn-outline-secondary">আজ</g:link>
                <g:link action="calendar" params="[month: next]" class="btn btn-outline-secondary"><i class="bi bi-chevron-right"></i></g:link>
            </div>
        </div>
    </div>
    <div class="table-responsive">
        <table class="table table-bordered leave-cal mb-0">
            <thead class="table-light">
            <tr>
                <th class="text-start">কর্মী</th>
                <g:each in="${days}" var="d"><th class="${d.dayOfWeek.value == 5 ? 'off' : ''}">${d.dayOfMonth}</th></g:each>
            </tr>
            </thead>
            <tbody>
            <g:each in="${rows}" var="row">
                <tr>
                    <td class="name">${row.employee.name}</td>
                    <g:each in="${days}" var="d">
                        <g:set var="cell" value="${row.cells[d]}"/>
                        <g:if test="${cell}">
                            <td class="${cell.status == 'APPROVED' ? 'lv-approved' : 'lv-pending'}"
                                title="${seeDetails ? cell.request.leaveType.name : 'ছুটি'} (${cell.status == 'APPROVED' ? 'অনুমোদিত' : 'অপেক্ষমাণ'})">
                                ${seeDetails ? cell.request.leaveType.code : '•'}
                            </td>
                        </g:if>
                        <g:else><td class="${d.dayOfWeek.value == 5 ? 'off' : ''}"></td></g:else>
                    </g:each>
                </tr>
            </g:each>
            <g:if test="${!rows}"><tr><td colspan="${days.size() + 1}" class="text-center text-muted py-4">দেখানোর মতো কর্মী নেই।</td></tr></g:if>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>

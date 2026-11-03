<%-- params: active (index|revisions) --%>
<ul class="nav nav-pills mb-3 gap-1">
    <li class="nav-item"><g:link action="index" class="nav-link ${active == 'index' ? 'active' : ''}"><i class="bi bi-table"></i> কর্মীদের বেতন কাঠামো</g:link></li>
    <li class="nav-item"><g:link action="revisions" class="nav-link ${active == 'revisions' ? 'active' : ''}"><i class="bi bi-graph-up-arrow"></i> ইনক্রিমেন্ট / সংশোধনের লগ</g:link></li>
    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_ACCOUNTANT">
        <li class="nav-item"><g:link controller="payroll" action="index" class="nav-link"><i class="bi bi-cash-coin"></i> পে-রোল</g:link></li>
    </sec:ifAnyGranted>
    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_HR">
        <li class="nav-item"><g:link controller="master" action="index" params="[type: 'salaryhead']" class="nav-link"><i class="bi bi-sliders"></i> ভাতা/কর্তনের খাত</g:link></li>
    </sec:ifAnyGranted>
</ul>

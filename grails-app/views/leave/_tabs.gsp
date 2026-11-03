<%-- params: active (index|apply|approvals|calendar|all|balances) --%>
<ul class="nav nav-pills mb-3 gap-1">
    <li class="nav-item"><g:link action="index" class="nav-link ${active == 'index' ? 'active' : ''}"><i class="bi bi-wallet2"></i> আমার ছুটি</g:link></li>
    <li class="nav-item"><g:link action="apply" class="nav-link ${active == 'apply' ? 'active' : ''}"><i class="bi bi-pencil-square"></i> ছুটির আবেদন</g:link></li>
    <li class="nav-item"><g:link action="approvals" class="nav-link ${active == 'approvals' ? 'active' : ''}"><i class="bi bi-check2-square"></i> অনুমোদন</g:link></li>
    <li class="nav-item"><g:link action="calendar" class="nav-link ${active == 'calendar' ? 'active' : ''}"><i class="bi bi-calendar3"></i> দলের ক্যালেন্ডার</g:link></li>
    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_HR">
        <li class="nav-item"><g:link action="all" class="nav-link ${active == 'all' ? 'active' : ''}"><i class="bi bi-list-ul"></i> সব আবেদন</g:link></li>
        <li class="nav-item"><g:link action="balances" class="nav-link ${active == 'balances' ? 'active' : ''}"><i class="bi bi-table"></i> সবার ব্যালেন্স</g:link></li>
    </sec:ifAnyGranted>
</ul>

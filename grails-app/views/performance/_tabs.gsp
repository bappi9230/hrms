<%-- params: active (index|appraisals|cycles) --%>
<ul class="nav nav-pills mb-3 gap-1">
    <li class="nav-item"><g:link action="index" class="nav-link ${active == 'index' ? 'active' : ''}"><i class="bi bi-bullseye"></i> লক্ষ্য / KPI</g:link></li>
    <li class="nav-item"><g:link action="appraisals" class="nav-link ${active == 'appraisals' ? 'active' : ''}"><i class="bi bi-clipboard2-check"></i> মূল্যায়ন</g:link></li>
    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_HR">
        <li class="nav-item"><g:link action="cycles" class="nav-link ${active == 'cycles' ? 'active' : ''}"><i class="bi bi-arrow-repeat"></i> মূল্যায়ন চক্র</g:link></li>
    </sec:ifAnyGranted>
</ul>

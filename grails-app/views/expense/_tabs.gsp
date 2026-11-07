<%-- "খরচ ও সম্পদ" মডিউলের ট্যাব। params: active (expense|travel|asset|benefit) --%>
<ul class="nav nav-pills mb-3 gap-1">
    <li class="nav-item"><g:link controller="expense" action="index" class="nav-link ${active == 'expense' ? 'active' : ''}"><i class="bi bi-receipt-cutoff"></i> খরচের দাবি</g:link></li>
    <li class="nav-item"><g:link controller="travel" action="index" class="nav-link ${active == 'travel' ? 'active' : ''}"><i class="bi bi-geo-alt"></i> ভ্রমণ</g:link></li>
    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_HR,ROLE_ACCOUNTANT">
        <li class="nav-item"><g:link controller="asset" action="index" class="nav-link ${active == 'asset' ? 'active' : ''}"><i class="bi bi-laptop"></i> সম্পদ রেজিস্টার</g:link></li>
    </sec:ifAnyGranted>
    <sec:ifNotGranted roles="ROLE_ADMIN,ROLE_HR,ROLE_ACCOUNTANT">
        <li class="nav-item"><g:link controller="asset" action="mine" class="nav-link ${active == 'asset' ? 'active' : ''}"><i class="bi bi-laptop"></i> আমার সম্পদ</g:link></li>
    </sec:ifNotGranted>
    <li class="nav-item"><g:link controller="benefit" action="index" class="nav-link ${active == 'benefit' ? 'active' : ''}"><i class="bi bi-heart-pulse"></i> সুবিধা</g:link></li>
</ul>

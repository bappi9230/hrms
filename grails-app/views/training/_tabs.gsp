<%-- params: active (index|my) --%>
<ul class="nav nav-pills mb-3 gap-1">
    <li class="nav-item"><g:link action="index" class="nav-link ${active == 'index' ? 'active' : ''}"><i class="bi bi-grid-3x3-gap"></i> কোর্স ক্যাটালগ</g:link></li>
    <li class="nav-item"><g:link action="my" class="nav-link ${active == 'my' ? 'active' : ''}"><i class="bi bi-person-check"></i> আমার প্রশিক্ষণ</g:link></li>
</ul>

<!doctype html>
<html lang="bn">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title><g:layoutTitle default="HRMS"/> | HRMS</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&family=Noto+Sans+Bengali:wght@400;500;600;700&display=swap" rel="stylesheet">
    <asset:link rel="icon" href="favicon.ico" type="image/x-ico"/>
    <asset:stylesheet src="application.css"/>
    <g:layoutHead/>
</head>

<body>

<header class="navbar navbar-dark hrms-topbar sticky-top">
    <div class="container-fluid">
        <div class="d-flex align-items-center">
            <button class="btn btn-outline-light btn-sm d-lg-none me-2" type="button"
                    data-bs-toggle="offcanvas" data-bs-target="#hrmsSidebar" aria-controls="hrmsSidebar" aria-label="Menu">
                <i class="bi bi-list fs-5"></i>
            </button>
            <a class="navbar-brand" href="${createLink(uri: '/')}"><i class="bi bi-people-fill me-1"></i> HRMS</a>
        </div>

        <sec:ifLoggedIn>
            <div class="dropdown">
                <a class="btn btn-sm btn-outline-light dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                    <i class="bi bi-person-circle me-1"></i> <sec:username/>
                </a>
                <ul class="dropdown-menu dropdown-menu-end">
                    <li><g:link controller="employee" action="myProfile" class="dropdown-item"><i class="bi bi-person me-2"></i>আমার প্রোফাইল</g:link></li>
                    <li><g:link controller="account" action="changePassword" class="dropdown-item"><i class="bi bi-key me-2"></i>পাসওয়ার্ড পরিবর্তন</g:link></li>
                    <li><hr class="dropdown-divider"></li>
                    <li><a class="dropdown-item text-danger" href="${createLink(uri: '/logoff')}"><i class="bi bi-box-arrow-right me-2"></i>লগআউট</a></li>
                </ul>
            </div>
        </sec:ifLoggedIn>
    </div>
</header>

<g:ifPageProperty name="page.nav">
    <nav class="hrms-subnav"><ul class="nav"><g:pageProperty name="page.nav"/></ul></nav>
</g:ifPageProperty>

<div class="hrms-shell">
    <aside class="offcanvas-lg offcanvas-start hrms-sidebar" tabindex="-1" id="hrmsSidebar" aria-labelledby="hrmsSidebarLabel">
        <div class="offcanvas-header d-lg-none">
            <h5 class="offcanvas-title" id="hrmsSidebarLabel">মেনু</h5>
            <button type="button" class="btn-close" data-bs-dismiss="offcanvas" data-bs-target="#hrmsSidebar" aria-label="Close"></button>
        </div>
        <div class="offcanvas-body p-0 d-block">
            <g:render template="/layouts/sidebar"/>
        </div>
    </aside>

    <main class="hrms-main">
        <g:layoutBody/>
    </main>
</div>

<footer class="hrms-footer d-flex justify-content-between">
    <span>&copy; HRMS</span>
    <span>সংস্করণ <g:meta name="info.app.version"/></span>
</footer>

<div class="toast-container position-fixed top-0 end-0 p-3">
    <g:each in="${[[key: 'success', css: 'success'], [key: 'error', css: 'danger'], [key: 'warning', css: 'warning']]}" var="t">
        <g:if test="${flash[t.key]}">
            <div class="toast align-items-center text-bg-${t.css} border-0 hrms-toast" role="alert" aria-live="assertive" aria-atomic="true">
                <div class="d-flex">
                    <div class="toast-body">${flash[t.key]}</div>
                    <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
                </div>
            </div>
        </g:if>
    </g:each>
</div>

<asset:javascript src="application.js"/>
<script>
    document.querySelectorAll('.hrms-toast').forEach(function (el) {
        new bootstrap.Toast(el, {delay: 5000}).show();
    });
</script>
</body>
</html>
<sec:ifLoggedIn>
<ul class="nav flex-column pb-3">

    <g:render template="/layouts/menuItem" model="[label: 'ড্যাশবোর্ড', icon: 'bi-speedometer2', controller: 'dashboard']"/>

    <sec:ifAnyGranted roles="ROLE_EMPLOYEE">
        <g:render template="/layouts/menuItem" model="[label: 'আমার প্রোফাইল', icon: 'bi-person-badge', controller: 'employee', action: 'myProfile']"/>
    </sec:ifAnyGranted>

    <li class="menu-heading">ব্যবস্থাপনা</li>

    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_HR,ROLE_MANAGER">
        <g:render template="/layouts/menuItem" model="[label: 'কর্মী', icon: 'bi-people', controller: 'employee']"/>
    </sec:ifAnyGranted>

    <g:render template="/layouts/menuItem" model="[label: 'অর্গ চার্ট', icon: 'bi-diagram-2', controller: 'orgChart']"/>

    <g:render template="/layouts/menuItem" model="[label: 'উপস্থিতি', icon: 'bi-calendar-check', controller: 'attendance', soon: 3]"/>
    <g:render template="/layouts/menuItem" model="[label: 'ছুটি', icon: 'bi-airplane', controller: 'leave', soon: 4]"/>

    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_ACCOUNTANT">
        <g:render template="/layouts/menuItem" model="[label: 'বেতন', icon: 'bi-cash-coin', controller: 'payroll', soon: 5]"/>
    </sec:ifAnyGranted>

    <sec:ifAnyGranted roles="ROLE_EMPLOYEE">
        <g:render template="/layouts/menuItem" model="[label: 'আমার পে-স্লিপ', icon: 'bi-receipt', controller: 'payslip', soon: 6]"/>
    </sec:ifAnyGranted>

    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_HR">
        <g:render template="/layouts/menuItem" model="[label: 'নিয়োগ', icon: 'bi-person-plus', controller: 'recruitment', soon: 7]"/>
    </sec:ifAnyGranted>

    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_HR,ROLE_MANAGER">
        <g:render template="/layouts/menuItem" model="[label: 'পারফরম্যান্স', icon: 'bi-graph-up-arrow', controller: 'performance', soon: 8]"/>
    </sec:ifAnyGranted>

    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_HR">
        <g:render template="/layouts/menuItem" model="[label: 'প্রশিক্ষণ', icon: 'bi-mortarboard', controller: 'training', soon: 8]"/>
    </sec:ifAnyGranted>

    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_ACCOUNTANT">
        <g:render template="/layouts/menuItem" model="[label: 'খরচ ও সম্পদ', icon: 'bi-wallet2', controller: 'expense', soon: 8]"/>
    </sec:ifAnyGranted>

    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_HR">
        <g:render template="/layouts/menuItem" model="[label: 'চাকরি ছাড়া', icon: 'bi-box-arrow-right', controller: 'offboarding', soon: 9]"/>
    </sec:ifAnyGranted>

    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_HR,ROLE_ACCOUNTANT">
        <g:render template="/layouts/menuItem" model="[label: 'রিপোর্ট', icon: 'bi-bar-chart-line', controller: 'report', soon: 10]"/>
    </sec:ifAnyGranted>

    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_HR">
        <li class="menu-heading">সেটিংস</li>
        <g:render template="/layouts/menuItem" model="[label: 'বিভাগ', icon: 'bi-diagram-3', controller: 'department']"/>
        <g:render template="/layouts/menuItem" model="[label: 'পদবি', icon: 'bi-award', controller: 'designation']"/>
        <g:render template="/layouts/menuItem" model="[label: 'শাখা', icon: 'bi-building', controller: 'master', linkParams: [type: 'branch']]"/>
        <g:render template="/layouts/menuItem" model="[label: 'গ্রেড', icon: 'bi-layers', controller: 'master', linkParams: [type: 'grade']]"/>
        <g:render template="/layouts/menuItem" model="[label: 'কস্ট সেন্টার', icon: 'bi-piggy-bank', controller: 'master', linkParams: [type: 'costcenter']]"/>
    </sec:ifAnyGranted>

</ul>
</sec:ifLoggedIn>
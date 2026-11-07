<sec:ifLoggedIn>
<ul class="nav flex-column pb-3">

    <g:render template="/layouts/menuItem" model="[label: 'আমার পোর্টাল', icon: 'bi-house-heart', controller: 'portal']"/>
    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_HR,ROLE_ACCOUNTANT">
        <g:render template="/layouts/menuItem" model="[label: 'ড্যাশবোর্ড', icon: 'bi-speedometer2', controller: 'dashboard']"/>
    </sec:ifAnyGranted>
    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_HR,ROLE_MANAGER,ROLE_ACCOUNTANT">
        <g:render template="/layouts/menuItem" model="[label: 'অনুমোদন ইনবক্স', icon: 'bi-inbox', controller: 'inbox']"/>
    </sec:ifAnyGranted>

    <sec:ifAnyGranted roles="ROLE_EMPLOYEE">
        <g:render template="/layouts/menuItem" model="[label: 'আমার প্রোফাইল', icon: 'bi-person-badge', controller: 'employee', action: 'myProfile']"/>
    </sec:ifAnyGranted>

    <li class="menu-heading">ব্যবস্থাপনা</li>

    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_HR,ROLE_MANAGER">
        <g:render template="/layouts/menuItem" model="[label: 'কর্মী', icon: 'bi-people', controller: 'employee']"/>
    </sec:ifAnyGranted>

    <g:render template="/layouts/menuItem" model="[label: 'অর্গ চার্ট', icon: 'bi-diagram-2', controller: 'orgChart']"/>

    <g:render template="/layouts/menuItem" model="[label: 'উপস্থিতি', icon: 'bi-calendar-check', controller: 'attendance']"/>
    <g:render template="/layouts/menuItem" model="[label: 'ছুটি', icon: 'bi-airplane', controller: 'leave']"/>

    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_ACCOUNTANT">
        <g:render template="/layouts/menuItem" model="[label: 'বেতন (পে-রোল)', icon: 'bi-cash-coin', controller: 'payroll']"/>
    </sec:ifAnyGranted>

    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_HR,ROLE_ACCOUNTANT">
        <g:render template="/layouts/menuItem" model="[label: 'বেতন কাঠামো', icon: 'bi-cash-stack', controller: 'salary']"/>
    </sec:ifAnyGranted>

    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_ACCOUNTANT">
        <g:render template="/layouts/menuItem" model="[label: 'বোনাস', icon: 'bi-gift', controller: 'bonus']"/>
        <g:render template="/layouts/menuItem" model="[label: 'ঋণ ও অগ্রিম', icon: 'bi-cash-stack', controller: 'loan']"/>
        <g:render template="/layouts/menuItem" model="[label: 'পেমেন্ট তথ্য', icon: 'bi-credit-card', controller: 'paymentInfo']"/>
    </sec:ifAnyGranted>

    <sec:ifAnyGranted roles="ROLE_EMPLOYEE">
        <g:render template="/layouts/menuItem" model="[label: 'আমার পে-স্লিপ', icon: 'bi-receipt', controller: 'payslip']"/>
    </sec:ifAnyGranted>

    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_HR">
        <g:render template="/layouts/menuItem" model="[label: 'নিয়োগ (Kanban)', icon: 'bi-person-plus', controller: 'recruitment']"/>
        <g:render template="/layouts/menuItem" model="[label: 'অনবোর্ডিং', icon: 'bi-clipboard-check', controller: 'onboarding']"/>
    </sec:ifAnyGranted>

    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_HR,ROLE_MANAGER">
        <g:render template="/layouts/menuItem" model="[label: 'জনবল চাহিদা', icon: 'bi-person-lines-fill', controller: 'requisition']"/>
        <g:render template="/layouts/menuItem" model="[label: 'আমার ইন্টারভিউ', icon: 'bi-chat-square-text', controller: 'candidate', action: 'myInterviews']"/>
    </sec:ifAnyGranted>

    <%-- Day 8: সবাই নিজের লক্ষ্য/প্রশিক্ষণ/খরচ দেখে; ম্যানেজার ও HR আরও বেশি --%>
    <g:render template="/layouts/menuItem" model="[label: 'পারফরম্যান্স', icon: 'bi-graph-up-arrow', controller: 'performance']"/>
    <g:render template="/layouts/menuItem" model="[label: 'প্রশিক্ষণ', icon: 'bi-mortarboard', controller: 'training']"/>
    <g:render template="/layouts/menuItem" model="[label: 'খরচ ও সম্পদ', icon: 'bi-wallet2', controller: 'expense']"/>

    <%-- Day 9: চাকরি ছাড়া, কর্মী সম্পর্ক, ঘোষণা (সবার জন্য; ভেতরে রোল অনুযায়ী সীমা) --%>
    <g:render template="/layouts/menuItem" model="[label: 'চাকরি ছাড়া', icon: 'bi-box-arrow-right', controller: 'offboarding']"/>
    <g:render template="/layouts/menuItem" model="[label: 'শৃঙ্খলা', icon: 'bi-shield-exclamation', controller: 'discipline']"/>
    <g:render template="/layouts/menuItem" model="[label: 'অভিযোগ', icon: 'bi-megaphone', controller: 'grievance']"/>
    <g:render template="/layouts/menuItem" model="[label: 'ঘোষণা', icon: 'bi-broadcast', controller: 'announcement']"/>
    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_HR">
        <g:render template="/layouts/menuItem" model="[label: 'অডিট ট্রেইল', icon: 'bi-journal-text', controller: 'audit']"/>
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
        <g:render template="/layouts/menuItem" model="[label: 'ছুটির নীতি', icon: 'bi-airplane-engines', controller: 'master', linkParams: [type: 'leavetype']]"/>
        <g:render template="/layouts/menuItem" model="[label: 'ভাতা/কর্তনের খাত', icon: 'bi-sliders', controller: 'master', linkParams: [type: 'salaryhead']]"/>
        <g:render template="/layouts/menuItem" model="[label: 'শিফট', icon: 'bi-clock', controller: 'master', linkParams: [type: 'shift']]"/>
        <g:render template="/layouts/menuItem" model="[label: 'অনবোর্ডিং কাজ', icon: 'bi-list-check', controller: 'master', linkParams: [type: 'onboardingtemplate']]"/>
        <g:render template="/layouts/menuItem" model="[label: 'সুবিধার প্ল্যান', icon: 'bi-heart-pulse', controller: 'master', linkParams: [type: 'benefitplan']]"/>
    </sec:ifAnyGranted>

</ul>
</sec:ifLoggedIn>
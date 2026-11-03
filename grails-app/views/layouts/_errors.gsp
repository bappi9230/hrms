<%-- params: bean (ডোমেইন অবজেক্ট) - ভ্যালিডেশন ত্রুটির তালিকা --%>
<g:hasErrors bean="${bean}">
    <div class="alert alert-danger">
        <ul class="mb-0">
            <g:eachError bean="${bean}" var="err"><li><g:message error="${err}"/></li></g:eachError>
        </ul>
    </div>
</g:hasErrors>

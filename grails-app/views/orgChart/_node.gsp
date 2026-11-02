<%-- params: emp, children (ম্যানেজার id -> অধীনের তালিকা) --%>
<li>
    <div class="org-node">
        <g:if test="${emp.hasPhoto}">
            <img class="avatar avatar-sm" src="${createLink(controller: 'employee', action: 'photo', id: emp.id)}" alt="">
        </g:if>
        <g:else><span class="avatar avatar-sm"><hrms:initials name="${emp.name}"/></span></g:else>
        <div>
            <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_HR">
                <g:link controller="employee" action="show" id="${emp.id}" class="fw-semibold text-decoration-none">${emp.name}</g:link>
            </sec:ifAnyGranted>
            <sec:ifNotGranted roles="ROLE_ADMIN,ROLE_HR"><span class="fw-semibold">${emp.name}</span></sec:ifNotGranted>
            <div class="small text-muted">${emp.designation} &middot; ${emp.department}</div>
        </div>
    </div>
    <g:set var="subs" value="${children[emp.id]}"/>
    <g:if test="${subs}">
        <ul>
            <g:each in="${subs}" var="c">
                <g:render template="/orgChart/node" model="[emp: c, children: children]"/>
            </g:each>
        </ul>
    </g:if>
</li>
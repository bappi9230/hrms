<%-- params: label, icon, controller, action (optional), soon (optional: day number the module arrives) --%>
<g:if test="${soon}">
    <li class="nav-item">
        <span class="nav-link soon" title="Day ${soon}-এ আসছে">
            <i class="bi ${icon}"></i> ${label} <span class="badge text-bg-light border">Day ${soon}</span>
        </span>
    </li>
</g:if>
<g:else>
    <li class="nav-item">
        <g:link controller="${controller}" action="${action ?: 'index'}"
                class="nav-link ${controllerName == controller && ((action == 'myProfile') == (actionName == 'myProfile')) ? 'active' : ''}">
            <i class="bi ${icon}"></i> ${label}
        </g:link>
    </li>
</g:else>
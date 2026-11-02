<%-- params: label, icon, controller, action (optional), linkParams (optional map, যেমন [type: 'branch']), soon (optional: day number the module arrives) --%>
<g:if test="${soon}">
    <li class="nav-item">
        <span class="nav-link soon" title="Day ${soon}-এ আসছে">
            <i class="bi ${icon}"></i> ${label} <span class="badge text-bg-light border">Day ${soon}</span>
        </span>
    </li>
</g:if>
<g:else>
    <li class="nav-item">
        <g:link controller="${controller}" action="${action ?: 'index'}" params="${linkParams ?: [:]}"
                class="nav-link ${controllerName == controller && ((action == 'myProfile') == (actionName == 'myProfile')) && (!linkParams || params.type == linkParams.type) ? 'active' : ''}">
            <i class="bi ${icon}"></i> ${label}
        </g:link>
    </li>
</g:else>
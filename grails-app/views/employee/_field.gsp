<%-- params: bean, name, label, type (text|email|date|number|textarea), required, col (default 6), placeholder, help --%>
<g:set var="fieldValue" value="${bean[name]}"/>
<g:set var="hasError" value="${bean.errors.hasFieldErrors(name)}"/>
<div class="col-md-${col ?: 6}">
    <label for="${name}" class="form-label">${label}<g:if test="${required}"> <span class="text-danger">*</span></g:if></label>
    <g:if test="${type == 'textarea'}">
        <textarea id="${name}" name="${name}" rows="2" class="form-control ${hasError ? 'is-invalid' : ''}" placeholder="${placeholder ?: ''}">${fieldValue}</textarea>
    </g:if>
    <g:else>
        <input type="${type ?: 'text'}" id="${name}" name="${name}" value="${fieldValue}"
               class="form-control ${hasError ? 'is-invalid' : ''}" placeholder="${placeholder ?: ''}"
               <g:if test="${type == 'number'}">min="0" step="0.01"</g:if>
               <g:if test="${required}">required</g:if>>
    </g:else>
    <g:hasErrors bean="${bean}" field="${name}">
        <div class="invalid-feedback d-block"><g:fieldError bean="${bean}" field="${name}"/></div>
    </g:hasErrors>
    <g:if test="${help}"><div class="form-text">${help}</div></g:if>
</div>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>${instance.id ? 'সম্পাদনা' : 'নতুন'} ${cfg.title}</title>
</head>
<body>
<h1 class="page-title">${instance.id ? cfg.title + ' সম্পাদনা' : 'নতুন ' + cfg.title}</h1>

<g:hasErrors bean="${instance}">
    <div class="alert alert-danger">
        <ul class="mb-0">
            <g:eachError bean="${instance}" var="err"><li><g:message error="${err}"/></li></g:eachError>
        </ul>
    </div>
</g:hasErrors>

<div class="card" style="max-width:640px;">
    <div class="card-body">
        <g:form controller="master" action="${instance.id ? 'update' : 'save'}" id="${instance.id}" params="[type: type]" method="POST">
            <g:each in="${cfg.fields}" var="f">
                <div class="mb-3">
                    <label for="${f.name}" class="form-label">${f.label}<g:if test="${f.required}"> <span class="text-danger">*</span></g:if></label>
                    <input type="${f.number ? 'number' : 'text'}" id="${f.name}" name="${f.name}" value="${instance[f.name]}"
                           class="form-control ${instance.errors.hasFieldErrors(f.name) ? 'is-invalid' : ''}"
                           <g:if test="${f.number}">min="0" step="any"</g:if>
                           <g:if test="${f.required}">required</g:if>>
                    <g:hasErrors bean="${instance}" field="${f.name}">
                        <div class="invalid-feedback d-block"><g:fieldError bean="${instance}" field="${f.name}"/></div>
                    </g:hasErrors>
                </div>
            </g:each>
            <button type="submit" class="btn btn-primary"><i class="bi bi-check-lg"></i> সংরক্ষণ</button>
            <g:link controller="master" action="index" params="[type: type]" class="btn btn-secondary">বাতিল</g:link>
        </g:form>
    </div>
</div>
</body>
</html>
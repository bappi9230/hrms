<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>${cfg.title}</title>
</head>
<body>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="page-title mb-0">${cfg.title} <span class="badge text-bg-secondary fs-6">${items.size()}</span></h1>
    <g:link controller="master" action="create" params="[type: type]" class="btn btn-primary"><i class="bi bi-plus-lg"></i> নতুন ${cfg.title}</g:link>
</div>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
            <tr>
                <g:each in="${cfg.fields}" var="f"><th>${f.label}</th></g:each>
                <th style="width:120px;"></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${items}" var="item">
                <tr>
                    <g:each in="${cfg.fields}" var="f">
                        <td>${item[f.name] != null ? item[f.name] : '—'}</td>
                    </g:each>
                    <td class="text-end">
                        <g:link controller="master" action="edit" id="${item.id}" params="[type: type]" class="btn btn-sm btn-outline-secondary"><i class="bi bi-pencil"></i></g:link>
                        <g:form controller="master" action="delete" id="${item.id}" params="[type: type]" method="POST" class="d-inline"
                                onsubmit="return confirm('মুছে ফেলবেন?');">
                            <button type="submit" class="btn btn-sm btn-outline-danger"><i class="bi bi-trash"></i></button>
                        </g:form>
                    </td>
                </tr>
            </g:each>
            <g:if test="${!items}">
                <tr><td colspan="${cfg.fields.size() + 1}" class="text-center text-muted py-4">কিছু নেই।</td></tr>
            </g:if>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>
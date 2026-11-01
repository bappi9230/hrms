<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>কর্মী সম্পাদনা</title>
</head>
<body>
<h1 class="page-title">কর্মী সম্পাদনা: ${employee.name}</h1>

<g:uploadForm action="update" id="${employee.id}" method="POST">
    <g:render template="form"/>
    <button type="submit" class="btn btn-primary"><i class="bi bi-check-lg"></i> হালনাগাদ</button>
    <g:link action="show" id="${employee.id}" class="btn btn-secondary">বাতিল</g:link>
</g:uploadForm>
</body>
</html>
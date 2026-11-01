<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>নতুন কর্মী</title>
</head>
<body>
<h1 class="page-title">নতুন কর্মী যোগ করুন</h1>

<g:uploadForm action="save" method="POST">
    <g:render template="form"/>
    <button type="submit" class="btn btn-primary"><i class="bi bi-check-lg"></i> সংরক্ষণ</button>
    <g:link action="index" class="btn btn-secondary">বাতিল</g:link>
</g:uploadForm>
</body>
</html>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>পাসওয়ার্ড পরিবর্তন</title>
</head>
<body>
<h1 class="page-title">পাসওয়ার্ড পরিবর্তন</h1>

<div class="card" style="max-width: 520px;">
    <div class="card-body">
        <g:form action="updatePassword" method="POST">
            <div class="mb-3">
                <label for="currentPassword" class="form-label">বর্তমান পাসওয়ার্ড <span class="text-danger">*</span></label>
                <input type="password" class="form-control" id="currentPassword" name="currentPassword" required autocomplete="current-password">
            </div>
            <div class="mb-3">
                <label for="newPassword" class="form-label">নতুন পাসওয়ার্ড <span class="text-danger">*</span></label>
                <input type="password" class="form-control" id="newPassword" name="newPassword" required minlength="8" autocomplete="new-password">
                <div class="form-text">কমপক্ষে ৮ অক্ষর।</div>
            </div>
            <div class="mb-3">
                <label for="confirmPassword" class="form-label">নতুন পাসওয়ার্ড (আবার) <span class="text-danger">*</span></label>
                <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" required minlength="8" autocomplete="new-password">
            </div>
            <button type="submit" class="btn btn-primary">পরিবর্তন করুন</button>
            <g:link controller="dashboard" class="btn btn-secondary">বাতিল</g:link>
        </g:form>
    </div>
</div>
</body>
</html>
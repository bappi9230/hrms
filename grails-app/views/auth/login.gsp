<!doctype html>
<html>
<head>
    <meta name="layout" content="auth"/>
    <title>লগইন</title>
</head>
<body>
<div class="card auth-card">
    <div class="card-body p-4 p-md-5">
        <div class="text-center mb-4">
            <div class="auth-brand"><i class="bi bi-people-fill"></i> HRMS</div>
            <div class="text-muted">হিউম্যান রিসোর্স ম্যানেজমেন্ট সিস্টেম</div>
        </div>

        <g:if test="${loginError}">
            <div class="alert alert-danger py-2" role="alert">ইউজারনেম বা পাসওয়ার্ড ভুল। আবার চেষ্টা করুন।</div>
        </g:if>

        <form action="${postUrl}" method="POST" autocomplete="on">
            <div class="mb-3">
                <label for="username" class="form-label">ইউজারনেম</label>
                <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-person"></i></span>
                    <input type="text" class="form-control" name="username" id="username" required autofocus autocomplete="username">
                </div>
            </div>
            <div class="mb-3">
                <label for="password" class="form-label">পাসওয়ার্ড</label>
                <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-lock"></i></span>
                    <input type="password" class="form-control" name="password" id="password" required autocomplete="current-password">
                </div>
            </div>
            <button type="submit" class="btn btn-primary w-100 py-2">লগইন</button>
            <div class="text-center mt-3 small text-muted">পাসওয়ার্ড ভুলে গেলে এইচআর/অ্যাডমিনের সাথে যোগাযোগ করুন।</div>
        </form>

        <g:if env="development">
            <hr>
            <div class="small text-muted">
                <div class="fw-semibold mb-1">ডেমো অ্যাকাউন্ট (শুধু development)</div>
                <table class="table table-sm table-borderless mb-0 small">
                    <tr><td>admin</td><td>অ্যাডমিন</td></tr>
                    <tr><td>hr</td><td>এইচআর</td></tr>
                    <tr><td>manager</td><td>ম্যানেজার</td></tr>
                    <tr><td>accountant</td><td>হিসাব বিভাগ</td></tr>
                    <tr><td>employee</td><td>কর্মী</td></tr>
                </table>
                <div>পাসওয়ার্ড (সবার): <code>Hrms@2026</code></div>
            </div>
        </g:if>
    </div>
</div>
</body>
</html>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>${course.id ? 'কোর্স সম্পাদনা' : 'নতুন কোর্স'}</title>
</head>
<body>
<h1 class="page-title">${course.id ? 'কোর্স সম্পাদনা' : 'নতুন কোর্স'}</h1>

<div class="card" style="max-width:760px;">
    <div class="card-body">
        <g:form action="${course.id ? 'update' : 'save'}" id="${course.id}" method="POST" class="row g-3">
            <div class="col-12">
                <label class="form-label" for="title">কোর্সের নাম <span class="text-danger">*</span></label>
                <input type="text" id="title" name="title" value="${course.title}" maxlength="150" class="form-control" required>
            </div>
            <div class="col-12">
                <label class="form-label" for="description">বিবরণ</label>
                <textarea id="description" name="description" rows="3" maxlength="1000" class="form-control">${course.description}</textarea>
            </div>
            <div class="col-md-4">
                <label class="form-label" for="category">ধরন <span class="text-danger">*</span></label>
                <select id="category" name="category" class="form-select"><g:each in="${categories}" var="c"><option value="${c}" ${course.category == c ? 'selected' : ''}><hrms:d8Name value="${c}"/></option></g:each></select>
            </div>
            <div class="col-md-4">
                <label class="form-label" for="trainingMode">মাধ্যম <span class="text-danger">*</span></label>
                <select id="trainingMode" name="trainingMode" class="form-select"><g:each in="${modes}" var="m"><option value="${m}" ${course.trainingMode == m ? 'selected' : ''}><hrms:d8Name value="${m}"/></option></g:each></select>
            </div>
            <div class="col-md-4">
                <label class="form-label" for="trainer">প্রশিক্ষক</label>
                <input type="text" id="trainer" name="trainer" value="${course.trainer}" maxlength="100" class="form-control">
            </div>
            <div class="col-md-6">
                <label class="form-label" for="location">স্থান / লিংক</label>
                <input type="text" id="location" name="location" value="${course.location}" maxlength="150" class="form-control">
            </div>
            <div class="col-md-3">
                <label class="form-label" for="hours">মোট ঘণ্টা <span class="text-danger">*</span></label>
                <input type="number" id="hours" name="hours" value="${course.hours}" min="1" max="500" class="form-control" required>
            </div>
            <div class="col-md-3">
                <label class="form-label" for="capacity">আসন (0 = সীমাহীন)</label>
                <input type="number" id="capacity" name="capacity" value="${course.capacity}" min="0" class="form-control">
            </div>
            <div class="col-md-6">
                <label class="form-label" for="startDate">শুরুর তারিখ <span class="text-danger">*</span></label>
                <input type="date" id="startDate" name="startDate" value="${course.startDate}" class="form-control" required>
            </div>
            <div class="col-md-6">
                <label class="form-label" for="endDate">শেষের তারিখ <span class="text-danger">*</span></label>
                <input type="date" id="endDate" name="endDate" value="${course.endDate}" class="form-control" required>
            </div>
            <div class="col-12">
                <button type="submit" class="btn btn-primary"><i class="bi bi-check-lg"></i> সংরক্ষণ</button>
                <g:link action="${course.id ? 'show' : 'index'}" id="${course.id}" class="btn btn-secondary">বাতিল</g:link>
            </div>
        </g:form>
    </div>
</div>
</body>
</html>

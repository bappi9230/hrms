<g:hasErrors bean="${employee}">
    <div class="alert alert-danger">
        <div class="fw-semibold mb-1">ফর্মে ভুল আছে, নিচের লাল চিহ্নিত ঘরগুলো ঠিক করুন:</div>
        <ul class="mb-0">
            <g:eachError bean="${employee}" var="err"><li><g:message error="${err}"/></li></g:eachError>
        </ul>
    </div>
</g:hasErrors>

<div class="card mb-3">
    <div class="card-header bg-white fw-semibold">ব্যক্তিগত তথ্য</div>
    <div class="card-body">
        <div class="row g-3">
            <g:render template="field" model="[bean: employee, name: 'name', label: 'পূর্ণ নাম', required: true]"/>
            <g:render template="field" model="[bean: employee, name: 'email', label: 'ইমেইল', type: 'email', required: true]"/>
            <g:render template="field" model="[bean: employee, name: 'phone', label: 'ফোন', placeholder: '01XXXXXXXXX']"/>
            <g:render template="field" model="[bean: employee, name: 'dateOfBirth', label: 'জন্ম তারিখ', type: 'date']"/>

            <div class="col-md-6">
                <label for="gender" class="form-label">লিঙ্গ</label>
                <g:select name="gender" id="gender" from="${genders}" value="${employee.gender}"
                          noSelection="['': '-- নির্বাচন করুন --']" class="form-select ${employee.errors.hasFieldErrors('gender') ? 'is-invalid' : ''}"/>
            </div>
            <g:render template="field" model="[bean: employee, name: 'nid', label: 'NID নম্বর', help: '১০, ১৩ বা ১৭ অঙ্কের সংখ্যা']"/>
            <g:render template="field" model="[bean: employee, name: 'presentAddress', label: 'বর্তমান ঠিকানা', type: 'textarea']"/>
            <g:render template="field" model="[bean: employee, name: 'permanentAddress', label: 'স্থায়ী ঠিকানা', type: 'textarea']"/>
        </div>
    </div>
</div>

<div class="card mb-3">
    <div class="card-header bg-white fw-semibold">জরুরি যোগাযোগ</div>
    <div class="card-body">
        <div class="row g-3">
            <g:render template="field" model="[bean: employee, name: 'emergencyContactName', label: 'নাম', col: 4]"/>
            <g:render template="field" model="[bean: employee, name: 'emergencyContactPhone', label: 'ফোন', col: 4]"/>
            <g:render template="field" model="[bean: employee, name: 'emergencyContactRelation', label: 'সম্পর্ক', col: 4, placeholder: 'যেমন: পিতা, স্ত্রী']"/>
        </div>
    </div>
</div>

<div class="card mb-3">
    <div class="card-header bg-white fw-semibold">চাকরির তথ্য</div>
    <div class="card-body">
        <div class="row g-3">
            <div class="col-md-6">
                <label for="department.id" class="form-label">বিভাগ <span class="text-danger">*</span></label>
                <g:select name="department.id" id="department.id" from="${departments}" optionKey="id" value="${employee.department?.id}"
                          noSelection="['': '-- নির্বাচন করুন --']" required="required"
                          class="form-select ${employee.errors.hasFieldErrors('department') ? 'is-invalid' : ''}"/>
            </div>
            <div class="col-md-6">
                <label for="designation.id" class="form-label">পদবি <span class="text-danger">*</span></label>
                <g:select name="designation.id" id="designation.id" from="${designations}" optionKey="id" value="${employee.designation?.id}"
                          noSelection="['': '-- নির্বাচন করুন --']" required="required"
                          class="form-select ${employee.errors.hasFieldErrors('designation') ? 'is-invalid' : ''}"/>
            </div>
            <div class="col-md-6">
                <label for="employeeType" class="form-label">কর্মীর ধরন <span class="text-danger">*</span></label>
                <g:select name="employeeType" id="employeeType" from="${types}" value="${employee.employeeType}" class="form-select"/>
            </div>
            <div class="col-md-6">
                <label for="status" class="form-label">স্ট্যাটাস <span class="text-danger">*</span></label>
                <g:select name="status" id="status" from="${statuses}" value="${employee.status}" class="form-select"/>
            </div>
            <g:render template="field" model="[bean: employee, name: 'joinDate', label: 'যোগদানের তারিখ', type: 'date', required: true]"/>
            <g:render template="field" model="[bean: employee, name: 'salary', label: 'বেতন (৳)', type: 'number', required: true]"/>
        </div>
    </div>
</div>

<div class="card mb-3">
    <div class="card-header bg-white fw-semibold">ছবি</div>
    <div class="card-body">
        <div class="row g-3 align-items-center">
            <div class="col-auto">
                <g:if test="${employee.hasPhoto}">
                    <img id="photoPreview" class="avatar avatar-xl" src="${createLink(action: 'photo', id: employee.id)}" alt="">
                </g:if>
                <g:else>
                    <img id="photoPreview" class="avatar avatar-xl d-none" alt="">
                    <span id="photoPlaceholder" class="avatar avatar-xl"><i class="bi bi-person"></i></span>
                </g:else>
            </div>
            <div class="col">
                <input type="file" class="form-control" name="photoFile" id="photoFile" accept="image/jpeg,image/png,image/webp">
                <div class="form-text">JPG, PNG বা WEBP, সর্বোচ্চ ১ MB।</div>
                <g:hasErrors bean="${employee}" field="photo">
                    <div class="text-danger small"><g:fieldError bean="${employee}" field="photo"/></div>
                </g:hasErrors>
                <g:if test="${employee.hasPhoto}">
                    <div class="form-check mt-2">
                        <input class="form-check-input" type="checkbox" name="removePhoto" id="removePhoto">
                        <label class="form-check-label" for="removePhoto">বর্তমান ছবি মুছে ফেলুন</label>
                    </div>
                </g:if>
            </div>
        </div>
    </div>
</div>

<script>
    document.getElementById('photoFile').addEventListener('change', function () {
        var f = this.files[0];
        if (!f) return;
        var img = document.getElementById('photoPreview');
        img.src = URL.createObjectURL(f);
        img.classList.remove('d-none');
        var ph = document.getElementById('photoPlaceholder');
        if (ph) ph.classList.add('d-none');
    });
</script>
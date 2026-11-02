<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>অর্গ চার্ট</title>
</head>
<body>
<h1 class="page-title">অর্গ চার্ট <span class="badge text-bg-secondary fs-6">${total}</span></h1>
<p class="text-muted small">রিপোর্টিং ম্যানেজার অনুযায়ী সাজানো। ম্যানেজার ঠিক করা হয় কর্মীর প্রোফাইল সম্পাদনা থেকে।</p>

<div class="card">
    <div class="card-body">
        <g:if test="${roots}">
            <ul class="org-tree">
                <g:each in="${roots}" var="r">
                    <g:render template="node" model="[emp: r, children: children]"/>
                </g:each>
            </ul>
        </g:if>
        <g:else><span class="text-muted">কোনো কর্মী নেই।</span></g:else>
    </div>
</div>
</body>
</html>
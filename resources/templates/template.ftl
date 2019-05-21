<#macro mainLayout title="Welcome">
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

    <title>${title} | Pizza service</title>
    <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/pure/0.6.0/pure-min.css">
    <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/pure/0.6.0/grids-responsive-min.css">
    <#--<link rel="stylesheet" type="text/css" href="/static/blog.css">-->
</head>
<body>
<div class="pure-g">

    <div class="content pure-u-1 pure-u-md-3-4">
        <h2>${title}</h2>
        <#nested />
    </div>
    <div class="footer">
       Pizza service, ${.now?string("yyyy")}
    </div>
</div>
</body>
</html>
</#macro>
<#macro order_li order>
<section class="post">
    <header class="post-header">
        <p class="post-meta">
            <a href="/order/${order.id}">${order.id}</a>
    </header>
    <div class="post-description">${kweet.text}</div>
</section>
</#macro>

<#macro order_list orders>
<ul>
    <#list orders as order>
        <@order_li order=order></@order_li>
    <#else>
        <li>There are no orders yet</li>
    </#list>
</ul>
</#macro>



<#macro enumSelect selectName enumValues>
    <select name="${selectName}">
        <#list enumValues as enum>
        <option value="${enum}">${enum}</option>
        </#list>
    </select>
</#macro>

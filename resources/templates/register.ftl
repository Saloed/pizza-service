<#-- @ftlvariable name="error" type="java.lang.String" -->
<#-- @ftlvariable name="login" type="java.lang.String" -->

<#import "template.ftl" as layout />

<@layout.mainLayout title="Registration form">
<form class="pure-form-stacked" action="/register" method="post" enctype="application/x-www-form-urlencoded">
    <#if error??>
        <p class="error">${error}</p>
    </#if>

    <label for="login">Login
        <input type="text" name="login" id="login" value="${login}">
    </label>

    <label for="password">Password
        <input type="password" name="password" id="password">
    </label>


    <input class="pure-button pure-button-primary" type="submit" value="Register">
</form>
</@layout.mainLayout>

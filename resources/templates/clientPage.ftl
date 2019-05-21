<#-- @ftlvariable name="login" type="java.lang.String" -->
<#-- @ftlvariable name="address" type="java.lang.String" -->

<#import "template.ftl" as layout />

<@layout.mainLayout title="User ${login}">
    <h3>My address: ${address}</h3>
</@layout.mainLayout>

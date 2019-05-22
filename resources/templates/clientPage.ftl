<#-- @ftlvariable name="login" type="java.lang.String" -->
<#-- @ftlvariable name="address" type="java.lang.String" -->
<#-- @ftlvariable name="orders" type="java.util.List<ru.spbstu.architectures.pizzaService.web.UserKt.UserOrderListItem>" -->

<#import "template.ftl" as layout />

<@layout.mainLayout title="User ${login}">
    <h3>My address: ${address}</h3>

<@layout.order_list orders=orders></@layout.order_list>
</@layout.mainLayout>

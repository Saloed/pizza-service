package ru.spbstu.architectures

import io.restassured.RestAssured.given
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import ru.spbstu.architectures.pizzaService.models.*
import ru.spbstu.architectures.pizzaService.web.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class OrderTest : ApplicationTest() {

    @Test
    fun testOrderCreate() {
        createOrder(client)
    }

    @Test
    fun testOrderCancelAfterCreate() {
        val order = createOrder(client)
        cancelOrder(client, order.id)
    }

    @Test
    fun testSimpleWorkflow() {
        var order = createOrder(client)
        order = runOrder(client, order.id)
        var dbOrder = runBlocking { Order.modelManager.get(order.id) }
        assertNotNull(dbOrder)
        assertEquals(client.id, dbOrder.client.id, "client")
        assertEquals(manager.id, dbOrder.manager?.id, "manager")
        assertEquals(operator.id, dbOrder.operator?.id, "operator")
        order = approveOrder(operator, order.id)
        order = processOrder(manager, order.id)
        order = readyOrder(manager, order.id)
        dbOrder = runBlocking { Order.modelManager.get(order.id) }
        assertNotNull(dbOrder)
        assertEquals(courier.id, dbOrder.courier?.id, "courier")
        order = deliverOrder(courier, order.id)
        val orderCost = order.cost
        assertNotNull(orderCost, "Order cost")
        val payment = createOrderPayment(courier, order.id, orderCost)
        closeOrder(courier, order.id)
    }

    @Test
    fun testWorkflowWithPromo() {
        var promo = createPromo()
        promo = runPromo(promo.id)
        var order = createOrder(client)
        order = applyOrderPromo(client, order.id, promo)
        order = runOrder(client, order.id)
        var dbOrder = runBlocking { Order.modelManager.get(order.id) }
        assertNotNull(dbOrder)
        assertEquals(client.id, dbOrder.client.id, "client")
        assertEquals(manager.id, dbOrder.manager?.id, "manager")
        assertEquals(operator.id, dbOrder.operator?.id, "operator")
        order = approveOrder(operator, order.id)
        order = processOrder(manager, order.id)
        order = readyOrder(manager, order.id)
        dbOrder = runBlocking { Order.modelManager.get(order.id) }
        assertNotNull(dbOrder)
        assertEquals(courier.id, dbOrder.courier?.id, "courier")
        order = deliverOrder(courier, order.id)
        val orderCost = order.cost
        assertNotNull(orderCost, "Order cost")
        val payment = createOrderPayment(courier, order.id, orderCost)
        closeOrder(courier, order.id)
    }

    private fun createOrder(user: User, statusCode: Int = 200): OrderWithPermission {
        val allPizza = given()
            .withUser(user)
            .get("/pizza")
            .then().statusCode(200)
            .extract().toList<Pizza>()
        val orderPizza = allPizza.take(5)
        val orderPizzaIds = orderPizza.map { it.id }
        val orderForm = OrderCreateForm(orderPizzaIds)
        val response = given()
            .withUser(user)
            .jsonBody(orderForm)
            .When().post("/order")
            .then().statusCode(statusCode)
            .extract().to<OrderWithPermission>()
        assertNotNull(response.id, "Order id")
        assertEquals(OrderStatus.DRAFT.name, response.status, "Status")
        return response
    }

    private fun cancelOrder(user: User, orderId: Int, statusCode: Int = 200) =
        changeOrderStatus(user, orderId, OrderStatus.CANCELED, statusCode)

    private fun applyOrderPromo(
        user: User,
        orderId: Int,
        promo: PromoWithPermission,
        statusCode: Int = 200
    ): OrderWithPermission {
        val form = OrderModificationForm(OrderStatus.DRAFT.name, promo.id)
        val response = changeOrderStatus(user, orderId, OrderStatus.DRAFT, statusCode, form)
        assertNotNull(response.promo, "Promo is null")
        return response
    }

    private fun runOrder(user: User, orderId: Int, statusCode: Int = 200) =
        changeOrderStatus(user, orderId, OrderStatus.NEW, statusCode)


    private fun approveOrder(user: User, orderId: Int, statusCode: Int = 200) =
        changeOrderStatus(user, orderId, OrderStatus.APPROVED, statusCode)

    private fun processOrder(user: User, orderId: Int, statusCode: Int = 200) =
        changeOrderStatus(user, orderId, OrderStatus.PROCESSING, statusCode)

    private fun readyOrder(user: User, orderId: Int, statusCode: Int = 200) =
        changeOrderStatus(user, orderId, OrderStatus.READY, statusCode)

    private fun deliverOrder(user: User, orderId: Int, statusCode: Int = 200) =
        changeOrderStatus(user, orderId, OrderStatus.SHIPPING, statusCode)

    private fun closeOrder(user: User, orderId: Int, statusCode: Int = 200) =
        changeOrderStatus(user, orderId, OrderStatus.CLOSED, statusCode)


    private fun changeOrderStatus(
        user: User,
        orderId: Int,
        status: OrderStatus,
        statusCode: Int = 200,
        form: OrderModificationForm? = null
    ): OrderWithPermission {
        val modificationForm = form?.copy(status = status.name) ?: OrderModificationForm(status.name, null)
        val response = modifyOrder(user, orderId, modificationForm, statusCode)
        assertEquals(status.name, response.status, "Status")
        return response
    }

    private fun modifyOrder(
        user: User,
        orderId: Int,
        modification: OrderModificationForm,
        statusCode: Int = 200
    ): OrderWithPermission = given()
        .withUser(user)
        .jsonBody(modification)
        .When().put("/order/$orderId")
        .then().statusCode(statusCode)
        .extract().to<OrderWithPermission>().also {
            assertNotNull(it.id, "Order id")
        }

    private fun createOrderPayment(
        user: User,
        orderId: Int,
        amount: Int,
        statusCode: Int = 200
    ): PaymentWithPermission {
        val paymentForm = PaymentCreateForm(orderId, PaymentType.CARD.name, amount, "some random transaction")
        return given().withUser(user)
            .jsonBody(paymentForm)
            .When().post("/payment")
            .then().statusCode(statusCode)
            .extract().to<PaymentWithPermission>()
            .also { assertNotNull(it.id, "Payment id") }
    }

    private fun createPromo(): PromoWithPermission {
        val clientIds = listOf(client, client2).map { it.id }
        val promoCreationForm = PromoCreationForm(clientIds, PromoEffect.DISCOUNT_10.name, "promo description")
        return given().withUser(manager)
            .jsonBody(promoCreationForm)
            .When().post("/promo")
            .then().statusCode(200)
            .extract().to<PromoWithPermission>()
            .also { assertNotNull(it.id, "Promo id") }
    }

    private fun runPromo(promoId: Int): PromoWithPermission{
        val promoModification = PromoModificationForm(PromoStatus.ACTIVE.name, null)
        return given().withUser(manager)
            .jsonBody(promoModification)
            .When().put("/promo/$promoId")
            .then().statusCode(200)
            .extract().to<PromoWithPermission>()
            .also { assertNotNull(it.id, "Promo id") }
            .also { assertEquals(PromoStatus.ACTIVE.name, it.status, "Promo status") }
    }

}

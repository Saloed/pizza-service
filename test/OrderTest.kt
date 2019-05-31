package ru.spbstu.architectures

import io.ktor.http.HttpMethod
import org.junit.Test
import ru.spbstu.architectures.pizzaService.models.Pizza
import ru.spbstu.architectures.pizzaService.web.OrderCreateForm

class OrderTest : ApplicationTest() {

    @Test
    fun testLoginSuccessWithTracker() = testApp {
        val allPizza = handleRequestWithUser(HttpMethod.Get, "/pizza", client).getJsonList<Pizza>()
        val orderPizza = allPizza.take(5)
        val orderPizzaIds = orderPizza.map { it.id }
        val orderForm = OrderCreateForm(orderPizzaIds)
        val response = handleRequestWithUser(HttpMethod.Post, "/order", manager) {
            setJsonBody(orderForm)
        }

        println("$response")
    }


}

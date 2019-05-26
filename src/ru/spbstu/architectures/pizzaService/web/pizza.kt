package ru.spbstu.architectures.pizzaService.web

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import ru.spbstu.architectures.pizzaService.db.manager.all
import ru.spbstu.architectures.pizzaService.logic.OrderLogic
import ru.spbstu.architectures.pizzaService.logic.PizzaLogic
import ru.spbstu.architectures.pizzaService.models.Pizza
import ru.spbstu.architectures.pizzaService.utils.getListQueryParams
import ru.spbstu.architectures.pizzaService.utils.responseListRange
import ru.spbstu.architectures.pizzaService.utils.userOrNull


data class PizzaListFilter(val orderId: Int?, val id: Int?)

data class PizzaResponse(val id: Int, val name: String)

fun Route.pizza() {
    get("/pizza") {
        val user = call.userOrNull ?: return@get call.respond(HttpStatusCode.Forbidden, "")
        val params = call.getListQueryParams<PizzaListFilter>()
        val pizza = when {
            params.filter?.orderId != null -> PizzaLogic.list(user, params.filter.orderId)
            else -> PizzaLogic.list()
        } ?: return@get call.respond(HttpStatusCode.NotFound, "")
        val response = call.responseListRange(pizza, params.range)
        call.respond(response)
    }
}

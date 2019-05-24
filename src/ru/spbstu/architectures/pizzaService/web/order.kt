package ru.spbstu.architectures.pizzaService.web

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.contentRange
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import ru.spbstu.architectures.pizzaService.logic.ClientOrder
import ru.spbstu.architectures.pizzaService.models.Client
import ru.spbstu.architectures.pizzaService.utils.getListQueryParams
import ru.spbstu.architectures.pizzaService.utils.userOrNull

data class OrderResponse(val id: Int, val status: String, val isPayed: Boolean)

@Location("/order/{id}")
data class SingleOrderRequest(val id: Int)

data class OrderPizzaItem(val id: Int, val name: String)

fun Route.order() {
    get("/order") {
        val user = call.userOrNull ?: return@get call.respond(HttpStatusCode.Forbidden, "")
        val data = when (user) {
            is Client -> ClientOrder.list(user)
            else -> emptyList()
        }
        val responseData = data.map { OrderResponse(it.id, it.status.name, it.payment != null) }
        call.response.contentRange(0..responseData.lastIndex.toLong(), responseData.size.toLong())
        call.respond(responseData)
    }
    get<SingleOrderRequest> {
        val user = call.userOrNull ?: return@get call.respond(HttpStatusCode.Forbidden, "")
        val data = when (user) {
            is Client -> ClientOrder.get(user, it.id)
            else -> null
        } ?: return@get call.respond(HttpStatusCode.NotFound, "")
        val responseData = OrderResponse(data.id, data.status.name, data.payment != null)
        call.respond(responseData)
    }
}

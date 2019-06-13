package ru.spbstu.architectures.pizzaService.web

import de.nielsfalk.ktor.swagger.*
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.response.respond
import io.ktor.routing.Route
import ru.spbstu.architectures.pizzaService.logic.OrderLogic
import ru.spbstu.architectures.pizzaService.logic.OrderModification
import ru.spbstu.architectures.pizzaService.models.OrderStatus
import ru.spbstu.architectures.pizzaService.models.OrderWithPermission
import ru.spbstu.architectures.pizzaService.utils.*

data class OrderListFilter(val id: Int?)

@Location("/order")
class OrderPath

@Location("/order/{id}")
data class SingleOrderRequest(val id: Int)

data class OrderCreateForm(val pizza: List<Int>)

data class OrderModificationForm(val status: String, val promoId: Int?)

fun Route.order() {
    get<OrderPath>(
        "all".description("List of orders")
            .withAuthorization()
            .listQueryParameters<OrderListFilter>()
            .responds(
                ok<OrderWithPermission>()
            )
    ) {
        val user = call.userOrNull ?: return@get call.respond(HttpStatusCode.Forbidden, "")
        val params = call.getListQueryParams<OrderListFilter>()
        val data = OrderLogic.list(user)
        call.responseListRange(data, params.range)
    }
    get<SingleOrderRequest>(
        "all".description("Single order")
            .withAuthorization()
            .responds(
                notFound(),
                ok<OrderWithPermission>()
            )
    ) {
        val user = call.userOrNull ?: return@get call.respond(HttpStatusCode.Forbidden, "")
        val data = OrderLogic.get(user, it.id) ?: return@get call.respond(HttpStatusCode.NotFound, "")
        call.respond(data)
    }
    post<OrderPath, OrderCreateForm>(
        "all".description("Create order")
            .withAuthorization()
            .responds(
                created<OrderWithPermission>()
            )
    ) { _, form ->
        val user = call.userOrNull ?: return@post call.respond(HttpStatusCode.Forbidden, "")
        val result = OrderLogic.create(user, form.pizza)
        call.respondMyResult(result)
    }
    put<SingleOrderRequest, OrderModificationForm>(
        "all".description("Modify order")
            .withAuthorization()
            .responds(
                created<OrderWithPermission>()
            )
    ) { it, form ->
        val user = call.userOrNull ?: return@put call.respond(HttpStatusCode.Forbidden, "")
        val status = OrderStatus.valueOf(form.status.toUpperCase())
        val orderModification = OrderModification(status, form.promoId)
        val result = OrderLogic.change(user, it.id, orderModification)
        call.respondMyResult(result)
    }
}

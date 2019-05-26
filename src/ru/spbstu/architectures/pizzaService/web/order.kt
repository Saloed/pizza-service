package ru.spbstu.architectures.pizzaService.web

import io.ktor.application.call
import io.ktor.application.log
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import ru.spbstu.architectures.pizzaService.logic.OrderLogic
import ru.spbstu.architectures.pizzaService.models.OrderStatus
import ru.spbstu.architectures.pizzaService.utils.MyResult
import ru.spbstu.architectures.pizzaService.utils.getListQueryParams
import ru.spbstu.architectures.pizzaService.utils.responseListRange
import ru.spbstu.architectures.pizzaService.utils.userOrNull

data class OrderListFilter(val id: Int?)

@Location("/order/{id}")
data class SingleOrderRequest(val id: Int)

data class OrderCreateForm(val pizza: List<Int>)

data class OrderModificationForm(val status: String)

fun Route.order() {
    get("/order") {
        val user = call.userOrNull ?: return@get call.respond(HttpStatusCode.Forbidden, "")
        val params = call.getListQueryParams<OrderListFilter>()
        val data = OrderLogic.list(user)
        val response = call.responseListRange(data, params.range)
        call.respond(response)
    }
    get<SingleOrderRequest> {
        val user = call.userOrNull ?: return@get call.respond(HttpStatusCode.Forbidden, "")
        val data = OrderLogic.get(user, it.id) ?: return@get call.respond(HttpStatusCode.NotFound, "")
        call.respond(data)
    }
    post("/order") {
        val user = call.userOrNull ?: return@post call.respond(HttpStatusCode.Forbidden, "")
        val form = call.receive<OrderCreateForm>()
        val result = OrderLogic.create(user, form.pizza)
        val res = when (result) {
            is MyResult.Error -> return@post call.respond(HttpStatusCode.BadRequest, result.message)
            is MyResult.Success -> result.data
        } ?: let {
            call.application.log.error("Order not found after create")
            return@post call.respond(HttpStatusCode.NotFound, "")
        }
        call.respond(res)
    }
    post<SingleOrderRequest> {
        val user = call.userOrNull ?: return@post call.respond(HttpStatusCode.Forbidden, "")
        val form = call.receive<OrderModificationForm>()
        val status = OrderStatus.valueOf(form.status.toUpperCase())
        val result = OrderLogic.change(user, it.id, status)
        val res = when (result) {
            is MyResult.Error -> return@post call.respond(HttpStatusCode.BadRequest, result.message)
            is MyResult.Success -> result.data
        } ?: let {
            call.application.log.error("Order not found after update")
            return@post call.respond(HttpStatusCode.NotFound, "")
        }
        call.respond(res)
    }
}

package ru.spbstu.architectures.pizzaService.web

import de.nielsfalk.ktor.swagger.*
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.response.respond
import io.ktor.routing.Route
import ru.spbstu.architectures.pizzaService.logic.PizzaLogic
import ru.spbstu.architectures.pizzaService.models.Pizza
import ru.spbstu.architectures.pizzaService.utils.getListQueryParams
import ru.spbstu.architectures.pizzaService.utils.responseListRange
import ru.spbstu.architectures.pizzaService.utils.userOrNull
import ru.spbstu.architectures.pizzaService.utils.withAuthorization


data class PizzaListFilter(val orderId: Int?, val id: Int?)

@Location("/pizza")
class PizzaPath

fun Route.pizza() {
    get<PizzaPath>(
        "all".description("List pizza")
            .withAuthorization()
            .parameter<PizzaListFilter>()
            .responds(
                ok<Pizza>(),
                notFound()
            )
    ) {
        val user = call.userOrNull ?: return@get call.respond(HttpStatusCode.Forbidden, "")
        val params = call.getListQueryParams<PizzaListFilter>()
        val pizza = when {
            params.filter?.orderId != null -> PizzaLogic.list(user, params.filter.orderId)
            else -> PizzaLogic.list()
        } ?: return@get call.respond(HttpStatusCode.NotFound, "")
        call.responseListRange(pizza, params.range)
    }
}

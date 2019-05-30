package ru.spbstu.architectures.pizzaService.web

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import ru.spbstu.architectures.pizzaService.logic.PromoClientLogic
import ru.spbstu.architectures.pizzaService.utils.getListQueryParams
import ru.spbstu.architectures.pizzaService.utils.respondMyResult
import ru.spbstu.architectures.pizzaService.utils.responseListRange
import ru.spbstu.architectures.pizzaService.utils.userOrNull

@Location("/promoClient/{id}")
data class SinglePromoClient(val id: Int)

data class PromoClientFilter(val promoId: Int?)

fun Route.promoClient() {
    get("/promoClient") {
        val user = call.userOrNull ?: return@get call.respond(HttpStatusCode.Forbidden, "")
        val params = call.getListQueryParams<PromoClientFilter>()
        if (params.filter?.promoId == null) return@get call.respond(HttpStatusCode.BadRequest, "Promo id is required")
        val data = PromoClientLogic.list(user, params.filter.promoId)
        call.respondMyResult(data) {
            responseListRange(it, params.range)
        }
    }
    get<SinglePromoClient> {
        val user = call.userOrNull ?: return@get call.respond(HttpStatusCode.Forbidden, "")
        val data = PromoClientLogic.get(user, it.id)
        call.respondMyResult(data)
    }

}

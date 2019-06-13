package ru.spbstu.architectures.pizzaService.web

import de.nielsfalk.ktor.swagger.*
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.put
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import ru.spbstu.architectures.pizzaService.logic.PromoClientLogic
import ru.spbstu.architectures.pizzaService.models.PromoClientStatus
import ru.spbstu.architectures.pizzaService.models.PromoClientWithPermission
import ru.spbstu.architectures.pizzaService.utils.*

@Location("/promoClient/{id}")
data class SinglePromoClient(val id: Int)

@Location("/promoClient")
class PromoClientPath

data class PromoClientFilter(val promoId: Int?)

data class PromoClientModificationForm(val status: String)

fun Route.promoClient() {
    get<PromoClientPath>(
        "all".description("List promo client")
            .withAuthorization()
            .parameter<PromoClientFilter>()
            .responds(
                ok<PromoClientWithPermission>(),
                badRequest()
            )
    ) {
        val user = call.userOrNull ?: return@get call.respond(HttpStatusCode.Forbidden, "")
        val params = call.getListQueryParams<PromoClientFilter>()
        val data = PromoClientLogic.list(user, params.filter?.promoId)
        call.respondMyResult(data) {
            responseListRange(it, params.range)
        }
    }
    get<SinglePromoClient>(
        "all".description("Get promo client")
            .withAuthorization()
            .responds(
                ok<PromoClientWithPermission>(),
                badRequest()
            )
    ) {
        val user = call.userOrNull ?: return@get call.respond(HttpStatusCode.Forbidden, "")
        val data = PromoClientLogic.get(user, it.id)
        call.respondMyResult(data)
    }
    put<SinglePromoClient, PromoClientModificationForm>(
        "all".description("modify promo client")
            .withAuthorization()
            .responds(
                ok<PromoClientWithPermission>(),
                badRequest()
            )
    ) { it, form ->
        val user = call.userOrNull ?: return@put call.respond(HttpStatusCode.Forbidden, "")
        val status = PromoClientStatus.valueOf(form.status.toUpperCase())
        val data = PromoClientLogic.update(user, it.id, status)
        call.respondMyResult(data)
    }

}

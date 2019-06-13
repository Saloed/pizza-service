package ru.spbstu.architectures.pizzaService.web

import de.nielsfalk.ktor.swagger.*
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.locations.get
import io.ktor.locations.put
import io.ktor.request.receive
import io.ktor.response.respond
import ru.spbstu.architectures.pizzaService.logic.PromoCreationParameters
import ru.spbstu.architectures.pizzaService.logic.PromoLogic
import ru.spbstu.architectures.pizzaService.logic.PromoModificationParams
import ru.spbstu.architectures.pizzaService.models.PromoEffect
import ru.spbstu.architectures.pizzaService.models.PromoStatus
import ru.spbstu.architectures.pizzaService.models.PromoWithPermission
import ru.spbstu.architectures.pizzaService.utils.*

@Location("/promo/{id}")
data class SinglePromo(val id: Int)

@Location("/promo")
class PromoPath

data class PromoListFilter(val id: List<Int>?)

data class PromoCreationForm(val clientIds: List<Int>, val effect: String, val description: String)
data class PromoModificationForm(val status: String, val result: String?)

fun Route.promo() {
    get<PromoPath>(
        "all".description("Get list of promo")
            .withAuthorization()
            .parameter<PromoListFilter>()
            .responds(
                ok<PromoWithPermission>()
            )
    ) {
        val user = call.userOrNull ?: return@get call.respond(HttpStatusCode.Forbidden, "")
        val params = call.getListQueryParams<PromoListFilter>()
        val data = PromoLogic.list(user)
        call.respondMyResult(data) {
            val result = if (params.filter?.id != null) it.filter { it.id in params.filter.id } else it
            responseListRange(result, params.range)
        }
    }
    get<SinglePromo>(
        "all".description("Get promo")
            .withAuthorization()
            .responds(
                ok<PromoWithPermission>()
            )
    ) {
        val user = call.userOrNull ?: return@get call.respond(HttpStatusCode.Forbidden, "")
        val data = PromoLogic.get(user, it.id)
        call.respondMyResult(data)
    }
    post<PromoPath, PromoCreationForm>(
        "all".description("Create promo")
            .withAuthorization()
            .responds(
                ok<PromoWithPermission>(),
                badRequest()
            )
    ) { _, form ->
        val user = call.userOrNull ?: return@post call.respond(HttpStatusCode.Forbidden, "")
        val effect = PromoEffect.valueOf(form.effect.toUpperCase())
        val parameters = PromoCreationParameters(form.clientIds, effect, form.description)
        val data = PromoLogic.create(user, parameters)
        call.respondMyResult(data)
    }
    put<SinglePromo, PromoModificationForm>(
        "all".description("Modify promo")
            .withAuthorization()
            .responds(
                ok<PromoWithPermission>(),
                badRequest()
            )
    ) { it, form ->
        val user = call.userOrNull ?: return@put call.respond(HttpStatusCode.Forbidden, "")
        val status = PromoStatus.valueOf(form.status.toUpperCase())
        val parameters = PromoModificationParams(status, form.result)
        val data = PromoLogic.update(user, it.id, parameters)
        call.respondMyResult(data)
    }
}

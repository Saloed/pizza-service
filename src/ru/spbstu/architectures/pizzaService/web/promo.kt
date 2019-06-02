package ru.spbstu.architectures.pizzaService.web

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
import ru.spbstu.architectures.pizzaService.utils.getListQueryParams
import ru.spbstu.architectures.pizzaService.utils.respondMyResult
import ru.spbstu.architectures.pizzaService.utils.responseListRange
import ru.spbstu.architectures.pizzaService.utils.userOrNull

@Location("/promo/{id}")
data class SinglePromo(val id: Int)

data class PromoListFilter(val id: List<Int>?)

data class PromoCreationForm(val clientIds: List<Int>, val effect: String, val description: String)
data class PromoModificationForm(val status: String, val result: String?)

fun Route.promo() {
    get("/promo") {
        val user = call.userOrNull ?: return@get call.respond(HttpStatusCode.Forbidden, "")
        val params = call.getListQueryParams<PromoListFilter>()
        val data = PromoLogic.list(user)
        call.respondMyResult(data) {
            val result = if (params.filter?.id != null) it.filter { it.id in params.filter.id } else it
            responseListRange(result, params.range)
        }
    }
    get<SinglePromo> {
        val user = call.userOrNull ?: return@get call.respond(HttpStatusCode.Forbidden, "")
        val data = PromoLogic.get(user, it.id)
        call.respondMyResult(data)
    }
    post("/promo") {
        val user = call.userOrNull ?: return@post call.respond(HttpStatusCode.Forbidden, "")
        val form = call.receive<PromoCreationForm>()
        val effect = PromoEffect.valueOf(form.effect.toUpperCase())
        val parameters = PromoCreationParameters(form.clientIds, effect, form.description)
        val data = PromoLogic.create(user, parameters)
        call.respondMyResult(data)
    }
    put<SinglePromo> {
        val user = call.userOrNull ?: return@put call.respond(HttpStatusCode.Forbidden, "")
        val form = call.receive<PromoModificationForm>()
        val status = PromoStatus.valueOf(form.status.toUpperCase())
        val parameters = PromoModificationParams(status, form.result)
        val data = PromoLogic.update(user, it.id, parameters)
        call.respondMyResult(data)
    }
}

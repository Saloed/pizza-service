package ru.spbstu.architectures.pizzaService.web

import de.nielsfalk.ktor.swagger.description
import de.nielsfalk.ktor.swagger.ok
import de.nielsfalk.ktor.swagger.post
import de.nielsfalk.ktor.swagger.responds
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.response.respond
import io.ktor.routing.Route
import ru.spbstu.architectures.pizzaService.logic.PaymentLogic
import ru.spbstu.architectures.pizzaService.models.PaymentWithPermission
import ru.spbstu.architectures.pizzaService.utils.respondMyResult
import ru.spbstu.architectures.pizzaService.utils.userOrNull
import ru.spbstu.architectures.pizzaService.utils.withAuthorization

data class PaymentCreateForm(val orderId: Int, val type: String, val amount: Int, val transaction: String?)

@Location("/payment")
class PaymentPath

fun Route.payment() {
    post<PaymentPath, PaymentCreateForm>(
        "all".description("Create payment")
            .withAuthorization()
            .responds(
                ok<PaymentWithPermission>()
            )
    ) { _, form ->
        val user = call.userOrNull ?: return@post call.respond(HttpStatusCode.Forbidden, "")
        val result = PaymentLogic.create(user, form)
        call.respondMyResult(result)
    }
}

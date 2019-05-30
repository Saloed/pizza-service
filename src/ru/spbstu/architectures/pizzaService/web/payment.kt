package ru.spbstu.architectures.pizzaService.web


import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import ru.spbstu.architectures.pizzaService.logic.PaymentLogic
import ru.spbstu.architectures.pizzaService.utils.MyResult
import ru.spbstu.architectures.pizzaService.utils.respondMyResult
import ru.spbstu.architectures.pizzaService.utils.userOrNull

data class PaymentCreateForm(val orderId: Int, val type: String, val amount: Int, val transaction: String?)

fun Route.payment() {
    post("/payment") {
        val user = call.userOrNull ?: return@post call.respond(HttpStatusCode.Forbidden, "")
        val form = call.receive<PaymentCreateForm>()
        val result = PaymentLogic.create(user, form)
        call.respondMyResult(result)
    }
}

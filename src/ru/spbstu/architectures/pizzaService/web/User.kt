package ru.spbstu.architectures.pizzaService.web

import io.ktor.application.call
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import ru.spbstu.architectures.pizzaService.logic.ClientOrder
import ru.spbstu.architectures.pizzaService.models.Client
import ru.spbstu.architectures.pizzaService.models.Courier
import ru.spbstu.architectures.pizzaService.models.Manager
import ru.spbstu.architectures.pizzaService.models.Operator
import ru.spbstu.architectures.pizzaService.utils.redirect
import ru.spbstu.architectures.pizzaService.utils.userOrNull

@Location("/user/client/{login}")
class ClientPage(val login: String)

@Location("/user/operator/{login}")
class OperatorPage(val login: String)

@Location("/user/manager/{login}")
class ManagerPage(val login: String)

@Location("/user/courier/{login}")
class CourierPage(val login: String)

@Location("/user/{login}")
class UserPage(val login: String)

data class Order(val id: Int)

fun Route.userPage() {
    get<UserPage> {
        val user = call.userOrNull() ?: return@get call.redirect(Login())
        val userPage = when (user) {
            is Client -> ClientPage(user.login)
            is Manager -> ManagerPage(user.login)
            is Operator -> OperatorPage(user.login)
            is Courier -> CourierPage(user.login)
        }
        call.redirect(userPage)
    }
    get<ClientPage> {
        val user = call.userOrNull() ?: return@get call.redirect(Login())
        if (user !is Client) return@get call.redirect(UserPage(user.login))
        val orders = ClientOrder.list(user)

        call.respond(
            FreeMarkerContent(
                "clientPage.ftl",
                mapOf(
                    "login" to user.login,
                    "address" to user.address,
                    "orders" to listOf(Order(14))
                ),
                ""
            )
        )

    }
}



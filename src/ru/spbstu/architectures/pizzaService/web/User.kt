package ru.spbstu.architectures.pizzaService.web

import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
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


fun Route.userPage() {
    get<UserPage> {
        val user = call.userOrNull() ?: return@get call.redirect(Login())
        val role = sequenceOf(
            user.roleClient,
            user.roleOperator,
            user.roleManager,
            user.roleCourier
        ).firstOrNull { it != null }
        if (role == null) {
            application.log.error("User ${user.login} has no roles")
            return@get call.redirect(Logout())
        }

        val userPage = when (role) {
            is Client -> ClientPage(user.login)
            is Manager -> ManagerPage(user.login)
            is Operator -> OperatorPage(user.login)
            is Courier -> CourierPage(user.login)
            else -> throw IllegalStateException("Unknown user role: ${role::class}")
        }
        call.redirect(userPage)
    }
    get<ClientPage> {
        val user = call.userOrNull() ?: return@get call.redirect(Login())
        val role = user.roleClient ?: return@get call.redirect(UserPage(user.login))
        call.respond(
            FreeMarkerContent(
                "clientPage.ftl",
                mapOf("login" to role.user.login, "address" to role.address),
                ""
            )
        )

    }
}



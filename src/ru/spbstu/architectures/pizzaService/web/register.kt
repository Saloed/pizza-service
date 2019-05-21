package ru.spbstu.architectures.pizzaService.web

import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.Parameters
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import ru.spbstu.architectures.pizzaService.models.Client
import ru.spbstu.architectures.pizzaService.models.User
import ru.spbstu.architectures.pizzaService.utils.Hasher
import ru.spbstu.architectures.pizzaService.utils.UserValidator
import ru.spbstu.architectures.pizzaService.utils.redirect
import ru.spbstu.architectures.pizzaService.utils.userOrNull

@Location("/register")
data class Register(val login: String = "", val error: String = "")

fun Route.register() {

    post<Register> {
        val user = call.userOrNull()
        if (user != null) return@post call.redirect(UserPage(user.login))

        // TODO: use conneg when it's ready and `call.receive<Register>()`
        val registration = call.receive<Parameters>()
        val userId = registration["login"] ?: return@post call.redirect(it)
        val password = registration["password"] ?: return@post call.redirect(it)

        val error = Register(userId)

        when {
            !UserValidator.passwordValid(password) -> call.redirect(error.copy(error = "Password should be at least 6 characters long"))
            !UserValidator.loginValid(userId) -> call.redirect(error.copy(error = "Login should be at least 4 characters long and consists of digits, letters, dots or underscores"))
            User.manager.get(userId) != null -> call.redirect(error.copy(error = "User with the following login is already registered"))
            else -> {
                val passwordHash = Hasher.hash(password)
                val newUser = try {
                    User.manager.create(userId, passwordHash)
                } catch (e: Throwable) {
                    application.log.error("Failed to register user", e)
                    return@post call.redirect(error.copy(error = "Failed to register"))
                }

                Client.manager.create(newUser)
                call.sessions.set(Session(newUser.login))
                call.redirect(UserPage(newUser.login))
            }
        }
    }

    get<Register> {
        val user = call.userOrNull()
        if (user != null) return@get call.redirect(UserPage(user.login))
        call.respond(
            FreeMarkerContent(
                "register.ftl",
                mapOf("login" to it.login, "error" to it.error),
                ""
            )
        )

    }
}
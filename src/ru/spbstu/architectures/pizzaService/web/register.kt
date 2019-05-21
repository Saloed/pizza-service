package ru.spbstu.architectures.pizzaService.web

import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import ru.spbstu.architectures.pizzaService.db.UserRoleType
import ru.spbstu.architectures.pizzaService.logic.UserCreator
import ru.spbstu.architectures.pizzaService.models.User
import ru.spbstu.architectures.pizzaService.utils.Hasher
import ru.spbstu.architectures.pizzaService.utils.UserValidator
import ru.spbstu.architectures.pizzaService.utils.redirect
import ru.spbstu.architectures.pizzaService.utils.userOrNull

@Location("/register")
data class Register(val login: String = "", val error: String = "")

data class RegistrationForm(val login: String, val password: String, val roleType: String)

fun Route.register() {

    post<Register> {
        val currentUser = call.userOrNull()
        val parameters = call.receiveParameters()
        // todo: change to gson
        println("$parameters")
        val registration = RegistrationForm(
            parameters["login"] ?: return@post call.redirect(Register(error = "Login required")),
            parameters["password"] ?: return@post call.redirect(Register(error = "Password required")),
            parameters["roleType"] ?: return@post call.redirect(Register(error = "Role required"))
        )

        val error = Register(registration.login)

        when {
            !UserValidator.passwordValid(registration.password) -> call.redirect(error.copy(error = "Password should be at least 6 characters long"))
            !UserValidator.loginValid(registration.login) -> call.redirect(error.copy(error = "Login should be at least 4 characters long and consists of digits, letters, dots or underscores"))
            User.manager.get(registration.login) != null -> call.redirect(error.copy(error = "User with the following login is already registered"))
            else -> {
                val passwordHash = Hasher.hash(registration.password)
                val roleType = UserRoleType.valueOf(registration.roleType)
                val newUser = try {
                    UserCreator.create(currentUser, registration.login, passwordHash, roleType)
                } catch (e: Throwable) {
                    application.log.error("Failed to register user", e)
                    return@post call.redirect(error.copy(error = "Failed to register"))
                }
                    ?: return@post call.redirect(error.copy(error = "Only manager can register user with role ${registration.roleType}"))


                if (currentUser == null) {
                    call.sessions.set(Session(newUser.login))
                    call.redirect(UserPage(newUser.login))
                }
            }
        }
    }

    get<Register> {
        val user = call.userOrNull()
        if (user != null) return@get call.redirect(UserPage(user.login))
        call.respond(
            FreeMarkerContent(
                "register.ftl",
                mapOf(
                    "login" to it.login,
                    "error" to it.error,
                    "roleTypes" to UserRoleType.values().map { it.name }.toList()
                ),
                ""
            )
        )

    }
}
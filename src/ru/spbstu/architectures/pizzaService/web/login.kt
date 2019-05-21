package ru.spbstu.architectures.pizzaService.web

import io.ktor.application.call
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.Parameters
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.sessions.clear
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import ru.spbstu.architectures.pizzaService.models.User
import ru.spbstu.architectures.pizzaService.utils.Hasher
import ru.spbstu.architectures.pizzaService.utils.UserValidator
import ru.spbstu.architectures.pizzaService.utils.redirect
import ru.spbstu.architectures.pizzaService.utils.userOrNull

data class Session(val login: String) {
    fun userOrNull() = User.manager.get(login, null)
}

@Location("/login")
data class Login(val login: String = "", val error: String = "")


@Location("/logout")
class Logout


fun Route.login() {
    get<Login> {
        val user = call.userOrNull()

        if (user != null) {
            call.redirect(UserPage(user.login))
        } else {
            val responseData = mapOf("login" to it.login, "error" to "Unauthorized")
            val response = FreeMarkerContent("login.ftl", responseData, "")
            call.respond(response)
        }
    }

    post<Login> {
        val post = call.receive<Parameters>()
        val userId = post["login"] ?: return@post call.redirect(it)
        val password = post["password"] ?: return@post call.redirect(it)
        val error = Login(userId)

        val user = when {
            !UserValidator.passwordValid(password) -> null
            !UserValidator.loginValid(userId) -> null
            else -> User.manager.get(userId, Hasher.hash(password))
        }

        if (user == null) {
            call.redirect(error.copy(error = "Invalid username or password"))
        } else {
            call.sessions.set(Session(user.login))
            call.redirect(UserPage(user.login))
        }
    }

    get<Logout> {
        call.sessions.clear<Session>()
        call.redirect(Login())
    }
}


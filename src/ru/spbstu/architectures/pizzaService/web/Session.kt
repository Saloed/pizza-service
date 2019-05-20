package ru.spbstu.architectures.pizzaService.web

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
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
import ru.spbstu.architectures.pizzaService.models.UserManager
import ru.spbstu.architectures.pizzaService.utils.Hasher

data class Session(val userId: String)

@Location("/session")
class SessionStatus(val userId: String = "")


@Location("/session/new")
data class SessionNew(val userId: String = "")


@Location("/session/drop")
data class SessionDrop(val userId: String = "")


private val userIdPattern = "[a-zA-Z0-9_\\.]+".toRegex()

internal fun userNameValid(userId: String) = userId.matches(userIdPattern)

fun Route.sessionManagement() {
    get<SessionStatus> {
        val session = call.sessions.get<Session>()
        val user = session?.let { UserManager.user(it.userId, null) }

        if (user != null) {
            val sessionStatus = SessionStatus(user.login)
            call.respond(HttpStatusCode.OK, sessionStatus)
        } else {
            val sessionStatus = SessionStatus(it.userId)
            call.respond(HttpStatusCode.Unauthorized, sessionStatus)
        }

    }

    post<SessionNew> {
        val post = call.receive<Parameters>()
        val userId = post["userId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "No userId provided")
        val password = post["password"] ?: return@post call.respond(HttpStatusCode.BadRequest, "No password provided")

        val user = when {
            userId.length < 4 -> null
            password.length < 6 -> null
            !userNameValid(userId) -> null
            else -> UserManager.user(userId, Hasher.hash(password))
        }

        if (user == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid username or password")
        } else {
            call.sessions.set(Session(user.login))
            call.respond(HttpStatusCode.OK, SessionNew(user.login))
        }
    }
}


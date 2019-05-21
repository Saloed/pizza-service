package ru.spbstu.architectures.pizzaService.utils

import io.ktor.application.ApplicationCall
import io.ktor.locations.locations
import io.ktor.request.host
import io.ktor.request.port
import io.ktor.response.respondRedirect
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import ru.spbstu.architectures.pizzaService.models.User
import ru.spbstu.architectures.pizzaService.web.Login
import ru.spbstu.architectures.pizzaService.web.Session

/**
 * Allows to respond with a absolute redirect from a typed [location] instance of a class annotated
 * with [Location] using the Locations feature.
 */
suspend fun ApplicationCall.redirect(location: Any) {
    val host = request.host() ?: "localhost"
    val portSpec = request.port().let { if (it == 80) "" else ":$it" }
    val address = host + portSpec

    respondRedirect("http://$address${application.locations.href(location)}")
}


fun ApplicationCall.userOrNull() = sessions.get<Session>()?.userOrNull()

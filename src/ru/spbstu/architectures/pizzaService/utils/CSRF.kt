package ru.spbstu.architectures.pizzaService.utils

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpHeaders
import io.ktor.request.header
import io.ktor.request.host
import ru.spbstu.architectures.pizzaService.models.User
import java.net.URI
import java.util.concurrent.TimeUnit

/**
 * Generates a security code using a [hashFunction], a [date], a [user] and an implicit [HttpHeaders.Referrer]
 * to generate tokens to prevent CSRF attacks.
 */
fun ApplicationCall.securityCode(date: Long, user: User, hashFunction: (String) -> String) =
    hashFunction("$date:${user.login}:${request.host()}:${refererHost()}")

/**
 * Verifies that a code generated from [securityCode] is valid for a [date] and a [user] and an implicit [HttpHeaders.Referrer].
 * It should match the generated [securityCode] and also not be older than two hours.
 * Used to prevent CSRF attacks.
 */
fun ApplicationCall.verifyCode(date: Long, user: User, code: String, hashFunction: (String) -> String) =
    securityCode(date, user, hashFunction) == code
            && (System.currentTimeMillis() - date).let { it > 0 && it < TimeUnit.MILLISECONDS.convert(2, TimeUnit.HOURS) }

fun ApplicationCall.refererHost() = request.header(HttpHeaders.Referrer)?.let { URI.create(it).host }

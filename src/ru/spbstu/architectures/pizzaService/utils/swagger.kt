package ru.spbstu.architectures.pizzaService.utils

import de.nielsfalk.ktor.swagger.*
import io.ktor.http.HttpStatusCode

class AuthorizationHeader(val Authorization: String)

fun Metadata.withAuthorization() = header<AuthorizationHeader>().responds((HttpStatusCode.Unauthorized)())

inline fun <reified T> Metadata.listQueryParameters(): Metadata {
    return parameter<T>()
}



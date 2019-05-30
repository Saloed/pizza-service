package ru.spbstu.architectures.pizzaService.utils

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond

sealed class MyResult<T> {
    data class Success<T>(val data: T) : MyResult<T>()
    data class Error<T>(val message: String) : MyResult<T>()
}

suspend fun <T : Any> ApplicationCall.respondMyResult(
    result: MyResult<T>,
    onSuccess: suspend ApplicationCall.(T) -> Unit = { respond(it) }
) = when (result) {
    is MyResult.Error -> respond(HttpStatusCode.BadRequest, result)
    is MyResult.Success -> onSuccess(result.data)
}

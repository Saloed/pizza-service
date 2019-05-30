package ru.spbstu.architectures.pizzaService.utils

import io.ktor.application.ApplicationCall
import io.ktor.auth.authentication
import ru.spbstu.architectures.pizzaService.web.UserPrincipal


val ApplicationCall.userOrNull
    get() = authentication.principal<UserPrincipal>()?.user

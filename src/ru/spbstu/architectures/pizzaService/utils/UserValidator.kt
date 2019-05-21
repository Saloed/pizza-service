package ru.spbstu.architectures.pizzaService.utils

object UserValidator {

    private val userLoginPattern = "[a-zA-Z0-9_\\.]+".toRegex()

    fun loginValid(login: String) = login.matches(userLoginPattern) && login.length >= 4

    fun passwordValid(password: String) = password.length >= 6

}
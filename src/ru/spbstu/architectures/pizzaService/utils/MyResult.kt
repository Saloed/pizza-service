package ru.spbstu.architectures.pizzaService.utils

sealed class MyResult<T> {
    class Success<T>(val data: T) : MyResult<T>()
    class Error<T>(val message: String) : MyResult<T>()
}

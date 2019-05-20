package ru.spbstu.architectures.pizzaService.models

enum class OrderStatus {
    NEW,
    CLOSED
}

data class Order(
    val id: Int,
    val status: OrderStatus
) {
    val manager: Manager? = null
    val operator: Operator? = null
    val payment: Payment? = null

    val pizza: List<Pizza>? = null
}


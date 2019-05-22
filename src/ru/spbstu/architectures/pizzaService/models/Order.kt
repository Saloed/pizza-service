package ru.spbstu.architectures.pizzaService.models

import ru.spbstu.architectures.pizzaService.db.OrderModelManager

enum class OrderStatus {
    NEW,
    APPROVED,
    PROCESSING,
    READY,
    SHIPPING,
    CLOSED,
    DRAFT
}

data class Order(
    val id: Int,
    val status: OrderStatus,
    val isActive: Boolean,
    val clientId: Int
) {
    val orderManager: Manager? by lazy { manager.manager(this) }
    val operator: Operator? by lazy { manager.operator(this) }
    val payment: Payment? by lazy { manager.payment(this) }

    val pizza: List<Pizza> by lazy { manager.pizza(this) }

    companion object {
        val manager = OrderModelManager
    }
}


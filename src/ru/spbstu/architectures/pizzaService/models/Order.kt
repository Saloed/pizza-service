package ru.spbstu.architectures.pizzaService.models

import org.joda.time.DateTime

enum class OrderStatus {
    NEW,
    APPROVED,
    PROCESSING,
    READY,
    SHIPPING,
    CLOSED,
    CANCELED,
    DRAFT;

    companion object {
        val forManager = listOf(APPROVED, PROCESSING, READY)
        val forOperator = listOf(NEW)
        val forCourier = listOf(READY, SHIPPING)
    }
}

data class Order(
    val id: Int,
    val status: OrderStatus,
    val cost: Int,
    val isActive: Boolean,
    val client: Client,
    val manager: Manager?,
    val operator: Operator?,
    val courier: Courier?,
    val payment: Payment?,
    val createdAt: DateTime,
    val updatedAt: DateTime
) : Model<Order> {

    companion object : ModelManagerFactory<Order>(Order::class.java)
}


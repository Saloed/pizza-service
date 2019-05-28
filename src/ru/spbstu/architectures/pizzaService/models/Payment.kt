package ru.spbstu.architectures.pizzaService.models

import org.joda.time.DateTime

enum class PaymentType {
    CASH, CARD
}

data class Payment(
    val id: Int,
    val orderId: Int,
    val type: PaymentType,
    val amount: Int,
    val cardTransaction: String?,
    val createdAt: DateTime,
    val updatedAt: DateTime
) : Model<Payment> {
    companion object : ModelManagerFactory<Payment>(Payment::class.java)
}

package ru.spbstu.architectures.pizzaService.models

enum class PaymentType {
    CASH, CARD
}

data class Payment(val id: Int, val type: PaymentType, val amount: Int, val cardTransaction: String?)

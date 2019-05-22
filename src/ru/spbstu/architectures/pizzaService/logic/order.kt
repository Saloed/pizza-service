package ru.spbstu.architectures.pizzaService.logic

import ru.spbstu.architectures.pizzaService.models.Client
import ru.spbstu.architectures.pizzaService.models.OrderStatus
import ru.spbstu.architectures.pizzaService.models.Payment
import ru.spbstu.architectures.pizzaService.models.Pizza


object ClientOrder {
    data class Order(val id: Int, val status: OrderStatus, val payment: Payment?, val pizza: List<Pizza>)

    fun list(client: Client) {

    }

}


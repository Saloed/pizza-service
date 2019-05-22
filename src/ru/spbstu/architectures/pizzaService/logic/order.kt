package ru.spbstu.architectures.pizzaService.logic

import ru.spbstu.architectures.pizzaService.models.*


object ClientOrder {

    fun list(client: Client): List<Order> {
        return Client.manager.orders(client)
    }

}


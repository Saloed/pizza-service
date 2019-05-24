package ru.spbstu.architectures.pizzaService.logic

import ru.spbstu.architectures.pizzaService.models.*


object ClientOrder {

    fun list(client: Client): List<Order> {
        return Client.manager.orders(client)
    }

    fun get(client: Client, id: Int): Order? {
        val order = Order.manager.get(id) ?: return null
        if (order.clientId != client.id) return null
        return order
    }

}


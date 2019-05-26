package ru.spbstu.architectures.pizzaService.logic

import ru.spbstu.architectures.pizzaService.db.manager.all
import ru.spbstu.architectures.pizzaService.db.manager.pizza
import ru.spbstu.architectures.pizzaService.models.Order
import ru.spbstu.architectures.pizzaService.models.Pizza
import ru.spbstu.architectures.pizzaService.models.User

object PizzaLogic {
    suspend fun list(user: User, orderId: Int): List<Pizza>? {
        val order = OrderLogic.get(user, orderId) ?: return null
        return Order.modelManager.pizza(order.id)
    }

    suspend fun list() = Pizza.modelManager.all()
}
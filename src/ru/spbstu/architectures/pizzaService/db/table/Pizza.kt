package ru.spbstu.architectures.pizzaService.db.table

import org.jetbrains.exposed.sql.Table

object OrderPizzaTable : Table("order_pizza") {
    val pizzaId = integer("pizza_id")
    val orderId = integer("order_id")
}

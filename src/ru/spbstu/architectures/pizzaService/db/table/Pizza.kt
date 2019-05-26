package ru.spbstu.architectures.pizzaService.db.table

import org.jetbrains.exposed.sql.Table


object PizzaTable : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val name = text("name")
}

object OrderPizzaTable : Table() {
    val pizzaId = reference("pizza_id", PizzaTable.id)
    val orderId = integer("order_id")
}

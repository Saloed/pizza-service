package ru.spbstu.architectures.pizzaService.db

import org.jetbrains.exposed.sql.Table


object PizzaTable : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val name = text("name")
}

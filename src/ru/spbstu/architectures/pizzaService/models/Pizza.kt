package ru.spbstu.architectures.pizzaService.models

import ru.spbstu.architectures.pizzaService.db.ModelManagerFactory

data class Pizza(
    val id: Int,
    val name: String
) : Model<Pizza> {
    companion object : ModelManagerFactory<Pizza>(Pizza::class.java)
}

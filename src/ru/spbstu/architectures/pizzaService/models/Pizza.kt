package ru.spbstu.architectures.pizzaService.models

data class PizzaTopping(val id: Int, val name: String)

data class Pizza(
    val id: Int,
    val name: String,
    val toppings: List<PizzaTopping>
) : Model<Pizza> {
    companion object : ModelManagerFactory<Pizza>(Pizza::class.java)
}

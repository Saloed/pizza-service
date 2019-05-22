package ru.spbstu.architectures.pizzaService.models

import ru.spbstu.architectures.pizzaService.db.PizzaModelManager

data class Pizza(
    val id: Int,
    val name: String
){
    companion object{
        val manager = PizzaModelManager
    }
}

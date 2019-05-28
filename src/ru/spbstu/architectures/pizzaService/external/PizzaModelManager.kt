package ru.spbstu.architectures.pizzaService.external

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import ru.spbstu.architectures.pizzaService.models.ModelManager
import ru.spbstu.architectures.pizzaService.models.Pizza
import ru.spbstu.architectures.pizzaService.models.PizzaTopping

object PizzaModelManager : ModelManager<Pizza> {
    override suspend fun create(model: Pizza) = throw IllegalStateException("Pizza creation is not possible")
    override suspend fun update(model: Pizza) = throw IllegalStateException("Pizza update is not possible")

    suspend fun qetPizzaFromApi(ids: List<Int> = emptyList()): List<Pizza> {
        val data = PizzaApi.query()
        val pizza = data.map {
            val toppings = it.Toppings.map { PizzaTopping(it.ID, it.Name) }
            Pizza(it.ID, it.Name, toppings)
        }
        if (ids.isEmpty()) return pizza
        val idSet = ids.toSet()
        return pizza.filter { it.id in idSet }
    }

    override suspend fun get(id: Int): Pizza? = this.qetPizzaFromApi().find { it.id == id }

    override suspend fun list(where: SqlExpressionBuilder.() -> Op<Boolean>) =
        throw IllegalStateException("Pizza selection with where filter")
}

suspend fun ModelManager<Pizza>.list(ids: List<Int>) = PizzaModelManager.qetPizzaFromApi(ids)
suspend fun ModelManager<Pizza>.all() = PizzaModelManager.qetPizzaFromApi(emptyList())


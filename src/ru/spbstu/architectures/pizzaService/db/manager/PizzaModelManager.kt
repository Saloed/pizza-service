package ru.spbstu.architectures.pizzaService.db.manager

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.select
import ru.spbstu.architectures.pizzaService.db.Db
import ru.spbstu.architectures.pizzaService.db.ModelManager
import ru.spbstu.architectures.pizzaService.db.table.PizzaTable
import ru.spbstu.architectures.pizzaService.models.Pizza

object PizzaModelManager :
    ModelManager<Pizza> {
    override  suspend  fun list(where: SqlExpressionBuilder.() -> Op<Boolean>) =
        Db.transaction {
            PizzaTable.select(where).map {
                Pizza(
                    it[PizzaTable.id],
                    it[PizzaTable.name]
                )
            }
        }

    override  suspend  fun get(id: Int): Pizza? = list { PizzaTable.id.eq(id) }.firstOrNull()

    override  suspend fun create(model: Pizza): Pizza {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override  suspend fun update(model: Pizza): Pizza {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

 suspend fun ModelManager<Pizza>.all() = list { Op.TRUE }
package ru.spbstu.architectures.pizzaService.db.manager

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.select
import ru.spbstu.architectures.pizzaService.db.Db
import ru.spbstu.architectures.pizzaService.db.ModelManager
import ru.spbstu.architectures.pizzaService.db.table.PromoStatusTable
import ru.spbstu.architectures.pizzaService.db.table.PromoTable
import ru.spbstu.architectures.pizzaService.models.Promo
import ru.spbstu.architectures.pizzaService.models.PromoStatus

object PromoModelManager :
    ModelManager<Promo> {
    override suspend fun get(id: Int) = list {
        PromoTable.id.eq(id)
    }.firstOrNull()

    override suspend fun create(model: Promo): Promo {
        TODO("not implemented: create")
    }

    override suspend fun update(model: Promo): Promo {
        TODO("not implemented: update")
    }

    override suspend fun list(where: SqlExpressionBuilder.() -> Op<Boolean>) =
        Db.transaction {
            (PromoTable innerJoin PromoStatusTable).select(where).map {
                val status =
                    PromoStatus.valueOf(it[PromoStatusTable.name].toUpperCase())
                Promo(
                    it[PromoTable.id],
                    status,
                    it[PromoTable.result],
                    it[PromoTable.created],
                    it[PromoTable.updated]
                )
            }
        }

}
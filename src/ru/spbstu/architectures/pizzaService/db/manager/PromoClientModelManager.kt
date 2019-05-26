package ru.spbstu.architectures.pizzaService.db.manager

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.select
import ru.spbstu.architectures.pizzaService.db.Db
import ru.spbstu.architectures.pizzaService.db.ModelManager
import ru.spbstu.architectures.pizzaService.db.table.ClientTable
import ru.spbstu.architectures.pizzaService.db.table.PromoClientStatusTable
import ru.spbstu.architectures.pizzaService.db.table.PromoClientTable
import ru.spbstu.architectures.pizzaService.models.Promo
import ru.spbstu.architectures.pizzaService.models.PromoClient
import ru.spbstu.architectures.pizzaService.models.PromoClientStatus

object PromoClientModelManager :
    ModelManager<PromoClient> {
    override suspend  fun get(id: Int) = throw IllegalArgumentException("Promo client has no ID")

    override suspend fun create(model: PromoClient): PromoClient {
        TODO("not implemented: create")
    }

    override suspend fun update(model: PromoClient): PromoClient {
        TODO("not implemented: update")
    }

    override suspend fun list(where: SqlExpressionBuilder.() -> Op<Boolean>) = Db.transaction {
        (PromoClientTable innerJoin PromoClientStatusTable).select(where).map {
            val status = PromoClientStatus.valueOf(it[PromoClientStatusTable.name].toUpperCase())
            PromoClient(
                it[PromoClientTable.clientId],
                it[PromoClientTable.promoId],
                status,
                it[PromoClientTable.created],
                it[PromoClientTable.updated]
            )
        }
    }
}

 suspend fun ModelManager<PromoClient>.listPromo(promo: Promo) = list {
    PromoClientTable.promoId.eq(promo.id)
}

package ru.spbstu.architectures.pizzaService.db.manager

import org.jetbrains.exposed.sql.*
import ru.spbstu.architectures.pizzaService.db.Db
import ru.spbstu.architectures.pizzaService.db.table.PromoClientTable
import ru.spbstu.architectures.pizzaService.db.table.PromoTable
import ru.spbstu.architectures.pizzaService.models.*

object PromoClientModelManager : ModelManager<PromoClient> {
    override suspend fun get(id: Int) = list { PromoClientTable.id eq id }.singleOrNull()


    fun insertPromoClient(model: PromoClient) =
        PromoClientTable.insert {
            it[clientId] = model.clientId
            it[promoId] = model.promoId
            it[operatorId] = model.operatorId
            it[status] = model.status
            it[created] = model.createdAt
            it[updated] = model.updatedAt
        }.let { model.copy(id = it[PromoClientTable.id]) }

    fun updatePromoClient(model: PromoClient) = PromoClientTable.update({ PromoClientTable.id eq model.id }) {
        it[clientId] = model.clientId
        it[promoId] = model.promoId
        it[operatorId] = model.operatorId
        it[status] = model.status
        it[created] = model.createdAt
        it[updated] = model.updatedAt
    }.let { model }

    override suspend fun create(model: PromoClient) = Db.transaction {
        insertPromoClient(model)
    }

    override suspend fun update(model: PromoClient) = Db.transaction {
        updatePromoClient(model)
    }

    suspend fun promoByIds(ids: List<Int>) = Promo.modelManager.list {
        PromoTable.id inList ids.toSet()
    }.map { it.id to it }.toMap()

    override suspend fun list(where: SqlExpressionBuilder.() -> Op<Boolean>) = Db.transaction {
        PromoClientTable.select(where).map {
            PromoClient(
                it[PromoClientTable.id],
                it[PromoClientTable.operatorId],
                it[PromoClientTable.promoId],
                it[PromoClientTable.clientId],
                it[PromoClientTable.status],
                it[PromoClientTable.created],
                it[PromoClientTable.updated]
            )
        }
    }
}

suspend fun ModelManager<PromoClient>.listPromo(promo: Promo) = list {
    PromoClientTable.promoId.eq(promo.id)
}

suspend fun ModelManager<PromoClient>.bulkCreate(promoClients: List<PromoClient>) = Db.transaction {
    promoClients.map { PromoClientModelManager.insertPromoClient(it) }
}

suspend fun ModelManager<PromoClient>.bulkUpdate(promoClients: List<PromoClient>) = Db.transaction {
    promoClients.map { PromoClientModelManager.updatePromoClient(it) }
}



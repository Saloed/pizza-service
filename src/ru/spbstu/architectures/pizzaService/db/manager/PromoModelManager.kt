package ru.spbstu.architectures.pizzaService.db.manager

import org.jetbrains.exposed.sql.*
import ru.spbstu.architectures.pizzaService.db.Db
import ru.spbstu.architectures.pizzaService.db.table.PromoClientTable
import ru.spbstu.architectures.pizzaService.db.table.PromoTable
import ru.spbstu.architectures.pizzaService.models.*
import ru.spbstu.architectures.pizzaService.db.manager.PromoModelManager.buildPromo
import ru.spbstu.architectures.pizzaService.db.table.OrderPromoTable

object PromoModelManager : ModelManager<Promo> {
    override suspend fun get(id: Int) = list {
        PromoTable.id.eq(id)
    }.firstOrNull()

    override suspend fun create(model: Promo) = Db.transaction {
        val promo = PromoTable.insert {
            it[managerId] = model.managerId
            it[effect] = model.effect
            it[description] = model.description
            it[status] = model.status
            it[result] = model.result
            it[created] = model.createdAt
            it[updated] = model.updatedAt
        }
        model.copy(id = promo[PromoTable.id])
    }

    override suspend fun update(model: Promo) = Db.transaction {
        PromoTable.update({ PromoTable.id eq model.id }) {
            it[managerId] = model.managerId
            it[status] = model.status
            it[effect] = model.effect
            it[description] = model.description
            it[result] = model.result
            it[created] = model.createdAt
            it[updated] = model.updatedAt
        }
        model
    }

    public fun ResultRow.buildPromo() = Promo(
        this[PromoTable.id],
        this[PromoTable.status],
        this[PromoTable.result],
        this[PromoTable.managerId],
        this[PromoTable.description],
        this[PromoTable.effect],
        this[PromoTable.created],
        this[PromoTable.updated]
    )

    override suspend fun list(where: SqlExpressionBuilder.() -> Op<Boolean>) = Db.transaction {
        PromoTable.select(where).map { it.buildPromo() }
    }

}

suspend fun ModelManager<Promo>.listForManager(manager: Manager) = list {
    PromoTable.managerId eq manager.id
}

suspend fun ModelManager<Promo>.listActive() = list {
    PromoTable.status eq PromoStatus.ACTIVE
}

suspend fun ModelManager<Promo>.listForClient(client: Client) = Db.transaction {
    (PromoTable leftJoin PromoClientTable).select {
        PromoTable.status.eq(PromoStatus.ACTIVE) and
                PromoClientTable.clientId.eq(client.id)
    }.map { it.buildPromo() }
}

suspend fun ModelManager<Promo>.getForOrder(orderId: Int) = Db.transaction {
    OrderPromoTable.select { OrderPromoTable.orderId eq orderId }
        .map { it[OrderPromoTable.promoId] }
        .singleOrNull()
}?.let { Promo.modelManager.get(it) }

suspend fun ModelManager<Promo>.setForOrder(promoOrderId: Int, promo: Promo): Promo? {
    val current = Db.transaction {
        OrderPromoTable.select { OrderPromoTable.orderId eq promoOrderId }
            .map { it[OrderPromoTable.orderId] to it[OrderPromoTable.promoId] }
            .singleOrNull()
    }
    if (current?.second == promo.id) return promo
    Db.transaction<Unit> {
        if (current == null) {
            OrderPromoTable.insert {
                it[orderId] = promoOrderId
                it[promoId] = promo.id
            }
        } else {
            OrderPromoTable.update({ OrderPromoTable.orderId eq promoOrderId }) {
                it[promoId] = promo.id
            }
        }
    }
    return promo
}


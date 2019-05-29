package ru.spbstu.architectures.pizzaService.db.manager

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import ru.spbstu.architectures.pizzaService.db.Db
import ru.spbstu.architectures.pizzaService.models.ModelManager
import ru.spbstu.architectures.pizzaService.db.table.PaymentTable
import ru.spbstu.architectures.pizzaService.db.table.PaymentTypeTable
import ru.spbstu.architectures.pizzaService.models.Payment
import ru.spbstu.architectures.pizzaService.models.PaymentType

object PaymentModelManager :
    ModelManager<Payment> {
    override suspend fun get(id: Int) = list {
        PaymentTable.id.eq(id)
    }.firstOrNull()

    override suspend fun create(model: Payment) = Db.transaction {
        val type = PaymentTypeTable.select {
            PaymentTypeTable.name eq model.type.name.toLowerCase()
        }.single().let { it[PaymentTypeTable.id] }
        val inserted = PaymentTable.insert {
            it[amount] = model.amount
            it[orderId] = model.orderId
            it[typeId] = type
            it[transaction] = model.cardTransaction
            it[updated] = model.updatedAt
            it[created] = model.createdAt
        }
        model.copy(id = inserted[PaymentTable.id])
    }

    override suspend fun update(model: Payment): Payment {
        TODO("not implemented: update")
    }

    override suspend fun list(where: SqlExpressionBuilder.() -> Op<Boolean>) =
        Db.transaction {
            (PaymentTable innerJoin PaymentTypeTable).select(
                where
            ).map {
                val type =
                    PaymentType.valueOf(it[PaymentTypeTable.name].toUpperCase())
                Payment(
                    it[PaymentTable.id],
                    it[PaymentTable.orderId],
                    type,
                    it[PaymentTable.amount],
                    it[PaymentTable.transaction],
                    it[PaymentTable.created],
                    it[PaymentTable.updated]
                )
            }
        }

}

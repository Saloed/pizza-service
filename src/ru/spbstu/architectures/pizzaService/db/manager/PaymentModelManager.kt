package ru.spbstu.architectures.pizzaService.db.manager

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.select
import ru.spbstu.architectures.pizzaService.db.Db
import ru.spbstu.architectures.pizzaService.models.ModelManager
import ru.spbstu.architectures.pizzaService.db.table.PaymentTable
import ru.spbstu.architectures.pizzaService.db.table.PaymentTypeTable
import ru.spbstu.architectures.pizzaService.models.Payment
import ru.spbstu.architectures.pizzaService.models.PaymentType

object PaymentModelManager :
    ModelManager<Payment> {
    override suspend  fun get(id: Int) = list {
        PaymentTable.id.eq(id)
    }.firstOrNull()

    override suspend  fun create(model: Payment): Payment {
        TODO("not implemented: create")
    }

    override suspend  fun update(model: Payment): Payment {
        TODO("not implemented: update")
    }

    override suspend  fun list(where: SqlExpressionBuilder.() -> Op<Boolean>) =
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

package ru.spbstu.architectures.pizzaService.db.table

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object PaymentTypeTable : Table("payment_type") {
    val id = integer("id").autoIncrement().primaryKey()
    val name = varchar("name", 100)
}

object PaymentTable : Table("payment") {
    val id = integer("id").autoIncrement().primaryKey()
    val typeId = reference("type_id", PaymentTypeTable.id)
    val orderId = reference("order_id", OrderTable.id, ReferenceOption.CASCADE)
    val amount = integer("amount")
    val transaction = varchar("transaction", 255).nullable()
    val created = datetime("created_at")
    val updated = datetime("updated_at")
}

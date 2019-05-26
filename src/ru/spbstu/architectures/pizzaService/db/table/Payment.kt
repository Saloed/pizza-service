package ru.spbstu.architectures.pizzaService.db.table

import org.jetbrains.exposed.sql.Table

object PaymentTypeTable : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val name = varchar("name", 100)
}

object PaymentTable : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val typeId = reference("type_id", PaymentTypeTable.id)
    val orderId = integer("order_id")
    val amount = integer("amount")
    val transaction = varchar("transaction", 255).nullable()
    val created = datetime("created_at")
    val updated = datetime("updated_at")
}

package ru.spbstu.architectures.pizzaService.db.table

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object OrderStatusTable : Table("order_status") {
    val id = integer("id").primaryKey().autoIncrement()
    val name = varchar("name", 100)
}

object OrderTable : Table("client_order") {
    val id = integer("id").primaryKey().autoIncrement()
    val statusId = reference("status_id", OrderStatusTable.id)
    val isActive = bool("is_active")
    val cost = integer("cost")
    val managerId = reference("manager_id", ManagerTable.id, ReferenceOption.SET_NULL).nullable()
    val operatorId = reference("operator_id", OperatorTable.id, ReferenceOption.SET_NULL).nullable()
    val courierId = reference("courier_id", CourierTable.id, ReferenceOption.SET_NULL).nullable()
    val clientId = reference("client_id", ClientTable.id, ReferenceOption.CASCADE)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

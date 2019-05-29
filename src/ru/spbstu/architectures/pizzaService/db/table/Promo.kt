package ru.spbstu.architectures.pizzaService.db.table

import org.jetbrains.exposed.sql.Table


object PromoClientStatusTable : Table("promo_client_status") {
    val id = integer("id").autoIncrement().primaryKey()
    val name = varchar("name", 100)
}

object PromoStatusTable : Table("promo_status") {
    val id = integer("id").autoIncrement().primaryKey()
    val name = varchar("name", 100)
}

object PromoTable : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val managerId = reference("manager_id", ManagerTable.id)
    val statusId = reference("status_id", PromoStatusTable.id)
    val result = text("result").nullable()
    val created = datetime("created_at")
    val updated = datetime("updated_at")
}


object PromoClientTable : Table("promo_client") {
    val promoId = reference("promo_id", PromoTable.id)
    val clientId = reference("client_id", ClientTable.id)
    val statusId = reference("status_id", PromoClientStatusTable.id)
    val created = datetime("created_at")
    val updated = datetime("updated_at")
}

package ru.spbstu.architectures.pizzaService.db.table

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import ru.spbstu.architectures.pizzaService.models.PromoClientStatus
import ru.spbstu.architectures.pizzaService.models.PromoEffect
import ru.spbstu.architectures.pizzaService.models.PromoStatus

object PromoTable : Table("promo") {
    val id = integer("id").autoIncrement().primaryKey()
    val managerId = reference("manager_id", ManagerTable.id, ReferenceOption.CASCADE)
    val status = enumerationByName("status", 255, PromoStatus::class)
    val effect = enumerationByName("effect", 255, PromoEffect::class)
    val description = text("description")
    val result = text("result").nullable()
    val created = datetime("created_at")
    val updated = datetime("updated_at")
}


object PromoClientTable : Table("promo_client") {
    val id = integer("id").primaryKey().autoIncrement()
    val promoId = reference("promo_id", PromoTable.id, ReferenceOption.CASCADE)
    val clientId = reference("client_id", ClientTable.id, ReferenceOption.CASCADE)
    val operatorId = reference("operator_id", OperatorTable.id, ReferenceOption.SET_NULL).nullable()
    val status = enumerationByName("status", 255, PromoClientStatus::class)
    val created = datetime("created_at")
    val updated = datetime("updated_at")
}

object OrderPromoTable : Table("order_promo") {
    val promoId = reference("promo_id", PromoTable.id, ReferenceOption.CASCADE)
    val orderId = reference("order_id", OrderTable.id, ReferenceOption.CASCADE)
}

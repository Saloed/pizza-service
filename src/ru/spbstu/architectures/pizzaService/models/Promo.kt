package ru.spbstu.architectures.pizzaService.models

import org.joda.time.DateTime
import ru.spbstu.architectures.pizzaService.db.manager.listPromo

enum class PromoStatus {
    NEW, ACTIVE, FINISHED, CLOSED
}


enum class PromoEffect {
    DISCOUNT_5, DISCOUNT_10, DISCOUNT_15
}

data class Promo(
    val id: Int,
    val manager: Manager,
    val status: PromoStatus,
    val result: String?,
    val description: String,
    val effect: PromoEffect,
    val createdAt: DateTime,
    val updatedAt: DateTime
) : Model<Promo> {
    companion object : ModelManagerFactory<Promo>(Promo::class.java)
}

enum class PromoClientStatus {
    NOTINFORMED,
    PROCESSING,
    INFORMED
}

data class PromoClient(
    val id: Int,
    val client: Client,
    val operator: Operator?,
    val promo: Promo,
    val status: PromoClientStatus,
    val createdAt: DateTime,
    val updatedAt: DateTime
) : Model<PromoClient> {
    companion object : ModelManagerFactory<PromoClient>(PromoClient::class.java)
}

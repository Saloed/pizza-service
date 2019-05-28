package ru.spbstu.architectures.pizzaService.models

import org.joda.time.DateTime
import ru.spbstu.architectures.pizzaService.db.manager.listPromo

enum class PromoStatus {
    NEW, ACTIVE, CLOSED
}

data class Promo(
    val id: Int,
    val status: PromoStatus,
    val result: String?,
    val createdAt: DateTime,
    val updatedAt: DateTime
) : Model<Promo> {
    suspend fun getClients() = PromoClient.modelManager.listPromo(this)

    companion object : ModelManagerFactory<Promo>(Promo::class.java)
}

enum class PromoClientStatus {
    NOTINFORMED,
    PROCESSING,
    INFORMED
}

data class PromoClient(
    val clientId: Int,
    val promoId: Int,
    val status: PromoClientStatus,
    val createdAt: DateTime,
    val updatedAt: DateTime
) : Model<PromoClient> {
    companion object : ModelManagerFactory<PromoClient>(PromoClient::class.java)
}

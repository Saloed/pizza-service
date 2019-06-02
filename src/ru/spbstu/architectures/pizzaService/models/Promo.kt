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
    val status: PromoStatus,
    val result: String?,
    val managerId: Int,
    val description: String,
    val effect: PromoEffect,
    val createdAt: DateTime,
    val updatedAt: DateTime
) : Model<Promo> {
    suspend fun getManager() = Manager.modelManager.get(managerId)

    companion object : ModelManagerFactory<Promo>(Promo::class.java)
}

enum class PromoClientStatus {
    NOTINFORMED,
    PROCESSING,
    INFORMED
}

data class PromoClient(
    val id: Int,
    val operatorId: Int?,
    val promoId: Int,
    val clientId: Int,
    val status: PromoClientStatus,
    val createdAt: DateTime,
    val updatedAt: DateTime
) : Model<PromoClient> {

    suspend fun getPromo() = Promo.modelManager.get(promoId)
    suspend fun getClient() = Client.modelManager.get(clientId)
    suspend fun getOperator() = operatorId?.let { Operator.modelManager.get(it) }

    companion object : ModelManagerFactory<PromoClient>(PromoClient::class.java)
}

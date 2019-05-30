package ru.spbstu.architectures.pizzaService.logic

import org.jetbrains.exposed.sql.Op
import org.joda.time.DateTime
import ru.spbstu.architectures.pizzaService.db.manager.*
import ru.spbstu.architectures.pizzaService.models.*
import ru.spbstu.architectures.pizzaService.utils.MyResult

data class PromoCreationParameters(val clientIds: List<Int>, val effect: PromoEffect, val description: String)

data class PromoModificationParams(val status: PromoStatus, val result: String?)

object PromoLogic {
    suspend fun create(user: User, parameters: PromoCreationParameters): MyResult<PromoWithPermission> {
        if (user !is Manager) return MyResult.Error("Only manager can create promos")
        if (parameters.clientIds.isEmpty()) return MyResult.Error("Promo without users is not permitted")
        val clients = Client.modelManager.getForIds(parameters.clientIds)
        if (!clients.map { it.id }.containsAll(parameters.clientIds)) return MyResult.Error("All users must be a clients")
        val promoRecord = Promo(
            0,
            user,
            PromoStatus.NEW,
            null,
            parameters.description,
            parameters.effect,
            DateTime.now(),
            DateTime.now()
        )
        val promo = Promo.modelManager.create(promoRecord)
        val promoClients = clients.map {
            PromoClient(0, it, null, promo, PromoClientStatus.NOTINFORMED, DateTime.now(), DateTime.now())
        }
        PromoClient.modelManager.bulkCreate(promoClients)
        return MyResult.Success(promo.fullPermission())
    }


    suspend fun list(user: User) = when (user) {
        is Manager -> {
            val userPromos = Promo.modelManager.listForManager(user)
            val activePromos = Promo.modelManager.listActive()
            val managerPromoIds = userPromos.map { it.id }.toSet()
            val full = userPromos.map { it.fullPermission() }
            val infoOnly = activePromos.filterNot { it.id in managerPromoIds }.map { it.infoOnlyPermission() }
            MyResult.Success(full + infoOnly)
        }
        is Operator, is Courier -> {
            val result = Promo.modelManager.listActive().map { it.infoOnlyPermission() }
            MyResult.Success(result)
        }
        is Client -> {
            val result = Promo.modelManager.listForClient(user).map { it.infoOnlyPermission() }
            MyResult.Success(result)
        }
    }

    suspend fun get(user: User, promoId: Int): MyResult<PromoWithPermission> {
        val promo = Promo.modelManager.get(promoId) ?: return MyResult.Error("Not found")
        if (user is Manager && user.id == promo.manager.id) return MyResult.Success(promo.fullPermission())
        if (user is Client) {
            val clientPromoIds = Promo.modelManager.listForClient(user).map { it.id }
            if (promo.id !in clientPromoIds) return MyResult.Error("No access")
        }
        return MyResult.Success(promo.infoOnlyPermission())
    }

    private suspend fun startPromo(user: User, promo: Promo): Promo {
        val clients = PromoClient.modelManager.listPromo(promo)
        val operators = Operator.modelManager.list { Op.TRUE }
        val clientsWithOperator = clients.map {
            it.copy(
                status = PromoClientStatus.NOTINFORMED,
                operator = operators.random(),
                updatedAt = DateTime.now()
            )
        }
        PromoClient.modelManager.bulkUpdate(clientsWithOperator)
        return promo.copy(status = PromoStatus.ACTIVE)
    }

    private suspend fun closePromo(user: User, promo: Promo): Promo {
        return promo.copy(status = PromoStatus.FINISHED)
    }

    private suspend fun analyzePromo(user: User, promo: Promo, result: String): Promo {
        return promo.copy(status = PromoStatus.CLOSED, result = result)
    }


    suspend fun update(user: User, promoId: Int, params: PromoModificationParams): MyResult<PromoWithPermission> {
        if (user !is Manager) return MyResult.Error("Only manager can modify promos")
        val promo = Promo.modelManager.get(promoId) ?: return MyResult.Error("Not found")
        if (promo.manager.id != user.id) return MyResult.Error("No access")

        val updatedPromo = when (promo.status to params.status) {
            (PromoStatus.NEW to PromoStatus.ACTIVE) -> startPromo(user, promo)
            (PromoStatus.ACTIVE to PromoStatus.FINISHED) -> closePromo(user, promo)
            (PromoStatus.FINISHED to PromoStatus.CLOSED) -> {
                val promoResult = params.result ?: return MyResult.Error("No result supplied")
                analyzePromo(user, promo, promoResult)
            }
            else -> return MyResult.Error("No possible transition")
        }.copy(updatedAt = DateTime.now())
        Promo.modelManager.update(updatedPromo)
        return MyResult.Success(updatedPromo.fullPermission())
    }

}



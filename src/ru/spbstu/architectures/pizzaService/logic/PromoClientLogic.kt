package ru.spbstu.architectures.pizzaService.logic

import org.joda.time.DateTime
import ru.spbstu.architectures.pizzaService.db.manager.listPromo
import ru.spbstu.architectures.pizzaService.models.*
import ru.spbstu.architectures.pizzaService.utils.MyResult

object PromoClientLogic {

    suspend fun list(user: User, promoId: Int): MyResult<List<PromoClientWithPermission>> {
        if (user !is Manager && user !is Operator) return MyResult.Error("No access")
        val promo = Promo.modelManager.get(promoId) ?: return MyResult.Error("No such promo")
        if (user is Manager && promo.managerId != user.id) return MyResult.Error("No access")
        val clients = PromoClient.modelManager.listPromo(promo).map { it.fullPermission() }
        if (user is Manager) return MyResult.Success(clients)
        val operatorClients = clients.filter { it.operator?.id == user.id }
        return MyResult.Success(operatorClients)
    }

    suspend fun get(user: User, id: Int): MyResult<PromoClientWithPermission> {
        if (user !is Manager && user !is Operator) return MyResult.Error("No access")
        val promoClient = PromoClient.modelManager.get(id) ?: return MyResult.Error("Not found")
        if (user is Manager && promoClient.getPromo()?.managerId != user.id) return MyResult.Error("No access")
        if (user is Operator && promoClient.operatorId != user.id) return MyResult.Error("No access")
        return MyResult.Success(promoClient.fullPermission())
    }

    suspend fun update(user: User, id: Int, status: PromoClientStatus): MyResult<PromoClientWithPermission> {
        if (user !is Operator) return MyResult.Error("Only operator can change promo client status")
        val promoClient = PromoClient.modelManager.get(id) ?: return MyResult.Error("Not found")
        if (promoClient.getOperator()?.id != user.id) return MyResult.Error("No access")
        if (promoClient.getPromo()?.status != PromoStatus.ACTIVE) return MyResult.Error("No access")
        val nextStatus = when (promoClient.status to status) {
            (PromoClientStatus.NOTINFORMED to PromoClientStatus.PROCESSING) -> PromoClientStatus.PROCESSING
            (PromoClientStatus.PROCESSING to PromoClientStatus.NOTINFORMED) -> PromoClientStatus.NOTINFORMED
            (PromoClientStatus.PROCESSING to PromoClientStatus.INFORMED) -> PromoClientStatus.INFORMED
            else -> return MyResult.Error("Transition not possible")
        }
        val updatedClient = promoClient.copy(status = nextStatus, updatedAt = DateTime.now())
        PromoClient.modelManager.update(updatedClient)
        return MyResult.Success(updatedClient.fullPermission())
    }

}

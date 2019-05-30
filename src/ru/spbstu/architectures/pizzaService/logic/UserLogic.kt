package ru.spbstu.architectures.pizzaService.logic

import org.jetbrains.exposed.sql.Op
import ru.spbstu.architectures.pizzaService.models.UserRoleType
import ru.spbstu.architectures.pizzaService.models.*
import ru.spbstu.architectures.pizzaService.utils.MyResult

object UserLogic {
    suspend fun createClient(login: String, password: String): User? {
        val client = Client(0, login, password, "", "")
        return Client.modelManager.create(client)
    }

    suspend fun create(currentUser: User?, login: String, password: String, role: UserRoleType): User? {
        if (currentUser !is Manager) return null
        return when (role) {
            UserRoleType.Client -> createClient(login, password)
            UserRoleType.Manager -> {

                val manager = Manager(0, login, password, "")
                Manager.modelManager.create(manager)
            }
            UserRoleType.Operator -> {
                val operator = Operator(0, login, password, -1)
                Operator.modelManager.create(operator)
            }
            UserRoleType.Courier -> {
                val courier = Courier(0, login, password)
                Courier.modelManager.create(courier)
            }
        }
    }

    suspend fun listClients(user: User): MyResult<List<ClientWithPermission>> {
        if (user !is Manager) return MyResult.Error("No access")
        val result = Client.modelManager.list { Op.TRUE }.map { it.fullPermission() }
        return MyResult.Success(result)
    }

}

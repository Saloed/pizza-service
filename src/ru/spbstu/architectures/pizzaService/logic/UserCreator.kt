package ru.spbstu.architectures.pizzaService.logic

import ru.spbstu.architectures.pizzaService.models.UserRoleType
import ru.spbstu.architectures.pizzaService.models.*

object UserCreator {
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
}

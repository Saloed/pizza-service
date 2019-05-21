package ru.spbstu.architectures.pizzaService.logic

import ru.spbstu.architectures.pizzaService.db.UserRoleType
import ru.spbstu.architectures.pizzaService.models.*

object UserCreator {
    fun create(currentUser: User?, login: String, password: String, role: UserRoleType): User? {
        if (role == UserRoleType.Client) {
            val client = Client(0, login, password, "")
            return Client.manager.create(client)
        }
        if (currentUser !is Manager) return null
        return when (role) {
            UserRoleType.Client -> throw IllegalStateException("Client case already checked")
            UserRoleType.Manager -> {

                val manager = Manager(0, login, password, "")
                Manager.manager.create(manager)
            }
            UserRoleType.Operator -> {
                val operator = Operator(0, login, password, -1)
                Operator.manager.create(operator)
            }
            UserRoleType.Courier -> {
                val courier = Courier(0, login, password)
                Courier.manager.create(courier)
            }
        }
    }
}
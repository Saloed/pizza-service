package ru.spbstu.architectures.pizzaService.models

import ru.spbstu.architectures.pizzaService.db.*

data class User(val id: Int, val login: String, val password: String) {
    val roleClient
        get() = Client.manager.get(this)
    val roleManager
        get() = Manager.manager.get(this)
    val roleOperator
        get() = Operator.manager.get(this)
    val roleCourier
        get() = Courier.manager.get(this)

    companion object {
        val manager = UserModelManager
    }
}

data class Client(val user: User, val address: String) {
    companion object {
        val manager = ClientModelManager
    }
}

data class Manager(val user: User, val restaurant: String) {
    companion object {
        val manager = ManagerModelManager
    }
}


data class Operator(val user: User, val number: Int) {
    companion object {
        val manager = OperatorModelManager
    }
}

data class Courier(val user: User) {
    companion object {
        val manager = CourierModelManager
    }
}


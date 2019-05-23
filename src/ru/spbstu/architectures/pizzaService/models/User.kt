package ru.spbstu.architectures.pizzaService.models

import ru.spbstu.architectures.pizzaService.db.*

enum class UserRoleType {
    Client, Manager, Operator, Courier
}

sealed class User(val id: Int, val login: String, val password: String) {
    abstract val role: UserRoleType

    companion object {
        val manager = UserModelManager
    }
}

class Client(id: Int, login: String, password: String, val address: String) : User(id, login, password) {
    override val role = UserRoleType.Client

    companion object {
        val manager = ClientModelManager
    }
}

class Manager(id: Int, login: String, password: String, val restaurant: String) : User(id, login, password) {
    override val role = UserRoleType.Manager

    companion object {
        val manager = ManagerModelManager
    }
}

class Operator(id: Int, login: String, password: String, val number: Int) : User(id, login, password) {
    override val role = UserRoleType.Operator

    companion object {
        val manager = OperatorModelManager
    }
}

class Courier(id: Int, login: String, password: String) : User(id, login, password) {
    override val role = UserRoleType.Courier

    companion object {
        val manager = CourierModelManager
    }
}


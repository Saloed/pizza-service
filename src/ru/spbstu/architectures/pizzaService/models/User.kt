package ru.spbstu.architectures.pizzaService.models

import ru.spbstu.architectures.pizzaService.db.ModelManagerFactory
import ru.spbstu.architectures.pizzaService.db.manager.UserModelManager

enum class UserRoleType {
    Client, Manager, Operator, Courier
}

sealed class User(val id: Int, val login: String, val password: String) {
    abstract val role: UserRoleType

    companion object {
        val manager = UserModelManager
    }
}

class Client(
    id: Int,
    login: String,
    password: String,
    val address: String,
    val phone: String
) : User(id, login, password), Model<Client> {
    override val role = UserRoleType.Client

    companion object : ModelManagerFactory<Client>(Client::class.java)
}

class Manager(id: Int, login: String, password: String, val restaurant: String) : User(id, login, password),
    Model<Manager> {
    override val role = UserRoleType.Manager

    companion object : ModelManagerFactory<Manager>(Manager::class.java)
}

class Operator(id: Int, login: String, password: String, val number: Int) : User(id, login, password), Model<Operator> {
    override val role = UserRoleType.Operator

    companion object : ModelManagerFactory<Operator>(Operator::class.java)
}

class Courier(id: Int, login: String, password: String) : User(id, login, password), Model<Courier> {
    override val role = UserRoleType.Courier

    companion object : ModelManagerFactory<Courier>(Courier::class.java)
}


package ru.spbstu.architectures.pizzaService.models

import org.jetbrains.exposed.sql.*

object UserTable : Table() {
    val id = integer("id").primaryKey()
    val login = varchar("login", 255).uniqueIndex()
    val password = varchar("password", 64)
}

data class User(val id: Int, val login: String, val password: String)


data class Client(val user: User, val address: String)
data class Manager(val user: User, val restaurant: String)
data class Operator(val user: User, val number: Int)
data class Courier(val user: User)


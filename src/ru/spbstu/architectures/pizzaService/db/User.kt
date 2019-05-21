package ru.spbstu.architectures.pizzaService.db

import org.jetbrains.exposed.sql.Table

object UserTable : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val login = varchar("login", 255).uniqueIndex()
    val password = varchar("password", 64)
}

object ClientTable : Table() {
    val id = reference("id", UserTable.id).primaryKey()
    val address = text("address")
}

object ManagerTable : Table() {
    val id = reference("id", UserTable.id).primaryKey()
    val restaurant = text("restaurant")
}

object OperatorTable : Table() {
    val id = reference("id", UserTable.id).primaryKey()
    val number = integer("number")
}

object CourierTable : Table() {
    val id = reference("id", UserTable.id).primaryKey()
}
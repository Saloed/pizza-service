package ru.spbstu.architectures.pizzaService.db.table

import org.jetbrains.exposed.sql.Table
import ru.spbstu.architectures.pizzaService.models.UserRoleType

object UserTable : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val login = varchar("login", 255).uniqueIndex()
    val password = varchar("password", 64)
    val role = enumeration("role", UserRoleType::class)
}

object ClientTable : Table() {
    val id = reference("id", UserTable.id).primaryKey()
    val address = text("address")
    val phone = varchar("phone", 100)
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
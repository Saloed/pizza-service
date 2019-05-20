package ru.spbstu.architectures.pizzaService.models

import org.jetbrains.exposed.sql.select
import ru.spbstu.architectures.pizzaService.Db

object UserManager {
    fun user(userId: String, passwordHash: String? = null) = Db.transaction {
        UserTable.select { UserTable.login.eq(userId) }
            .mapNotNull {
                if (passwordHash == null || it[UserTable.password] == passwordHash) {
                    User(it[UserTable.id], it[UserTable.login], it[UserTable.password])
                } else {
                    null
                }
            }
            .singleOrNull()
    }
}

//
//class ClientManager: ModelManager<Client>(){
//
//}
//
//abstract class ModelPropertyManager<M, P>{
//
//}
//
//class ClientOrderPropertyManager: ModelPropertyManager<Client, List<Order>>(){
//
//}
//

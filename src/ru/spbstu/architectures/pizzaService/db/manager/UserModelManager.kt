package ru.spbstu.architectures.pizzaService.db.manager

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import ru.spbstu.architectures.pizzaService.db.*
import ru.spbstu.architectures.pizzaService.db.table.*
import ru.spbstu.architectures.pizzaService.models.*
import ru.spbstu.architectures.pizzaService.db.manager.OperatorModelManager.buildOperator
import ru.spbstu.architectures.pizzaService.db.manager.ClientModelManager.buildClient
import ru.spbstu.architectures.pizzaService.db.manager.ManagerModelManager.buildManager
import ru.spbstu.architectures.pizzaService.db.manager.CourierModelManager.buildCourier

object UserModelManager {
    suspend fun get(userId: String, passwordHash: String? = null): User? =
        Db.transaction {
            val user =
                (UserTable leftJoin ClientTable leftJoin OperatorTable leftJoin ManagerTable leftJoin CourierTable)
                    .select { UserTable.login.eq(userId) }
                    .singleOrNull { passwordHash == null || it[UserTable.password] == passwordHash }
                    ?: return@transaction null
            println("User try to login: $user")
            return@transaction when (user[UserTable.role]) {
                UserRoleType.Client -> user.buildClient()
                UserRoleType.Manager -> user.buildManager()
                UserRoleType.Operator -> user.buildOperator()
                UserRoleType.Courier -> user.buildCourier()
            }
        }

    fun create(userLogin: String, passwordHash: String, role: UserRoleType) = UserTable.insert {
        it[UserTable.login] = userLogin
        it[UserTable.password] = passwordHash
        it[UserTable.role] = role
    }.let {
        it[UserTable.id]
    }

}

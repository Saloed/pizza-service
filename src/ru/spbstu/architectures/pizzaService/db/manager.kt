package ru.spbstu.architectures.pizzaService.db

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import ru.spbstu.architectures.pizzaService.models.*


object UserModelManager {
    fun get(userId: String, passwordHash: String? = null) = Db.transaction {
        val user = (UserTable leftJoin ClientTable leftJoin OperatorTable leftJoin ManagerTable leftJoin CourierTable)
            .select { UserTable.login.eq(userId) }
            .singleOrNull { passwordHash == null || it[UserTable.password] == passwordHash }
            ?: return@transaction null
        return@transaction when (user[UserTable.role]) {
            UserRoleType.Client -> Client(
                user[UserTable.id],
                user[UserTable.login],
                user[UserTable.password],
                user[ClientTable.address]
            )
            UserRoleType.Manager -> Manager(
                user[UserTable.id],
                user[UserTable.login],
                user[UserTable.password],
                user[ManagerTable.restaurant]
            )
            UserRoleType.Operator -> Operator(
                user[UserTable.id],
                user[UserTable.login],
                user[UserTable.password],
                user[OperatorTable.number]
            )
            UserRoleType.Courier -> Courier(
                user[UserTable.id],
                user[UserTable.login],
                user[UserTable.password]
            )
        }
    }

    fun create(userLogin: String, passwordHash: String, role: UserRoleType) = Db.transaction {
        UserTable.insert {
            it[login] = userLogin
            it[password] = passwordHash
            it[this.role] = role
        }.let {
            it[UserTable.id]
        }
    }

}

interface UserRoleManager<T> {
    fun create(model: T): T
}

object ClientModelManager : UserRoleManager<Client> {
    override fun create(model: Client): Client {
        val userId = UserModelManager.create(model.login, model.password, UserRoleType.Client)
        Db.transaction {
            ClientTable.insert {
                it[id] = userId
                it[address] = model.address
            }
        }
        return Client(userId, model.login, model.password, model.address)
    }
}


object ManagerModelManager : UserRoleManager<Manager> {
    override fun create(model: Manager): Manager {
        val userId = UserModelManager.create(model.login, model.password, UserRoleType.Manager)
        Db.transaction {
            ManagerTable.insert {
                it[id] = userId
                it[restaurant] = model.restaurant
            }
        }
        return Manager(userId, model.login, model.password, model.restaurant)
    }
}

object OperatorModelManager : UserRoleManager<Operator> {
    override fun create(model: Operator): Operator {
        val userId = UserModelManager.create(model.login, model.password, UserRoleType.Operator)
        Db.transaction {
            OperatorTable.insert {
                it[id] = userId
                it[number] = model.number
            }
        }
        return Operator(userId, model.login, model.password, model.number)
    }
}

object CourierModelManager : UserRoleManager<Courier> {
    override fun create(model: Courier): Courier {
        val userId = UserModelManager.create(model.login, model.password, UserRoleType.Courier)
        Db.transaction {
            CourierTable.insert {
                it[id] = userId
            }
        }
        return Courier(userId, model.login, model.password)
    }
}

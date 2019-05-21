package ru.spbstu.architectures.pizzaService.db

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import ru.spbstu.architectures.pizzaService.models.*


object UserModelManager {
    fun get(userId: String, passwordHash: String? = null) = Db.transaction {
        UserTable.select { UserTable.login.eq(userId) }
            .mapNotNull {
                if (passwordHash == null || it[UserTable.password] == passwordHash) {
                    User(
                        it[UserTable.id],
                        it[UserTable.login],
                        it[UserTable.password]
                    )
                } else {
                    null
                }
            }
            .singleOrNull()
    }

    fun create(userLogin: String, passwordHash: String) = Db.transaction {
        UserTable.insert {
            it[login] = userLogin
            it[password] = passwordHash
        }.let {
            User(
                it[UserTable.id],
                it[UserTable.login],
                it[UserTable.password]
            )
        }
    }

}

interface UserRoleManager<T> {
    fun create(user: User): T
    fun update(model: T): Unit
    fun get(user: User): T?
}

object ClientModelManager : UserRoleManager<Client> {
    override fun create(user: User) = Db.transaction {
        ClientTable.insert {
            it[id] = user.id
            it[address] = ""
        }.let { Client(user, "") }
    }

    override fun update(model: Client): Unit = Db.transaction {
        ClientTable.update({ ClientTable.id.eq(model.user.id) }) {
            it[address] = model.address
        }
    }

    override fun get(user: User) = Db.transaction {
        ClientTable.select { ClientTable.id.eq(user.id) }
            .mapNotNull { Client(user, it[ClientTable.address]) }
            .singleOrNull()
    }
}


object ManagerModelManager : UserRoleManager<Manager> {
    override fun create(user: User) = Db.transaction {
        ManagerTable.insert {
            it[id] = user.id
            it[restaurant] = ""
        }.let { Manager(user, "") }
    }

    override fun update(model: Manager): Unit = Db.transaction {
        ManagerTable.update({ ManagerTable.id.eq(model.user.id) }) {
            it[restaurant] = model.restaurant
        }
    }

    override fun get(user: User) = Db.transaction {
        ManagerTable.select { ManagerTable.id.eq(user.id) }
            .mapNotNull { Manager(user, it[ManagerTable.restaurant]) }
            .singleOrNull()
    }
}

object OperatorModelManager : UserRoleManager<Operator> {
    override fun create(user: User) = Db.transaction {
        OperatorTable.insert {
            it[id] = user.id
            it[number] = -1
        }.let { Operator(user, -1) }
    }

    override fun update(model: Operator): Unit = Db.transaction {
        OperatorTable.update({ OperatorTable.id.eq(model.user.id) }) {
            it[number] = model.number
        }
    }

    override fun get(user: User) = Db.transaction {
        OperatorTable.select { OperatorTable.id.eq(user.id) }
            .mapNotNull { Operator(user, it[OperatorTable.number]) }
            .singleOrNull()
    }
}

object CourierModelManager : UserRoleManager<Courier> {
    override fun create(user: User) = Db.transaction {
        CourierTable.insert {
            it[id] = user.id
        }.let { Courier(user) }
    }

    override fun update(model: Courier): Unit = Db.transaction {
        CourierTable.update({ CourierTable.id.eq(model.user.id) }) {
        }
    }

    override fun get(user: User) = Db.transaction {
        CourierTable.select { CourierTable.id.eq(user.id) }
            .mapNotNull { Courier(user) }
            .singleOrNull()
    }
}

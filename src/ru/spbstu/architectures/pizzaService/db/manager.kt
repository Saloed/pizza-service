package ru.spbstu.architectures.pizzaService.db

import org.intellij.lang.annotations.Language
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import ru.spbstu.architectures.pizzaService.models.*
import ru.spbstu.architectures.pizzaService.utils.execAndMap


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

    fun orders(client: Client): List<Order> {
        @Language("PostgreSQL") val query = """
        select
        ord.id id,
         ord.client_id client_id,
         os.name status,
         ord.is_active is_active,
         ord.updated_at updated_at,
         ord.created_at created_at
         from "order" ord join order_status os on ord.status_id = os.id
        where ord.client_id = ${client.id}
    """.trimIndent()
        return Db.transaction {
            query.execAndMap {
                it.run {
                    val status = OrderStatus.valueOf(getString("status").toUpperCase())
                    Order(getInt("id"), status, getBoolean("is_active"), getInt("client_id"))
                }
            }
        }
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

object PizzaModelManager {

}

object OrderModelManager {
    fun get(id: Int): Order? {
        @Language("PostgreSQL") val query = """
        select
        ord.id id,
         ord.client_id client_id,
         os.name status,
         ord.is_active is_active,
         ord.updated_at updated_at,
         ord.created_at created_at
         from "order" ord join order_status os on ord.status_id = os.id
        where ord.id = ${id}
    """.trimIndent()
        return Db.transaction {
            query.execAndMap {
                it.run {
                    val status = OrderStatus.valueOf(getString("status").toUpperCase())
                    Order(getInt("id"), status, getBoolean("is_active"), getInt("client_id"))
                }
            }
        }.firstOrNull()
    }

    fun pizza(order: Order): List<Pizza> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun payment(order: Order): Payment? {
        @Language("PostgreSQL") val query = """
        select
        p.id id,
        p.order_id order_id,
        p.amount amount,
        pt.name payment_type,
        p.transaction payment_transaction,
        p.created_at created_at,
        p.updated_at updated_at
        from payment p join payment_type pt on p.type_id = pt.id
        where p.order_id = ${order.id}
    """.trimIndent()
        return Db.transaction {
            query.execAndMap {
                it.run {
                    val type = PaymentType.valueOf(getString("payment_type").toUpperCase())
                    Payment(getInt("id"), getInt("order_id"), type, getInt("amount"), getString("payment_transaction"))
                }
            }
        }.firstOrNull()
    }

    fun manager(order: Order): Manager? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun operator(order: Order): Operator? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

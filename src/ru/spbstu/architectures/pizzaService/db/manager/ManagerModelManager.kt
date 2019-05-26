package ru.spbstu.architectures.pizzaService.db.manager

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import ru.spbstu.architectures.pizzaService.db.*
import ru.spbstu.architectures.pizzaService.db.table.ClientTable
import ru.spbstu.architectures.pizzaService.db.table.ManagerTable
import ru.spbstu.architectures.pizzaService.db.table.UserTable
import ru.spbstu.architectures.pizzaService.models.Client
import ru.spbstu.architectures.pizzaService.models.Manager
import ru.spbstu.architectures.pizzaService.models.OrderStatus
import ru.spbstu.architectures.pizzaService.models.UserRoleType
import ru.spbstu.architectures.pizzaService.utils.boolColumn
import ru.spbstu.architectures.pizzaService.utils.intColumn
import ru.spbstu.architectures.pizzaService.utils.stringColumn

object ManagerModelManager : ModelManager<Manager> {


    override suspend fun update(model: Manager): Manager {
        TODO("not implemented")
    }

    override suspend fun list(where: SqlExpressionBuilder.() -> Op<Boolean>) = Db.transaction {
        (ManagerTable innerJoin UserTable)
            .select(where)
            .map { it.buildManager() }
    }


    override suspend fun create(model: Manager) = Db.transaction {
        val userId = UserModelManager.create(
            model.login,
            model.password,
            UserRoleType.Manager
        )
        ManagerTable.insert {
            it[ManagerTable.id] = userId
            it[ManagerTable.restaurant] = model.restaurant
        }
    }.let {
        Manager(
            it[ManagerTable.id],
            model.login,
            model.password,
            model.restaurant
        )
    }

    fun ResultRow.buildManager() = Manager(
        this[UserTable.id],
        this[UserTable.login],
        this[UserTable.password],
        this[ManagerTable.restaurant]
    )

    override suspend fun get(id: Int) = list {
        ManagerTable.id.eq(id)
    }.singleOrNull()


}

suspend fun ModelManager<Manager>.orders(manager: Manager) =
    OrderModelManager.list {
        intColumn("order", "manager_id").eq(manager.id)
    }

suspend fun ModelManager<Manager>.activeOrders(manager: Manager) =
    OrderModelManager.list {
        intColumn("order", "manager_id").eq(manager.id)
            .and(boolColumn("order", "is_active"))
            .and(stringColumn("order_status", "name").inList(OrderStatus.forManager.map { it.name.toLowerCase() }))
    }

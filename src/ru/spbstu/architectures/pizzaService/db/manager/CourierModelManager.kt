package ru.spbstu.architectures.pizzaService.db.manager

import org.jetbrains.exposed.sql.*
import ru.spbstu.architectures.pizzaService.db.*
import ru.spbstu.architectures.pizzaService.db.table.CourierTable
import ru.spbstu.architectures.pizzaService.db.table.UserTable
import ru.spbstu.architectures.pizzaService.models.Courier
import ru.spbstu.architectures.pizzaService.models.ModelManager
import ru.spbstu.architectures.pizzaService.models.OrderStatus
import ru.spbstu.architectures.pizzaService.models.UserRoleType
import ru.spbstu.architectures.pizzaService.utils.boolColumn
import ru.spbstu.architectures.pizzaService.utils.intColumn
import ru.spbstu.architectures.pizzaService.utils.stringColumn

object CourierModelManager : ModelManager<Courier> {

    override suspend fun update(model: Courier): Courier {
        TODO("not implemented")
    }

    override suspend fun list(where: SqlExpressionBuilder.() -> Op<Boolean>) = Db.transaction {
        (CourierTable innerJoin UserTable)
            .select(where)
            .map { it.buildCourier() }
    }

    override suspend fun create(model: Courier) = Db.transaction {
        val userId = UserModelManager.create(
            model.login,
            model.password,
            UserRoleType.Courier
        )
        CourierTable.insert {
            it[CourierTable.id] = userId
        }
    }.let { Courier(it[CourierTable.id], model.login, model.password) }

    fun ResultRow.buildCourier() = Courier(
        this[UserTable.id],
        this[UserTable.login],
        this[UserTable.password]
    )

    override suspend fun get(id: Int) = list {
        CourierTable.id.eq(id)
    }.singleOrNull()

}

suspend fun ModelManager<Courier>.activeOrders(courier: Courier) =
    OrderModelManager.list {
        intColumn("order", "courier_id").eq(courier.id)
            .and(boolColumn("order", "is_active"))
            .and(stringColumn("order_status", "name").inList(OrderStatus.forCourier.map { it.name.toLowerCase() }))
    }

package ru.spbstu.architectures.pizzaService.db.manager

import org.jetbrains.exposed.sql.*
import ru.spbstu.architectures.pizzaService.db.Db
import ru.spbstu.architectures.pizzaService.db.table.OperatorTable
import ru.spbstu.architectures.pizzaService.db.table.UserTable
import ru.spbstu.architectures.pizzaService.models.ModelManager
import ru.spbstu.architectures.pizzaService.models.Operator
import ru.spbstu.architectures.pizzaService.models.OrderStatus
import ru.spbstu.architectures.pizzaService.models.UserRoleType
import ru.spbstu.architectures.pizzaService.utils.boolColumn
import ru.spbstu.architectures.pizzaService.utils.intColumn
import ru.spbstu.architectures.pizzaService.utils.stringColumn

object OperatorModelManager : ModelManager<Operator> {

    override suspend fun update(model: Operator): Operator {
        TODO("not implemented")
    }

    override suspend fun list(where: SqlExpressionBuilder.() -> Op<Boolean>) = Db.transaction {
        (OperatorTable innerJoin UserTable)
            .select(where)
            .map { it.buildOperator() }
    }

    override suspend fun create(model: Operator) = Db.transaction {
        val userId = UserModelManager.create(
            model.login,
            model.password,
            UserRoleType.Operator
        )
        OperatorTable.insert {
            it[OperatorTable.id] = userId
            it[OperatorTable.number] = model.number
        }
    }.let { Operator(it[OperatorTable.id], model.login, model.password, model.number) }

    fun ResultRow.buildOperator() = Operator(
        this[UserTable.id],
        this[UserTable.login],
        this[UserTable.password],
        this[OperatorTable.number]
    )

    override suspend fun get(id: Int) = list {
        OperatorTable.id.eq(id)
    }.singleOrNull()

}

suspend fun ModelManager<Operator>.activeOrders(operator: Operator) =
    OrderModelManager.list {
        intColumn("order", "operator_id").eq(operator.id)
            .and(boolColumn("order", "is_active"))
            .and(stringColumn("order_status", "name").inList(OrderStatus.forOperator.map { it.name.toLowerCase() }))
    }



suspend fun ModelManager<Operator>.getForIds(ids: List<Int>) = list {
    OperatorTable.id inList ids
}


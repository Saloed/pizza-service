package ru.spbstu.architectures.pizzaService.db.manager

import org.intellij.lang.annotations.Language
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import ru.spbstu.architectures.pizzaService.db.Db
import ru.spbstu.architectures.pizzaService.db.table.OrderPizzaTable
import ru.spbstu.architectures.pizzaService.db.table.OrderStatusTable
import ru.spbstu.architectures.pizzaService.db.table.OrderTable
import ru.spbstu.architectures.pizzaService.external.list
import ru.spbstu.architectures.pizzaService.models.*
import ru.spbstu.architectures.pizzaService.utils.*
import java.sql.ResultSet

object OrderModelManager : ModelManager<Order> {
    private fun ResultSet.buildUser(prefix: String): User? {
        val login = getString("${prefix}_login") ?: return null
        val id = getInt("${prefix}_id")
        val password = getString("${prefix}_password")
        val role = UserRoleType.values()[getInt("${prefix}_role")]
        return when (role) {
            UserRoleType.Client -> Client(
                id, login, password,
                getString("${prefix}_address"),
                getString("${prefix}_phone")
            )
            UserRoleType.Manager -> Manager(
                id, login, password,
                getString("${prefix}_restaurant")
            )
            UserRoleType.Operator -> Operator(
                id, login, password,
                getInt("${prefix}_number")
            )
            UserRoleType.Courier -> Courier(id, login, password)
        }
    }

    private fun ResultSet.buildPayment(): Payment? {
        val id = getInt("payment_id")
        if (id == 0) return null
        val type = PaymentType.valueOf(getString("payment_type").toUpperCase())
        return Payment(
            id,
            getInt("payment_order_id"),
            type,
            getInt("payment_amount"),
            getString("payment_transaction"),
            getTimestamp("payment_created_at").toDateTime(),
            getTimestamp("payment_updated_at").toDateTime()
        )
    }

    private fun ResultSet.buildOrder(): Order {
        val status = OrderStatus.valueOf(getString("order_status").toUpperCase())
        val orderId = getInt("order_id")
        return Order(
            orderId,
            status,
            getInt("order_cost"),
            getBoolean("order_is_active"),
            buildUser("client") as Client,
            buildUser("manager") as? Manager,
            buildUser("operator") as? Operator,
            buildUser("courier") as? Courier,
            buildPayment(),
            null,
            getTimestamp("order_created_at").toDateTime(),
            getTimestamp("order_updated_at").toDateTime()
        )
    }

    override suspend fun list(where: SqlExpressionBuilder.() -> Op<Boolean>) =
        Db.transaction {
            @Language("PostgreSQL") val query = """
select client_order.id         order_id,
       is_active               order_is_active,
       client_order.created_at order_created_at,
       client_order.updated_at order_updated_at,
       order_status.name       order_status,
       client_order.cost       order_cost,

       payment.id              payment_id,
       payment.order_id        payment_order_id,
       amount                  payment_amount,
       transaction             payment_transaction,
       payment.created_at      payment_created_at,
       payment.updated_at      payment_updated_at,
       payment_type.name       payment_type,

       client.id               client_id,
       address                 client_address,
       phone                   client_phone,
       client_user.login       client_login,
       client_user.password    client_password,
       client_user.role        client_role,

       courier.id              courier_id,
       courier_user.login      courier_login,
       courier_user.password   courier_password,
       courier_user.role       courier_role,

       operator.id             operator_id,
       number                  operator_number,
       operator_user.login     operator_login,
       operator_user.password  operator_password,
       operator_user.role      operator_role,

       manager.id              manager_id,
       restaurant              manager_restaurant,
       manager_user.login      manager_login,
       manager_user.password   manager_password,
       manager_user.role       manager_role

from client_order
         join order_status on client_order.status_id = order_status.id
         left join payment on client_order.id = payment.order_id
         left join payment_type on payment.type_id = payment_type.id
         left join client on client_order.client_id = client.id
         left join courier on client_order.courier_id = courier.id
         left join operator on client_order.operator_id = operator.id
         left join manager on client_order.manager_id = manager.id
         left join "user" client_user on client_id = client_user.id
         left join "user" manager_user on client_order.manager_id = manager_user.id
         left join "user" operator_user on operator_id = operator_user.id
         left join "user" courier_user on courier_id = courier_user.id
where ${where.toSQL()}
""".trimIndent()
            query.execAndMap { buildOrder() }
        }.map {
            val promo = Promo.modelManager.getForOrder(it.id)
            it.copy(promo = promo)
        }

    override suspend fun update(model: Order): Order {
        Db.transaction {
            @Language("PostgreSQL") val query = """
                update client_order
                set status_id = (select id from order_status where name = '${model.status.name.toLowerCase()}'),
                is_active = ${model.isActive},
                cost = ${model.cost},
                manager_id = ${model.manager?.id},
                operator_id = ${model.operator?.id},
                courier_id = ${model.courier?.id},
                created_at = ${model.createdAt.toDb()},
                updated_at =  ${model.updatedAt.toDb()}
                where id = ${model.id}
""".trimIndent()
            query.exec()
        }

        return model
    }

    override suspend fun create(model: Order): Order =
        Db.transaction {
            val status = OrderStatusTable.select {
                OrderStatusTable.name eq model.status.name.toLowerCase()
            }.map { it[OrderStatusTable.id] }.single()
            val inserted = OrderTable.insert {
                it[statusId] = status
                it[isActive] = model.isActive
                it[clientId] = model.client.id
                it[cost] = model.cost
                it[managerId] = model.manager?.id
                it[operatorId] = model.operator?.id
                it[courierId] = model.courier?.id
                it[createdAt] = model.createdAt
                it[updatedAt] = model.updatedAt
            }
            model.copy(id = inserted[OrderTable.id])
        }

    override suspend fun get(id: Int): Order? = list {
        intColumn("client_order", "id").eq(id)
    }.firstOrNull()

}

suspend fun ModelManager<Order>.pizza(orderId: Int): List<Pizza> {
    @Language("PostgreSQL") val query = """
select pizza_id id
from order_pizza
where order_pizza.order_id = ${orderId}
    """.trimIndent()
    val pizzaIds = Db.transaction {
        query.execAndMap { getInt("id") }
    }
    return Pizza.modelManager.list(pizzaIds)
}

suspend fun ModelManager<Order>.addPizzaToOrder(order: Order, pizza: List<Int>) = Db.transaction {
    pizza.forEach { pizzaId ->
        OrderPizzaTable.insert {
            it[OrderPizzaTable.orderId] = order.id
            it[OrderPizzaTable.pizzaId] = pizzaId
        }
    }
}



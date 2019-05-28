package ru.spbstu.architectures.pizzaService.db.manager

import org.intellij.lang.annotations.Language
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.insert
import ru.spbstu.architectures.pizzaService.db.Db
import ru.spbstu.architectures.pizzaService.models.ModelManager
import ru.spbstu.architectures.pizzaService.db.table.OrderPizzaTable
import ru.spbstu.architectures.pizzaService.external.list
import ru.spbstu.architectures.pizzaService.models.*
import ru.spbstu.architectures.pizzaService.utils.*
import java.sql.ResultSet

object OrderModelManager :
    ModelManager<Order> {
    private fun ResultSet.buildUser(prefix: String): User? {
        val login = getString("${prefix}_login") ?: return null
        val id = getInt("${prefix}_id")
        val password = getString("${prefix}_password")
        val role = UserRoleType.valueOf(getString("${prefix}_role"))
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
        return Order(
            getInt("order_id"),
            status,
            getBoolean("order_is_active"),
            buildUser("client") as Client,
            buildUser("modelManager") as? Manager,
            buildUser("operator") as? Operator,
            buildUser("courier") as? Courier,
            buildPayment(),
            getTimestamp("order_created_at").toDateTime(),
            getTimestamp("order_updated_at").toDateTime()
        )
    }

    override suspend fun list(where: SqlExpressionBuilder.() -> Op<Boolean>) =
        Db.transaction {
            @Language("PostgreSQL") val query = """select
  "order".id             order_id,
  is_active              order_is_active,
  "order".created_at     order_created_at,
  "order".updated_at     order_updated_at,
  order_status.name      order_status,

  payment.id             payment_id,
  order_id               payment_order_id,
  amount                 payment_amount,
  transaction            payment_transaction,
  payment.created_at     payment_created_at,
  payment.updated_at     payment_updated_at,
  payment_type.name      payment_type,

  client.id              client_id,
  address                client_address,
  client_user.login      client_login,
  client_user.password   client_password,
  client_user.role       client_role,

  courier.id             courier_id,
  courier_user.login     courier_login,
  courier_user.password  courier_password,
  courier_user.role      courier_role,

  operator.id            operator_id,
  number                 operator_number,
  operator_user.login    operator_login,
  operator_user.password operator_password,
  operator_user.role     operator_role,

  manager.id             manager_id,
  restaurant             manager_restaurant,
  manager_user.login     manager_login,
  manager_user.password  manager_password,
  manager_user.role      manager_role

from "order"
       join order_status on "order".status_id = order_status.id
       left join payment on "order".id = payment.order_id
       left join payment_type on payment.type_id = payment_type.id
       left join client on "order".client_id = client.id
       left join courier on "order".courier_id = courier.id
       left join operator on "order".operator_id = operator.id
       left join manager on "order".manager_id = manager.id
       left join "user" client_user on client_id = client_user.id
       left join "user" manager_user on manager_id = manager_user.id
       left join "user" operator_user on operator_id = operator_user.id
       left join "user" courier_user on courier_id = courier_user.id
where ${where.toSQL()}
""".trimIndent()
            query.execAndMap { it.buildOrder() }
        }

    override suspend fun update(model: Order): Order {
        Db.transaction {
            @Language("PostgreSQL") val query = """
                update "order"
                set status_id = (select id from order_status where name = '${model.status.name.toLowerCase()}'),
                is_active = ${model.isActive},
                manager_id = ${model.manager?.id},
                operator_id = ${model.operator?.id},
                courier_id = ${model.courier?.id},
                created_at = ${model.createdAt.toTimestamp()},
                updated_at =  ${model.updatedAt.toTimestamp()}
                where id = ${model.id}
""".trimIndent()
            query.execAndMap { }.first()
        }

        return model
    }

    override suspend fun create(model: Order): Order =
        Db.transaction {
            @Language("PostgreSQL") val query = """
                insert into "order"
values (default, (select id from order_status where name = '${model.status.name.toLowerCase()}'), false, ${model.client.id}, null, null, null,
        ${model.createdAt.toTimestamp()}, ${model.updatedAt.toTimestamp()})
""".trimIndent()
            query.execAndMap { model.copy(id = it.getInt("id")) }.first()
        }

    override suspend fun get(id: Int): Order? = list {
        intColumn("order", "id").eq(id)
    }.firstOrNull()

}

suspend fun ModelManager<Order>.pizza(orderId: Int): List<Pizza> {
    @Language("PostgreSQL") val query = """
        select pizza_id from order_pizza
        where order_pizza.order_id = ${orderId}
    """.trimIndent()
    val pizzaIds = Db.transaction {
        query.execAndMap {
            it.run { getInt("id") }
        }
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

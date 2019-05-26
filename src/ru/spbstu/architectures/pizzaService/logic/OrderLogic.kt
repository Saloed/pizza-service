package ru.spbstu.architectures.pizzaService.logic

import com.sun.org.apache.xpath.internal.operations.Or
import org.jetbrains.exposed.sql.Op
import org.joda.time.DateTime
import ru.spbstu.architectures.pizzaService.db.manager.activeOrders
import ru.spbstu.architectures.pizzaService.db.manager.addPizzaToOrder
import ru.spbstu.architectures.pizzaService.db.manager.orders
import ru.spbstu.architectures.pizzaService.models.*
import ru.spbstu.architectures.pizzaService.utils.MyResult


private abstract class OrderTransition<T : User>(val from: List<OrderStatus>, val to: OrderStatus) {
    constructor(vararg from: OrderStatus, to: OrderStatus) : this(from.asList(), to)

    fun match(user: User, order: Order, status: OrderStatus) =
        checkUserAccess(user, order) && order.status in from && status == to

    abstract fun checkUserAccess(user: User, order: Order): Boolean
    abstract suspend fun apply(user: User, order: Order): MyResult<Order>

    companion object {
        fun success(order: Order) = MyResult.Success(order)
        fun fail(message: String) = MyResult.Error<Order>(message)
    }
}

private abstract class ClientOrderTransition(vararg from: OrderStatus, to: OrderStatus) :
    OrderTransition<Client>(from.asList(), to) {
    override fun checkUserAccess(user: User, order: Order) = user is Client && order.client.id == user.id
}

private abstract class ManagerOrderTransition(vararg from: OrderStatus, to: OrderStatus) :
    OrderTransition<Manager>(from.asList(), to) {
    override fun checkUserAccess(user: User, order: Order) = user is Manager && order.manager?.id == user.id
}

private abstract class OperatorOrderTransition(vararg from: OrderStatus, to: OrderStatus) :
    OrderTransition<Operator>(from.asList(), to) {
    override fun checkUserAccess(user: User, order: Order) = user is Operator && order.operator?.id == user.id
}

private abstract class CourierOrderTransition(vararg from: OrderStatus, to: OrderStatus) :
    OrderTransition<Courier>(from.asList(), to) {
    override fun checkUserAccess(user: User, order: Order) = user is Courier && order.courier?.id == user.id
}


private val transitions = listOf(
    object : ClientOrderTransition(OrderStatus.DRAFT, to = OrderStatus.NEW) {
        suspend fun findOperator(): Operator {
            val operators = Operator.modelManager.list { Op.TRUE }
            return operators.random()
        }

        suspend fun findManager(): Manager {
            val managers = Manager.modelManager.list { Op.TRUE }
            return managers.random()
        }

        override suspend fun apply(user: User, order: Order): MyResult<Order> {
            val operator = findOperator()
            val manager = findManager()
            return success(order.copy(status = to, operator = operator, manager = manager, isActive = true))
        }
    },
    object : ClientOrderTransition(
        OrderStatus.DRAFT,
        OrderStatus.NEW,
        OrderStatus.APPROVED,
        OrderStatus.PROCESSING,
        OrderStatus.READY,
        OrderStatus.SHIPPING,
        to = OrderStatus.CANCELED
    ) {
        override suspend fun apply(user: User, order: Order): MyResult<Order> {
            return success(order.copy(isActive = false, status = to))
        }
    },
    object : OperatorOrderTransition(OrderStatus.NEW, to = OrderStatus.APPROVED) {
        override suspend fun apply(user: User, order: Order): MyResult<Order> {
            return success(order.copy(status = to))
        }
    },
    object : OperatorOrderTransition(OrderStatus.NEW, to = OrderStatus.CANCELED) {
        override suspend fun apply(user: User, order: Order): MyResult<Order> {
            return success(order.copy(status = to, isActive = false))
        }
    },
    object : ManagerOrderTransition(OrderStatus.APPROVED, to = OrderStatus.PROCESSING) {
        override suspend fun apply(user: User, order: Order): MyResult<Order> {
            return success(order.copy(status = to))
        }
    },
    object : ManagerOrderTransition(OrderStatus.PROCESSING, to = OrderStatus.READY) {
        suspend fun findCourier(): Courier {
            val couriers = Courier.modelManager.list { Op.TRUE }
            return couriers.random()
        }

        override suspend fun apply(user: User, order: Order): MyResult<Order> {
            val courier = findCourier()
            return success(order.copy(status = to, courier = courier))
        }

    },
    object : ManagerOrderTransition(OrderStatus.APPROVED, OrderStatus.PROCESSING, to = OrderStatus.CANCELED) {
        override suspend fun apply(user: User, order: Order): MyResult<Order> {
            return success(order.copy(status = to, isActive = false))
        }
    },
    object : CourierOrderTransition(OrderStatus.READY, to = OrderStatus.SHIPPING) {
        override suspend fun apply(user: User, order: Order): MyResult<Order> {
            return success(order.copy(status = to))
        }
    },
    object : CourierOrderTransition(OrderStatus.SHIPPING, to = OrderStatus.CLOSED) {
        override suspend fun apply(user: User, order: Order): MyResult<Order> {
            if (order.payment == null) return fail("No payment for order")
            return success(order.copy(status = to, isActive = false))
        }
    }
)


object OrderLogic {

    data class OrderClientWithPermission(val address: String?, val phone: String?)
    data class OrderManagerWithPermission(val id: Int?, val login: String?, val restaurant: String?)
    data class OrderOperatorWithPermission(val id: Int?, val login: String?, val number: Int?)
    data class OrderCourierWithPermission(val id: Int?, val login: String?)
    data class OrderPaymentWithPermission(val id: Int?, val type: String?, val amount: Int?, val transaction: String?)
    data class OrderWithPermission(
        val id: Int,
        val status: String,
        val payment: OrderPaymentWithPermission?,
        val client: OrderClientWithPermission?,
        val operator: OrderOperatorWithPermission?,
        val manager: OrderManagerWithPermission?,
        val courier: OrderCourierWithPermission?
    )

    private fun sanitizeOrder(user: User, order: Order) = when (user) {
        is Client -> {
            val client = OrderClientWithPermission(order.client.address, order.client.phone)
            val operator = OrderOperatorWithPermission(null, null, order.operator?.number)
            val manager = OrderManagerWithPermission(null, null, order.manager?.restaurant)
            val payment = OrderPaymentWithPermission(
                order.payment?.id,
                order.payment?.type?.name,
                order.payment?.amount,
                order.payment?.cardTransaction
            )
            OrderWithPermission(order.id, order.status.name, payment, client, operator, manager, null)
        }
        is Manager -> {
            val client = OrderClientWithPermission(order.client.address, order.client.phone)
            val operator = OrderOperatorWithPermission(
                order.operator?.id, order.operator?.login, order.operator?.number
            )
            val manager = OrderManagerWithPermission(order.manager?.id, order.manager?.login, order.manager?.restaurant)
            val courier = OrderCourierWithPermission(order.courier?.id, order.courier?.login)
            val payment = OrderPaymentWithPermission(
                order.payment?.id,
                order.payment?.type?.name,
                order.payment?.amount,
                order.payment?.cardTransaction
            )
            OrderWithPermission(order.id, order.status.name, payment, client, operator, manager, courier)
        }
        is Operator -> {
            val client = OrderClientWithPermission(order.client.address, order.client.phone)
            val operator = OrderOperatorWithPermission(
                order.operator?.id, order.operator?.login, order.operator?.number
            )
            val manager = OrderManagerWithPermission(order.manager?.id, order.manager?.login, order.manager?.restaurant)
            val payment = OrderPaymentWithPermission(
                order.payment?.id,
                order.payment?.type?.name,
                order.payment?.amount,
                order.payment?.cardTransaction
            )
            OrderWithPermission(order.id, order.status.name, payment, client, operator, manager, null)
        }
        is Courier -> {
            val client = OrderClientWithPermission(order.client.address, order.client.phone)
            val manager = OrderManagerWithPermission(order.manager?.id, order.manager?.login, order.manager?.restaurant)
            val courier = OrderCourierWithPermission(order.courier?.id, order.courier?.login)

            OrderWithPermission(order.id, order.status.name, null, client, null, manager, courier)
        }
    }

    suspend fun list(user: User) = when (user) {
        is Client -> Client.modelManager.orders(user)
        is Manager -> Manager.modelManager.activeOrders(user)
        is Operator -> Operator.modelManager.activeOrders(user)
        is Courier -> Courier.modelManager.activeOrders(user)
    }.map { sanitizeOrder(user, it) }

    suspend fun get(user: User, id: Int): OrderWithPermission? {
        val order = Order.modelManager.get(id) ?: return null
        return when (user) {
            is Client -> if (order.client.id == user.id) order else null
            is Manager -> if (order.manager?.id == user.id) order else null
            is Operator -> if (order.operator?.id == user.id && order.isActive && order.status in OrderStatus.forOperator) order else null
            is Courier -> if (order.courier?.id == user.id && order.isActive && order.status in OrderStatus.forCourier) order else null
        }?.let { sanitizeOrder(user, it) }
    }

    suspend fun create(user: User, pizza: List<Int>): MyResult<OrderWithPermission?> {
        if (user !is Client) return MyResult.Error("Only client can create orders")
        if (pizza.isEmpty()) return MyResult.Error("Order pizza is empty")
        val order = Order(0, OrderStatus.DRAFT, false, user, null, null, null, null, DateTime.now(), DateTime.now())
        val createdOrder = Order.modelManager.create(order)
        Order.modelManager.addPizzaToOrder(createdOrder, pizza)
        val result = get(user, createdOrder.id)
        return MyResult.Success(result)
    }


    suspend fun change(user: User, orderId: Int, status: OrderStatus): MyResult<OrderWithPermission?> {
        val order = Order.modelManager.get(orderId) ?: return MyResult.Error("No such order")
        val transition =
            transitions.find { it.match(user, order, status) } ?: return MyResult.Error("Transition is not possible")
        val changed = transition.apply(user, order)
        val result = when (changed) {
            is MyResult.Error -> return MyResult.Error(changed.message)
            is MyResult.Success -> changed.data
        }
        val resultWithDate = result.copy(updatedAt = DateTime.now())
        Order.modelManager.update(resultWithDate)
        val resultWithPermission = get(user, resultWithDate.id)
        return MyResult.Success(resultWithPermission)
    }

}


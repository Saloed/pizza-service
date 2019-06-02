package ru.spbstu.architectures.pizzaService.logic

import org.joda.time.DateTime
import ru.spbstu.architectures.pizzaService.models.*
import ru.spbstu.architectures.pizzaService.utils.MyResult
import ru.spbstu.architectures.pizzaService.web.PaymentCreateForm

object PaymentLogic {
    suspend fun create(user: User, payment: PaymentCreateForm): MyResult<PaymentWithPermission> {
        if (user !is Courier) return MyResult.Error("Only courier can create payments")
        val order = Order.modelManager.get(payment.orderId)
        if (order == null || order.courier?.id != user.id) return MyResult.Error("Order not found")
        if (order.payment != null) return MyResult.Error("Order already payed")
        if (order.cost != payment.amount) return MyResult.Error("Incorrect payment amount")
        val paymentType = PaymentType.valueOf(payment.type.toUpperCase())
        if (paymentType == PaymentType.CARD && payment.transaction == null) return MyResult.Error("Card payments must have a transaction")
        val paymentRecord = Payment(
            0, order.id, paymentType, payment.amount, payment.transaction,
            DateTime.now(), DateTime.now()
        )
        val result = Payment.modelManager.create(paymentRecord)
        return MyResult.Success(result.fullPermission())
    }
}

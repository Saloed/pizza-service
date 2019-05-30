package ru.spbstu.architectures.pizzaService.models

data class ClientWithPermission(val id: Int?, val login: String?, val address: String?, val phone: String?)

fun Client.fullPermission() = ClientWithPermission(id, login, address, phone)
fun Client.infoOnlyPermission() = ClientWithPermission(null, null, address, phone)

data class ManagerWithPermission(val id: Int?, val login: String?, val restaurant: String?)

fun Manager.fullPermission() = ManagerWithPermission(id, login, restaurant)
fun Manager.infoOnlyPermission() = ManagerWithPermission(null, null, restaurant)

data class OperatorWithPermission(val id: Int?, val login: String?, val number: Int?)

fun Operator.fullPermission() = OperatorWithPermission(id, login, number)
fun Operator.infoOnlyPermission() = OperatorWithPermission(null, null, number)

data class CourierWithPermission(val id: Int?, val login: String?)

fun Courier.fullPermission() = CourierWithPermission(id, login)
fun Courier.infoOnlyPermission() = CourierWithPermission(null, null)

data class PaymentWithPermission(val id: Int?, val type: String?, val amount: Int?, val transaction: String?)

fun Payment.fullPermission() = PaymentWithPermission(id, type.name, amount, cardTransaction)

data class OrderWithPermission(
    val id: Int,
    val status: String,
    val cost: Int?,
    val payment: PaymentWithPermission?,
    val promo: PromoWithPermission?,
    val client: ClientWithPermission?,
    val operator: OperatorWithPermission?,
    val manager: ManagerWithPermission?,
    val courier: CourierWithPermission?
)

data class PromoClientWithPermission(
    val promoId: Int,
    val client: ClientWithPermission,
    val operator: OperatorWithPermission?,
    val status: String?
)
fun PromoClient.fullPermission() = PromoClientWithPermission(
    promo.id, client.fullPermission(), operator?.fullPermission(), status.name
)

data class PromoWithPermission(
    val id: Int,
    val effect: String,
    val description: String,
    val status: String?,
    val manager: ManagerWithPermission?,
    val result: String?
)

fun Promo.fullPermission() = PromoWithPermission(
    id, effect.name, description, status.name, manager.infoOnlyPermission(), result
)

fun Promo.infoOnlyPermission() = PromoWithPermission(id, effect.name, description, null, null, null)


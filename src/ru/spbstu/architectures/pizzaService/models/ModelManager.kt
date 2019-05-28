package ru.spbstu.architectures.pizzaService.models

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import ru.spbstu.architectures.pizzaService.db.manager.*
import ru.spbstu.architectures.pizzaService.external.PizzaModelManager


interface ModelManager<T : Model<T>> {
    suspend fun get(id: Int): T?
    suspend fun create(model: T): T
    suspend fun update(model: T): T
    suspend fun list(where: SqlExpressionBuilder.() -> Op<Boolean>): List<T>
}


abstract class ModelManagerFactory<T : Model<T>>(clazz: Class<T>) {
    private val managers = mapOf(
        Client::class.java to ClientModelManager,
        Manager::class.java to ManagerModelManager,
        Operator::class.java to OperatorModelManager,
        Courier::class.java to CourierModelManager,
        Pizza::class.java to PizzaModelManager,
        Order::class.java to OrderModelManager,
        Payment::class.java to PaymentModelManager,
        Promo::class.java to PromoModelManager,
        PromoClient::class.java to PromoClientModelManager
    )
    val modelManager: ModelManager<T> =
        managers[clazz] as? ModelManager<T>
            ?: throw IllegalArgumentException("Manager for model ${clazz} is not registered")


}

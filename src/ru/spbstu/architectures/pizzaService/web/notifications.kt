package ru.spbstu.architectures.pizzaService.web

enum class ChangeType {
    CREATE, UPDATE, DELETE
}

enum class NotificationType {
    ORDER
}

data class Notification<T>(val userId: Int, val data: T, val type: ChangeType, val notificationType: NotificationType)

object NotificationService {
    private val listeners = mutableMapOf<Int, suspend (Notification<*>) -> Unit>()

    fun addChangeListener(userId: Int, listener: suspend (Notification<*>) -> Unit) {
        listeners[userId] = listener
    }

    fun removeChangeListener(userId: Int) = listeners.remove(userId)

    private suspend fun <T> onChange(type: ChangeType, userId: Int, data: T, notificationType: NotificationType) {
        val listener = listeners[userId] ?: return
        val notification = Notification(userId, data, type, notificationType)
        listener(notification)
    }

    suspend fun notifyNewOrder(userId: Int, orderId: Int) {
        return onChange(ChangeType.CREATE, userId, orderId, NotificationType.ORDER)
    }

    suspend fun notifyUpdateOrder(userId: Int, orderId: Int) {
        return onChange(ChangeType.UPDATE, userId, orderId, NotificationType.ORDER)
    }

}

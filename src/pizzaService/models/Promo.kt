package ru.spbstu.architectures.pizzaService.models

enum class PromoStatus {
    NEW, ACTIVE, CLOSED
}


data class Promo(val id: Int, val status: PromoStatus, val result: String?){
    val clients: List<Client>? = null
}

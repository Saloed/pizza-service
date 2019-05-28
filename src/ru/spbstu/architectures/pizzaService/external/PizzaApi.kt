package ru.spbstu.architectures.pizzaService.external

import io.ktor.client.request.get

data class Topping(val ID: Int, val Name: String)
data class Pizza(val ID: Int, val Name: String, val MediaDetailed: String, val Toppings: List<Topping>)


object PizzaApi {

    private const val url = "https://api.dominos.is/api/pizza?lang=en"
    private const val mediaPrefixUrl = "https://www.dominos.is"


    suspend fun query(): List<Pizza> {
        return ExternalApiDbCache.get("pizza") {
            val data = HttpClient.client.get<List<Pizza>>(url)
            val dataWithMediaUrl = data.map { it.copy(MediaDetailed = "$mediaPrefixUrl${it.MediaDetailed}") }
            dataWithMediaUrl
        }
    }

}

package ru.spbstu.architectures.pizzaService.external

import io.ktor.client.request.get


data class Topping(val id: Int, val name: String)
data class Pizza(val id: Int, val name: String, val imageUrl: String, val price: Int, val toppings: List<Topping>)

object PizzaApi {

    private data class ApiPrice(val RefID: String, val Size: String, val PriceDelivery: Int)
    private data class ApiTopping(val ID: Int, val Name: String)
    private data class ApiPizza(
        val ID: Int,
        val Name: String,
        val MediaDetailed: String,
        val Toppings: List<ApiTopping>,
        val Prices: List<ApiPrice>
    )

    private const val url = "https://api.dominos.is/api/pizza?lang=en"
    private const val mediaPrefixUrl = "https://www.dominos.is"


    suspend fun query(): List<Pizza> {
        return ExternalApiDbCache.get("pizza") {
            val data = HttpClient.client.get<List<ApiPizza>>(url)
            val dataWithMediaUrl = data.map { it.copy(MediaDetailed = "$mediaPrefixUrl${it.MediaDetailed}") }
            dataWithMediaUrl.map {
                val toppings = it.Toppings.map { Topping(it.ID, it.Name) }
                val price = it.Prices.last()
                Pizza(it.ID, it.Name, it.MediaDetailed, price.PriceDelivery, toppings)
            }
        }
    }

}

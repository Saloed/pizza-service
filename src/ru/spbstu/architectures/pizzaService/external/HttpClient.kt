package ru.spbstu.architectures.pizzaService.external

import io.ktor.client.engine.apache.Apache
import io.ktor.client.HttpClient
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature

object HttpClient {
    val client = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = GsonSerializer {
                serializeNulls()
                disableHtmlEscaping()
            }
        }
    }
}

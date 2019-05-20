package ru.spbstu.architectures.pizzaService.utils

import io.ktor.util.hex
import ru.spbstu.architectures.ConfigurationFacade
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object Hasher {
    private lateinit var hmac: Mac
    fun init(config: ConfigurationFacade) {
        val hmacKey = SecretKeySpec(config.secretKey, "HmacSHA1")
        hmac = Mac.getInstance("HmacSHA1")
        hmac.init(hmacKey)
    }

    fun hash(data: String): String = hex(hmac.doFinal(data.toByteArray(Charsets.UTF_8)))

}

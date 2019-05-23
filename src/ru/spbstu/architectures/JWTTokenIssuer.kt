package ru.spbstu.architectures

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.Payload
import ru.spbstu.architectures.pizzaService.models.User
import java.util.*

private const val validityInMs = 36_000_00 * 10 // 10 hours

data class UserTokenParams(val id: Int, val login: String, val role: String)

data class Token(val token: String)

class JWTTokenIssuer(config: ConfigurationFacade) {

    private val algorithm = Algorithm.HMAC512(config.secretKey)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(config.tokenIssuer)
        .build()


    fun makeToken(user: User): Token = JWT.create()
        .withSubject("Authentication")
        .withIssuer(config.tokenIssuer)
        .withClaim("id", user.id)
        .withClaim("login", user.login)
        .withClaim("role", user.role.name)
        .withExpiresAt(getExpiration())
        .sign(algorithm)
        .let { Token(it) }


    private fun getExpiration() = Date(System.currentTimeMillis() + validityInMs)

}


fun Payload.tokenParams(): UserTokenParams? {
    val id = getClaim("id").asInt()
    val login = getClaim("login").asString()
    val role = getClaim("role").asString()
    if (id == null || login == null || role == null) return null
    return UserTokenParams(id, login, role)
}

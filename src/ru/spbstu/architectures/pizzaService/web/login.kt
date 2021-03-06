package ru.spbstu.architectures.pizzaService.web

import io.ktor.auth.Credential
import io.ktor.auth.Principal
import ru.spbstu.architectures.UserTokenParams
import ru.spbstu.architectures.pizzaService.models.User
import ru.spbstu.architectures.pizzaService.utils.Hasher

data class UserCredentials(val username: String, val password: String) : Credential

data class UserPrincipal(val user: User) : Principal


object UserAuthorization {
    suspend fun authorize(tokenParams: UserTokenParams): Principal? {
        val user = User.modelManager.get(tokenParams.login) ?: return null
        if (user.id != tokenParams.id) return null
        if (user.login != tokenParams.login) return null
        if (user.role.name != tokenParams.role) return null
        return UserPrincipal(user)
    }

    suspend fun authenticate(credentials: UserCredentials) =
        User.modelManager.get(credentials.username, Hasher.hash(credentials.password))

}



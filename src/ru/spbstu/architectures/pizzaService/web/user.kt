package ru.spbstu.architectures.pizzaService.web

import io.ktor.application.*
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import ru.spbstu.architectures.pizzaService.logic.UserLogic
import ru.spbstu.architectures.pizzaService.models.User
import ru.spbstu.architectures.pizzaService.models.UserRoleType
import ru.spbstu.architectures.pizzaService.utils.*

abstract class RegistrationForm {
    abstract val username: String
    abstract val password: String
}

data class ClientRegistrationForm(
    override val username: String,
    override val password: String,
    val address: String,
    val phone: String
) : RegistrationForm()

data class ManagerRegistrationForm(
    override val username: String,
    override val password: String,
    val restaurant: String
) : RegistrationForm()

data class OperatorRegistrationForm(
    override val username: String,
    override val password: String,
    val number: Int
) : RegistrationForm()

data class CourierRegistrationForm(
    override val username: String,
    override val password: String
) : RegistrationForm()


data class RegistrationErrorResponse(val message: String)

suspend fun createGenericUser(
    call: ApplicationCall,
    application: Application,
    form: RegistrationForm,
    userCreator: suspend (login: String, password: String) -> User?
) {
    val error = when {
        !UserValidator.passwordValid(form.password) -> "Password should be at least 6 characters long"
        !UserValidator.loginValid(form.username) -> "Login should be at least 4 characters long and consists of digits, letters, dots or underscores"
        User.manager.get(form.username) != null -> "User with the following login is already registered"
        else -> null
    }
    if (error != null) {
        val message = RegistrationErrorResponse(error)
        return call.respond(HttpStatusCode.BadRequest, message)
    }

    val passwordHash = Hasher.hash(form.password)
    val newUser = try {
        userCreator(form.username, passwordHash)
    } catch (e: Throwable) {
        application.log.error("Failed to register user", e)
        val error = "Failed to register"
        val message = RegistrationErrorResponse(error)
        return call.respond(HttpStatusCode.BadRequest, message)
    }

    if (newUser == null) {
        val error = "Failed to register"
        val message = RegistrationErrorResponse(error)
        return call.respond(HttpStatusCode.BadRequest, message)
    }
    call.respond(HttpStatusCode.OK, RegistrationErrorResponse(""))
}

fun Route.createClient() {
    post("/client") {
        val form = call.receive<ClientRegistrationForm>()
        createGenericUser(call, application, form) { login, password ->
            UserLogic.createClient(login, password)
        }
    }
}

fun Route.createUser() {
    post("/manager") {
        val user = call.userOrNull ?: return@post call.respond(HttpStatusCode.Unauthorized, "")
        val form = call.receive<ManagerRegistrationForm>()
        createGenericUser(call, application, form) { login, password ->
            UserLogic.create(user, login, password, UserRoleType.Manager)
        }
    }
    post("/operator") {
        val user = call.userOrNull ?: return@post call.respond(HttpStatusCode.Unauthorized, "")
        val form = call.receive<OperatorRegistrationForm>()
        createGenericUser(call, application, form) { login, password ->
            UserLogic.create(user, login, password, UserRoleType.Operator)
        }
    }
    post("/courier") {
        val user = call.userOrNull ?: return@post call.respond(HttpStatusCode.Unauthorized, "")
        val form = call.receive<CourierRegistrationForm>()
        createGenericUser(call, application, form) { login, password ->
            UserLogic.create(user, login, password, UserRoleType.Courier)
        }
    }
}

fun Route.listClients() {
    get("/client") {
        val user = call.userOrNull ?: return@get call.respond(HttpStatusCode.Unauthorized, "")
        val result = UserLogic.listClients(user)
        call.respondMyResult(result)
    }
}

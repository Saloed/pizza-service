package ru.spbstu.architectures.pizzaService.web

import de.nielsfalk.ktor.swagger.*
import io.ktor.application.*
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import ru.spbstu.architectures.pizzaService.logic.UserLogic
import ru.spbstu.architectures.pizzaService.models.User
import ru.spbstu.architectures.pizzaService.models.UserRoleType
import ru.spbstu.architectures.pizzaService.utils.*
import io.ktor.locations.Location
import io.ktor.routing.header
import ru.spbstu.architectures.pizzaService.models.ClientWithPermission

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
        User.modelManager.get(form.username) != null -> "User with the following login is already registered"
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

@Location("/client")
class ClientPath


@Location("/manager")
class ManagerPath

@Location("/operator")
class OperatorPath

@Location("/courier")
class CourierPath

fun Route.createClient() {
    post<ClientPath, ClientRegistrationForm>(
        "create".description("Register a new client").responds(
            ok<RegistrationErrorResponse>(),
            badRequest<RegistrationErrorResponse>()
        )
    ) { _, form ->
        createGenericUser(call, application, form) { login, password ->
            UserLogic.createClient(login, password)
        }
    }
}

fun Route.createUser() {
    post<ManagerPath, ManagerRegistrationForm>(
        "create".description("Create new manager")
            .withAuthorization()
            .responds(
                ok<RegistrationErrorResponse>(),
                badRequest<RegistrationErrorResponse>()
            )
    ) { _, form ->
        val user = call.userOrNull ?: return@post call.respond(HttpStatusCode.Unauthorized, "")
        createGenericUser(call, application, form) { login, password ->
            UserLogic.create(user, login, password, UserRoleType.Manager)
        }
    }
    post<OperatorPath, OperatorRegistrationForm>(
        "create".description("Create new operator")
            .withAuthorization()
            .responds(
                ok<RegistrationErrorResponse>(),
                badRequest<RegistrationErrorResponse>()
            )
    ) { _, form ->
        val user = call.userOrNull ?: return@post call.respond(HttpStatusCode.Unauthorized, "")
        createGenericUser(call, application, form) { login, password ->
            UserLogic.create(user, login, password, UserRoleType.Operator)
        }
    }
    post<CourierPath, CourierRegistrationForm>(
        "create".description("Create new courier")
            .withAuthorization()
            .responds(
                ok<RegistrationErrorResponse>(),
                badRequest<RegistrationErrorResponse>()
            )
    ) { _, form ->
        val user = call.userOrNull ?: return@post call.respond(HttpStatusCode.Unauthorized, "")
        createGenericUser(call, application, form) { login, password ->
            UserLogic.create(user, login, password, UserRoleType.Courier)
        }
    }
}

data class ClientListFilter(val id: Int?)

fun Route.listClients() {
    get<ClientPath>(
        "all".description("List of clients")
            .withAuthorization()
            .listQueryParameters<ClientListFilter>()
            .responds(
                ok<ClientWithPermission>()
            )
    ) {
        val user = call.userOrNull ?: return@get call.respond(HttpStatusCode.Unauthorized, "")
        val params = call.getListQueryParams<ClientListFilter>()
        val result = UserLogic.listClients(user)
        call.respondMyResult(result) {
            responseListRange(it, params.range)
        }
    }
}


package ru.spbstu.architectures

import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.jwt
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.Frame
import io.ktor.locations.Locations
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.netty.EngineMain
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import ru.spbstu.architectures.pizzaService.db.Db
import ru.spbstu.architectures.pizzaService.external.PizzaApi
import ru.spbstu.architectures.pizzaService.utils.Hasher
import ru.spbstu.architectures.pizzaService.utils.userOrNull
import ru.spbstu.architectures.pizzaService.web.*


val config: ConfigurationFacade = ConfigurationFacadeDummy

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    Db.init(config.db)
    environment.monitor.subscribe(ApplicationStopped) { Db.close() }

    Hasher.init(config)

    install(DefaultHeaders)
    install(CallLogging)
    install(ConditionalHeaders)
    install(PartialContent)
    install(Compression)
    install(Locations)
    install(WebSockets)
    install(StatusPages) {
        exception<NotImplementedError> { call.respond(HttpStatusCode.NotImplemented) }
    }

    val jwtTokenIssuer = JWTTokenIssuer(config)

    install(Authentication) {
        jwt {
            verifier(jwtTokenIssuer.verifier)
            realm = config.realm
            validate {
                it.payload.tokenParams()?.let { UserAuthorization.authorize(it) }
            }
        }
    }


    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        exposeHeader(HttpHeaders.ContentRange)
        allowCredentials = true
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }


    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
            serializeNulls()
        }
    }

    routing {
        post("/authenticate") {
            val credentials = call.receive<UserCredentials>()
            val user = UserAuthorization.authenticate(credentials) ?: return@post call.respond(
                HttpStatusCode.Unauthorized,
                "Invalid username or password"
            )
            val token = jwtTokenIssuer.makeToken(user)
            call.respond(token)
        }
        createClient()
        authenticate {
            createUser()
            order()
            pizza()
            webSocket("/notifications") {
                val user = call.userOrNull ?: return@webSocket
                log.info("User ${user.login} subscribed for notifications")
                try {
                    NotificationService.addChangeListener(user.id) { data ->
                        val dataText = Gson().toJson(data)
                        outgoing.send(Frame.Text(dataText))
                    }
                    while (true) {
                        incoming.receiveOrNull() ?: break
                    }
                } finally {
                    NotificationService.removeChangeListener(user.id)
                }
            }
        }
    }


}



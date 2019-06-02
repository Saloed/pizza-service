package ru.spbstu.architectures

import io.ktor.application.Application
import io.ktor.http.HttpHeaders
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.mockk.*
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.mapper.ObjectMapperType
import io.restassured.response.ResponseBodyExtractionOptions
import io.restassured.specification.RequestSpecification
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import ru.spbstu.architectures.pizzaService.db.Db
import ru.spbstu.architectures.pizzaService.db.table.*
import ru.spbstu.architectures.pizzaService.external.ApiCacheTable
import ru.spbstu.architectures.pizzaService.external.Pizza
import ru.spbstu.architectures.pizzaService.external.PizzaApi
import ru.spbstu.architectures.pizzaService.external.Topping
import ru.spbstu.architectures.pizzaService.models.*
import ru.spbstu.architectures.pizzaService.utils.Hasher
import ru.spbstu.architectures.pizzaService.web.UserCredentials
import java.util.concurrent.TimeUnit

abstract class ApplicationTest {


    private fun authenticate(user: User) = userTokens.computeIfAbsent(user) {
        val credentials = UserCredentials(user.login, user.password)
        given()
            .jsonBody(credentials)
            .When()
            .post("/authenticate")
            .then()
            .statusCode(200)
            .extract()
            .to()
    }


    protected fun RequestSpecification.When(): RequestSpecification {
        return this.`when`()
    }

    protected inline fun <reified T> ResponseBodyExtractionOptions.to(): T {
        return this.`as`(T::class.java, ObjectMapperType.GSON)
    }

    protected inline fun <reified T> ResponseBodyExtractionOptions.toList(): List<T> {
        return jsonPath().getList(".", T::class.java)
    }

    protected inline fun <reified T : Any> RequestSpecification.jsonBody(data: T): RequestSpecification =
        contentType(io.restassured.http.ContentType.JSON)
            .body(data)

    protected fun RequestSpecification.withUser(user: User): RequestSpecification {
        val token = authenticate(user)
        return header(HttpHeaders.Authorization, "Bearer ${token.token}")
    }

    @BeforeEach
    fun beforeEveryTest() {
        createUsers()
    }

    @AfterEach
    fun afterEveryTest() {
        userTokens.clear()
        debugUserStorage.clear()
        transaction(testDatabase) {
            UserTable.deleteAll()
            PaymentTable.deleteAll()
        }
    }


    companion object {

        private var serverStarted = false

        private lateinit var server: ApplicationEngine

        @BeforeAll
        @JvmStatic
        fun beforeTests() {
            testDatabase =
                    Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;", "org.h2.Driver")
            mockkObject(Db)
            every { Db.database } returns testDatabase
            every { Db.init(any()) } returns Unit

            initDb()
            mockPizzaApi()

            mockkObject(Hasher)
            every { Hasher.hash(password) } returns password

            if (!serverStarted) {
                server = embeddedServer(Netty, 8080, watchPaths = listOf("Main"), module = Application::module)
                server.start()
                serverStarted = true

                RestAssured.baseURI = "http://localhost"
                RestAssured.port = 8080
                Runtime.getRuntime().addShutdownHook(Thread { server.stop(0, 0, TimeUnit.SECONDS) })
            }
        }

        val password = "password"

        private val debugUserStorage = mutableMapOf<String, User>()

        val client by debugUserStorage
        val client2 by debugUserStorage
        val manager by debugUserStorage
        val operator by debugUserStorage
        val courier by debugUserStorage

        private val debugUsers = listOf<User>(
            Client(11, "client", password, "address", "phone"),
            Client(12, "client2", password, "address2", "phone2"),
            Manager(21, "manager", password, "restaurant"),
            Operator(31, "operator", password, 1),
            Courier(41, "courier", password)
        )

        private val userTokens = mutableMapOf<User, Token>()

        private lateinit var testDatabase: Database

        private val pizzaToppings = listOf(Topping(0, "topping"))
        private val pizzaList = (0..20).map {
            Pizza(it, "$it", "", it * 10, pizzaToppings)
        }

        private fun mockPizzaApi() {
            mockkObject(PizzaApi)
            coEvery { PizzaApi.query() } returns pizzaList
        }

        private fun initDb() = transaction(testDatabase) {
            SchemaUtils.create(UserTable, ClientTable, OperatorTable, ManagerTable, CourierTable)
            SchemaUtils.create(ApiCacheTable)
            SchemaUtils.create(OrderStatusTable, OrderTable)
            SchemaUtils.create(OrderPizzaTable)
            SchemaUtils.create(PaymentTypeTable, PaymentTable)
            SchemaUtils.create(PromoTable, PromoClientTable, OrderPromoTable)
            OrderStatus.values().forEach { status ->
                OrderStatusTable.insert {
                    it[id] = status.ordinal
                    it[name] = status.name.toLowerCase()
                }
            }
            PaymentType.values().forEach { paymentType ->
                PaymentTypeTable.insert {
                    it[id] = paymentType.ordinal
                    it[name] = paymentType.name.toLowerCase()
                }
            }
        }


        private fun createUsers() = runBlocking {
            debugUsers.map<User, User> {
                when (it) {
                    is Client -> Client.modelManager.create(it)
                    is Manager -> Manager.modelManager.create(it)
                    is Operator -> Operator.modelManager.create(it)
                    is Courier -> Courier.modelManager.create(it)
                }
            }.forEach {
                debugUserStorage[it.login] = it
            }
        }


        @AfterAll
        @JvmStatic
        fun afterTests() {
            unmockkAll()
        }
    }
}

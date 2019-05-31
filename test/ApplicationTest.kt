package ru.spbstu.architectures

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.*
import io.ktor.server.testing.handleRequest
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.restassured.RestAssured
import io.restassured.response.ResponseBodyExtractionOptions
import io.restassured.specification.RequestSpecification
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.Before
import ru.spbstu.architectures.pizzaService.db.Db
import ru.spbstu.architectures.pizzaService.db.table.*
import ru.spbstu.architectures.pizzaService.external.ApiCacheTable
import ru.spbstu.architectures.pizzaService.external.Pizza
import ru.spbstu.architectures.pizzaService.external.PizzaApi
import ru.spbstu.architectures.pizzaService.external.Topping
import ru.spbstu.architectures.pizzaService.models.*
import ru.spbstu.architectures.pizzaService.utils.Hasher
import ru.spbstu.architectures.pizzaService.web.UserCredentials
import kotlin.test.assertNotNull

val gson = Gson()

abstract class ApplicationTest {

    val password = "password"

    val client = Client(11, "client", password, "address", "phone")
    val client2 = Client(12, "client2", password, "address2", "phone2")
    val manager = Manager(21, "manager", password, "restaurant")
    val operator = Operator(31, "operator", password, 1)
    val courier = Courier(41, "courier", password)

    private val debugUsers = listOf(
        client,
        client2,
        manager,
        operator,
        courier
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
        debugUsers.forEach {
            when (it) {
                is Client -> Client.modelManager.create(it)
                is Manager -> Manager.modelManager.create(it)
                is Operator -> Operator.modelManager.create(it)
                is Courier -> Courier.modelManager.create(it)
            }
        }
    }

    @Before
    fun beforeTests() {
        testDatabase = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;", "org.h2.Driver")
        mockkObject(Db)
        every { Db.database } returns testDatabase
        every { Db.init(any()) } returns Unit

        initDb()
        mockPizzaApi()

        mockkObject(Hasher)
        every { Hasher.hash(password) } returns password

        createUsers()



    }

    fun TestApplicationEngine.authenticate(user: User) = userTokens.computeIfAbsent(user) {
        val credentials = UserCredentials(user.login, user.password)
        handleRequest(HttpMethod.Post, "/authenticate") {
            setJsonBody(credentials)
        }.getJson()
    }

    fun TestApplicationEngine.handleRequestWithUser(
        method: HttpMethod,
        uri: String,
        user: User,
        setup: TestApplicationRequest.() -> Unit = {}
    ): TestApplicationCall = handleRequest {
        this.uri = uri
        this.method = method
        val token = authenticate(user)
        addHeader(HttpHeaders.Authorization, "Bearer ${token.token}")
        setup()
    }


    fun testApp(callback: suspend TestApplicationEngine.() -> Unit) {
        withTestApplication({ module() }) { runBlocking { callback() } }
    }

    @After
    fun afterTests() {
        unmockkAll()
    }

    protected fun RequestSpecification.When(): RequestSpecification {
        return this.`when`()
    }

    protected inline fun <reified T> ResponseBodyExtractionOptions.to(): T {
        return this.`as`(T::class.java)
    }

}

inline fun <reified T> TestApplicationRequest.setJsonBody(value: T) {
    val data = gson.toJson(value)
    setBody(data.toByteArray())
    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
}


inline fun <reified T> TestApplicationCall.getJson(): T {
    val value = assertNotNull(response.content, "response content is null")
    return gson.fromJson<T>(value, T::class.java)
}

inline fun <reified T> TestApplicationCall.getJsonList(): List<T> {
    val value = assertNotNull(response.content, "response content is null")
    val resultListType = TypeToken.getParameterized(List::class.java, T::class.java).type
    return gson.fromJson(value, resultListType)
}


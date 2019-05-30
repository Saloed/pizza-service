package ru.spbstu.architectures

import com.google.gson.Gson
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.*
import io.ktor.server.testing.handleRequest
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import ru.spbstu.architectures.pizzaService.db.manager.UserModelManager
import ru.spbstu.architectures.pizzaService.models.*
import ru.spbstu.architectures.pizzaService.utils.Hasher
import ru.spbstu.architectures.pizzaService.web.UserCredentials
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

val gson = Gson()

class ApplicationTest {

    val password = "password"


    private val debugUsers = mapOf<String, User>(
        "client" to Client(11, "client", password, "address", "phone"),
        "client2" to Client(12, "client2", password, "address2", "phone2"),
        "manager" to Manager(21, "manager", password, "restaurant"),
        "operator" to Operator(31, "operator", password, 1),
        "courier" to Courier(41, "courier", password)
    )

    private val userTokens = mutableMapOf<User, Token>()

    @Before
    fun beforeTests() {
        mockkObject(Hasher)
        every { Hasher.hash(password) } returns password
        mockkObject(UserModelManager)
        for ((login, user) in debugUsers) {
            coEvery { UserModelManager.get(login, password) } returns user
            coEvery { UserModelManager.get(login) } returns user
        }
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


    @Test
    fun testLoginSuccessWithTracker() = testApp {
        val hash = Hasher.hash(password)
        val user = User.manager.get("client", hash)!!

        val response = handleRequestWithUser(HttpMethod.Get, "/pizza", user).getJson<List<Pizza>>()


        println("$response")
        println("$user $hash")
    }


    private fun testApp(callback: suspend TestApplicationEngine.() -> Unit) {
        withTestApplication({ module() }) { runBlocking { callback() } }
    }

    @After
    fun afterTests() {
        unmockkAll()
    }

}

inline fun <reified T> TestApplicationRequest.setJsonBody(value: T) {
    val data = gson.toJson(value)
    setBody(data.toByteArray())
    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
}


inline fun <reified T> TestApplicationCall.getJson(): T {
    println("$this")
    val value = assertNotNull(response.content, "response content is null")
    return gson.fromJson<T>(value, T::class.java)
}

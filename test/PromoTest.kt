package ru.spbstu.architectures

import io.restassured.RestAssured.given
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import ru.spbstu.architectures.pizzaService.db.manager.listPromo
import ru.spbstu.architectures.pizzaService.models.*
import ru.spbstu.architectures.pizzaService.web.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PromoTest : ApplicationTest() {

    @Test
    fun testPromoSimpleWorkflow() {
        val clientIds = listOf(client, client2).map { it.id }
        var promo = createPromo(manager, clientIds)
        promo = runPromo(manager, promo.id)
        val dbPromo = runBlocking { Promo.modelManager.get(promo.id) }
        assertNotNull(dbPromo, "Db promo")
        val dbPromoClient = runBlocking { PromoClient.modelManager.listPromo(dbPromo) }
        assertEquals(2, dbPromoClient.size, "Promo clients count")
        assertTrue(dbPromoClient.all { it.operatorId != null }, "Promo client operators: $dbPromoClient")
        promo = finishPromo(manager, promo.id)
        promo = closePromo(manager, promo.id)
    }


    private fun createPromo(user: User, clientIds: List<Int>, statusCode: Int = 200): PromoWithPermission {
        val promoCreationForm = PromoCreationForm(clientIds, PromoEffect.DISCOUNT_10.name, "promo description")
        return given().withUser(user)
            .jsonBody(promoCreationForm)
            .When().post("/promo")
            .then().statusCode(statusCode)
            .extract().to<PromoWithPermission>()
            .also { assertNotNull(it.id, "Promo id") }
    }

    private fun runPromo(user: User, promoId: Int, statusCode: Int = 200): PromoWithPermission {
        val promoModification = PromoModificationForm(PromoStatus.ACTIVE.name, null)
        return given().withUser(user)
            .jsonBody(promoModification)
            .When().put("/promo/$promoId")
            .then().statusCode(statusCode)
            .extract().to<PromoWithPermission>()
            .also { assertNotNull(it.id, "Promo id") }
            .also { assertEquals(PromoStatus.ACTIVE.name, it.status, "Promo status") }
    }

    private fun finishPromo(user: User, promoId: Int, statusCode: Int = 200): PromoWithPermission {
        val promoModification = PromoModificationForm(PromoStatus.FINISHED.name, null)
        return given().withUser(user)
            .jsonBody(promoModification)
            .When().put("/promo/$promoId")
            .then().statusCode(statusCode)
            .extract().to<PromoWithPermission>()
            .also { assertNotNull(it.id, "Promo id") }
            .also { assertEquals(PromoStatus.FINISHED.name, it.status, "Promo status") }
    }

    private fun closePromo(user: User, promoId: Int, statusCode: Int = 200): PromoWithPermission {
        val promoModification = PromoModificationForm(PromoStatus.CLOSED.name, "some results")
        return given().withUser(user)
            .jsonBody(promoModification)
            .When().put("/promo/$promoId")
            .then().statusCode(statusCode)
            .extract().to<PromoWithPermission>()
            .also { assertNotNull(it.id, "Promo id") }
            .also { assertEquals(PromoStatus.CLOSED.name, it.status, "Promo status") }
    }

}

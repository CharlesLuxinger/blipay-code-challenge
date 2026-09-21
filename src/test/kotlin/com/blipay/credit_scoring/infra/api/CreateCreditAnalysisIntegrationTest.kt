package com.blipay.credit_scoring.infra.api

import io.restassured.http.ContentType
import io.restassured.RestAssured.given
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class CreateCreditAnalysisIntegrationTest : CreditAnalysisIntegrationSupport() {
    @Test
    fun createsUserAndFirstScore() {
        given()
            .contentType(ContentType.JSON)
            .body(validCreateBody())
            .post("/credit-analyses")
            .then()
            .statusCode(201)
        assertEquals(1, users.count())
        assertEquals(1, scores.count())
        assertEquals("12345678909", users.findAll().single().documentNumber)
    }

    @Test
    fun duplicateDocumentReturns409() {
        create()
        given()
            .contentType(ContentType.JSON)
            .body(validCreateBody(document = "12345678909"))
            .post("/credit-analyses")
            .then()
            .statusCode(409)
        assertEquals(1, users.count())
    }

    @Test
    fun invalidCreateDoesNotPersist() {
        given()
            .contentType(ContentType.JSON)
            .body(validCreateBody(age = 17, monthlyIncome = "-1"))
            .post("/credit-analyses")
            .then()
            .statusCode(400)
        assertEquals(0, users.count())
        assertEquals(0, scores.count())
    }

    @Test
    fun exhaustedWeatherFailureReturns502() {
        weatherStub.status = 500
        val response =
            given()
                .contentType(ContentType.JSON)
                .body(validCreateBody())
                .post("/credit-analyses")
        response.then().statusCode(502).body("detail", containsString("weather"))
        assertFalse(response.asString().contains("test-key"))
        assertFalse(response.asString().contains("provider failure"))
        assertEquals(4, weatherStub.calls)
        assertEquals(0, users.count())
        assertEquals(0, scores.count())
    }

    private fun create() {
        given()
            .contentType(ContentType.JSON)
            .body(validCreateBody())
            .post("/credit-analyses")
            .then()
            .statusCode(201)
    }
}

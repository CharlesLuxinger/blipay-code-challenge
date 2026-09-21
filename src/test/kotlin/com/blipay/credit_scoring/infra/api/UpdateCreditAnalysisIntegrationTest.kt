package com.blipay.credit_scoring.infra.api

import com.blipay.credit_scoring.infra.error.GlobalExceptionHandler
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class UpdateCreditAnalysisIntegrationTest : CreditAnalysisIntegrationSupport() {
    @Test
    fun validPutAppendsScore() {
        create()
        given()
            .contentType(ContentType.JSON)
            .body(validUpdateBody())
            .put("/credit-analyses/12345678909")
            .then()
            .statusCode(201)
        assertEquals(2, scores.count())
    }

    @Test
    fun putPreservesHistoryAndUpdatesUser() {
        create()
        val firstScore = scores.findAll().single().score
        given()
            .contentType(ContentType.JSON)
            .body(validUpdateBody(name = "Ana", city = "Olinda"))
            .put("/credit-analyses/12345678909")
            .then()
            .statusCode(201)

        val user = users.findAll().single()
        assertEquals("Ana", user.name)
        assertEquals("Olinda", user.city)
        assertEquals(firstScore, scores.findAll().first { it.id != scores.findAll().maxOf { score -> score.id } }.score)
        assertEquals(2, scores.count())
    }

    @Test
    fun unknownDocumentReturns404() {
        given()
            .contentType(ContentType.JSON)
            .body(validUpdateBody())
            .put("/credit-analyses/99999999999")
            .then()
            .statusCode(404)
    }

    @Test
    fun failedPutDoesNotMutate() {
        create()
        val before = users.findAll().single().name
        weatherStub.status = 500
        given()
            .contentType(ContentType.JSON)
            .body(validUpdateBody(name = "Should Not Persist"))
            .put("/credit-analyses/12345678909")
            .then()
            .statusCode(502)
        assertEquals(before, users.findAll().single().name)
        assertEquals(1, scores.count())
    }

    @Test
    fun optimisticLockConflictReturns409() {
        assertEquals(409, GlobalExceptionHandler().conflict().status)
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

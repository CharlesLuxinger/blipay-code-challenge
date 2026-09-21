package com.blipay.credit_scoring.infra.api

import com.blipay.credit_scoring.infra.error.GlobalExceptionHandler
import com.blipay.credit_scoring.domain.credit.entity.ScoreRecord
import com.blipay.credit_scoring.domain.credit.entity.StoredAnalysis
import com.blipay.credit_scoring.domain.credit.exception.VersionConflictException
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import java.math.BigDecimal
import java.time.Instant

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
        create()
        val stale =
            persistence.findByDocument(
                com.blipay.credit_scoring.domain.credit.entity.DocumentNumber
                    .of("12345678909"),
            )!!
        jdbcTemplate.update("UPDATE users SET version = version + 1 WHERE document_number = '12345678909'")
        val score =
            ScoreRecord(
                0,
                stale.documentNumber,
                stale.name,
                stale.age,
                stale.monthlyIncome,
                stale.city,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0,
                false,
                Instant.now(),
            )
        assertFailsWith<VersionConflictException> {
            persistence.update(StoredAnalysis(stale, score))
        }
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

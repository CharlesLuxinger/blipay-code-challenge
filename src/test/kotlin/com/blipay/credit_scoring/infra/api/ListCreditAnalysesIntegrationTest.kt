package com.blipay.credit_scoring.infra.api

import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ListCreditAnalysesIntegrationTest : CreditAnalysisIntegrationSupport() {
    @Test
    fun returnsDefaultPage() {
        create()
        given()
            .get("/credit-analyses/12345678909")
            .then()
            .statusCode(200)
            .body("page", equalTo(0))
            .body("size", equalTo(20))
            .body("items[0].id", org.hamcrest.Matchers.notNullValue())
            .body("items[0].score", org.hamcrest.Matchers.equalTo(201))
    }

    @Test
    fun ordersByCreatedAtAndIdDescending() {
        create()
        given()
            .contentType(ContentType.JSON)
            .body(validUpdateBody())
            .put("/credit-analyses/12345678909")
            .then()
            .statusCode(201)
        jdbcTemplate.update("UPDATE scores SET created_at = TIMESTAMPTZ '2026-09-20T12:00:00Z'")
        val ids =
            given()
                .get("/credit-analyses/12345678909?size=100")
                .then()
                .extract()
                .jsonPath()
                .getList<Long>("items.id")
        assertEquals(ids.sortedDescending(), ids)
    }

    @Test
    fun rejectsPageAndSizeBounds() {
        create()
        given().get("/credit-analyses/12345678909?page=-1").then().statusCode(400)
        given().get("/credit-analyses/12345678909?size=101").then().statusCode(400)
    }

    @Test
    fun noAnalysesReturns404() {
        given().get("/credit-analyses/12345678909").then().statusCode(404)
    }

    @Test
    fun normalizesDocumentPath() {
        create()
        given().get("/credit-analyses/123.456.789-09").then().statusCode(200)
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

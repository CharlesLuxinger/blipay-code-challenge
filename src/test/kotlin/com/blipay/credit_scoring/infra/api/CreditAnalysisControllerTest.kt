package com.blipay.credit_scoring.infra.api

import io.restassured.http.ContentType
import io.restassured.RestAssured.given
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test

class CreditAnalysisControllerTest : CreditAnalysisIntegrationSupport() {
    @Test
    fun createReturns201SuccessBody() {
        given()
            .contentType(ContentType.JSON)
            .body(validCreateBody())
            .post("/credit-analyses")
            .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("score", equalTo(201))
            .body("approved", equalTo(true))
            .body("createdAt", notNullValue())
            .body("size()", equalTo(3))
    }
}

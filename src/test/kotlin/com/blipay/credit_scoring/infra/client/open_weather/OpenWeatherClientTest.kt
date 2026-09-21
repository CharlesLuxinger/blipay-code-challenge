package com.blipay.credit_scoring.infra.client.open_weather

import com.blipay.credit_scoring.domain.credit.exception.UnknownCityException
import feign.Request
import feign.RequestTemplate
import feign.Response
import feign.RetryableException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class OpenWeatherClientTest {
    private val configuration = OpenWeatherFeignConfiguration()

    @Test
    fun retriesTransientFailuresWithConfiguredBackoff() {
        val retryer = configuration.retryer().clone()
        val request = Request.create(Request.HttpMethod.GET, "/weather", emptyMap(), null, RequestTemplate())
        val failure =
            RetryableException(
                503,
                "server",
                Request.HttpMethod.GET,
                null as Throwable?,
                System.currentTimeMillis(),
                request,
            )
        retryer.continueOrPropagate(failure)
        retryer.continueOrPropagate(failure)
        retryer.continueOrPropagate(failure)
        assertFailsWith<RetryableException> { retryer.continueOrPropagate(failure) }
        val retryerType = retryer.javaClass
        assertEquals(100L, retryerType.getDeclaredField("period").valueOf(retryer))
        assertEquals(400L, retryerType.getDeclaredField("maxPeriod").valueOf(retryer))
        assertEquals(4, retryerType.getDeclaredField("maxAttempts").valueOf(retryer))
    }

    @Test
    fun doesNotRetryClientErrors() {
        val request = Request.create(Request.HttpMethod.GET, "/weather", emptyMap(), null, RequestTemplate())
        val response =
            Response
                .builder()
                .status(404)
                .reason("not found")
                .request(request)
                .build()
        val decoded = configuration.errorDecoder().decode("OpenWeatherClient#weather", response)
        assertIs<UnknownCityException>(decoded)
    }

    @Test
    fun serverErrorsAreRetryableAndTimeoutsAreTwoSeconds() {
        val request = Request.create(Request.HttpMethod.GET, "/weather", emptyMap(), null, RequestTemplate())
        val response =
            Response
                .builder()
                .status(503)
                .reason("server error")
                .request(request)
                .build()
        assertIs<RetryableException>(configuration.errorDecoder().decode("OpenWeatherClient#weather", response))
        val options = OpenWeatherRequestConfiguration().requestOptions()
        kotlin.test.assertEquals(2000, options.connectTimeoutMillis())
        kotlin.test.assertEquals(2000, options.readTimeoutMillis())
    }

    private inline fun <reified T> java.lang.reflect.Field.valueOf(target: Any): T {
        isAccessible = true
        return get(target) as T
    }
}

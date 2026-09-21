package com.blipay.credit_scoring.infra.client.open_weather

import com.blipay.credit_scoring.domain.credit.exception.UnknownCityException
import feign.RetryableException
import feign.Retryer
import feign.Retryer.Default
import feign.codec.ErrorDecoder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenWeatherFeignConfiguration {
    @Bean
    fun retryer(): Retryer = Default(INITIAL_BACKOFF_MS, MAX_BACKOFF_MS, MAX_ATTEMPTS)

    @Bean
    fun errorDecoder(): ErrorDecoder =
        ErrorDecoder { _, response ->
            if (response.status() in SERVER_ERROR_MIN..SERVER_ERROR_MAX) {
                RetryableException(
                    response.status(),
                    "OpenWeather server failure",
                    response.request().httpMethod(),
                    null as Throwable?,
                    System.currentTimeMillis(),
                    response.request(),
                )
            } else {
                UnknownCityException()
            }
        }

    companion object {
        private const val INITIAL_BACKOFF_MS = 100L
        private const val MAX_BACKOFF_MS = 400L
        private const val MAX_ATTEMPTS = 4
        private const val SERVER_ERROR_MIN = 500
        private const val SERVER_ERROR_MAX = 599
    }
}

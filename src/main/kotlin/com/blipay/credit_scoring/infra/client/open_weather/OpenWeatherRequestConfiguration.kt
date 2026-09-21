package com.blipay.credit_scoring.infra.client.open_weather

import feign.Request.Options
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class OpenWeatherRequestConfiguration {
    @Bean
    fun requestOptions(): Options =
        Options(Duration.ofSeconds(TIMEOUT_SECONDS), Duration.ofSeconds(TIMEOUT_SECONDS), FOLLOW_REDIRECTS)

    companion object {
        private const val TIMEOUT_SECONDS = 2L
        private const val FOLLOW_REDIRECTS = true
    }
}

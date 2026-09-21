package com.blipay.credit_scoring.infra.configuration

import java.time.Clock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationConfiguration {
    @Bean
    fun clock(): Clock = Clock.systemUTC()
}

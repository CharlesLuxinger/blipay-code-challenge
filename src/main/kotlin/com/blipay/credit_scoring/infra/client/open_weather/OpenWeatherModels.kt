package com.blipay.credit_scoring.infra.client.open_weather

import java.math.BigDecimal

data class WeatherMain(
    val temp: BigDecimal,
)

data class WeatherResponse(
    val main: WeatherMain,
)

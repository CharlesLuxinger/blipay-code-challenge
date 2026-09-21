package com.blipay.credit_scoring.domain.credit.port

import com.blipay.credit_scoring.domain.credit.entity.City
import java.math.BigDecimal

fun interface WeatherPort {
    fun temperatureFor(city: City): BigDecimal
}

package com.blipay.credit_scoring.infra.client.open_weather

import com.blipay.credit_scoring.domain.credit.entity.City
import com.blipay.credit_scoring.domain.credit.exception.UnknownCityException
import com.blipay.credit_scoring.domain.credit.exception.WeatherUnavailableException
import com.blipay.credit_scoring.domain.credit.port.WeatherPort
import feign.FeignException
import feign.RetryableException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class OpenWeatherAdapter(
    private val client: OpenWeatherFeignClient,
    @Value("\${openweather.api.key}") private val apiKey: String,
) : WeatherPort {
    override fun temperatureFor(city: City): BigDecimal =
        try {
            client.weather(city.value, apiKey).main.temp
        } catch (exception: UnknownCityException) {
            throw exception
        } catch (_: RetryableException) {
            throw WeatherUnavailableException()
        } catch (_: FeignException) {
            throw WeatherUnavailableException()
        }
}

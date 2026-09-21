package com.blipay.credit_scoring.infra.client.open_weather

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "openWeatherClient",
    url = "\${openweather.api.base-url}",
    configuration = [OpenWeatherFeignConfiguration::class],
)
interface OpenWeatherFeignClient {
    @GetMapping("/weather")
    fun weather(
        @RequestParam("q") city: String,
        @RequestParam("appid") apiKey: String,
        @RequestParam("units") units: String = "metric",
    ): WeatherResponse
}

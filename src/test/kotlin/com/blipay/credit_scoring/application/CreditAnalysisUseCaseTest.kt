package com.blipay.credit_scoring.application

import com.blipay.credit_scoring.domain.credit.entity.CreditAnalysisInput
import com.blipay.credit_scoring.domain.credit.entity.City
import com.blipay.credit_scoring.domain.credit.port.CustomerStore
import com.blipay.credit_scoring.domain.credit.port.WeatherPort
import com.blipay.credit_scoring.application.credit.CreateCreditAnalysisUseCase
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CreditAnalysisUseCaseTest {
    private val clock = Clock.fixed(Instant.parse("2026-09-20T12:00:00Z"), ZoneOffset.UTC)
    private val input = CreditAnalysisInput.of("Maria", 30, BigDecimal("1800"), "Recife", "12345678909")

    @Test
    fun createCalculatesAndPersistsUsingMockitoDoubles() {
        val store = mock<CustomerStore>()
        val weather = mock<WeatherPort>()
        whenever(store.findByDocument(input.documentNumber)).thenReturn(null)
        whenever(weather.temperatureFor(City.of("Recife"))).thenReturn(BigDecimal("30"))
        val useCase = CreateCreditAnalysisUseCase(store, weather, clock)

        val result = useCase.execute(input)

        assertEquals(201, result.score)
        verify(store).create(org.mockito.kotlin.any())
    }

    @Test
    fun weatherFailureDoesNotPersist() {
        val store = mock<CustomerStore>()
        val weather = mock<WeatherPort>()
        whenever(store.findByDocument(input.documentNumber)).thenReturn(null)
        whenever(weather.temperatureFor(City.of("Recife"))).thenThrow(RuntimeException("failure"))
        val useCase = CreateCreditAnalysisUseCase(store, weather, clock)

        assertFailsWith<RuntimeException> { useCase.execute(input) }
        verify(store, never()).create(org.mockito.kotlin.any())
    }
}

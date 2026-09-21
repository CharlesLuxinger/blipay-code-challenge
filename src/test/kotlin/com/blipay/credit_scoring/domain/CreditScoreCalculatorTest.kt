package com.blipay.credit_scoring.domain

import com.blipay.credit_scoring.domain.credit.entity.CreditAnalysisInput
import java.math.BigDecimal
import com.blipay.credit_scoring.domain.credit.service.CreditScoreCalculator
import kotlin.test.Test
import kotlin.test.assertEquals

class CreditScoreCalculatorTest {
    @Test
    fun exampleProducesScore201() {
        val input = CreditAnalysisInput.of("Maria", 30, BigDecimal("1800"), "Recife", "12345678909")
        val result = CreditScoreCalculator.calculate(input.age, input.monthlyIncome, BigDecimal("30"))
        assertEquals(0, result.ageComponent.compareTo(BigDecimal("15")))
        assertEquals(0, result.incomeComponent.compareTo(BigDecimal("36")))
        assertEquals(0, result.temperatureComponent.compareTo(BigDecimal("150")))
        assertEquals(201, result.score)
    }

    @Test
    fun roundsHalfUp() {
        val input = CreditAnalysisInput.of("Maria", 19, BigDecimal.ZERO, "Recife", "12345678909")
        val result = CreditScoreCalculator.calculate(input.age, input.monthlyIncome, BigDecimal.ZERO)
        assertEquals(10, result.score)
        val exactHalfInput = CreditAnalysisInput.of("Maria", 18, BigDecimal.ZERO, "Recife", "12345678909")
        val exactHalf =
            CreditScoreCalculator.calculate(
                exactHalfInput.age,
                exactHalfInput.monthlyIncome,
                BigDecimal("0.1"),
            )
        assertEquals(10, exactHalf.score)
    }

    @Test
    fun approvalBoundary() {
        val input = CreditAnalysisInput.of("Maria", 18, BigDecimal.ZERO, "Recife", "12345678909")
        assertEquals(false, CreditScoreCalculator.calculate(input.age, input.monthlyIncome, BigDecimal("38")).approved)
        assertEquals(true, CreditScoreCalculator.calculate(input.age, input.monthlyIncome, BigDecimal("38.2")).approved)
    }
}

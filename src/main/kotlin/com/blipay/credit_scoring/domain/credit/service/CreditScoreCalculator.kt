package com.blipay.credit_scoring.domain.credit.service

import com.blipay.credit_scoring.domain.credit.entity.Age
import com.blipay.credit_scoring.domain.credit.entity.MonthlyIncome
import com.blipay.credit_scoring.domain.credit.entity.ScoreCalculation
import java.math.BigDecimal
import java.math.RoundingMode

object CreditScoreCalculator {
    fun calculate(
        age: Age,
        monthlyIncome: MonthlyIncome,
        temperatureCelsius: BigDecimal,
    ): ScoreCalculation {
        val ageComponent = BigDecimal.valueOf(age.value.toLong()).multiply(BigDecimal(AGE_FACTOR))
        val incomeComponent = monthlyIncome.value.divide(BigDecimal(INCOME_DIVISOR))
        val temperatureComponent = temperatureCelsius.multiply(BigDecimal(TEMPERATURE_FACTOR))
        val total = ageComponent + incomeComponent + temperatureComponent
        val score = total.setScale(0, RoundingMode.HALF_UP).intValueExact()
        return ScoreCalculation(
            ageComponent,
            incomeComponent,
            temperatureComponent,
            score,
            score >= APPROVAL_THRESHOLD,
        )
    }

    private const val AGE_FACTOR = 0.5
    private const val INCOME_DIVISOR = 50
    private const val TEMPERATURE_FACTOR = 5
    private const val APPROVAL_THRESHOLD = 200
}

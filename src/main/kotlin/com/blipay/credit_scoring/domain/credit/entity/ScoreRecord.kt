package com.blipay.credit_scoring.domain.credit.entity

import java.math.BigDecimal
import java.time.Instant

class ScoreRecord(
    val id: Long,
    val documentNumber: DocumentNumber,
    val name: Name,
    val age: Age,
    val monthlyIncome: MonthlyIncome,
    val city: City,
    val temperatureCelsius: BigDecimal,
    val ageComponent: BigDecimal,
    val incomeComponent: BigDecimal,
    val temperatureComponent: BigDecimal,
    val score: Int,
    val approved: Boolean,
    val createdAt: Instant,
)

package com.blipay.credit_scoring.domain.credit.entity

import java.math.BigDecimal

class ScoreCalculation(
    val ageComponent: BigDecimal,
    val incomeComponent: BigDecimal,
    val temperatureComponent: BigDecimal,
    val score: Int,
    val approved: Boolean,
)

package com.blipay.credit_scoring.domain.credit.entity

import java.math.BigDecimal

@JvmInline
value class MonthlyIncome private constructor(
    val value: BigDecimal,
) {
    companion object {
        fun of(value: BigDecimal): MonthlyIncome {
            require(
                value.scale() <= 2 && value >= BigDecimal.ZERO && value <= MAXIMUM,
            ) { "monthlyIncome must be between 0 and 999999999999.99 with at most 2 decimals" }
            return MonthlyIncome(value)
        }

        private val MAXIMUM = BigDecimal("999999999999.99")
    }
}

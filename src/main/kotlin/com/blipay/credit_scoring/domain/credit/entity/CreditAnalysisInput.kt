package com.blipay.credit_scoring.domain.credit.entity

import java.math.BigDecimal

class CreditAnalysisInput private constructor(
    val name: Name,
    val age: Age,
    val monthlyIncome: MonthlyIncome,
    val city: City,
    val documentNumber: DocumentNumber,
) {
    companion object {
        fun of(
            name: String,
            age: Int,
            monthlyIncome: BigDecimal,
            city: String,
            documentNumber: String,
        ) = CreditAnalysisInput(
            Name.of(name),
            Age.of(age),
            MonthlyIncome.of(monthlyIncome),
            City.of(city),
            DocumentNumber.of(documentNumber),
        )
    }
}

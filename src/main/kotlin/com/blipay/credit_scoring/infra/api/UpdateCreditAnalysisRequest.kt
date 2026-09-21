package com.blipay.credit_scoring.infra.api

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import com.blipay.credit_scoring.domain.credit.entity.CreditAnalysisInput
import com.blipay.credit_scoring.domain.credit.entity.DocumentNumber

data class UpdateCreditAnalysisRequest(
    @field:NotBlank(message = "name is required")
    @field:Size(max = 150, message = "name must contain at most 150 characters")
    val name: String,
    @field:Min(value = 18, message = "age must be at least 18")
    val age: Int,
    @field:DecimalMin(value = "0", message = "monthlyIncome must be at least 0")
    @field:DecimalMax(value = "999999999999.99", message = "monthlyIncome is too high")
    @field:Digits(integer = 12, fraction = 2, message = "monthlyIncome must have at most 2 decimals")
    val monthlyIncome: BigDecimal,
    @field:NotBlank(message = "city is required")
    @field:Size(max = 50, message = "city must contain at most 50 characters")
    val city: String,
) {
    fun toInput(documentNumber: DocumentNumber) =
        CreditAnalysisInput.of(name, age, monthlyIncome, city, documentNumber.value)
}

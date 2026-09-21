package com.blipay.credit_scoring.domain

import com.blipay.credit_scoring.domain.credit.entity.CreditAnalysisInput
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DomainValidationTest {
    @Test
    fun namesAreTrimmedAndNameLimitIs150() {
        val input = validInput(name = "  Maria  ", city = "  Recife  ")
        assertEquals("Maria", input.name.value)
        assertEquals("Recife", input.city.value)
        assertEquals(150, validInput(name = "x".repeat(150)).name.value.length)
        assertFailsWith<IllegalArgumentException> {
            validInput(name = "x".repeat(151))
        }
        assertFailsWith<IllegalArgumentException> { validInput(name = " ") }
        assertFailsWith<IllegalArgumentException> { validInput(city = " ") }
        assertFailsWith<IllegalArgumentException> { validInput(city = "x".repeat(51)) }
    }

    @Test
    fun ageBoundary() {
        assertEquals(18, validInput(age = 18).age.value)
        assertFailsWith<IllegalArgumentException> { validInput(age = 17) }
    }

    @Test
    fun monthlyIncomeBoundsAndScale() {
        assertEquals(BigDecimal.ZERO, validInput(monthlyIncome = BigDecimal.ZERO).monthlyIncome.value)
        assertEquals(
            BigDecimal("999999999999.99"),
            validInput(monthlyIncome = BigDecimal("999999999999.99")).monthlyIncome.value,
        )
        assertFailsWith<IllegalArgumentException> { validInput(monthlyIncome = BigDecimal("-0.01")) }
        assertFailsWith<IllegalArgumentException> { validInput(monthlyIncome = BigDecimal("1000000000000")) }
        assertFailsWith<IllegalArgumentException> { validInput(monthlyIncome = BigDecimal("1.001")) }
    }

    @Test
    fun documentNumberNormalization() {
        assertEquals("12345678909", validInput(documentNumber = "123.456.789-09").documentNumber.value)
        assertEquals("11111111111", validInput(documentNumber = "11111111111").documentNumber.value)
        assertFailsWith<IllegalArgumentException> { validInput(documentNumber = "1234567890") }
        assertFailsWith<IllegalArgumentException> { validInput(documentNumber = "123456789012") }
    }

    private fun validInput(
        name: String = "Maria",
        age: Int = 30,
        monthlyIncome: BigDecimal = BigDecimal("1800"),
        city: String = "Recife",
        documentNumber: String = "12345678909",
    ) = CreditAnalysisInput.of(name, age, monthlyIncome, city, documentNumber)
}

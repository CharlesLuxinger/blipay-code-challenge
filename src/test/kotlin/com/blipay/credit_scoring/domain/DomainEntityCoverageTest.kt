package com.blipay.credit_scoring.domain

import com.blipay.credit_scoring.domain.credit.entity.Age
import com.blipay.credit_scoring.domain.credit.entity.City
import com.blipay.credit_scoring.domain.credit.entity.CustomerRecord
import com.blipay.credit_scoring.domain.credit.entity.DocumentNumber
import com.blipay.credit_scoring.domain.credit.entity.MonthlyIncome
import com.blipay.credit_scoring.domain.credit.entity.Name
import com.blipay.credit_scoring.domain.credit.entity.ScoreCalculation
import com.blipay.credit_scoring.domain.credit.entity.ScorePage
import com.blipay.credit_scoring.domain.credit.entity.ScoreRecord
import com.blipay.credit_scoring.domain.credit.entity.StoredAnalysis
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class DomainEntityCoverageTest {
    @Test
    fun recordsExposeImmutableDomainState() {
        val document = DocumentNumber.of("12345678909")
        val name = Name.of("Maria")
        val age = Age.of(30)
        val income = MonthlyIncome.of(BigDecimal("1800"))
        val city = City.of("Recife")
        val timestamp = Instant.parse("2026-09-20T12:00:00Z")
        val customer = CustomerRecord(UUID.randomUUID(), document, name, age, income, city, 0, timestamp, timestamp)
        val score =
            ScoreRecord(
                1,
                document,
                name,
                age,
                income,
                city,
                BigDecimal("30"),
                BigDecimal("15"),
                BigDecimal("36"),
                BigDecimal("150"),
                201,
                true,
                timestamp,
            )
        val calculation = ScoreCalculation(BigDecimal("15"), BigDecimal("36"), BigDecimal("150"), 201, true)
        val stored = StoredAnalysis(customer, score)
        val page = ScorePage(0, 20, 1, 1, listOf(score))

        assertEquals(customer.name, customer.updated(name, age, income, city, timestamp).name)
        assertEquals(201, page.items.single().score)
        assertEquals("Maria", stored.customer.name.value)
        assertEquals(customer.id, customer.id)
        assertEquals(document, customer.documentNumber)
        assertEquals(age, customer.age)
        assertEquals(income, customer.monthlyIncome)
        assertEquals(city, customer.city)
        assertEquals(0, customer.version)
        assertEquals(timestamp, customer.createdAt)
        assertEquals(timestamp, customer.updatedAt)
        assertEquals(score.id, score.id)
        assertEquals(document, score.documentNumber)
        assertEquals(name, score.name)
        assertEquals(age, score.age)
        assertEquals(income, score.monthlyIncome)
        assertEquals(city, score.city)
        assertEquals(BigDecimal("30"), score.temperatureCelsius)
        assertEquals(BigDecimal("15"), score.ageComponent)
        assertEquals(BigDecimal("36"), score.incomeComponent)
        assertEquals(BigDecimal("150"), score.temperatureComponent)
        assertEquals(true, score.approved)
        assertEquals(timestamp, score.createdAt)
        assertEquals(stored.customer, stored.customer)
        assertEquals(stored.score, stored.score)
        assertEquals(page.page, page.page)
        assertEquals(20, page.size)
        assertEquals(1L, page.totalElements)
        assertEquals(1, page.totalPages)
        assertEquals(page.items, page.items)
    }
}

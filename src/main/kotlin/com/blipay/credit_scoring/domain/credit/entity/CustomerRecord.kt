package com.blipay.credit_scoring.domain.credit.entity

import java.time.Instant
import java.util.UUID

class CustomerRecord(
    val id: UUID,
    val documentNumber: DocumentNumber,
    val name: Name,
    val age: Age,
    val monthlyIncome: MonthlyIncome,
    val city: City,
    val version: Long,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    fun updated(
        name: Name,
        age: Age,
        monthlyIncome: MonthlyIncome,
        city: City,
        updatedAt: Instant,
    ) = CustomerRecord(id, documentNumber, name, age, monthlyIncome, city, version, createdAt, updatedAt)
}

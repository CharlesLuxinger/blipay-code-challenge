package com.blipay.credit_scoring.infra.persistence.entity

import com.blipay.credit_scoring.domain.credit.entity.Age
import com.blipay.credit_scoring.domain.credit.entity.City
import com.blipay.credit_scoring.domain.credit.entity.CustomerRecord
import com.blipay.credit_scoring.domain.credit.entity.DocumentNumber
import com.blipay.credit_scoring.domain.credit.entity.MonthlyIncome
import com.blipay.credit_scoring.domain.credit.entity.Name
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
class UserEntity(
    @Id val id: UUID,
    @Column(name = "document_number", nullable = false, unique = true, length = 11)
    val documentNumber: String,
    @Column(nullable = false, length = 150)
    var name: String,
    @Column(nullable = false)
    var age: Int,
    @Column(name = "monthly_income", nullable = false, precision = 14, scale = 2)
    var monthlyIncome: BigDecimal,
    @Column(nullable = false, length = 50)
    var city: String,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
    @Version @Column(nullable = false)
    var version: Long = 0,
) {
    fun toRecord() =
        CustomerRecord(
            id,
            DocumentNumber.of(documentNumber),
            Name.of(name),
            Age.of(age),
            MonthlyIncome.of(monthlyIncome),
            City.of(city),
            version,
            createdAt,
            updatedAt,
        )

    companion object {
        fun from(record: CustomerRecord) =
            UserEntity(
                record.id,
                record.documentNumber.value,
                record.name.value,
                record.age.value,
                record.monthlyIncome.value,
                record.city.value,
                record.createdAt,
                record.updatedAt,
                record.version,
            )
    }
}

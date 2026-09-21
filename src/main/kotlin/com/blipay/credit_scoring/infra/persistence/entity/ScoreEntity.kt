package com.blipay.credit_scoring.infra.persistence.entity

import com.blipay.credit_scoring.domain.credit.entity.Age
import com.blipay.credit_scoring.domain.credit.entity.City
import com.blipay.credit_scoring.domain.credit.entity.DocumentNumber
import com.blipay.credit_scoring.domain.credit.entity.MonthlyIncome
import com.blipay.credit_scoring.domain.credit.entity.Name
import com.blipay.credit_scoring.domain.credit.entity.ScoreRecord
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "scores")
class ScoreEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,
    @Column(name = "document_number", nullable = false, length = 11)
    val documentNumber: String,
    @Column(nullable = false, length = 150)
    val name: String,
    @Column(nullable = false)
    val age: Int,
    @Column(name = "monthly_income", nullable = false, precision = 14, scale = 2)
    val monthlyIncome: BigDecimal,
    @Column(nullable = false, length = 50)
    val city: String,
    @Column(name = "temperature_celsius", nullable = false, precision = 10, scale = 2)
    val temperatureCelsius: BigDecimal,
    @Column(name = "age_component", nullable = false, precision = 14, scale = 4)
    val ageComponent: BigDecimal,
    @Column(name = "income_component", nullable = false, precision = 14, scale = 4)
    val incomeComponent: BigDecimal,
    @Column(name = "temperature_component", nullable = false, precision = 14, scale = 4)
    val temperatureComponent: BigDecimal,
    @Column(nullable = false)
    val score: Int,
    @Column(nullable = false)
    val approved: Boolean,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
) {
    fun toRecord() =
        ScoreRecord(
            id,
            DocumentNumber.of(documentNumber),
            Name.of(name),
            Age.of(age),
            MonthlyIncome.of(monthlyIncome),
            City.of(city),
            temperatureCelsius,
            ageComponent,
            incomeComponent,
            temperatureComponent,
            score,
            approved,
            createdAt,
        )

    companion object {
        fun from(
            record: ScoreRecord,
            user: UserEntity,
        ) = ScoreEntity(
            user = user,
            documentNumber = record.documentNumber.value,
            name = record.name.value,
            age = record.age.value,
            monthlyIncome = record.monthlyIncome.value,
            city = record.city.value,
            temperatureCelsius = record.temperatureCelsius,
            ageComponent = record.ageComponent,
            incomeComponent = record.incomeComponent,
            temperatureComponent = record.temperatureComponent,
            score = record.score,
            approved = record.approved,
            createdAt = record.createdAt,
        )
    }
}

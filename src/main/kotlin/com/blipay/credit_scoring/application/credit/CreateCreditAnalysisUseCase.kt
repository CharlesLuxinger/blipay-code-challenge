package com.blipay.credit_scoring.application.credit

import com.blipay.credit_scoring.domain.credit.entity.CreditAnalysisInput
import com.blipay.credit_scoring.domain.credit.entity.CustomerRecord
import com.blipay.credit_scoring.domain.credit.entity.ScoreRecord
import com.blipay.credit_scoring.domain.credit.entity.StoredAnalysis
import com.blipay.credit_scoring.domain.credit.exception.DuplicateDocumentException
import com.blipay.credit_scoring.domain.credit.port.CustomerStore
import com.blipay.credit_scoring.domain.credit.port.WeatherPort
import com.blipay.credit_scoring.domain.credit.service.CreditScoreCalculator
import java.time.Clock
import java.time.Instant
import java.util.UUID
import org.springframework.stereotype.Service

@Service
class CreateCreditAnalysisUseCase(
    private val customerStore: CustomerStore,
    private val weatherPort: WeatherPort,
    private val clock: Clock,
) {
    fun execute(input: CreditAnalysisInput): AnalysisResult {
        if (customerStore.findByDocument(input.documentNumber) != null) throw DuplicateDocumentException()
        val temperature = weatherPort.temperatureFor(input.city)
        val calculation = CreditScoreCalculator.calculate(input.age, input.monthlyIncome, temperature)
        val timestamp = Instant.now(clock)
        customerStore.create(
            StoredAnalysis(
                CustomerRecord(
                    UUID.randomUUID(),
                    input.documentNumber,
                    input.name,
                    input.age,
                    input.monthlyIncome,
                    input.city,
                    0,
                    timestamp,
                    timestamp,
                ),
                ScoreRecord(
                    0,
                    input.documentNumber,
                    input.name,
                    input.age,
                    input.monthlyIncome,
                    input.city,
                    temperature,
                    calculation.ageComponent,
                    calculation.incomeComponent,
                    calculation.temperatureComponent,
                    calculation.score,
                    calculation.approved,
                    timestamp,
                ),
            ),
        )
        return AnalysisResult(calculation.score, calculation.approved, timestamp)
    }
}

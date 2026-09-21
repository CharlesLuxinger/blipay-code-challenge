package com.blipay.credit_scoring.infra.persistence

import com.blipay.credit_scoring.domain.credit.entity.Age
import com.blipay.credit_scoring.domain.credit.entity.City
import com.blipay.credit_scoring.domain.credit.entity.CustomerRecord
import com.blipay.credit_scoring.domain.credit.entity.DocumentNumber
import com.blipay.credit_scoring.domain.credit.entity.MonthlyIncome
import com.blipay.credit_scoring.domain.credit.entity.Name
import com.blipay.credit_scoring.domain.credit.entity.ScoreRecord
import com.blipay.credit_scoring.domain.credit.entity.StoredAnalysis
import com.blipay.credit_scoring.domain.credit.exception.VersionConflictException
import com.blipay.credit_scoring.infra.persistence.adapter.CustomerPersistenceAdapter
import com.blipay.credit_scoring.infra.persistence.entity.UserEntity
import com.blipay.credit_scoring.infra.persistence.repository.ScoreJpaRepository
import com.blipay.credit_scoring.infra.persistence.repository.UserJpaRepository
import jakarta.persistence.OptimisticLockException
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class CustomerPersistenceAdapterTest {
    private val userRepo = mock<UserJpaRepository>()
    private val scoreRepo = mock<ScoreJpaRepository>()
    private val adapter = CustomerPersistenceAdapter(userRepo, scoreRepo)

    private val docNum = DocumentNumber.of("12345678909")

    private fun userEntity(version: Long = 0L) =
        UserEntity(
            UUID.randomUUID(),
            docNum.value,
            "Maria",
            30,
            BigDecimal("1800.00"),
            "Recife",
            Instant.now(),
            Instant.now(),
            version,
        )

    private fun customerRecord(version: Long = 0L): CustomerRecord {
        val u = userEntity(version)
        return u.toRecord()
    }

    private fun scoreRecord(doc: DocumentNumber = docNum) =
        ScoreRecord(
            0,
            doc,
            Name.of("Maria"),
            Age.of(30),
            MonthlyIncome.of(BigDecimal("1800.00")),
            City.of("Recife"),
            BigDecimal("15"),
            BigDecimal("36"),
            BigDecimal("150"),
            BigDecimal("201"),
            201,
            true,
            Instant.now(),
        )

    @Test
    fun updateThrowsVersionConflictOnOptimisticLockException() {
        val entity = userEntity(0L)
        whenever(userRepo.findByDocumentNumber(docNum.value)).thenReturn(entity)
        whenever(userRepo.saveAndFlush(any())).thenThrow(OptimisticLockException("concurrent update"))
        val customer = customerRecord(0L)
        val analysis = StoredAnalysis(customer, scoreRecord())
        assertFailsWith<VersionConflictException> { adapter.update(analysis) }
    }

    @Test
    fun updateThrowsVersionConflictOnOptimisticLockingFailureException() {
        val entity = userEntity(0L)
        whenever(userRepo.findByDocumentNumber(docNum.value)).thenReturn(entity)
        whenever(userRepo.saveAndFlush(any())).thenThrow(
            OptimisticLockingFailureException("concurrent update"),
        )
        val customer = customerRecord(0L)
        val analysis = StoredAnalysis(customer, scoreRecord())
        assertFailsWith<VersionConflictException> { adapter.update(analysis) }
    }

    @Test
    fun findScoresReturnsNullWhenUserExistsButHasNoScores() {
        val entity = userEntity()
        whenever(userRepo.findByDocumentNumber(docNum.value)).thenReturn(entity)
        whenever(scoreRepo.findByUserId(any(), any<Pageable>())).thenReturn(PageImpl(emptyList()))
        val result = adapter.findScores(docNum, 0, 20)
        assertNull(result)
    }
}

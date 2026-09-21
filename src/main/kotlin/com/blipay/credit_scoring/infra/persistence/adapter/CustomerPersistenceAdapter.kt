package com.blipay.credit_scoring.infra.persistence.adapter

import com.blipay.credit_scoring.domain.credit.entity.CustomerRecord
import com.blipay.credit_scoring.domain.credit.entity.DocumentNumber
import com.blipay.credit_scoring.domain.credit.entity.ScorePage
import com.blipay.credit_scoring.domain.credit.entity.StoredAnalysis
import com.blipay.credit_scoring.domain.credit.exception.CustomerNotFoundException
import com.blipay.credit_scoring.domain.credit.exception.DuplicateDocumentException
import com.blipay.credit_scoring.domain.credit.exception.VersionConflictException
import com.blipay.credit_scoring.domain.credit.port.CustomerStore
import com.blipay.credit_scoring.infra.persistence.entity.ScoreEntity
import com.blipay.credit_scoring.infra.persistence.entity.UserEntity
import com.blipay.credit_scoring.infra.persistence.repository.ScoreJpaRepository
import com.blipay.credit_scoring.infra.persistence.repository.UserJpaRepository
import jakarta.persistence.OptimisticLockException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class CustomerPersistenceAdapter(
    private val users: UserJpaRepository,
    private val scores: ScoreJpaRepository,
) : CustomerStore {
    @Transactional
    override fun create(analysis: StoredAnalysis): StoredAnalysis {
        try {
            val user = users.saveAndFlush(UserEntity.from(analysis.customer))
            val score = scores.saveAndFlush(ScoreEntity.from(analysis.score, user))
            return StoredAnalysis(user.toRecord(), score.toRecord())
        } catch (_: DataIntegrityViolationException) {
            throw DuplicateDocumentException()
        }
    }

    @Transactional(readOnly = true)
    override fun findByDocument(documentNumber: DocumentNumber): CustomerRecord? =
        users.findByDocumentNumber(documentNumber.value)?.toRecord()

    @Transactional
    override fun update(analysis: StoredAnalysis): StoredAnalysis {
        try {
            val user =
                users.findByDocumentNumber(analysis.customer.documentNumber.value)
                    ?: missingCustomer()
            if (user.version != analysis.customer.version) versionConflict()
            user.name = analysis.customer.name.value
            user.age = analysis.customer.age.value
            user.monthlyIncome = analysis.customer.monthlyIncome.value
            user.city = analysis.customer.city.value
            user.updatedAt = analysis.customer.updatedAt
            val saved = users.saveAndFlush(user)
            val score = scores.saveAndFlush(ScoreEntity.from(analysis.score, saved))
            return StoredAnalysis(saved.toRecord(), score.toRecord())
        } catch (_: OptimisticLockException) {
            versionConflict()
        } catch (_: OptimisticLockingFailureException) {
            versionConflict()
        }
    }

    @Transactional(readOnly = true)
    override fun findScores(
        documentNumber: DocumentNumber,
        page: Int,
        size: Int,
    ): ScorePage? {
        val user = users.findByDocumentNumber(documentNumber.value)
        val result =
            user?.let {
                scores.findByUserId(
                    it.id,
                    PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))),
                )
            }
        return result?.takeIf { it.totalElements > 0 }?.let {
            ScorePage(page, size, it.totalElements, it.totalPages, it.content.map { score -> score.toRecord() })
        }
    }

    private fun missingCustomer(): Nothing = throw CustomerNotFoundException()

    private fun versionConflict(): Nothing = throw VersionConflictException()
}

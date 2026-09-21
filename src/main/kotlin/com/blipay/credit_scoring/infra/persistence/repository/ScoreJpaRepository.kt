package com.blipay.credit_scoring.infra.persistence.repository

import com.blipay.credit_scoring.infra.persistence.entity.ScoreEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ScoreJpaRepository : JpaRepository<ScoreEntity, Long> {
    fun findByUserId(
        userId: UUID,
        pageable: Pageable,
    ): Page<ScoreEntity>
}

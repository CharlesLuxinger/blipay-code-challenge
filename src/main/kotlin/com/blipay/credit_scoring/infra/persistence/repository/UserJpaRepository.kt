package com.blipay.credit_scoring.infra.persistence.repository

import com.blipay.credit_scoring.infra.persistence.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserJpaRepository : JpaRepository<UserEntity, UUID> {
    fun findByDocumentNumber(documentNumber: String): UserEntity?
}

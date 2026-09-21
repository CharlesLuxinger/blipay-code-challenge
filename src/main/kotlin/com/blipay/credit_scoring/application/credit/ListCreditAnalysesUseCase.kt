package com.blipay.credit_scoring.application.credit

import com.blipay.credit_scoring.domain.credit.entity.DocumentNumber
import com.blipay.credit_scoring.domain.credit.entity.ScorePage
import com.blipay.credit_scoring.domain.credit.exception.CustomerNotFoundException
import com.blipay.credit_scoring.domain.credit.port.CustomerStore
import org.springframework.stereotype.Service

@Service
class ListCreditAnalysesUseCase(
    private val customerStore: CustomerStore,
) {
    fun execute(
        documentNumber: DocumentNumber,
        page: Int,
        size: Int,
    ): ScorePage = customerStore.findScores(documentNumber, page, size) ?: throw CustomerNotFoundException()
}

package com.blipay.credit_scoring.domain.credit.port

import com.blipay.credit_scoring.domain.credit.entity.DocumentNumber
import com.blipay.credit_scoring.domain.credit.entity.StoredAnalysis
import com.blipay.credit_scoring.domain.credit.entity.ScorePage
import com.blipay.credit_scoring.domain.credit.entity.CustomerRecord

interface CustomerStore {
    fun create(analysis: StoredAnalysis): StoredAnalysis

    fun findByDocument(documentNumber: DocumentNumber): CustomerRecord?

    fun update(analysis: StoredAnalysis): StoredAnalysis

    fun findScores(
        documentNumber: DocumentNumber,
        page: Int,
        size: Int,
    ): ScorePage?
}

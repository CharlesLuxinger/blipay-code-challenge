package com.blipay.credit_scoring.infra.api

import com.blipay.credit_scoring.application.credit.AnalysisResult
import java.time.Instant

data class AnalysisResponse(
    val score: Int,
    val approved: Boolean,
    val createdAt: Instant,
) {
    companion object {
        fun from(result: AnalysisResult) = AnalysisResponse(result.score, result.approved, result.createdAt)
    }
}

package com.blipay.credit_scoring.application.credit

import java.time.Instant

data class AnalysisResult(
    val score: Int,
    val approved: Boolean,
    val createdAt: Instant,
)

package com.blipay.credit_scoring.infra.api

import java.time.Instant

data class ScoreItem(
    val id: Long,
    val score: Int,
    val approved: Boolean,
    val createdAt: Instant,
)

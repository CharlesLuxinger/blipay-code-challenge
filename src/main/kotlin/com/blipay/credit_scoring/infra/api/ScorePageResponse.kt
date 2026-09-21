package com.blipay.credit_scoring.infra.api

data class ScorePageResponse(
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val items: List<ScoreItem>,
)

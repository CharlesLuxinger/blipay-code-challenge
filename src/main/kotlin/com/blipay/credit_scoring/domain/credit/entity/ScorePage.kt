package com.blipay.credit_scoring.domain.credit.entity

class ScorePage(
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val items: List<ScoreRecord>,
)

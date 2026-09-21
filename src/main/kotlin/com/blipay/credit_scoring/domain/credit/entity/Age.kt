package com.blipay.credit_scoring.domain.credit.entity

@JvmInline
value class Age private constructor(
    val value: Int,
) {
    companion object {
        fun of(value: Int): Age {
            require(value >= MINIMUM) { "age must be at least 18" }
            return Age(value)
        }

        private const val MINIMUM = 18
    }
}

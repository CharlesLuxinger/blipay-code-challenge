package com.blipay.credit_scoring.domain.credit.entity

@JvmInline
value class Name private constructor(
    val value: String,
) {
    companion object {
        fun of(raw: String): Name {
            val value = raw.trim()
            require(value.isNotEmpty() && value.length <= MAX_LENGTH) {
                "name must contain between 1 and 150 characters"
            }
            return Name(value)
        }

        private const val MAX_LENGTH = 150
    }
}

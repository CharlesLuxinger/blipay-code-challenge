package com.blipay.credit_scoring.domain.credit.entity

@JvmInline
value class City private constructor(
    val value: String,
) {
    companion object {
        fun of(raw: String): City {
            val value = raw.trim()
            require(value.isNotEmpty() && value.length <= MAX_LENGTH) {
                "city must contain between 1 and 50 characters"
            }
            return City(value)
        }

        private const val MAX_LENGTH = 50
    }
}

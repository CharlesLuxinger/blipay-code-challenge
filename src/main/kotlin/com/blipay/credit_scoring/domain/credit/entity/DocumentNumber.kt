package com.blipay.credit_scoring.domain.credit.entity

@JvmInline
value class DocumentNumber private constructor(
    val value: String,
) {
    companion object {
        fun of(raw: String): DocumentNumber {
            val digits = raw.filter(Char::isDigit)
            require(digits.length == DIGIT_COUNT) {
                "document_number must contain exactly 11 digits"
            }
            return DocumentNumber(digits)
        }

        private const val DIGIT_COUNT = 11
    }
}

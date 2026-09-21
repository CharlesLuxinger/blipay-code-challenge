package com.blipay.credit_scoring.infra.api

import com.blipay.credit_scoring.application.credit.CreateCreditAnalysisUseCase
import com.blipay.credit_scoring.application.credit.ListCreditAnalysesUseCase
import com.blipay.credit_scoring.application.credit.UpdateCreditAnalysisUseCase
import com.blipay.credit_scoring.domain.credit.entity.DocumentNumber
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/credit-analyses")
class CreditAnalysisController(
    private val create: CreateCreditAnalysisUseCase,
    private val update: UpdateCreditAnalysisUseCase,
    private val list: ListCreditAnalysesUseCase,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid @RequestBody request: CreateCreditAnalysisRequest,
    ): AnalysisResponse = AnalysisResponse.from(create.execute(request.toInput()))

    @PutMapping("/{documentNumber}")
    @ResponseStatus(HttpStatus.CREATED)
    fun update(
        @PathVariable documentNumber: String,
        @Valid @RequestBody request: UpdateCreditAnalysisRequest,
    ): AnalysisResponse {
        val canonicalDocument = DocumentNumber.of(documentNumber)
        return AnalysisResponse.from(update.execute(canonicalDocument, request.toInput(canonicalDocument)))
    }

    @GetMapping("/{documentNumber}")
    fun list(
        @PathVariable documentNumber: String,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) size: Int,
    ): ScorePageResponse =
        list.execute(DocumentNumber.of(documentNumber), page, size).let { result ->
            ScorePageResponse(
                result.page,
                result.size,
                result.totalElements,
                result.totalPages,
                result.items.map { ScoreItem(it.id, it.score, it.approved, it.createdAt) },
            )
        }

    companion object {
        private const val MAX_PAGE_SIZE = 100L
    }
}

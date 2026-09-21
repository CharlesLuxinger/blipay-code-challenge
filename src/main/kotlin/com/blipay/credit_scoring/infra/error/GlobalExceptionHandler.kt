package com.blipay.credit_scoring.infra.error

import com.blipay.credit_scoring.domain.credit.exception.CustomerNotFoundException
import com.blipay.credit_scoring.domain.credit.exception.DuplicateDocumentException
import com.blipay.credit_scoring.domain.credit.exception.UnknownCityException
import com.blipay.credit_scoring.domain.credit.exception.VersionConflictException
import com.blipay.credit_scoring.domain.credit.exception.WeatherUnavailableException
import feign.FeignException
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.HandlerMethodValidationException
import java.net.URI

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validation(exception: MethodArgumentNotValidException): ProblemDetail {
        val problem = problem(HttpStatus.BAD_REQUEST, "Request validation failed")
        problem.setProperty(
            "errors",
            exception.bindingResult.fieldErrors.map { mapOf("field" to it.field, "message" to it.defaultMessage) },
        )
        return problem
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun constraintViolation(exception: ConstraintViolationException): ProblemDetail {
        val problem = problem(HttpStatus.BAD_REQUEST, "Request validation failed")
        problem.setProperty(
            "errors",
            exception.constraintViolations.map {
                mapOf("field" to it.propertyPath.toString(), "message" to it.message)
            },
        )
        return problem
    }

    @ExceptionHandler(HandlerMethodValidationException::class)
    fun methodValidation(): ProblemDetail = problem(HttpStatus.BAD_REQUEST, "Request validation failed")

    @ExceptionHandler(HttpMessageNotReadableException::class, IllegalArgumentException::class)
    fun malformedInput(): ProblemDetail = problem(HttpStatus.BAD_REQUEST, "Request contains invalid input")

    @ExceptionHandler(DuplicateDocumentException::class)
    fun duplicate(): ProblemDetail = problem(HttpStatus.CONFLICT, "document_number is already registered")

    @ExceptionHandler(CustomerNotFoundException::class)
    fun notFound(): ProblemDetail = problem(HttpStatus.NOT_FOUND, "customer was not found")

    @ExceptionHandler(UnknownCityException::class)
    fun unknownCity(): ProblemDetail = problem(HttpStatus.BAD_REQUEST, "city was not found")

    @ExceptionHandler(VersionConflictException::class)
    fun conflict(): ProblemDetail = problem(HttpStatus.CONFLICT, "customer was changed by another request")

    @ExceptionHandler(WeatherUnavailableException::class, FeignException::class)
    fun weatherFailure(): ProblemDetail = problem(HttpStatus.BAD_GATEWAY, "weather provider is unavailable")

    @ExceptionHandler(Exception::class)
    fun unexpected(): ProblemDetail = problem(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal error occurred")

    private fun problem(
        status: HttpStatus,
        detail: String,
    ): ProblemDetail =
        ProblemDetail.forStatusAndDetail(status, detail).apply {
            type = URI.create("https://blipay.example/problems/${status.value()}")
            title = status.reasonPhrase
        }
}

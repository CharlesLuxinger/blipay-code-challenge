package com.blipay.credit_scoring.infra.error

import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import com.blipay.credit_scoring.domain.credit.exception.CustomerNotFoundException
import com.blipay.credit_scoring.domain.credit.exception.DuplicateDocumentException
import com.blipay.credit_scoring.domain.credit.exception.UnknownCityException
import com.blipay.credit_scoring.domain.credit.exception.VersionConflictException
import com.blipay.credit_scoring.domain.credit.exception.WeatherUnavailableException
import java.lang.reflect.Method
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalExceptionHandlerTest {
    private val handler = GlobalExceptionHandler()

    @Test
    fun validationReturnsProblemDetailWithAllFields() {
        val target = object {}
        val binding = BeanPropertyBindingResult(target, "request")
        binding.addError(FieldError("request", "name", "name is required"))
        binding.addError(FieldError("request", "age", "age must be at least 18"))
        val method = Any::class.java.getDeclaredMethod("toString")
        val exception =
            MethodArgumentNotValidException(
                org.springframework.core.MethodParameter(method, -1),
                binding,
            )

        val problem = handler.validation(exception)

        assertEquals(400, problem.status)
        assertEquals("Request validation failed", problem.detail)
        assertEquals(2, (problem.properties!!["errors"] as List<*>).size)
    }

    @Test
    fun domainConflictsAndMissingResourcesUseMappedStatuses() {
        assertEquals(409, handler.duplicate().status)
        assertEquals(404, handler.notFound().status)
    }

    @Test
    fun externalAndConcurrencyFailuresAreSafe() {
        assertEquals(502, handler.weatherFailure().status)
        assertEquals(409, handler.conflict().status)
        assertTrue(handler.weatherFailure().detail!!.contains("weather"))
    }

    @Test
    fun unexpectedExceptionUsesSafe500Detail() {
        val problem = handler.unexpected()
        assertEquals(500, problem.status)
        assertEquals("An unexpected internal error occurred", problem.detail)
    }

    @Test
    fun domainExceptionsHaveStableMessages() {
        assertEquals("customer was not found", CustomerNotFoundException().message)
        assertEquals("document_number is already registered", DuplicateDocumentException().message)
        assertEquals("city was not found", UnknownCityException().message)
        assertEquals("customer was changed by another request", VersionConflictException().message)
        assertEquals("weather provider is unavailable", WeatherUnavailableException().message)
    }
}

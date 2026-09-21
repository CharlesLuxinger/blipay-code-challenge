package com.blipay.credit_scoring.domain.credit.exception

class DuplicateDocumentException : RuntimeException("document_number is already registered")

class CustomerNotFoundException : RuntimeException("customer was not found")

class WeatherUnavailableException : RuntimeException("weather provider is unavailable")

class UnknownCityException : RuntimeException("city was not found")

class VersionConflictException : RuntimeException("customer was changed by another request")

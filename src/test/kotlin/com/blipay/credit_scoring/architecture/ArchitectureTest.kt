package com.blipay.credit_scoring.architecture

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.blipay.credit_scoring.Application
import com.blipay.credit_scoring.infra.client.open_weather.OpenWeatherFeignClient
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.cloud.openfeign.EnableFeignClients
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class ArchitectureTest {
    @Test
    fun `domain does not depend on application or infrastructure`() {
        domainMustNotDependOnApplicationOrInfrastructure.check(importedClasses)
    }

    @Test
    fun `application does not depend on infrastructure`() {
        applicationMustNotDependOnInfrastructure.check(importedClasses)
    }

    @Test
    fun feignClientIsUsedForOpenWeather() {
        check(OpenWeatherFeignClient::class.java.isAnnotationPresent(FeignClient::class.java))
        check(Application::class.java.isAnnotationPresent(EnableFeignClients::class.java))
    }

    companion object {
        private lateinit var importedClasses: JavaClasses

        private val domainMustNotDependOnApplicationOrInfrastructure: ArchRule =
            noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..application..", "..infra..")
                .allowEmptyShould(true)

        private val applicationMustNotDependOnInfrastructure: ArchRule =
            noClasses()
                .that()
                .resideInAPackage("..application..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..infra..")
                .allowEmptyShould(true)

        @JvmStatic
        @BeforeAll
        fun importClasses() {
            importedClasses = ClassFileImporter().importPackages("com.blipay.credit_scoring")
        }
    }
}

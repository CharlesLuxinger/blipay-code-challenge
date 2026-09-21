package com.blipay.credit_scoring.repository

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertTrue

class RepositoryConfigurationTest {
    private val root = Path.of(System.getProperty("user.dir"))

    @Test
    fun ciWorkflowContainsRequiredGates() {
        val workflow = Files.readString(root.resolve(".github/workflows/build.yml"))
        assertTrue(workflow.contains("java-version: '25'"))
        assertTrue(workflow.contains("ktlintMainSourceSetCheck"))
        assertTrue(workflow.contains("ktlintTestSourceSetCheck"))
        assertTrue(workflow.contains("detekt"))
        assertTrue(workflow.contains("test"))
        assertTrue(workflow.contains("jacocoTestReport"))
        assertTrue(workflow.contains("jacocoTestCoverageVerification"))
    }

    @Test
    fun dependabotDeclaresGradleUpdates() {
        val config = Files.readString(root.resolve(".github/dependabot.yml"))
        assertTrue(config.contains("package-ecosystem: gradle"))
        assertTrue(config.contains("interval: weekly"))
    }

    @Test
    fun readmeDocumentsRuntimeAndApi() {
        val readme = Files.readString(root.resolve("README.md"))
        listOf(
            "Java 25",
            "Docker Compose",
            "OPENWEATHER_API_URL",
            "OPENWEATHER_API_KEY",
            "DB_URL",
            "./gradlew test",
            "jacocoTestCoverageVerification",
            "POST /credit-analyses",
            "PUT /credit-analyses/{document_number}",
            "GET",
        ).forEach { assertTrue(readme.contains(it), "README missing $it") }
    }

    @Test
    fun approvedTestingLibrariesAreUsedByTheirTestLevels() {
        val gradle = Files.readString(root.resolve("build.gradle.kts"))
        val tests =
            Files.walk(root.resolve("src/test")).use { paths ->
                paths
                    .filter { Files.isRegularFile(it) }
                    .map { Files.readString(it) }
                    .toList()
                    .joinToString("\n")
            }
        assertTrue(gradle.contains("io.rest-assured:rest-assured"))
        assertTrue(gradle.contains("org.mockito.kotlin:mockito-kotlin"))
        assertTrue(gradle.contains("testcontainers-postgresql"))
        assertTrue(tests.contains("RestAssured"))
        assertTrue(tests.contains("PostgreSQLContainer"))
        assertTrue(tests.contains("Mockito") || tests.contains("mock<"))
    }
}

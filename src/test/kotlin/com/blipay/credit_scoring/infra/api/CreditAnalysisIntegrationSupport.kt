package com.blipay.credit_scoring.infra.api

import com.blipay.credit_scoring.infra.persistence.repository.ScoreJpaRepository
import com.blipay.credit_scoring.infra.persistence.repository.UserJpaRepository
import com.blipay.credit_scoring.infra.persistence.adapter.CustomerPersistenceAdapter
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import io.restassured.RestAssured
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets

class WeatherStub : HttpHandler {
    @Volatile var status: Int = 200

    @Volatile var temperature: String = "30.0"

    @Volatile var calls: Int = 0

    override fun handle(exchange: HttpExchange) {
        calls++
        val body = if (status == 200) "{\"main\":{\"temp\":$temperature}}" else "{\"message\":\"provider failure\"}"
        val bytes = body.toByteArray(StandardCharsets.UTF_8)
        exchange.responseHeaders.set("Content-Type", "application/json")
        exchange.sendResponseHeaders(status, bytes.size.toLong())
        exchange.responseBody.use { it.write(bytes) }
    }

    fun reset() {
        status = 200
        temperature = "30.0"
        calls = 0
    }
}

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class CreditAnalysisIntegrationSupport {
    @LocalServerPort
    protected var port: Int = 0

    @Autowired
    protected lateinit var users: UserJpaRepository

    @Autowired
    protected lateinit var scores: ScoreJpaRepository

    @Autowired
    protected lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    protected lateinit var persistence: CustomerPersistenceAdapter

    protected val weatherStub: WeatherStub
        get() = Companion.weather

    @BeforeEach
    fun cleanDatabase() {
        scores.deleteAllInBatch()
        users.deleteAllInBatch()
        weather.reset()
        RestAssured.port = port
    }

    companion object {
        @JvmStatic
        val postgres: PostgreSQLContainer<*> by lazy {
            PostgreSQLContainer("postgres:18-alpine").also { it.start() }
        }

        @JvmStatic
        val weather = WeatherStub()

        @JvmStatic
        val weatherServer: HttpServer =
            HttpServer.create(InetSocketAddress(0), 0).apply {
                createContext("/weather", weather)
                start()
            }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgres.jdbcUrl }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
            registry.add("openweather.api.base-url") { "http://localhost:${weatherServer.address.port}" }
            registry.add("openweather.api.key") { "test-key" }
        }
    }

    protected fun validCreateBody(
        name: String = "Maria",
        age: Int = 30,
        monthlyIncome: String = "1800.00",
        city: String = "Recife",
        document: String = "123.456.789-09",
    ) = """
        {"name":"$name","age":$age,"monthlyIncome":$monthlyIncome,"city":"$city","document_number":"$document"}
        """.trimIndent()

    protected fun validUpdateBody(
        name: String = "Maria Updated",
        age: Int = 31,
        monthlyIncome: String = "2000.00",
        city: String = "Olinda",
    ) = """
        {"name":"$name","age":$age,"monthlyIncome":$monthlyIncome,"city":"$city"}
        """.trimIndent()
}

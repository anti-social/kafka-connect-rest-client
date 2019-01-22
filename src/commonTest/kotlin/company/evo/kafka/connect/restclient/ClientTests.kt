package company.evo.kafka.connect.restclient

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.response
import io.ktor.client.engine.mock.responseError
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf

import kotlin.test.Test
import kotlin.test.assertEquals

import kotlinx.coroutines.runBlocking

class ClientTests {

    @Test
    fun testInfo() = runBlocking {
        val client = KafkaConnectClient("http://localhost:8083", MockEngine {
            response(
                """{"version":"1.1","commit":"deadbeaf"}""",
                headers = headersOf(
                    HttpHeaders.ContentType, ContentType.Application.Json.toString()
                )
            )
        })
        val info = client.info()
        assertEquals(info.version, "1.1")
        assertEquals(info.commit, "deadbeaf")
    }

    @Test
    fun testPauseWhenRebalance() = runBlocking {
        val client = KafkaConnectClient("http://localhost:8083", MockEngine {
            responseError(
                HttpStatusCode.Conflict,
                headers = headersOf(
                    HttpHeaders.ContentType, ContentType.Application.Json.toString()
                )
            )
        })
        val isExceptionRaised = try {
            client.pause("test-connector")
            false
        } catch (ex: KafkaConnectRebalanceException) {
            true
        }
        if (!isExceptionRaised) {
            throw AssertionError(
                "${KafkaConnectRebalanceException::class} exception was not raised"
            )
        }
    }
}

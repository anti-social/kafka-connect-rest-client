package company.evo.kafka.connect.restclient

import kotlinx.coroutines.runBlocking

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.response
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf

import kotlin.test.Test
import kotlin.test.assertEquals

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

        Unit
    }
}

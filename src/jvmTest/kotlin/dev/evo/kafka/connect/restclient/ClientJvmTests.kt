package dev.evo.kafka.connect.restclient

import java.net.ConnectException

import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.Url

import kotlin.test.Test
import kotlin.test.assertTrue

import kotlinx.coroutines.runBlocking

class ClientJvmTests {
    @Test
    fun testInfoWithConnectException() = runBlocking {
        val client = KafkaConnectClient(Url("http://localhost:8083"), MockEngine {
            throw ConnectException()
        })
        val raised = try {
            client.info()
            false
        } catch (ex: ConnectException) {
            true
        }
        assertTrue(raised)
    }
}

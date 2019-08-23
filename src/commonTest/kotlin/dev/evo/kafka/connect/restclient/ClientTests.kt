package dev.evo.kafka.connect.restclient

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf

import kotlin.test.Test
import kotlin.test.assertEquals

import kotlinx.coroutines.CoroutineScope

expect fun runTest(block: suspend CoroutineScope.() -> Unit)

class ClientTests {

    companion object {
        fun getClientWithResponse(
            expectedMethod: HttpMethod,
            expectPath: String,
            content: String,
            shouldCheckContentType: Boolean = false
        ): KafkaConnectClient {
            return KafkaConnectClient(Url("http://localhost:8083"), MockEngine { request ->
                assertEquals(expectedMethod, request.method)
                assertEquals(expectPath, request.url.encodedPath)
                assertEquals("application/json", request.headers["Accept"])
                if (shouldCheckContentType) {
                    assertEquals("application/json", request.headers["Content-type"])
                }
                respond(
                    content,
                    headers = headersOf(
                        HttpHeaders.ContentType, ContentType.Application.Json.toString()
                    )
                )
            })
        }
    }

    @Test
    fun testInfo() = runTest {
        val client = getClientWithResponse(
            HttpMethod.Get, "/",
            """{"version":"1.1","commit":"deadbeaf"}"""
        )
        val info = client.info()
        assertEquals(info.version, "1.1")
        assertEquals(info.commit, "deadbeaf")
    }

    @Test
    fun testConnectors() = runTest {
        val client = getClientWithResponse(
            HttpMethod.Get, "/connectors",
            """["connector-1", "connector-2"]"""
        )
        assertEquals(
            listOf("connector-1", "connector-2"),
            client.connectors()
        )
    }

    @Test
    fun testConnectorStatus() = runTest {
        val client = getClientWithResponse(
            HttpMethod.Get, "/connectors/hdfs-sink-connector/status",
            """
                {
                    "name": "hdfs-sink-connector",
                    "connector": {
                        "state": "RUNNING",
                        "worker_id": "fakehost:8083"
                    },
                    "tasks": [
                        {
                            "id": 0,
                            "state": "RUNNING",
                            "worker_id": "fakehost:8083"
                        },
                        {
                            "id": 1,
                            "state": "FAILED",
                            "worker_id": "fakehost:8083",
                            "trace": "org.apache.kafka.common.errors.RecordTooLargeException"
                        }
                    ]
                }
            """.trimIndent()
        )
        assertEquals(
            ConnectorStatus(
                name = "hdfs-sink-connector",
                connector = ConnectorStatus.Connector(
                    workerId = "fakehost:8083",
                    state = ConnectorStatus.State.RUNNING
                ),
                tasks = listOf(
                    ConnectorStatus.Task(
                        id = 0,
                        workerId = "fakehost:8083",
                        state = ConnectorStatus.State.RUNNING
                    ),
                    ConnectorStatus.Task(
                        id = 1,
                        workerId = "fakehost:8083",
                        state = ConnectorStatus.State.FAILED,
                        trace = "org.apache.kafka.common.errors.RecordTooLargeException"
                    )
                )
            ),
            client.status("hdfs-sink-connector")
        )
    }

    @Test
    fun testPause() = runTest {
        val client = getClientWithResponse(
            HttpMethod.Put, "/connectors/hdfs-sink-connector/pause", ""
        )
        assertEquals(
            client.pause("hdfs-sink-connector"),
            Unit
        )
    }

    @Test
    fun testPauseWhenRebalance() = runTest {
        val client = KafkaConnectClient(Url("http://localhost:8083"), MockEngine {
            respondError(
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

    @Test
    fun testResume() = runTest {
        val client = getClientWithResponse(
            HttpMethod.Put, "/connectors/hdfs-sink-connector/resume", ""
        )
        assertEquals(
            client.resume("hdfs-sink-connector"),
            Unit
        )
    }
}

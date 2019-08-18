package dev.evo.kafka.connect.restclient

import io.ktor.client.HttpClient
import io.ktor.client.call.ReceivePipelineException
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.features.HttpClientFeature
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.response.HttpResponsePipeline
import io.ktor.content.TextContent
import io.ktor.http.clone
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.takeFrom
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.util.AttributeKey

import kotlinx.io.core.Closeable

import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import kotlinx.serialization.serializer

open class KafkaConnectRestException(
    val statusCode: Int, val statusDescription: String,
    message: String = "Kafka connect server respond with an error"
) : Exception("$message: $statusCode $statusDescription")
{
    open val isRetriable = false
}

class KafkaConnectRebalanceException(
    statusCode: Int, statusDescription: String
) : KafkaConnectRestException(
    statusCode, statusDescription,
    message = "Rebalance is in progress, try later"
) {
    override val isRetriable = true
}

internal class ExpectKafkaConnectSuccess {
    companion object : HttpClientFeature<Unit, ExpectKafkaConnectSuccess> {
        override val key = AttributeKey<ExpectKafkaConnectSuccess>("ExpectSuccess")

        override fun prepare(block: Unit.() -> Unit) = ExpectKafkaConnectSuccess()

        override fun install(feature: ExpectKafkaConnectSuccess, scope: HttpClient) {
            scope.responsePipeline.intercept(HttpResponsePipeline.Receive) {
                val status = context.response.status
                if (status == HttpStatusCode.Conflict) {
                    throw KafkaConnectRebalanceException(
                        status.value,
                        status.description
                    )
                }
                if (status.value >= 300) {
                    throw KafkaConnectRestException(status.value, status.description)
                }
            }
        }
    }
}

class KafkaConnectClient(
    val baseUrl: Url,
    httpClientEngine: HttpClientEngine
) : Closeable {
    val httpClient = HttpClient(httpClientEngine) {
        expectSuccess = false
        install(ExpectKafkaConnectSuccess)
        install(JsonFeature) {
            serializer = KotlinxSerializer().apply {
                register(ConnectInfo.serializer())
                register(String.serializer().list)
                register(ConnectorStatus.serializer())
            }
        }
    }

    companion object {
        private val CONNECTORS_ENDPOINT = "connectors"
    }

    suspend fun info(): ConnectInfo {
        return httpClient.get { url.takeFrom(baseUrl) }
    }

    suspend fun connectors(): List<String> {
        return httpClient.get {
            url.takeFrom(baseUrl).path(CONNECTORS_ENDPOINT)
        }
    }

    suspend fun status(connectorName: String): ConnectorStatus {
        return httpClient.get {
            url.takeFrom(baseUrl).path(listOf(CONNECTORS_ENDPOINT, connectorName, "status"))
        }
    }

    private suspend fun emptyPut(path: List<String>) {
        try {
            httpClient.put<Unit> {
                url.takeFrom(baseUrl).path(path)
            }
        } catch (ex: ReceivePipelineException) {
            throw ex.cause
        }
    }

    suspend fun pause(connectorName: String) {
        emptyPut(listOf(CONNECTORS_ENDPOINT, connectorName, "pause"))
    }

    suspend fun resume(connectorName: String) {
        emptyPut(listOf(CONNECTORS_ENDPOINT, connectorName, "resume"))
    }

    override fun close() {
        httpClient.close()
    }
}

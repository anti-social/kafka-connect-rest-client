package dev.evo.kafka.connect.restclient

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.appendPathSegments
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.core.Closeable
import kotlinx.serialization.json.Json

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

private val ExpectKafkaConnectSuccess = createClientPlugin("ExpectKafkaConnectSuccess") {
    onResponse { response ->
        val status = response.status
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

class KafkaConnectClient(
    val baseUrl: Url,
    httpClientEngine: HttpClientEngine
) : Closeable {
    val httpClient = HttpClient(httpClientEngine) {
        expectSuccess = false
        install(ExpectKafkaConnectSuccess)
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    companion object {
        private const val CONNECTORS_ENDPOINT = "connectors"
    }

    suspend fun info(): ConnectInfo {
        return httpClient.get(baseUrl).body()
    }

    suspend fun connectors(): List<String> {
        return httpClient.get(baseUrl) {
            url {
                appendPathSegments(CONNECTORS_ENDPOINT)
            }
        }.body()
    }

    suspend fun status(connectorName: String): ConnectorStatus {
        return httpClient.get(baseUrl) {
            url {
                appendPathSegments(CONNECTORS_ENDPOINT, connectorName, "status")
            }
        }.body()
    }

    private suspend fun emptyPut(path: List<String>) {
        httpClient.put(baseUrl) {
            url {
                appendPathSegments(path)
            }
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

package company.evo.kafka.connect.restclient

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
import io.ktor.util.AttributeKey

import kotlinx.io.core.Closeable

import kotlinx.serialization.json.JSON
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
                    throw KafkaConnectRebalanceException(status.value, status.description)
                }
                if (status.value >= 300) {
                    throw KafkaConnectRestException(status.value, status.description)
                }
            }
        }
    }
}

class KafkaConnectClient(
    url: String,
    httpClientEngine: HttpClientEngine
) : Closeable {
    val urlBuilder = URLBuilder(url)
    val httpClient = HttpClient(httpClientEngine) {
        expectSuccess = false
        install(ExpectKafkaConnectSuccess)
        install(JsonFeature) {
            serializer = KotlinxSerializer(JSON.nonstrict).apply {
                register(ConnectInfo.serializer())
                register(ConnectorStatus.serializer())
            }
        }
    }

    companion object {
        private val CONNECTORS_ENDPOINT = "connectors"
        private val EMPTY_JSON_CONTENT = TextContent(
            "", contentType = ContentType.Application.Json
        )
    }

    suspend fun info(): ConnectInfo {
        return httpClient.get(urlBuilder.build())
    }

    suspend fun connectors(): Iterable<String> {
        // We cannot register deserializer for the List<String>. See:
        // https://stackoverflow.com/questions/52971069/ktor-serialize-deserialize-json-with-list-as-root-in-multiplatform
        // https://github.com/Kotlin/kotlinx.serialization/issues/179
        val rawResp = httpClient.get<String>(
            urlBuilder.clone().path(listOf(CONNECTORS_ENDPOINT)).build()
        )
        return JSON.nonstrict.parse(String.serializer().list, rawResp)
    }

    suspend fun status(connectorName: String): ConnectorStatus {
        return httpClient.get(
            urlBuilder.clone().path(listOf(CONNECTORS_ENDPOINT, connectorName, "status")).build()
        )
    }

    private suspend fun emptyPut(path: List<String>) {
        try {
            httpClient.put<Unit> {
                url {
                    takeFrom(urlBuilder).path(path).build()
                }
                body = EMPTY_JSON_CONTENT
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

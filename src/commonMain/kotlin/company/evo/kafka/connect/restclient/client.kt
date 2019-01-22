package company.evo.kafka.connect.restclient

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.response.HttpResponse
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.clone
import io.ktor.http.takeFrom

import kotlinx.io.core.Closeable

import kotlinx.serialization.json.JSON
import kotlinx.serialization.list
import kotlinx.serialization.serializer

class KafkaConnectRestException(
    val statusCode: Int, val statusDescription: String
) : Exception()

class KafkaConnectClient(
    url: String,
    httpClientEngine: HttpClientEngine
) : Closeable {
    val urlBuilder = URLBuilder(url)
    val httpClient = HttpClient(httpClientEngine) {
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
        return httpClient.get<ConnectInfo>(urlBuilder.build())
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

    suspend fun status(connectorName: String): ConnectorStatus? {
        return httpClient.get<ConnectorStatus>(
            urlBuilder.clone().path(listOf(CONNECTORS_ENDPOINT, connectorName, "status")).build()
        )
    }

    private suspend fun emptyPut(path: List<String>) {
        val resp = httpClient.put<HttpResponse> {
            url {
                takeFrom(urlBuilder).path(path).build()
            }
            body = EMPTY_JSON_CONTENT
        }
        // TODO Make a feature
        if (resp.status.value >= 300) {
            throw KafkaConnectRestException(resp.status.value, resp.status.description)
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

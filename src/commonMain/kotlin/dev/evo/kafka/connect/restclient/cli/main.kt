package dev.evo.kafka.connect.restclient.cli

import dev.evo.kafka.connect.restclient.KafkaConnectClient

import io.ktor.client.engine.HttpClientEngine
import io.ktor.http.Url

suspend fun run(args: Array<String>, httpEngine: HttpClientEngine): Int {
    val url = args[0]
    val connector = args[1]
    println(url)
    val client = KafkaConnectClient(Url(url), httpEngine)
    val connectInfo = client.info()
    println(connectInfo)
    println(client.connectors())
    client.resume(connector)

    return 0
}

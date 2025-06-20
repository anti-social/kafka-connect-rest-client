package dev.evo.kafka.connect.restclient.cli

import dev.evo.kafka.connect.restclient.KafkaConnectClient

import io.ktor.client.engine.HttpClientEngine
import io.ktor.http.Url
import kotlinx.coroutines.delay

suspend fun cli(args: Array<String>, httpEngine: HttpClientEngine): Int {
    val url = args[0]
    val connector = args[1]
    println(url)
    val client = KafkaConnectClient(Url(url), httpEngine)
    val connectInfo = client.info()
    println(connectInfo)
    println("Connectors:")
    println(client.connectors())
    // println("Pausing...")
    // client.pause(connector)
    // delay(2000)
    // println("Resuming...")
    // client.resume(connector)

    return 0
}

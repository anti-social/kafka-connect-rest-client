package dev.evo.kafka.connect.restclient.cli

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO

import kotlin.system.exitProcess

import kotlinx.coroutines.runBlocking

suspend fun main(args: Array<String>) {
    val engine: HttpClientEngine = CIO.create {}
    cli(args, engine)
}

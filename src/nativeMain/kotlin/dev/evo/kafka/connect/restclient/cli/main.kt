package dev.evo.kafka.connect.restclient.cli

import io.ktor.client.engine.curl.Curl

import kotlin.system.exitProcess

suspend fun main(args: Array<String>) {
    exitProcess(run(args, Curl.create {}))
}

package dev.evo.kafka.connect.restclient.cli

import io.ktor.client.engine.curl.Curl

import kotlin.system.exitProcess

import kotlinx.coroutines.runBlocking

fun main(args: Array<String>): Unit = runBlocking {
    exitProcess(run(args, Curl.create {}))
}

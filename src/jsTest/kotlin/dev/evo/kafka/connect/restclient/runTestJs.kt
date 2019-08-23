package dev.evo.kafka.connect.restclient

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise

actual fun runTest(block: suspend CoroutineScope.() -> Unit) {
    GlobalScope.promise { block() }
}

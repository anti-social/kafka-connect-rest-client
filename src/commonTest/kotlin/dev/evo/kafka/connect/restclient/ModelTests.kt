package dev.evo.kafka.connect.restclient

import kotlin.test.Test

import kotlinx.serialization.json.Json
import kotlinx.serialization.SerializationException

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ModelTests {
    val json = Json

    @Test
    fun testSerializingConnectInfoFull() {
        assertEquals(
            json.encodeToString(
                ConnectInfo.serializer(),
                ConnectInfo("1.0", "abcdef")
            ),
            """{"version":"1.0","commit":"abcdef"}"""
        )
    }

    @Test
    fun testDeserializingConnectInfoFull() {
        assertEquals(
            json.decodeFromString(
                ConnectInfo.serializer(),
                """{"version":"1.0","commit":"abcdef"}"""
            ),
            ConnectInfo("1.0", "abcdef")
        )
    }

    @Test
    fun testDeserializingConnectInfoMissing() {
        assertFailsWith(SerializationException::class) {
            json.decodeFromString(
                ConnectInfo.serializer(),
                """{"version":"1.0"}"""
            )
        }
    }
}

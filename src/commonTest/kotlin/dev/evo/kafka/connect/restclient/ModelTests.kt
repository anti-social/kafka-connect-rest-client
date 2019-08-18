package dev.evo.kafka.connect.restclient

import kotlin.test.Test

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.MissingFieldException

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@kotlinx.serialization.UnstableDefault
class ModelTests {
    val json = Json(JsonConfiguration.Default)

    @Test
    fun testSerializingConnectInfoFull() {
        assertEquals(
            json.stringify(
                ConnectInfo.serializer(),
                ConnectInfo("1.0", "abcdef")
            ),
            """{"version":"1.0","commit":"abcdef"}"""
        )
    }

    @Test
    fun testDeserializingConnectInfoFull() {
        assertEquals(
            json.parse(
                ConnectInfo.serializer(),
                """{"version":"1.0","commit":"abcdef"}"""
            ),
            ConnectInfo("1.0", "abcdef")
        )
    }

    @Test
    fun testDeserializingConnectInfoMissing() {
        assertFailsWith(MissingFieldException::class) {
            json.parse(
                ConnectInfo.serializer(),
                """{"version":"1.0"}"""
            )
        }
    }
}

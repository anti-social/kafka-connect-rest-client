package company.evo.kafka.connect.restclient

import kotlin.test.Test

import kotlinx.serialization.json.JSON
import kotlinx.serialization.MissingFieldException

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ModelTests {
    @Test
    fun testSerializingConnectInfoFull() {
        assertEquals(
            JSON.nonstrict.stringify(
                ConnectInfo.serializer(),
                ConnectInfo("1.0", "abcdef")
            ),
            """{"version":"1.0","commit":"abcdef"}"""
        )
    }

    @Test
    fun testDeserializingConnectInfoFull() {
        assertEquals(
            JSON.nonstrict.parse(
                ConnectInfo.serializer(),
                """{"version":"1.0","commit":"abcdef"}"""
            ),
            ConnectInfo("1.0", "abcdef")
        )
    }

    @Test
    fun testDeserializingConnectInfoMissing() {
        assertFailsWith(MissingFieldException::class) {
            JSON.nonstrict.parse(
                ConnectInfo.serializer(),
                """{"version":"1.0"}"""
            )
        }
    }
}

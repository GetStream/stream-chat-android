package io.getstream.chat.android.client

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.parser.GsonChatParser
import io.getstream.chat.android.client.testing.loadResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class DatesTests {

    val parser = GsonChatParser()
    val jsonMessage = loadResource("/message.json")
    val expectedTime = 1591787071000L

    /**
     * Verifies that [io.getstream.chat.android.client.parser.DateAdapter] parses valid time
     */
    @Test
    fun multithreadedDateParsing() {

        userParser()

        val threadA = Thread {
            var n = 200
            while (n > 0) {
                userParser()
                Thread.sleep((10 * Math.random()).toLong())
                n--
            }
        }.apply { name = "threadA" }

        val threadB = Thread {
            var n = 200
            while (n > 0) {
                useGson()
                Thread.sleep((10 * Math.random()).toLong())
                n--
            }
        }.apply { name = "threadB" }

        threadA.start()
        threadB.start()

        threadA.join()
        threadB.join()
    }

    private fun useGson() {
        val message = parser.gson.fromJson(jsonMessage, Message::class.java)
        assert(message)
    }

    private fun userParser() {
        val message = parser.fromJson(jsonMessage, Message::class.java)
        assert(message)
    }

    private fun assert(message: Message) {
        val createdAt = message.createdAt
        val time = createdAt!!.time
        assertThat(time).isEqualTo(expectedTime)
    }
}

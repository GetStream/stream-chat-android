package io.getstream.chat.android.client.parser

import io.getstream.chat.android.client.events.ChatEvent
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Date

internal class GsonChatParserTest {
    private val chatParser = GsonChatParser()

    /** [mapArguments] */
    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.parser.GsonChatParserTest#mapArguments")
    fun `Should render proper maps`(map: Map<Any, Any>, expectedResult: String) {
        chatParser.toJson(map) `should be equal to` expectedResult
    }

    /** [dateFromJsonArguments] */
    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.parser.GsonChatParserTest#dateFromJsonArguments")
    fun `Should convert to proper date`(jsonDate: String, expectedDateTime: Long) {
        chatParser.fromJson(jsonDate, Date::class.java).time `should be equal to` expectedDateTime
    }

    /** [EventArguments.chatParserEventArguments] */
    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.parser.EventArguments#chatParserEventArguments")
    fun `Should create proper event`(eventData: String, expectedEvent: ChatEvent) {
        chatParser.fromJson(eventData, ChatEvent::class.java) `should be equal to` expectedEvent
    }

    companion object {

        @JvmStatic
        fun mapArguments() = listOf(
            Arguments.of(emptyMap<Any, Any>(), "null"),
            Arguments.of(mapOf<Any, Any>(), "null"),
            Arguments.of(mapOf("a" to null), "null"),
            Arguments.of(mapOf("a" to "b"), "{\"a\":\"b\"}"),
            Arguments.of(mapOf("a" to null, "b" to "c"), "{\"b\":\"c\"}")
        )

        @JvmStatic
        fun dateFromJsonArguments() = listOf(
            Arguments.of("\"2020-06-29T06:14:28.000Z\"", 1593411268000),
            Arguments.of("\"2020-06-29T06:14:28.0Z\"", 1593411268000),
            Arguments.of("\"2020-06-29T06:14:28.00Z\"", 1593411268000),
            Arguments.of("\"2020-06-29T06:14:28.000Z\"", 1593411268000),
            Arguments.of("\"2020-06-29T06:14:28.100Z\"", 1593411268100)
        )
    }
}

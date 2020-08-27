package io.getstream.chat.android.client.parser

import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class ChatParserImplTest {
    private val chatParser = ChatParserImpl()

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.parser.ChatParserImplTest#mapArguments")
    fun `Should render proper maps`(map: Map<Any, Any>, expectedResult: String) {
        chatParser.toJson(map) `should be equal to` expectedResult
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
    }
}

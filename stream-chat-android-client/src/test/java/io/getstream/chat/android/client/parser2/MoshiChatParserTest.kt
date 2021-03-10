package io.getstream.chat.android.client.parser2

import com.google.common.truth.Truth
import io.getstream.chat.android.client.events.ChatEvent
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class MoshiChatParserTest {

    private val parser = MoshiChatParser()

    /** [io.getstream.chat.android.client.parser.EventArguments.eventAdapterArguments] */
    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.parser.EventArguments#eventAdapterArguments")
    fun `Should create proper event`(eventData: String, expectedEvent: ChatEvent) {
        val parsedEvent = parser.fromJson(eventData, ChatEvent::class.java)
        Truth.assertThat(parsedEvent).isEqualTo(expectedEvent)
    }
}

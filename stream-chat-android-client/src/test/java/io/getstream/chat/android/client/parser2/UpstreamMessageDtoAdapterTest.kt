package io.getstream.chat.android.client.parser2

import io.getstream.chat.android.client.api2.model.dto.UpstreamMessageDto
import io.getstream.chat.android.client.parser2.testdata.MessageDtoTestData.upstreamJson
import io.getstream.chat.android.client.parser2.testdata.MessageDtoTestData.upstreamJsonWithoutExtraData
import io.getstream.chat.android.client.parser2.testdata.MessageDtoTestData.upstreamMessage
import io.getstream.chat.android.client.parser2.testdata.MessageDtoTestData.upstreamMessageWithoutExtraData
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test

internal class UpstreamMessageDtoAdapterTest {
    private val parser = MoshiChatParser()

    @Test
    fun `Serialize JSON message with custom fields`() {
        val jsonString = parser.toJson(upstreamMessage)
        jsonString shouldBeEqualTo upstreamJson
    }

    @Test
    fun `Serialize JSON message without custom fields`() {
        val jsonString = parser.toJson(upstreamMessageWithoutExtraData)
        jsonString shouldBeEqualTo upstreamJsonWithoutExtraData
    }

    @Test
    fun `Can't parse upstream message`() {
        invoking {
            parser.fromJson(upstreamJson, UpstreamMessageDto::class.java)
        }.shouldThrow(RuntimeException::class)
    }
}

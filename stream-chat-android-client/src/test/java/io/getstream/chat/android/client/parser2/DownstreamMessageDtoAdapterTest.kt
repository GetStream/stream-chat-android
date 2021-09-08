package io.getstream.chat.android.client.parser2

import com.google.common.truth.Truth.assertThat
import io.getstream.chat.android.client.api2.model.dto.DownstreamMessageDto
import io.getstream.chat.android.client.parser2.testdata.MessageDtoTestData.downstreamJson
import io.getstream.chat.android.client.parser2.testdata.MessageDtoTestData.downstreamJsonWithoutExtraData
import io.getstream.chat.android.client.parser2.testdata.MessageDtoTestData.downstreamMessage
import io.getstream.chat.android.client.parser2.testdata.MessageDtoTestData.downstreamMessageWithoutExtraData
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class DownstreamMessageDtoAdapterTest {
    private val parser = MoshiChatParser()

    @Test
    fun `Deserialize JSON message with custom fields`() {
        val message = parser.fromJson(downstreamJson, DownstreamMessageDto::class.java)
        assertThat(message).isEqualTo(downstreamMessage)
    }

    @Test
    fun `Deserialize JSON message without custom fields`() {
        val message = parser.fromJson(downstreamJsonWithoutExtraData, DownstreamMessageDto::class.java)
        assertThat(message).isEqualTo(downstreamMessageWithoutExtraData)
    }

    @Test
    fun `Can't serialize downstream dto`() {
        assertThrows<RuntimeException> {
            parser.toJson(downstreamMessage)
        }
    }
}

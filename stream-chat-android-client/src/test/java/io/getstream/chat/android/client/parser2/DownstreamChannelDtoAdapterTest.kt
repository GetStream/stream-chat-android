package io.getstream.chat.android.client.parser2

import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelDto
import io.getstream.chat.android.client.parser2.testdata.ChannelDtoTestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class DownstreamChannelDtoAdapterTest {
    private val parser = MoshiChatParser()

    @Test
    fun `Deserialize JSON channel with custom fields`() {
        val channel = parser.fromJson(ChannelDtoTestData.downstreamJson, DownstreamChannelDto::class.java)
        assertThat(channel).isEqualTo(ChannelDtoTestData.downstreamChannel)
    }

    @Test
    fun `Deserialize JSON channel without custom fields`() {
        val channel = parser.fromJson(ChannelDtoTestData.downstreamJsonWithoutExtraData, DownstreamChannelDto::class.java)
        assertThat(channel).isEqualTo(ChannelDtoTestData.downstreamChannelWithoutExtraData)
    }

    @Test
    fun `Can't serialize downstream dto`() {
        assertThrows<RuntimeException> {
            parser.toJson(ChannelDtoTestData.downstreamChannel)
        }
    }
}

package io.getstream.chat.android.client.parser2

import io.getstream.chat.android.client.api2.model.dto.UpstreamChannelDto
import io.getstream.chat.android.client.parser2.testdata.ChannelDtoTestData
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class UpstreamChannelDtoAdapterTest {

    private val parser = MoshiChatParser()

    @Test
    fun `Serialize JSON channel with custom fields`() {
        val jsonString = parser.toJson(ChannelDtoTestData.upstreamChannel)
        Assertions.assertThat(jsonString).isEqualTo(ChannelDtoTestData.upstreamJson)
    }

    @Test
    fun `Serialize JSON channel without custom fields`() {
        val jsonString = parser.toJson(ChannelDtoTestData.upstreamChannelWithoutExtraData)
        Assertions.assertThat(jsonString).isEqualTo(ChannelDtoTestData.upstreamJsonWithoutExtraData)
    }

    @Test
    fun `Can't parse upstream channel`() {
        assertThrows<RuntimeException> {
            parser.fromJson(ChannelDtoTestData.upstreamJson, UpstreamChannelDto::class.java)
        }
    }
}

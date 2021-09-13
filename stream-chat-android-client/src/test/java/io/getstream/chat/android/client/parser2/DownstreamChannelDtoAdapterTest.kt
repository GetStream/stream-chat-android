package io.getstream.chat.android.client.parser2

import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelDto
import io.getstream.chat.android.client.parser2.testdata.ChannelDtoTestData
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test

internal class DownstreamChannelDtoAdapterTest {
    private val parser = MoshiChatParser()

    @Test
    fun `Deserialize JSON channel with custom fields`() {
        val channel = parser.fromJson(ChannelDtoTestData.downstreamJson, DownstreamChannelDto::class.java)
        channel shouldBeEqualTo ChannelDtoTestData.downstreamChannel
    }

    @Test
    fun `Deserialize JSON channel without custom fields`() {
        val channel = parser.fromJson(ChannelDtoTestData.downstreamJsonWithoutExtraData, DownstreamChannelDto::class.java)
        channel shouldBeEqualTo ChannelDtoTestData.downstreamChannelWithoutExtraData
    }

    @Test
    fun `Can't serialize downstream dto`() {
        invoking {
            parser.toJson(ChannelDtoTestData.downstreamChannel)
        }.shouldThrow(RuntimeException::class)
    }
}

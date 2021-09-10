package io.getstream.chat.android.client.parser2

import io.getstream.chat.android.client.api2.model.dto.UpstreamChannelDto
import io.getstream.chat.android.client.parser2.testdata.ChannelDtoTestData
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test

internal class UpstreamChannelDtoAdapterTest {

    private val parser = MoshiChatParser()

    @Test
    fun `Serialize JSON channel with custom fields`() {
        val jsonString = parser.toJson(ChannelDtoTestData.upstreamChannel)
        jsonString shouldBeEqualTo ChannelDtoTestData.upstreamJson
    }

    @Test
    fun `Serialize JSON channel without custom fields`() {
        val jsonString = parser.toJson(ChannelDtoTestData.upstreamChannelWithoutExtraData)
        jsonString shouldBeEqualTo ChannelDtoTestData.upstreamJsonWithoutExtraData
    }

    @Test
    fun `Can't parse upstream channel`() {
        invoking {
            parser.fromJson(ChannelDtoTestData.upstreamJson, UpstreamChannelDto::class.java)
        }.shouldThrow(RuntimeException::class)
    }
}

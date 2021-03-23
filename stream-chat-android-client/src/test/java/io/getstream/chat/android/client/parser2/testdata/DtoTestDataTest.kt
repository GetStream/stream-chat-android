package io.getstream.chat.android.client.parser2.testdata

import com.squareup.moshi.Moshi
import org.junit.jupiter.api.Test

internal class DtoTestDataTest {

    @Test
    fun `Check test data is valid JSON`() {
        val moshi = Moshi.Builder().build()
        val anyAdapter = moshi.adapter(Any::class.java)

        anyAdapter.fromJson(MessageDtoTestData.downstreamJson)
        anyAdapter.fromJson(MessageDtoTestData.downstreamJsonWithoutExtraData)
        anyAdapter.fromJson(MessageDtoTestData.upstreamJson)
        anyAdapter.fromJson(MessageDtoTestData.upstreamJsonWithoutExtraData)

        anyAdapter.fromJson(ReactionDtoTestData.downstreamJson)
        anyAdapter.fromJson(ReactionDtoTestData.downstreamJsonWithoutExtraData)
        anyAdapter.fromJson(ReactionDtoTestData.upstreamJson)
        anyAdapter.fromJson(ReactionDtoTestData.upstreamJsonWithoutExtraData)

        anyAdapter.fromJson(UserDtoTestData.downstreamJson)
        anyAdapter.fromJson(UserDtoTestData.downstreamJsonWithoutExtraData)
        anyAdapter.fromJson(UserDtoTestData.upstreamJson)
        anyAdapter.fromJson(UserDtoTestData.upstreamJsonWithoutExtraData)

        anyAdapter.fromJson(ChannelDtoTestData.configJson)
        anyAdapter.fromJson(ChannelDtoTestData.downstreamJson)
        anyAdapter.fromJson(ChannelDtoTestData.downstreamJsonWithoutExtraData)
        anyAdapter.fromJson(ChannelDtoTestData.upstreamJson)
        anyAdapter.fromJson(ChannelDtoTestData.upstreamJsonWithoutExtraData)

        anyAdapter.fromJson(AttachmentDtoTestData.json)
        anyAdapter.fromJson(AttachmentDtoTestData.jsonWithoutExtraData)
    }
}

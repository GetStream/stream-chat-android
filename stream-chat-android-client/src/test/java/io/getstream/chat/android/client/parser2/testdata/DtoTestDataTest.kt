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

        anyAdapter.fromJson(UserDtoTestData.downstreamJson)
    }
}

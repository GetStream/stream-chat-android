package io.getstream.chat.android.client.parser2

import io.getstream.chat.android.client.api2.model.dto.DownstreamReactionDto
import io.getstream.chat.android.client.parser2.testdata.ReactionDtoTestData
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test

internal class UpstreamReactionDtoAdapterTest {

    private val parser = MoshiChatParser()

    @Test
    fun `Serialize JSON reaction with custom fields`() {
        val jsonString = parser.toJson(ReactionDtoTestData.upstreamReaction)
        jsonString shouldBeEqualTo ReactionDtoTestData.upstreamJson
    }

    @Test
    fun `Serialize JSON reaction without custom fields`() {
        val jsonString = parser.toJson(ReactionDtoTestData.upstreamReactionWithoutExtraData)
        jsonString shouldBeEqualTo ReactionDtoTestData.upstreamJsonWithoutExtraData
    }

    @Test
    fun `Can't parse upstream reaction`() {
        invoking {
            parser.fromJson(ReactionDtoTestData.upstreamJson, DownstreamReactionDto::class.java)
        }.shouldThrow(RuntimeException::class)
    }
}

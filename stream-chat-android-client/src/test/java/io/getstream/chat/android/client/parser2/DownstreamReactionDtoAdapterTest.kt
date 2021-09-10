package io.getstream.chat.android.client.parser2

import io.getstream.chat.android.client.api2.model.dto.DownstreamReactionDto
import io.getstream.chat.android.client.parser2.testdata.ReactionDtoTestData.downstreamJson
import io.getstream.chat.android.client.parser2.testdata.ReactionDtoTestData.downstreamJsonWithoutExtraData
import io.getstream.chat.android.client.parser2.testdata.ReactionDtoTestData.downstreamReaction
import io.getstream.chat.android.client.parser2.testdata.ReactionDtoTestData.downstreamReactionWithoutExtraData
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test

internal class DownstreamReactionDtoAdapterTest {
    private val parser = MoshiChatParser()

    @Test
    fun `Deserialize JSON reaction with custom fields`() {
        val reaction = parser.fromJson(downstreamJson, DownstreamReactionDto::class.java)
        reaction shouldBeEqualTo downstreamReaction
    }

    @Test
    fun `Deserialize JSON reaction without custom fields`() {
        val reaction = parser.fromJson(downstreamJsonWithoutExtraData, DownstreamReactionDto::class.java)
        reaction shouldBeEqualTo downstreamReactionWithoutExtraData
    }

    @Test
    fun `Can't serialize downstream dto`() {
        invoking {
            parser.toJson(downstreamReaction)
        }.shouldThrow(RuntimeException::class)
    }
}

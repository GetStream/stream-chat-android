package io.getstream.chat.android.client.parser2

import io.getstream.chat.android.client.api2.model.dto.DownstreamReactionDto
import io.getstream.chat.android.client.parser2.testdata.ReactionDtoTestData.downstreamJson
import io.getstream.chat.android.client.parser2.testdata.ReactionDtoTestData.downstreamJsonWithoutExtraData
import io.getstream.chat.android.client.parser2.testdata.ReactionDtoTestData.downstreamReaction
import io.getstream.chat.android.client.parser2.testdata.ReactionDtoTestData.downstreamReactionWithoutExtraData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class DownstreamReactionDtoAdapterTest {
    private val parser = MoshiChatParser()

    @Test
    fun `Deserialize JSON reaction with custom fields`() {
        val reaction = parser.fromJson(downstreamJson, DownstreamReactionDto::class.java)
        assertThat(reaction).isEqualTo(downstreamReaction)
    }

    @Test
    fun `Deserialize JSON reaction without custom fields`() {
        val reaction = parser.fromJson(downstreamJsonWithoutExtraData, DownstreamReactionDto::class.java)
        assertThat(reaction).isEqualTo(downstreamReactionWithoutExtraData)
    }

    @Test
    fun `Can't serialize downstream dto`() {
        assertThrows<RuntimeException> {
            parser.toJson(downstreamReaction)
        }
    }
}

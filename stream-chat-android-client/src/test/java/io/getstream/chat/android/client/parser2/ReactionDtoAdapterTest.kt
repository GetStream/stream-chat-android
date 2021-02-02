package io.getstream.chat.android.client.parser2

import io.getstream.chat.android.client.api2.model.dto.ReactionDto
import io.getstream.chat.android.client.parser2.DtoTestData.reaction
import io.getstream.chat.android.client.parser2.DtoTestData.reactionJson
import io.getstream.chat.android.client.parser2.DtoTestData.reactionJsonWithoutExtraData
import io.getstream.chat.android.client.parser2.DtoTestData.reactionWithoutExtraData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ReactionDtoAdapterTest {

    private val parser = MoshiChatParser()

    @Test
    fun `Serialize JSON message with custom fields`() {
        val jsonString = parser.toJson(reaction)
        assertThat(jsonString).isEqualTo(reactionJson)
    }

    @Test
    fun `Serialize JSON message without custom fields`() {
        val jsonString = parser.toJson(reactionWithoutExtraData)
        assertThat(jsonString).isEqualTo(reactionJsonWithoutExtraData)
    }

    @Test
    fun `Deserialize JSON message with custom fields`() {
        val message = parser.fromJson(reactionJson, ReactionDto::class.java)
        assertThat(message).isEqualTo(reaction)
    }

    @Test
    fun `Deserialize JSON message without custom fields`() {
        val message = parser.fromJson(reactionJsonWithoutExtraData, ReactionDto::class.java)
        assertThat(message).isEqualTo(reactionWithoutExtraData)
    }
}

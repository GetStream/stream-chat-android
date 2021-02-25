package io.getstream.chat.android.client.parser2

import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import io.getstream.chat.android.client.parser2.testdata.UserDtoTestData.downstreamJson
import io.getstream.chat.android.client.parser2.testdata.UserDtoTestData.downstreamUser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class DownstreamUserDtoAdapterTest {
    private val parser = MoshiChatParser()

    @Test
    fun `Deserialize JSON message with custom fields`() {
        val message = parser.fromJson(downstreamJson, DownstreamUserDto::class.java)
        assertThat(message).isEqualTo(downstreamUser)
    }

    @Test
    fun `Can't serialize downstream dto`() {
        assertThrows<RuntimeException> {
            parser.toJson(downstreamUser)
        }
    }
}

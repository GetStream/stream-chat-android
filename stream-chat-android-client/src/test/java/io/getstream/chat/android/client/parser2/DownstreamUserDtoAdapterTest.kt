package io.getstream.chat.android.client.parser2

import com.google.common.truth.Truth.assertThat
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import io.getstream.chat.android.client.parser2.testdata.UserDtoTestData.downstreamJson
import io.getstream.chat.android.client.parser2.testdata.UserDtoTestData.downstreamJsonWithoutExtraData
import io.getstream.chat.android.client.parser2.testdata.UserDtoTestData.downstreamUser
import io.getstream.chat.android.client.parser2.testdata.UserDtoTestData.downstreamUserWithoutExtraData
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class DownstreamUserDtoAdapterTest {
    private val parser = MoshiChatParser()

    @Test
    fun `Deserialize JSON user without custom fields`() {
        val user = parser.fromJson(downstreamJsonWithoutExtraData, DownstreamUserDto::class.java)
        assertThat(user).isEqualTo(downstreamUserWithoutExtraData)
    }

    @Test
    fun `Deserialize JSON user with custom fields`() {
        val user = parser.fromJson(downstreamJson, DownstreamUserDto::class.java)
        assertThat(user).isEqualTo(downstreamUser)
    }

    @Test
    fun `Can't serialize downstream dto`() {
        assertThrows<RuntimeException> {
            parser.toJson(downstreamUser)
        }
    }
}

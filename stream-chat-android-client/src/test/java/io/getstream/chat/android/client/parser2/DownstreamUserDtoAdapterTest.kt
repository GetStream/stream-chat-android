package io.getstream.chat.android.client.parser2

import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import io.getstream.chat.android.client.parser2.testdata.UserDtoTestData.downstreamJson
import io.getstream.chat.android.client.parser2.testdata.UserDtoTestData.downstreamJsonWithoutExtraData
import io.getstream.chat.android.client.parser2.testdata.UserDtoTestData.downstreamUser
import io.getstream.chat.android.client.parser2.testdata.UserDtoTestData.downstreamUserWithoutExtraData
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test

internal class DownstreamUserDtoAdapterTest {
    private val parser = MoshiChatParser()

    @Test
    fun `Deserialize JSON user without custom fields`() {
        val user = parser.fromJson(downstreamJsonWithoutExtraData, DownstreamUserDto::class.java)
        user shouldBeEqualTo downstreamUserWithoutExtraData
    }

    @Test
    fun `Deserialize JSON user with custom fields`() {
        val user = parser.fromJson(downstreamJson, DownstreamUserDto::class.java)
        user shouldBeEqualTo downstreamUser
    }

    @Test
    fun `Can't serialize downstream dto`() {
        invoking {
            parser.toJson(downstreamUser)
        }.shouldThrow(RuntimeException::class)
    }
}

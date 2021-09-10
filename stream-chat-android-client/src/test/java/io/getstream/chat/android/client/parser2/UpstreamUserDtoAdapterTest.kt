package io.getstream.chat.android.client.parser2

import io.getstream.chat.android.client.api2.model.dto.UpstreamUserDto
import io.getstream.chat.android.client.parser2.testdata.UserDtoTestData
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test

internal class UpstreamUserDtoAdapterTest {
    private val parser = MoshiChatParser()

    @Test
    fun `Serialize JSON user with custom fields`() {
        val jsonString = parser.toJson(UserDtoTestData.upstreamUser)
        jsonString shouldBeEqualTo UserDtoTestData.upstreamJson
    }

    @Test
    fun `Serialize JSON user without custom fields`() {
        val jsonString = parser.toJson(UserDtoTestData.upstreamUserWithoutExtraData)
        jsonString shouldBeEqualTo UserDtoTestData.upstreamJsonWithoutExtraData
    }

    @Test
    fun `Can't parse upstream user`() {
        invoking {
            parser.fromJson(UserDtoTestData.upstreamJson, UpstreamUserDto::class.java)
        }.shouldThrow(RuntimeException::class)
    }
}

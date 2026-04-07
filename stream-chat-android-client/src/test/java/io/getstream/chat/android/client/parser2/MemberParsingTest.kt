/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.parser2

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.api2.model.dto.DownstreamMemberDto
import io.getstream.chat.android.client.parser2.adapters.DateAdapter
import io.getstream.chat.android.client.parser2.event.DeviceAdapter
import io.getstream.chat.android.client.parser2.event.MemberAdapter
import io.getstream.chat.android.client.parser2.event.PrivacySettingsAdapter
import io.getstream.chat.android.client.parser2.event.UserAdapter
import io.getstream.chat.android.client.parser2.testdata.MemberTestData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Date

internal class MemberParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { "" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    private val moshi = Moshi.Builder().add(DateAdapter()).build()
    private val dateAdapter = moshi.adapter(Date::class.java)
    private val deviceAdapter = DeviceAdapter()
    private val privacySettingsAdapter = PrivacySettingsAdapter()
    private val userAdapter = UserAdapter(
        deviceAdapter = deviceAdapter,
        privacySettingsAdapter = privacySettingsAdapter,
        dateAdapter = dateAdapter,
        userTransformer = NoOpUserTransformer,
    )
    private val memberAdapter = MemberAdapter(
        userAdapter = userAdapter,
        dateAdapter = dateAdapter,
    )

    // region DTO path (JSON → DownstreamMemberDto → Member)

    @Test
    fun `DTO path - deserializes all fields`() {
        val dto = parser.fromJson(MemberTestData.jsonAllFields, DownstreamMemberDto::class.java)
        val member = with(domainMapping) { dto.toDomain() }
        assertEquals(MemberTestData.expectedAllFields, member)
    }

    @Test
    fun `DTO path - deserializes with optional fields missing`() {
        val dto = parser.fromJson(MemberTestData.jsonOptionalFieldsMissing, DownstreamMemberDto::class.java)
        val member = with(domainMapping) { dto.toDomain() }
        assertEquals(MemberTestData.expectedOptionalFieldsMissing, member)
    }

    // endregion

    // region Direct path (JSON → Member via MemberAdapter)

    @Test
    fun `Direct path - deserializes all fields`() {
        val member = memberAdapter.fromJson(MemberTestData.jsonAllFields)
        assertEquals(MemberTestData.expectedAllFields, member)
    }

    @Test
    fun `Direct path - deserializes with optional fields missing`() {
        val member = memberAdapter.fromJson(MemberTestData.jsonOptionalFieldsMissing)
        assertEquals(MemberTestData.expectedOptionalFieldsMissing, member)
    }

    // endregion

    // region Error message parity (both paths must throw identical errors)

    @Test
    fun `Both paths - same error message on missing user`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(MemberTestData.jsonMissingUser, DownstreamMemberDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            memberAdapter.fromJson(MemberTestData.jsonMissingUser)
        }
        assertEquals(dtoException.message, directException.message)
    }

    // endregion
}

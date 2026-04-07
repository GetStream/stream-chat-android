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
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import io.getstream.chat.android.client.parser2.adapters.DateAdapter
import io.getstream.chat.android.client.parser2.event.DeviceAdapter
import io.getstream.chat.android.client.parser2.event.PrivacySettingsAdapter
import io.getstream.chat.android.client.parser2.event.UserAdapter
import io.getstream.chat.android.client.parser2.testdata.UserTestData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Date

internal class UserParsingTest {

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

    // region DTO path (JSON → DownstreamUserDto → User)

    @Test
    fun `DTO path - deserializes all fields`() {
        val dto = parser.fromJson(UserTestData.jsonAllFields, DownstreamUserDto::class.java)
        val domain = with(domainMapping) { dto.toDomain() }
        assertEquals(UserTestData.expectedAllFields, domain)
    }

    @Test
    fun `DTO path - deserializes with optional fields missing`() {
        val dto = parser.fromJson(UserTestData.jsonOptionalFieldsMissing, DownstreamUserDto::class.java)
        val domain = with(domainMapping) { dto.toDomain() }
        assertEquals(UserTestData.expectedOptionalFieldsMissing, domain)
    }

    // endregion

    // region Direct path (JSON → User via UserAdapter)

    @Test
    fun `Direct path - deserializes all fields`() {
        val domain = userAdapter.fromJson(UserTestData.jsonAllFields)
        assertEquals(UserTestData.expectedAllFields, domain)
    }

    @Test
    fun `Direct path - deserializes with optional fields missing`() {
        val domain = userAdapter.fromJson(UserTestData.jsonOptionalFieldsMissing)
        assertEquals(UserTestData.expectedOptionalFieldsMissing, domain)
    }

    // endregion

    // region Error message parity (both paths must throw identical errors)

    @Test
    fun `Both paths - same error message on missing id`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(UserTestData.jsonMissingId, DownstreamUserDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            userAdapter.fromJson(UserTestData.jsonMissingId)
        }
        assertEquals(dtoException.message, directException.message)
    }

    @Test
    fun `Both paths - same error message on missing role`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(UserTestData.jsonMissingRole, DownstreamUserDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            userAdapter.fromJson(UserTestData.jsonMissingRole)
        }
        assertEquals(dtoException.message, directException.message)
    }

    @Test
    fun `Both paths - same error message on missing banned`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(UserTestData.jsonMissingBanned, DownstreamUserDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            userAdapter.fromJson(UserTestData.jsonMissingBanned)
        }
        assertEquals(dtoException.message, directException.message)
    }

    @Test
    fun `Both paths - same error message on missing online`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(UserTestData.jsonMissingOnline, DownstreamUserDto::class.java)
        }
        println(dtoException.message)
        val directException = assertThrows<JsonDataException> {
            userAdapter.fromJson(UserTestData.jsonMissingOnline)
        }
        assertEquals(dtoException.message, directException.message)
    }

    // endregion
}

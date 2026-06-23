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
import io.getstream.chat.android.network.infrastructure.IsoDateAdapter
import io.getstream.chat.android.client.parser2.direct.DeviceAdapter
import io.getstream.chat.android.client.parser2.direct.PrivacySettingsAdapter
import io.getstream.chat.android.client.parser2.direct.UserAdapter
import io.getstream.chat.android.client.parser2.testdata.UserTestData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.UserTransformer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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

    private val moshi = Moshi.Builder().add(IsoDateAdapter()).build()
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

    @Test
    fun `DTO path - throws on null unread counts`() {
        assertThrows<JsonDataException> {
            parser.fromJson(UserTestData.jsonUnreadCountsNull, DownstreamUserDto::class.java)
        }
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

    @Test
    fun `Direct path - throws on null unread counts`() {
        assertThrows<JsonDataException> {
            userAdapter.fromJson(UserTestData.jsonUnreadCountsNull)
        }
    }

    // endregion

    // region Explicit nulls (JSON with explicit null values)

    @Test
    fun `DTO path - deserializes with explicit nulls`() {
        val dto = parser.fromJson(UserTestData.jsonWithExplicitNulls, DownstreamUserDto::class.java)
        val domain = with(domainMapping) { dto.toDomain() }
        assertEquals(UserTestData.expectedWithExplicitNulls, domain)
    }

    @Test
    fun `Direct path - deserializes with explicit nulls`() {
        val domain = userAdapter.fromJson(UserTestData.jsonWithExplicitNulls)
        assertEquals(UserTestData.expectedWithExplicitNulls, domain)
    }

    // endregion

    // region Error message parity

    @Test
    fun `DTO path - throws on missing id`() {
        assertThrows<JsonDataException> {
            parser.fromJson(UserTestData.jsonMissingId, DownstreamUserDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing id`() {
        assertThrows<JsonDataException> {
            userAdapter.fromJson(UserTestData.jsonMissingId)
        }
    }

    @Test
    fun `DTO path - throws on missing role`() {
        assertThrows<JsonDataException> {
            parser.fromJson(UserTestData.jsonMissingRole, DownstreamUserDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing role`() {
        assertThrows<JsonDataException> {
            userAdapter.fromJson(UserTestData.jsonMissingRole)
        }
    }

    @Test
    fun `DTO path - throws on missing banned`() {
        assertThrows<JsonDataException> {
            parser.fromJson(UserTestData.jsonMissingBanned, DownstreamUserDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing banned`() {
        assertThrows<JsonDataException> {
            userAdapter.fromJson(UserTestData.jsonMissingBanned)
        }
    }

    @Test
    fun `DTO path - throws on missing online`() {
        assertThrows<JsonDataException> {
            parser.fromJson(UserTestData.jsonMissingOnline, DownstreamUserDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing online`() {
        assertThrows<JsonDataException> {
            userAdapter.fromJson(UserTestData.jsonMissingOnline)
        }
    }

    // endregion

    // region Transformer parity (both paths must apply transformer identically)

    @Test
    fun `Both paths apply custom UserTransformer identically - all fields`() {
        val customTransformer = UserTransformer { it.copy(name = it.name + " [transformed]") }
        val transformedDomainMapping = DomainMapping(
            currentUserIdProvider = { "" },
            channelTransformer = NoOpChannelTransformer,
            messageTransformer = NoOpMessageTransformer,
            userTransformer = customTransformer,
        )
        val transformedUserAdapter = UserAdapter(
            deviceAdapter = deviceAdapter,
            privacySettingsAdapter = privacySettingsAdapter,
            dateAdapter = dateAdapter,
            userTransformer = customTransformer,
        )

        val dto = parser.fromJson(UserTestData.jsonAllFields, DownstreamUserDto::class.java)
        val dtoResult = with(transformedDomainMapping) { dto.toDomain() }
        val directResult = transformedUserAdapter.fromJson(UserTestData.jsonAllFields)

        assertEquals(dtoResult, directResult)
        assertTrue(dtoResult.name.endsWith(" [transformed]"))
    }

    // endregion
}

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
import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelUserRead
import io.getstream.chat.android.client.parser2.adapters.DateAdapter
import io.getstream.chat.android.client.parser2.event.ChannelUserReadAdapter
import io.getstream.chat.android.client.parser2.event.DeviceAdapter
import io.getstream.chat.android.client.parser2.event.PrivacySettingsAdapter
import io.getstream.chat.android.client.parser2.event.UserAdapter
import io.getstream.chat.android.client.parser2.testdata.ChannelUserReadTestData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Date

internal class ChannelUserReadParsingTest {

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
    private val adapter = ChannelUserReadAdapter(
        userAdapter = userAdapter,
        dateAdapter = dateAdapter,
        lastReceivedEventDate = ChannelUserReadTestData.lastReceivedEventDate,
    )

    // region DTO path (JSON → DownstreamChannelUserRead → ChannelUserRead)

    @Test
    fun `DTO path - deserializes all fields`() {
        val dto = parser.fromJson(ChannelUserReadTestData.jsonAllFields, DownstreamChannelUserRead::class.java)
        val domain = with(domainMapping) { dto.toDomain(ChannelUserReadTestData.lastReceivedEventDate) }
        assertEquals(ChannelUserReadTestData.expectedAllFields, domain)
    }

    @Test
    fun `DTO path - deserializes with optional fields missing`() {
        val dto = parser.fromJson(ChannelUserReadTestData.jsonOptionalFieldsMissing, DownstreamChannelUserRead::class.java)
        val domain = with(domainMapping) { dto.toDomain(ChannelUserReadTestData.lastReceivedEventDate) }
        assertEquals(ChannelUserReadTestData.expectedOptionalFieldsMissing, domain)
    }

    // endregion

    // region Direct path (JSON → ChannelUserRead via ChannelUserReadAdapter)

    @Test
    fun `Direct path - deserializes all fields`() {
        val domain = adapter.fromJson(ChannelUserReadTestData.jsonAllFields)
        assertEquals(ChannelUserReadTestData.expectedAllFields, domain)
    }

    @Test
    fun `Direct path - deserializes with optional fields missing`() {
        val domain = adapter.fromJson(ChannelUserReadTestData.jsonOptionalFieldsMissing)
        assertEquals(ChannelUserReadTestData.expectedOptionalFieldsMissing, domain)
    }

    // endregion

    // region Error message parity

    @Test
    fun `DTO path - throws on missing user`() {
        assertThrows<JsonDataException> {
            parser.fromJson(ChannelUserReadTestData.jsonMissingUser, DownstreamChannelUserRead::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing user`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(ChannelUserReadTestData.jsonMissingUser)
        }
    }

    @Test
    fun `DTO path - throws on missing last_read`() {
        assertThrows<JsonDataException> {
            parser.fromJson(ChannelUserReadTestData.jsonMissingLastRead, DownstreamChannelUserRead::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing last_read`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(ChannelUserReadTestData.jsonMissingLastRead)
        }
    }

    @Test
    fun `DTO path - throws on missing unread_messages`() {
        assertThrows<JsonDataException> {
            parser.fromJson(ChannelUserReadTestData.jsonMissingUnreadMessages, DownstreamChannelUserRead::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing unread_messages`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(ChannelUserReadTestData.jsonMissingUnreadMessages)
        }
    }

    // endregion
}

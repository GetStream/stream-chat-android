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
import io.getstream.chat.android.client.api2.model.dto.DownstreamVoteDto
import io.getstream.chat.android.client.parser2.adapters.DateAdapter
import io.getstream.chat.android.client.parser2.event.DeviceAdapter
import io.getstream.chat.android.client.parser2.event.PrivacySettingsAdapter
import io.getstream.chat.android.client.parser2.event.UserAdapter
import io.getstream.chat.android.client.parser2.event.VoteAdapter
import io.getstream.chat.android.client.parser2.testdata.VoteTestData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Date

internal class VoteParsingTest {

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
    private val adapter = VoteAdapter(userAdapter, dateAdapter)

    // region DTO path (JSON → DownstreamVoteDto → Vote)

    @Test
    fun `DTO path - deserializes all fields`() {
        val dto = parser.fromJson(VoteTestData.jsonAllFields, DownstreamVoteDto::class.java)
        val domain = with(domainMapping) { dto.toDomain() }
        assertEquals(VoteTestData.expectedAllFields, domain)
    }

    @Test
    fun `DTO path - deserializes with optional fields missing`() {
        val dto = parser.fromJson(VoteTestData.jsonOptionalFieldsMissing, DownstreamVoteDto::class.java)
        val domain = with(domainMapping) { dto.toDomain() }
        assertEquals(VoteTestData.expectedOptionalFieldsMissing, domain)
    }

    // endregion

    // region Direct path (JSON → Vote via VoteAdapter)

    @Test
    fun `Direct path - deserializes all fields`() {
        val domain = adapter.fromJson(VoteTestData.jsonAllFields)
        assertEquals(VoteTestData.expectedAllFields, domain)
    }

    @Test
    fun `Direct path - deserializes with optional fields missing`() {
        val domain = adapter.fromJson(VoteTestData.jsonOptionalFieldsMissing)
        assertEquals(VoteTestData.expectedOptionalFieldsMissing, domain)
    }

    // endregion

    // region Error message parity

    @Test
    fun `DTO path - throws on missing id`() {
        assertThrows<JsonDataException> {
            parser.fromJson(VoteTestData.jsonMissingId, DownstreamVoteDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing id`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(VoteTestData.jsonMissingId)
        }
    }

    @Test
    fun `DTO path - throws on missing poll_id`() {
        assertThrows<JsonDataException> {
            parser.fromJson(VoteTestData.jsonMissingPollId, DownstreamVoteDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing poll_id`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(VoteTestData.jsonMissingPollId)
        }
    }

    @Test
    fun `DTO path - throws on missing option_id`() {
        assertThrows<JsonDataException> {
            parser.fromJson(VoteTestData.jsonMissingOptionId, DownstreamVoteDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing option_id`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(VoteTestData.jsonMissingOptionId)
        }
    }

    @Test
    fun `DTO path - throws on missing created_at`() {
        assertThrows<JsonDataException> {
            parser.fromJson(VoteTestData.jsonMissingCreatedAt, DownstreamVoteDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing created_at`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(VoteTestData.jsonMissingCreatedAt)
        }
    }

    @Test
    fun `DTO path - throws on missing updated_at`() {
        assertThrows<JsonDataException> {
            parser.fromJson(VoteTestData.jsonMissingUpdatedAt, DownstreamVoteDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing updated_at`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(VoteTestData.jsonMissingUpdatedAt)
        }
    }

    // endregion
}

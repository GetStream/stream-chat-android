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
import io.getstream.chat.android.client.api2.model.dto.DownstreamReactionDto
import io.getstream.chat.android.client.parser2.adapters.DateAdapter
import io.getstream.chat.android.client.parser2.event.DeviceAdapter
import io.getstream.chat.android.client.parser2.event.PrivacySettingsAdapter
import io.getstream.chat.android.client.parser2.event.ReactionAdapter
import io.getstream.chat.android.client.parser2.event.UserAdapter
import io.getstream.chat.android.client.parser2.testdata.ReactionTestData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Date

internal class ReactionParsingTest {

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
    private val reactionAdapter = ReactionAdapter(
        userAdapter = userAdapter,
        dateAdapter = dateAdapter,
    )

    // region DTO path (JSON → DownstreamReactionDto → Reaction)

    @Test
    fun `DTO path - deserializes all fields`() {
        val dto = parser.fromJson(ReactionTestData.jsonAllFields, DownstreamReactionDto::class.java)
        val reaction = with(domainMapping) { dto.toDomain() }
        assertEquals(ReactionTestData.expectedAllFields, reaction)
    }

    @Test
    fun `DTO path - deserializes with optional fields missing`() {
        val dto = parser.fromJson(ReactionTestData.jsonOptionalFieldsMissing, DownstreamReactionDto::class.java)
        val reaction = with(domainMapping) { dto.toDomain() }
        assertEquals(ReactionTestData.expectedOptionalFieldsMissing, reaction)
    }

    // endregion

    // region Direct path (JSON → Reaction via ReactionAdapter)

    @Test
    fun `Direct path - deserializes all fields`() {
        val reaction = reactionAdapter.fromJson(ReactionTestData.jsonAllFields)
        assertEquals(ReactionTestData.expectedAllFields, reaction)
    }

    @Test
    fun `Direct path - deserializes with optional fields missing`() {
        val reaction = reactionAdapter.fromJson(ReactionTestData.jsonOptionalFieldsMissing)
        assertEquals(ReactionTestData.expectedOptionalFieldsMissing, reaction)
    }

    // endregion

    // region Error message parity (both paths must throw identical errors)

    @Test
    fun `Both paths - same error message on missing message_id`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(ReactionTestData.jsonMissingMessageId, DownstreamReactionDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            reactionAdapter.fromJson(ReactionTestData.jsonMissingMessageId)
        }
        assertEquals(dtoException.message, directException.message)
    }

    @Test
    fun `Both paths - same error message on missing type`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(ReactionTestData.jsonMissingType, DownstreamReactionDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            reactionAdapter.fromJson(ReactionTestData.jsonMissingType)
        }
        assertEquals(dtoException.message, directException.message)
    }

    @Test
    fun `Both paths - same error message on missing score`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(ReactionTestData.jsonMissingScore, DownstreamReactionDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            reactionAdapter.fromJson(ReactionTestData.jsonMissingScore)
        }
        assertEquals(dtoException.message, directException.message)
    }

    @Test
    fun `Both paths - same error message on missing user_id`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(ReactionTestData.jsonMissingUserId, DownstreamReactionDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            reactionAdapter.fromJson(ReactionTestData.jsonMissingUserId)
        }
        assertEquals(dtoException.message, directException.message)
    }

    // endregion
}

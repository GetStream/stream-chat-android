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
import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.parser2.testdata.ReactionTestData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.network.models.ReactionResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ReactionParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { "" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    // region DTO path (JSON → ReactionResponse → Reaction)

    @Test
    fun `DTO path - deserializes all fields`() {
        val dto = parser.fromJson(ReactionTestData.jsonAllFields, ReactionResponse::class.java)
        val reaction = with(domainMapping) { dto.toDomain() }
        assertEquals(ReactionTestData.expectedAllFields, reaction)
    }

    @Test
    fun `DTO path - deserializes with optional fields missing`() {
        val dto = parser.fromJson(ReactionTestData.jsonOptionalFieldsMissing, ReactionResponse::class.java)
        val reaction = with(domainMapping) { dto.toDomain() }
        assertEquals(ReactionTestData.expectedOptionalFieldsMissing, reaction)
    }

    // endregion

    // region Explicit null values

    @Test
    fun `DTO path - deserializes with explicit null values`() {
        val dto = parser.fromJson(ReactionTestData.jsonWithExplicitNulls, ReactionResponse::class.java)
        val reaction = with(domainMapping) { dto.toDomain() }
        assertEquals(ReactionTestData.expectedWithExplicitNulls, reaction)
    }

    // endregion

    // region Errors

    @Test
    fun `DTO path - throws on missing message_id`() {
        assertThrows<JsonDataException> {
            parser.fromJson(ReactionTestData.jsonMissingMessageId, ReactionResponse::class.java)
        }
    }

    @Test
    fun `DTO path - throws on missing type`() {
        assertThrows<JsonDataException> {
            parser.fromJson(ReactionTestData.jsonMissingType, ReactionResponse::class.java)
        }
    }

    @Test
    fun `DTO path - throws on missing score`() {
        assertThrows<JsonDataException> {
            parser.fromJson(ReactionTestData.jsonMissingScore, ReactionResponse::class.java)
        }
    }

    @Test
    fun `DTO path - throws on missing user_id`() {
        assertThrows<JsonDataException> {
            parser.fromJson(ReactionTestData.jsonMissingUserId, ReactionResponse::class.java)
        }
    }

    @Test
    fun `DTO path - throws on missing created_at`() {
        assertThrows<JsonDataException> {
            parser.fromJson(ReactionTestData.jsonMissingCreatedAt, ReactionResponse::class.java)
        }
    }

    @Test
    fun `DTO path - throws on missing updated_at`() {
        assertThrows<JsonDataException> {
            parser.fromJson(ReactionTestData.jsonMissingUpdatedAt, ReactionResponse::class.java)
        }
    }

    @Test
    fun `DTO path - throws on missing user`() {
        assertThrows<JsonDataException> {
            parser.fromJson(ReactionTestData.jsonMissingUser, ReactionResponse::class.java)
        }
    }

    // endregion
}

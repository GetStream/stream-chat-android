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
import io.getstream.chat.android.client.parser2.direct.ModerationAdapter
import io.getstream.chat.android.client.parser2.testdata.ModerationTestData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import io.getstream.chat.android.network.models.ModerationV2Response as DownstreamModerationDto

internal class ModerationParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { "" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    private val adapter = ModerationAdapter()

    // region DTO path (JSON → DownstreamModerationDto → Moderation)

    @Test
    fun `DTO path - deserializes all fields`() {
        val dto = parser.fromJson(ModerationTestData.jsonAllFields, DownstreamModerationDto::class.java)
        val domain = with(domainMapping) { dto.toDomain() }
        assertEquals(ModerationTestData.expectedAllFields, domain)
    }

    @Test
    fun `DTO path - deserializes with optional fields missing`() {
        val dto = parser.fromJson(ModerationTestData.jsonOptionalFieldsMissing, DownstreamModerationDto::class.java)
        val domain = with(domainMapping) { dto.toDomain() }
        assertEquals(ModerationTestData.expectedOptionalFieldsMissing, domain)
    }

    // endregion

    // region Direct path (JSON → Moderation via ModerationAdapter)

    @Test
    fun `Direct path - deserializes all fields`() {
        val domain = adapter.fromJson(ModerationTestData.jsonAllFields)
        assertEquals(ModerationTestData.expectedAllFields, domain)
    }

    @Test
    fun `Direct path - deserializes with optional fields missing`() {
        val domain = adapter.fromJson(ModerationTestData.jsonOptionalFieldsMissing)
        assertEquals(ModerationTestData.expectedOptionalFieldsMissing, domain)
    }

    // endregion

    // region Explicit null values

    @Test
    fun `DTO path - deserializes with explicit null values`() {
        val dto = parser.fromJson(ModerationTestData.jsonWithExplicitNulls, DownstreamModerationDto::class.java)
        val domain = with(domainMapping) { dto.toDomain() }
        assertEquals(ModerationTestData.expectedWithExplicitNulls, domain)
    }

    @Test
    fun `Direct path - deserializes with explicit null values`() {
        val domain = adapter.fromJson(ModerationTestData.jsonWithExplicitNulls)
        assertEquals(ModerationTestData.expectedWithExplicitNulls, domain)
    }

    // endregion

    // region Error message parity

    @Test
    fun `DTO path - throws on missing action`() {
        assertThrows<JsonDataException> {
            parser.fromJson(ModerationTestData.jsonMissingAction, DownstreamModerationDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing action`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(ModerationTestData.jsonMissingAction)
        }
    }

    @Test
    fun `DTO path - throws on missing original_text`() {
        assertThrows<JsonDataException> {
            parser.fromJson(ModerationTestData.jsonMissingOriginalText, DownstreamModerationDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing original_text`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(ModerationTestData.jsonMissingOriginalText)
        }
    }

    // endregion
}

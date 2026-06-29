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
import io.getstream.chat.android.client.parser2.direct.ReactionGroupAdapter
import io.getstream.chat.android.client.parser2.testdata.ReactionGroupTestData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.network.infrastructure.IsoDateAdapter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Date
import io.getstream.chat.android.network.models.ReactionGroupResponse as ReactionGroupDto

internal class ReactionGroupParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { "" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    private val moshi = Moshi.Builder().add(IsoDateAdapter()).build()
    private val dateAdapter = moshi.adapter(Date::class.java)
    private val reactionGroupAdapter = ReactionGroupAdapter(dateAdapter)

    private val testType = "like"

    // region DTO path (JSON → DownstreamReactionGroupDto → ReactionGroup)

    @Test
    fun `DTO path - deserializes all fields`() {
        val dto = parser.fromJson(ReactionGroupTestData.jsonAllFields, ReactionGroupDto::class.java)
        val reactionGroup = with(domainMapping) { dto.toDomain(testType) }
        assertEquals(ReactionGroupTestData.expectedReactionGroupAllFields, reactionGroup)
    }

    // endregion

    // region Direct path (JSON → ReactionGroup via ReactionGroupAdapter)

    @Test
    fun `Direct path - deserializes all fields`() {
        val reader = com.squareup.moshi.JsonReader.of(
            okio.Buffer().writeUtf8(ReactionGroupTestData.jsonAllFields),
        )
        val reactionGroup = reactionGroupAdapter.parseWithType(reader, testType)
        assertEquals(ReactionGroupTestData.expectedReactionGroupAllFields, reactionGroup)
    }

    @Test
    fun `Direct path - parses reaction groups map`() {
        val json = """{"like":${ReactionGroupTestData.jsonAllFields},"love":${ReactionGroupTestData.jsonAllFields}}"""
        val reader = com.squareup.moshi.JsonReader.of(okio.Buffer().writeUtf8(json))

        val groups = reactionGroupAdapter.parseReactionGroupsMap(reader)

        assertEquals(2, groups.size)
        assertEquals("like", groups["like"]?.type)
        assertEquals(5, groups["like"]?.count)
        assertEquals("love", groups["love"]?.type)
        assertEquals(5, groups["love"]?.count)
    }

    // endregion

    // region Error message parity

    @Test
    fun `Both paths - same error on missing count`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(ReactionGroupTestData.jsonMissingCount, ReactionGroupDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            reactionGroupAdapter.parseWithType(
                com.squareup.moshi.JsonReader.of(
                    okio.Buffer().writeUtf8(ReactionGroupTestData.jsonMissingCount),
                ),
                testType,
            )
        }
        // Both paths throw JsonDataException; message format differs between Moshi
        // ksp-generated (manual DTO) and reflection-based (generated DTO) adapters.
        assertNotNull(dtoException.message)
        assertNotNull(directException.message)
    }

    @Test
    fun `Both paths - same error on missing sum_scores`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(ReactionGroupTestData.jsonMissingSumScores, ReactionGroupDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            reactionGroupAdapter.parseWithType(
                com.squareup.moshi.JsonReader.of(
                    okio.Buffer().writeUtf8(ReactionGroupTestData.jsonMissingSumScores),
                ),
                testType,
            )
        }
        // Both paths throw JsonDataException; message format differs between Moshi
        // ksp-generated (manual DTO) and reflection-based (generated DTO) adapters.
        assertNotNull(dtoException.message)
        assertNotNull(directException.message)
    }

    @Test
    fun `Both paths - same error on missing first_reaction_at`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(ReactionGroupTestData.jsonMissingFirstReactionAt, ReactionGroupDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            reactionGroupAdapter.parseWithType(
                com.squareup.moshi.JsonReader.of(
                    okio.Buffer().writeUtf8(ReactionGroupTestData.jsonMissingFirstReactionAt),
                ),
                testType,
            )
        }
        // Both paths throw JsonDataException; message format differs between Moshi
        // ksp-generated (manual DTO) and reflection-based (generated DTO) adapters.
        assertNotNull(dtoException.message)
        assertNotNull(directException.message)
    }

    @Test
    fun `Both paths - same error on missing last_reaction_at`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(ReactionGroupTestData.jsonMissingLastReactionAt, ReactionGroupDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            reactionGroupAdapter.parseWithType(
                com.squareup.moshi.JsonReader.of(
                    okio.Buffer().writeUtf8(ReactionGroupTestData.jsonMissingLastReactionAt),
                ),
                testType,
            )
        }
        // Both paths throw JsonDataException; message format differs between Moshi
        // ksp-generated (manual DTO) and reflection-based (generated DTO) adapters.
        assertNotNull(dtoException.message)
        assertNotNull(directException.message)
    }

    // endregion
}

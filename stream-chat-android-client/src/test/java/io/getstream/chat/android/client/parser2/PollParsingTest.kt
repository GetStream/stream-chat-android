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
import io.getstream.chat.android.client.api2.model.dto.DownstreamPollDto
import io.getstream.chat.android.client.parser2.direct.DeviceAdapter
import io.getstream.chat.android.client.parser2.direct.OptionAdapter
import io.getstream.chat.android.client.parser2.direct.PollAdapter
import io.getstream.chat.android.client.parser2.direct.PrivacySettingsAdapter
import io.getstream.chat.android.client.parser2.direct.UserAdapter
import io.getstream.chat.android.client.parser2.testdata.PollTestData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.network.infrastructure.IsoDateAdapter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Date

internal class PollParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { null },
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
    private val optionAdapter = OptionAdapter()
    private val adapter = PollAdapter(
        userAdapter = userAdapter,
        optionAdapter = optionAdapter,
        dateAdapter = dateAdapter,
        currentUserIdProvider = { null },
    )

    // region DTO path (JSON → DownstreamPollDto → Poll)

    @Test
    fun `DTO path - deserializes all fields`() {
        val dto = parser.fromJson(PollTestData.jsonAllFields, DownstreamPollDto::class.java)
        val domain = with(domainMapping) { dto.toDomain() }
        assertEquals(PollTestData.expectedAllFields, domain)
    }

    @Test
    fun `DTO path - deserializes with optional fields missing`() {
        val dto = parser.fromJson(PollTestData.jsonOptionalFieldsMissing, DownstreamPollDto::class.java)
        val domain = with(domainMapping) { dto.toDomain() }
        assertEquals(PollTestData.expectedOptionalFieldsMissing, domain)
    }

    // endregion

    // region Direct path (JSON → Poll via PollAdapter)

    @Test
    fun `Direct path - deserializes all fields`() {
        val domain = adapter.fromJson(PollTestData.jsonAllFields)
        assertEquals(PollTestData.expectedAllFields, domain)
    }

    @Test
    fun `Direct path - deserializes with optional fields missing`() {
        val domain = adapter.fromJson(PollTestData.jsonOptionalFieldsMissing)
        assertEquals(PollTestData.expectedOptionalFieldsMissing, domain)
    }

    // endregion

    // region Explicit nulls (JSON with explicit null values)

    @Test
    fun `DTO path - deserializes with explicit nulls`() {
        val dto = parser.fromJson(PollTestData.jsonWithExplicitNulls, DownstreamPollDto::class.java)
        val domain = with(domainMapping) { dto.toDomain() }
        assertEquals(PollTestData.expectedWithExplicitNulls, domain)
    }

    @Test
    fun `Direct path - deserializes with explicit nulls`() {
        val domain = adapter.fromJson(PollTestData.jsonWithExplicitNulls)
        assertEquals(PollTestData.expectedWithExplicitNulls, domain)
    }

    // endregion

    // region Error message parity

    @Test
    fun `DTO path - throws on missing id`() {
        assertThrows<JsonDataException> {
            parser.fromJson(PollTestData.jsonMissingId, DownstreamPollDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing id`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(PollTestData.jsonMissingId)
        }
    }

    @Test
    fun `DTO path - throws on missing name`() {
        assertThrows<JsonDataException> {
            parser.fromJson(PollTestData.jsonMissingName, DownstreamPollDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing name`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(PollTestData.jsonMissingName)
        }
    }

    @Test
    fun `DTO path - throws on missing description`() {
        assertThrows<JsonDataException> {
            parser.fromJson(PollTestData.jsonMissingDescription, DownstreamPollDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing description`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(PollTestData.jsonMissingDescription)
        }
    }

    @Test
    fun `DTO path - throws on missing options`() {
        assertThrows<JsonDataException> {
            parser.fromJson(PollTestData.jsonMissingOptions, DownstreamPollDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing options`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(PollTestData.jsonMissingOptions)
        }
    }

    @Test
    fun `DTO path - throws on missing enforce_unique_vote`() {
        assertThrows<JsonDataException> {
            parser.fromJson(PollTestData.jsonMissingEnforceUniqueVote, DownstreamPollDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing enforce_unique_vote`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(PollTestData.jsonMissingEnforceUniqueVote)
        }
    }

    // endregion

    // region Malformed vote parity

    @Test
    fun `DTO path - throws on malformed vote missing id`() {
        assertThrows<JsonDataException> {
            parser.fromJson(PollTestData.jsonWithMalformedVoteMissingId, DownstreamPollDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on malformed vote missing id`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(PollTestData.jsonWithMalformedVoteMissingId)
        }
    }

    // endregion

    // region is_answer filtering tests

    @Test
    fun `DTO path - filters votes with is_answer=true and keeps only actual votes`() {
        val dto = parser.fromJson(PollTestData.jsonWithMixedVotesAndAnswers, DownstreamPollDto::class.java)
        val domain = with(domainMapping) { dto.toDomain() }

        // Verify only votes (is_answer != true) are in votes list
        assertEquals(2, domain.votes.size)
        assertEquals("vote1", domain.votes[0].id)
        assertEquals("vote2", domain.votes[1].id)

        // Verify answers are parsed separately
        assertEquals(2, domain.answers.size)
        assertEquals("answer1", domain.answers[0].id)
        assertEquals("Purple", domain.answers[0].text)
        assertEquals("answer2", domain.answers[1].id)
        assertEquals("Green", domain.answers[1].text)

        // Verify own_votes only contains votes (not answers)
        assertEquals(1, domain.ownVotes.size)
        assertEquals("vote1", domain.ownVotes[0].id)
    }

    @Test
    fun `Direct path - filters votes with is_answer=true and keeps only actual votes`() {
        val domain = adapter.fromJson(PollTestData.jsonWithMixedVotesAndAnswers)

        // Verify only votes (is_answer != true) are in votes list
        assertEquals(2, domain!!.votes.size)
        assertEquals("vote1", domain.votes[0].id)
        assertEquals("vote2", domain.votes[1].id)

        // Verify answers are parsed separately
        assertEquals(2, domain.answers.size)
        assertEquals("answer1", domain.answers[0].id)
        assertEquals("Purple", domain.answers[0].text)
        assertEquals("answer2", domain.answers[1].id)
        assertEquals("Green", domain.answers[1].text)

        // Verify own_votes only contains votes (not answers)
        assertEquals(1, domain.ownVotes.size)
        assertEquals("vote1", domain.ownVotes[0].id)
    }

    @Test
    fun `Both paths - produce identical results with mixed votes and answers`() {
        val dto = parser.fromJson(PollTestData.jsonWithMixedVotesAndAnswers, DownstreamPollDto::class.java)
        val domainFromDto = with(domainMapping) { dto.toDomain() }
        val domainFromDirect = adapter.fromJson(PollTestData.jsonWithMixedVotesAndAnswers)

        assertEquals(PollTestData.expectedMixedVotesAndAnswers, domainFromDto)
        assertEquals(PollTestData.expectedMixedVotesAndAnswers, domainFromDirect)
    }

    // endregion

    // region extraData tests

    @Test
    fun `DTO path - deserializes with extraData`() {
        val dto = parser.fromJson(PollTestData.jsonWithExtraData, DownstreamPollDto::class.java)
        val domain = with(domainMapping) { dto.toDomain() }
        assertEquals(PollTestData.expectedWithExtraData, domain)
    }

    @Test
    fun `Direct path - deserializes with extraData`() {
        val domain = adapter.fromJson(PollTestData.jsonWithExtraData)
        assertEquals(PollTestData.expectedWithExtraData, domain)
    }

    @Test
    fun `Both paths - produce identical results with extraData`() {
        val dto = parser.fromJson(PollTestData.jsonWithExtraData, DownstreamPollDto::class.java)
        val domainFromDto = with(domainMapping) { dto.toDomain() }
        val domainFromDirect = adapter.fromJson(PollTestData.jsonWithExtraData)

        assertEquals(PollTestData.expectedWithExtraData, domainFromDto)
        assertEquals(PollTestData.expectedWithExtraData, domainFromDirect)
    }

    // endregion
}

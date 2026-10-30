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
import io.getstream.chat.android.client.parser2.testdata.PollTestData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.network.infrastructure.Serializer
import io.getstream.chat.android.network.models.PollResponseData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PollParsingTest {

    private inline fun <reified T> parse(json: String): T = Serializer.moshi.adapter(T::class.java).fromJson(json)!!

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { null },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    @Test
    fun `Response path - deserializes with optional fields missing`() {
        val dto = parse<PollResponseData>(PollTestData.jsonOptionalFieldsMissing)
        val domain = with(domainMapping) { dto.toDomain() }
        assertEquals(PollTestData.expectedOptionalFieldsMissing, domain)
    }

    // endregion

    // region Explicit nulls (JSON with explicit null values)

    @Test
    fun `Response path - deserializes with explicit nulls`() {
        val dto = parse<PollResponseData>(PollTestData.jsonWithExplicitNulls)
        val domain = with(domainMapping) { dto.toDomain() }
        assertEquals(PollTestData.expectedWithExplicitNulls, domain)
    }

    // endregion

    // region Errors

    @Test
    fun `Response path - throws on missing id`() {
        assertThrows<JsonDataException> {
            parse<PollResponseData>(PollTestData.jsonMissingId)
        }
    }

    @Test
    fun `Response path - throws on missing name`() {
        assertThrows<JsonDataException> {
            parse<PollResponseData>(PollTestData.jsonMissingName)
        }
    }

    @Test
    fun `Response path - throws on missing description`() {
        assertThrows<JsonDataException> {
            parse<PollResponseData>(PollTestData.jsonMissingDescription)
        }
    }

    @Test
    fun `Response path - missing options is empty`() {
        val dto = parse<PollResponseData>(PollTestData.jsonMissingOptions)

        assertEquals(emptyList<Option>(), with(domainMapping) { dto.toDomain() }.options)
    }

    @Test
    fun `Response path - missing own_votes is empty`() {
        val dto = parse<PollResponseData>(PollTestData.jsonMissingOwnVotes)

        assertEquals(emptyList<Vote>(), with(domainMapping) { dto.toDomain() }.ownVotes)
    }

    @Test
    fun `Response path - throws on missing enforce_unique_vote`() {
        assertThrows<JsonDataException> {
            parse<PollResponseData>(PollTestData.jsonMissingEnforceUniqueVote)
        }
    }

    // endregion

    // region Malformed votes

    @Test
    fun `Response path - throws on malformed vote missing id`() {
        assertThrows<JsonDataException> {
            parse<PollResponseData>(PollTestData.jsonWithMalformedVoteMissingId)
        }
    }

    // endregion

    // region is_answer filtering tests

    @Test
    fun `Response path - filters votes with is_answer=true and keeps only actual votes`() {
        val dto = parse<PollResponseData>(PollTestData.jsonWithMixedVotesAndAnswers)
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

    // endregion

    // endregion
}

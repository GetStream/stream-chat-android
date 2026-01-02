/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.internal.offline.repository.domain.message.internal

import io.getstream.chat.android.models.Answer
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.models.VotingVisibility
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Date

internal class PollsMapperTest {

    private val testUserId = "user-123"
    private val testUser = User(
        id = testUserId,
        name = "Test User",
        image = "https://example.com/image.jpg",
    )

    private val now = Date()
    private val later = Date(now.time + 1000)

    @Test
    fun `Poll toEntity should convert domain model to entity correctly`() {
        val poll = Poll(
            id = "poll-123",
            name = "Test Poll",
            description = "Test Description",
            options = listOf(
                Option(id = "opt-1", text = "Option 1"),
                Option(id = "opt-2", text = "Option 2"),
            ),
            votingVisibility = VotingVisibility.PUBLIC,
            enforceUniqueVote = true,
            maxVotesAllowed = 1,
            allowUserSuggestedOptions = false,
            allowAnswers = true,
            voteCount = 5,
            voteCountsByOption = mapOf("opt-1" to 3, "opt-2" to 2),
            votes = emptyList(),
            ownVotes = emptyList(),
            createdAt = now,
            updatedAt = later,
            closed = false,
            answersCount = 0,
            answers = emptyList(),
            createdBy = testUser,
            extraData = mapOf("custom" to "value"),
        )

        val entity = poll.toEntity()

        assertEquals("poll-123", entity.id)
        assertEquals("Test Poll", entity.name)
        assertEquals("Test Description", entity.description)
        assertEquals(2, entity.options.size)
        assertEquals("public", entity.votingVisibility)
        assertTrue(entity.enforceUniqueVote)
        assertEquals(1, entity.maxVotesAllowed)
        assertEquals(false, entity.allowUserSuggestedOptions)
        assertTrue(entity.allowAnswers)
        assertEquals(5, entity.voteCount)
        assertEquals(mapOf("opt-1" to 3, "opt-2" to 2), entity.voteCountsByOption)
        assertEquals(now, entity.createdAt)
        assertEquals(later, entity.updatedAt)
        assertEquals(false, entity.closed)
        assertEquals(0, entity.answersCount)
        assertEquals(testUserId, entity.createdById)
        assertEquals(mapOf("custom" to "value"), entity.extraData)
    }

    @Suppress("LongMethod")
    @Test
    fun `PollEntity toModel should convert entity to domain model correctly`() = runTest {
        val options = listOf(
            OptionEntity(id = "opt-1", text = "Option 1", extraData = emptyMap()),
            OptionEntity(id = "opt-2", text = "Option 2", extraData = mapOf("key" to "value")),
        )
        val votes = listOf(
            VoteEntity(id = "vote-1", pollId = "poll-123", optionId = "opt-1", createdAt = now, updatedAt = now, userId = testUserId),
        )
        val ownVotes = listOf(
            VoteEntity(id = "own-vote-1", pollId = "poll-123", optionId = "opt-2", createdAt = now, updatedAt = now, userId = testUserId),
        )
        val answers = listOf(
            AnswerEntity(id = "ans-1", pollId = "poll-123", text = "Custom answer", createdAt = now, updatedAt = now, userId = testUserId),
        )
        val entity = PollEntity(
            id = "poll-123",
            name = "Test Poll",
            description = "Test Description",
            options = options,
            votingVisibility = "public",
            enforceUniqueVote = true,
            maxVotesAllowed = 1,
            allowUserSuggestedOptions = false,
            allowAnswers = true,
            voteCount = 5,
            voteCountsByOption = mapOf("opt-1" to 3, "opt-2" to 2),
            votes = votes,
            ownVotes = ownVotes,
            createdAt = now,
            updatedAt = later,
            closed = false,
            answersCount = 1,
            answers = answers,
            createdById = testUserId,
            extraData = mapOf("custom" to "value"),
        )

        val poll = entity.toModel { userId ->
            if (userId == testUserId) testUser else throw IllegalArgumentException("Unknown user")
        }

        assertEquals("poll-123", poll.id)
        assertEquals("Test Poll", poll.name)
        assertEquals("Test Description", poll.description)
        assertEquals(2, poll.options.size)
        assertEquals(VotingVisibility.PUBLIC, poll.votingVisibility)
        assertTrue(poll.enforceUniqueVote)
        assertEquals(1, poll.maxVotesAllowed)
        assertEquals(false, poll.allowUserSuggestedOptions)
        assertTrue(poll.allowAnswers)
        assertEquals(5, poll.voteCount)
        assertEquals(mapOf("opt-1" to 3, "opt-2" to 2), poll.voteCountsByOption)
        assertEquals(1, poll.votes.size)
        assertEquals(1, poll.ownVotes.size)
        assertEquals(now, poll.createdAt)
        assertEquals(later, poll.updatedAt)
        assertEquals(false, poll.closed)
        assertEquals(1, poll.answersCount)
        assertEquals(1, poll.answers.size)
        assertEquals(testUser, poll.createdBy)
        assertEquals(mapOf("custom" to "value"), poll.extraData)
    }

    @Test
    fun `Option toEntity should convert domain model to entity correctly`() {
        val option = Option(
            id = "opt-1",
            text = "Option 1",
            extraData = mapOf("key1" to "value1", "key2" to "value2"),
        )

        val entity = option.toEntity()

        assertEquals("opt-1", entity.id)
        assertEquals("Option 1", entity.text)
        assertEquals(mapOf("key1" to "value1", "key2" to "value2"), entity.extraData)
    }

    @Test
    fun `OptionEntity toModel should convert entity to domain model correctly`() {
        val entity = OptionEntity(
            id = "opt-1",
            text = "Option 1",
            extraData = mapOf("key" to "value"),
        )

        val option = entity.toModel()

        assertEquals("opt-1", option.id)
        assertEquals("Option 1", option.text)
        assertEquals(mapOf("key" to "value"), option.extraData)
    }

    @Test
    fun `Vote toEntity should convert domain model to entity correctly`() {
        val vote = Vote(
            id = "vote-1",
            optionId = "opt-1",
            pollId = "poll-1",
            createdAt = now,
            updatedAt = later,
            user = testUser,
        )

        val entity = vote.toEntity()

        assertEquals("vote-1", entity.id)
        assertEquals("opt-1", entity.optionId)
        assertEquals("poll-1", entity.pollId)
        assertEquals(now, entity.createdAt)
        assertEquals(later, entity.updatedAt)
        assertEquals(testUserId, entity.userId)
    }

    @Test
    fun `VoteEntity toModel should convert entity to domain model correctly`() = runTest {
        val entity = VoteEntity(
            id = "vote-1",
            pollId = "poll-1",
            optionId = "opt-1",
            createdAt = now,
            updatedAt = later,
            userId = testUserId,
        )

        val vote = entity.toModel { userId ->
            if (userId == testUserId) testUser else throw IllegalArgumentException("Unknown user")
        }

        assertEquals("vote-1", vote.id)
        assertEquals("poll-1", vote.pollId)
        assertEquals("opt-1", vote.optionId)
        assertEquals(now, vote.createdAt)
        assertEquals(later, vote.updatedAt)
        assertEquals(testUser, vote.user)
    }

    @Test
    fun `Answer toEntity should convert domain model to entity correctly`() {
        val answer = Answer(
            id = "ans-1",
            pollId = "poll-1",
            text = "Custom answer text",
            createdAt = now,
            updatedAt = later,
            user = testUser,
        )

        val entity = answer.toEntity()

        assertEquals("ans-1", entity.id)
        assertEquals("poll-1", entity.pollId)
        assertEquals("Custom answer text", entity.text)
        assertEquals(now, entity.createdAt)
        assertEquals(later, entity.updatedAt)
        assertEquals(testUserId, entity.userId)
    }

    @Test
    fun `AnswerEntity toModel should convert entity to domain model correctly`() = runTest {
        val entity = AnswerEntity(
            id = "ans-1",
            pollId = "poll-1",
            text = "Custom answer text",
            createdAt = now,
            updatedAt = later,
            userId = testUserId,
        )

        val answer = entity.toModel { userId ->
            if (userId == testUserId) testUser else throw IllegalArgumentException("Unknown user")
        }

        assertEquals("ans-1", answer.id)
        assertEquals("poll-1", answer.pollId)
        assertEquals("Custom answer text", answer.text)
        assertEquals(now, answer.createdAt)
        assertEquals(later, answer.updatedAt)
        assertEquals(testUser, answer.user)
    }

    @Test
    fun `VotingVisibility PUBLIC toEntity should return 'public'`() {
        val visibility = VotingVisibility.PUBLIC
        val result = visibility.toEntity()
        assertEquals("public", result)
    }

    @Test
    fun `VotingVisibility ANONYMOUS toEntity should return 'anonymous'`() {
        val visibility = VotingVisibility.ANONYMOUS
        val result = visibility.toEntity()
        assertEquals("anonymous", result)
    }

    @Test
    fun `String 'public' toVotingVisibility should return PUBLIC`() {
        val result = "public".toVotingVisibility()
        assertEquals(VotingVisibility.PUBLIC, result)
    }

    @Test
    fun `String 'anonymous' toVotingVisibility should return ANONYMOUS`() {
        val result = "anonymous".toVotingVisibility()
        assertEquals(VotingVisibility.ANONYMOUS, result)
    }

    @Test
    fun `String with unknown visibility toVotingVisibility should throw IllegalArgumentException`() {
        assertThrows(IllegalArgumentException::class.java) {
            "unknown".toVotingVisibility()
        }
    }

    @Test
    fun `Poll round-trip conversion should preserve all data`() = runTest {
        val originalPoll = Poll(
            id = "poll-roundtrip",
            name = "Round Trip Poll",
            description = "Testing round trip conversion",
            options = listOf(
                Option(id = "opt-1", text = "Option 1", extraData = emptyMap()),
                Option(id = "opt-2", text = "Option 2", extraData = mapOf("rating" to 5)),
            ),
            votingVisibility = VotingVisibility.PUBLIC,
            enforceUniqueVote = true,
            maxVotesAllowed = 2,
            allowUserSuggestedOptions = true,
            allowAnswers = false,
            voteCount = 10,
            voteCountsByOption = mapOf("opt-1" to 6, "opt-2" to 4),
            votes = listOf(
                Vote(id = "v-1", optionId = "opt-1", pollId = "poll-roundtrip", createdAt = now, updatedAt = now, user = testUser),
            ),
            ownVotes = listOf(
                Vote(id = "ov-1", optionId = "opt-2", pollId = "poll-roundtrip", createdAt = now, updatedAt = now, user = testUser),
            ),
            createdAt = now,
            updatedAt = later,
            closed = false,
            answersCount = 2,
            answers = listOf(
                Answer(id = "a-1", pollId = "poll-roundtrip", text = "Answer 1", createdAt = now, updatedAt = now, user = testUser),
            ),
            createdBy = testUser,
            extraData = mapOf("source" to "test"),
        )

        val entity = originalPoll.toEntity()
        val resultPoll = entity.toModel { userId ->
            if (userId == testUserId) testUser else throw IllegalArgumentException("Unknown user")
        }

        assertEquals(originalPoll.id, resultPoll.id)
        assertEquals(originalPoll.name, resultPoll.name)
        assertEquals(originalPoll.description, resultPoll.description)
        assertEquals(originalPoll.options.size, resultPoll.options.size)
        assertEquals(originalPoll.votingVisibility, resultPoll.votingVisibility)
        assertEquals(originalPoll.enforceUniqueVote, resultPoll.enforceUniqueVote)
        assertEquals(originalPoll.maxVotesAllowed, resultPoll.maxVotesAllowed)
        assertEquals(originalPoll.voteCount, resultPoll.voteCount)
        assertEquals(originalPoll.closed, resultPoll.closed)
        assertEquals(originalPoll.extraData, resultPoll.extraData)
    }

    // endregion
}

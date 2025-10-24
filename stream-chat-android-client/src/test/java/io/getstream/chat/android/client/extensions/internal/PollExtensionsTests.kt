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

package io.getstream.chat.android.client.extensions.internal

import io.getstream.chat.android.client.events.AnswerCastedEvent
import io.getstream.chat.android.client.events.PollClosedEvent
import io.getstream.chat.android.client.events.PollUpdatedEvent
import io.getstream.chat.android.client.events.VoteCastedEvent
import io.getstream.chat.android.client.events.VoteChangedEvent
import io.getstream.chat.android.client.events.VoteRemovedEvent
import io.getstream.chat.android.models.Answer
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.models.VotingVisibility
import io.getstream.chat.android.randomPoll
import io.getstream.chat.android.randomPollOption
import io.getstream.chat.android.randomPollVote
import io.getstream.chat.android.randomString
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import java.util.Date

internal class PollExtensionsTests {

    private val now = Date()
    private val user1 = User(id = "user1", name = "User 1")
    private val user2 = User(id = "user2", name = "User 2")
    private val option1 = Option(id = "option1", text = "Option 1")
    private val option2 = Option(id = "option2", text = "Option 2")
    private val vote1 = Vote(
        id = "vote1",
        pollId = "poll1",
        optionId = "option1",
        createdAt = now,
        updatedAt = now,
        user = user1,
    )
    private val vote2 = Vote(
        id = "vote2",
        pollId = "poll1",
        optionId = "option2",
        createdAt = now,
        updatedAt = now,
        user = user2,
    )
    private val answer1 = Answer(
        id = "answer1",
        pollId = "poll1",
        text = "Answer 1",
        createdAt = now,
        updatedAt = now,
        user = user1,
    )

    private val basePoll = Poll(
        id = "poll1",
        name = "Test Poll",
        description = "Test Description",
        options = listOf(option1, option2),
        votingVisibility = VotingVisibility.PUBLIC,
        enforceUniqueVote = true,
        maxVotesAllowed = 1,
        allowUserSuggestedOptions = false,
        allowAnswers = true,
        voteCount = 2,
        voteCountsByOption = mapOf("option1" to 1, "option2" to 1),
        votes = listOf(vote1, vote2),
        ownVotes = listOf(vote1),
        createdAt = now,
        updatedAt = now,
        closed = false,
        answersCount = 1,
        answers = listOf(answer1),
        createdBy = user1,
    )

    @Test
    fun `VoteChangedEvent processPoll should update ownVotes when current user is vote owner`() {
        // given
        val newVote = vote2.copy(user = user1)
        val event = VoteChangedEvent(
            type = "vote.changed",
            createdAt = now,
            rawCreatedAt = null,
            cid = "channel1",
            channelType = "messaging",
            channelId = "channel1",
            messageId = "message1",
            poll = basePoll,
            newVote = newVote,
        )

        // when
        val result = event.processPoll(currentUserId = "user1") { basePoll }

        // then
        result.ownVotes shouldBeEqualTo listOf(newVote)
        result.answers shouldBeEqualTo basePoll.answers
    }

    @Test
    fun `VoteChangedEvent processPoll should keep old ownVotes when current user is not vote owner`() {
        // given
        val newVote = vote2.copy(user = user2)
        val event = VoteChangedEvent(
            type = "vote.changed",
            createdAt = now,
            rawCreatedAt = null,
            cid = "channel1",
            channelType = "messaging",
            channelId = "channel1",
            messageId = "message1",
            poll = basePoll,
            newVote = newVote,
        )

        // when
        val result = event.processPoll(currentUserId = "user1") { basePoll }

        // then
        result.ownVotes shouldBeEqualTo basePoll.ownVotes
        result.answers shouldBeEqualTo basePoll.answers
    }

    @Test
    fun `VoteCastedEvent processPoll should add new vote to ownVotes when current user is vote owner`() {
        // given
        val newVote = Vote(
            id = "vote3",
            pollId = "poll1",
            optionId = "option1",
            createdAt = now,
            updatedAt = now,
            user = user1,
        )
        val event = VoteCastedEvent(
            type = "vote.casted",
            createdAt = now,
            rawCreatedAt = null,
            cid = "channel1",
            channelType = "messaging",
            channelId = "channel1",
            messageId = "message1",
            poll = basePoll,
            newVote = newVote,
        )

        // when
        val result = event.processPoll(currentUserId = "user1") { basePoll }

        // then
        result.ownVotes.size shouldBeEqualTo 2
        result.ownVotes.last() shouldBeEqualTo newVote
        result.answers shouldBeEqualTo basePoll.answers
    }

    @Test
    fun `VoteRemovedEvent processPoll should remove vote from ownVotes`() {
        // given
        val event = VoteRemovedEvent(
            type = "vote.removed",
            createdAt = now,
            rawCreatedAt = null,
            cid = "channel1",
            channelType = "messaging",
            channelId = "channel1",
            messageId = "message1",
            poll = basePoll,
            removedVote = vote1,
        )

        // when
        val result = event.processPoll { basePoll }

        // then
        result.ownVotes.size shouldBeEqualTo 0
        result.answers shouldBeEqualTo basePoll.answers
    }

    @Test
    fun `AnswerCastedEvent processPoll should add new answer to answers`() {
        // given
        val newAnswer = Answer(
            id = "answer2",
            pollId = "poll1",
            text = "Answer 2",
            createdAt = now,
            updatedAt = now,
            user = user2,
        )
        val event = AnswerCastedEvent(
            type = "answer.casted",
            createdAt = now,
            rawCreatedAt = null,
            cid = "channel1",
            channelType = "messaging",
            channelId = "channel1",
            messageId = "message1",
            poll = basePoll,
            newAnswer = newAnswer,
        )

        // when
        val result = event.processPoll { basePoll }

        // then
        result.answers.size shouldBeEqualTo 2
        result.answers.last() shouldBeEqualTo newAnswer
        result.ownVotes shouldBeEqualTo basePoll.ownVotes
    }

    @Test
    fun `PollClosedEvent processPoll should set closed to true`() {
        // given
        val event = PollClosedEvent(
            type = "poll.closed",
            createdAt = now,
            rawCreatedAt = null,
            cid = "channel1",
            channelType = "messaging",
            channelId = "channel1",
            messageId = "message1",
            poll = basePoll,
        )

        // when
        val result = event.processPoll { basePoll }

        // then
        result.closed shouldBeEqualTo true
    }

    @Test
    fun `PollUpdatedEvent processPoll should keep old ownVotes and answers`() {
        // given
        val event = PollUpdatedEvent(
            type = "poll.updated",
            createdAt = now,
            rawCreatedAt = null,
            cid = "channel1",
            channelType = "messaging",
            channelId = "channel1",
            messageId = "message1",
            poll = basePoll,
        )

        // when
        val result = event.processPoll { basePoll }

        // then
        result.ownVotes shouldBeEqualTo basePoll.ownVotes
        result.answers shouldBeEqualTo basePoll.answers
    }

    @Test
    fun `processPoll should handle null oldPoll`() {
        // given
        val event = VoteCastedEvent(
            type = "vote.casted",
            createdAt = now,
            rawCreatedAt = null,
            cid = "channel1",
            channelType = "messaging",
            channelId = "channel1",
            messageId = "message1",
            poll = basePoll,
            newVote = vote1,
        )

        // when
        val result = event.processPoll(currentUserId = "user1") { null }

        // then
        result.ownVotes.size shouldBeEqualTo 1
        result.ownVotes.first() shouldBeEqualTo vote1
        result.answers shouldBeEqualTo basePoll.answers
    }

    @Test
    fun `getVotesUnlessAnonymous returns votes when poll is not anonymous`() {
        val optionId = randomString()
        val option = randomPollOption(id = optionId)
        val votes = listOf(randomPollVote(optionId = optionId))
        val poll = randomPoll(
            options = listOf(option),
            votingVisibility = VotingVisibility.PUBLIC,
            votes = votes,
        )

        val result = poll.getVotesUnlessAnonymous(option)

        assertEquals(votes, result)
    }

    @Test
    fun `getVotesUnlessAnonymous returns empty list when poll is anonymous`() {
        val optionId = randomString()
        val option = randomPollOption(id = optionId)
        val poll = randomPoll(
            options = listOf(option),
            votingVisibility = VotingVisibility.ANONYMOUS,
            votes = listOf(randomPollVote(optionId = optionId)),
        )

        val result = poll.getVotesUnlessAnonymous(option)

        assertEquals(0, result.size)
    }

    @Test
    fun `getWinner returns the option with the highest votes when there is a single winner`() {
        val optionId1 = randomString()
        val optionId2 = randomString()
        val optionId3 = randomString()
        val options = listOf(
            randomPollOption(id = optionId1),
            randomPollOption(id = optionId2),
            randomPollOption(id = optionId3),
        )
        val voteCountsByOption = mapOf(optionId1 to 10, optionId2 to 5, optionId3 to 3)
        val poll = randomPoll(options = options, voteCountsByOption = voteCountsByOption)

        val winner = poll.getWinner()

        assertEquals(options[0], winner)
    }

    @Test
    fun `getWinner returns null when there is a tie for the highest votes`() {
        val optionId1 = randomString()
        val optionId2 = randomString()
        val options = listOf(
            randomPollOption(id = optionId1),
            randomPollOption(id = optionId2),
        )
        val voteCountsByOption = mapOf(optionId1 to 5, optionId2 to 5)
        val poll = randomPoll(options = options, voteCountsByOption = voteCountsByOption)

        val winner = poll.getWinner()

        assertNull(winner)
    }

    @Test
    fun `getWinner returns null when there are no votes`() {
        val poll = randomPoll()

        val winner = poll.getWinner()

        assertNull(winner)
    }
}

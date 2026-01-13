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

package io.getstream.chat.android.ui.common.feature.messages.poll

import app.cash.turbine.test
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.models.VotingVisibility
import io.getstream.chat.android.randomPoll
import io.getstream.chat.android.randomPollVote
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Date

internal class PollResultsViewControllerTest {

    @Test
    fun `when initialized, should sort options by vote count descending`() = runTest {
        val option1 = Option(id = "opt1", text = "Option 1")
        val option2 = Option(id = "opt2", text = "Option 2")
        val option3 = Option(id = "opt3", text = "Option 3")
        val poll = randomPoll(
            options = listOf(option1, option2, option3),
            voteCountsByOption = mapOf("opt1" to 5, "opt2" to 10, "opt3" to 3),
        )
        val sut = PollResultsViewController(poll)

        sut.state.test {
            val state = awaitItem()

            assertEquals("opt2", state.results[0].option.id) // Highest vote count
            assertEquals("opt1", state.results[1].option.id)
            assertEquals("opt3", state.results[2].option.id) // Lowest vote count
        }
    }

    @Test
    fun `when initialized, should mark option with highest vote count as winner`() = runTest {
        val option1 = Option(id = "opt1", text = "Option 1")
        val option2 = Option(id = "opt2", text = "Option 2")
        val option3 = Option(id = "opt3", text = "Option 3")
        val poll = randomPoll(
            options = listOf(option1, option2, option3),
            voteCountsByOption = mapOf("opt1" to 5, "opt2" to 10, "opt3" to 3),
        )
        val sut = PollResultsViewController(poll)

        sut.state.test {
            val state = awaitItem()

            assertTrue(state.results[0].isWinner) // option2 has highest vote count
            assertFalse(state.results[1].isWinner)
            assertFalse(state.results[2].isWinner)
        }
    }

    @Test
    fun `when multiple options have same vote count, should mark all as winners`() = runTest {
        val option1 = Option(id = "opt1", text = "Option 1")
        val option2 = Option(id = "opt2", text = "Option 2")
        val poll = randomPoll(
            options = listOf(option1, option2),
            voteCountsByOption = mapOf("opt1" to 5, "opt2" to 5),
        )
        val sut = PollResultsViewController(poll)

        sut.state.test {
            val state = awaitItem()

            // When vote counts are equal, getWinner() can be null; no option should be marked as winner.
            assertFalse(state.results.any { it.isWinner })
        }
    }

    @Test
    fun `when poll has votes, should show up to MAX_VOTES_TO_SHOW votes per option`() = runTest {
        val option1 = Option(id = "opt1", text = "Option 1")
        val user1 = randomUser(id = "user1")
        val user2 = randomUser(id = "user2")
        val user3 = randomUser(id = "user3")
        val user4 = randomUser(id = "user4")
        val user5 = randomUser(id = "user5")
        val user6 = randomUser(id = "user6")
        val now = Date()
        val votes = listOf(
            randomPollVote(pollId = "poll1", optionId = "opt1", user = user1, createdAt = now),
            randomPollVote(pollId = "poll1", optionId = "opt1", user = user2, createdAt = now),
            randomPollVote(pollId = "poll1", optionId = "opt1", user = user3, createdAt = now),
            randomPollVote(pollId = "poll1", optionId = "opt1", user = user4, createdAt = now),
            randomPollVote(pollId = "poll1", optionId = "opt1", user = user5, createdAt = now),
            randomPollVote(pollId = "poll1", optionId = "opt1", user = user6, createdAt = now),
        )
        val poll = randomPoll(
            id = "poll1",
            options = listOf(option1),
            voteCountsByOption = mapOf("opt1" to 6),
            votes = votes,
            votingVisibility = VotingVisibility.PUBLIC,
        )
        val sut = PollResultsViewController(poll)

        sut.state.test {
            val state = awaitItem()

            assertEquals(1, state.results.size)
            assertEquals(5, state.results[0].votes.size) // MAX_VOTES_TO_SHOW = 5
            assertEquals(6, state.results[0].voteCount) // Total vote count
        }
    }

    @Test
    fun `when option has more than MAX_VOTES_TO_SHOW votes, should show showAllButton`() = runTest {
        val option1 = Option(id = "opt1", text = "Option 1")
        val votes = (1..6).map { i ->
            randomPollVote(pollId = "poll1", optionId = "opt1", user = randomUser(id = "user$i"))
        }
        val poll = randomPoll(
            id = "poll1",
            options = listOf(option1),
            voteCountsByOption = mapOf("opt1" to 6),
            votes = votes,
            votingVisibility = VotingVisibility.PUBLIC,
        )
        val sut = PollResultsViewController(poll)

        sut.state.test {
            val state = awaitItem()

            assertTrue(state.results[0].showAllButton)
        }
    }

    @Test
    fun `when option has exactly MAX_VOTES_TO_SHOW votes, should not show showAllButton`() = runTest {
        val option1 = Option(id = "opt1", text = "Option 1")
        val votes = (1..5).map { i ->
            randomPollVote(pollId = "poll1", optionId = "opt1", user = randomUser(id = "user$i"))
        }
        val poll = randomPoll(
            id = "poll1",
            options = listOf(option1),
            voteCountsByOption = mapOf("opt1" to 5),
            votes = votes,
            votingVisibility = VotingVisibility.PUBLIC,
        )
        val sut = PollResultsViewController(poll)

        sut.state.test {
            val state = awaitItem()

            assertFalse(state.results[0].showAllButton)
        }
    }

    @Test
    fun `when poll has anonymous votes, should filter out anonymous votes from preview`() = runTest {
        val option1 = Option(id = "opt1", text = "Option 1")
        val user1 = randomUser(id = "user1")
        val now = Date()
        val votes = listOf(
            randomPollVote(pollId = "poll1", optionId = "opt1", user = user1, createdAt = now),
            Vote(
                id = "vote2",
                pollId = "poll1",
                optionId = "opt1",
                createdAt = now,
                updatedAt = now,
                user = null, // Anonymous vote
            ),
        )
        val poll = randomPoll(
            id = "poll1",
            options = listOf(option1),
            voteCountsByOption = mapOf("opt1" to 2),
            votes = votes,
            votingVisibility = VotingVisibility.ANONYMOUS,
        )
        val sut = PollResultsViewController(poll)

        sut.state.test {
            val state = awaitItem()

            // getVotesUnlessAnonymous hides votes entirely for anonymous polls.
            assertTrue(state.results[0].votes.isEmpty())
        }
    }

    @Test
    fun `when poll has multiple options with votes, should show correct vote counts for each`() = runTest {
        val option1 = Option(id = "opt1", text = "Option 1")
        val option2 = Option(id = "opt2", text = "Option 2")
        val option3 = Option(id = "opt3", text = "Option 3")
        val poll = randomPoll(
            options = listOf(option1, option2, option3),
            voteCountsByOption = mapOf("opt1" to 3, "opt2" to 7, "opt3" to 1),
        )
        val sut = PollResultsViewController(poll)

        sut.state.test {
            val state = awaitItem()

            assertEquals(7, state.results[0].voteCount) // option2
            assertEquals(3, state.results[1].voteCount) // option1
            assertEquals(1, state.results[2].voteCount) // option3
        }
    }

    @Test
    fun `when poll has no votes, should show empty vote lists`() = runTest {
        val option1 = Option(id = "opt1", text = "Option 1")
        val option2 = Option(id = "opt2", text = "Option 2")
        val poll = randomPoll(
            options = listOf(option1, option2),
            voteCountsByOption = mapOf("opt1" to 0, "opt2" to 0),
            votes = emptyList(),
        )
        val sut = PollResultsViewController(poll)

        sut.state.test {
            val state = awaitItem()

            assertEquals(2, state.results.size)
            assertTrue(state.results[0].votes.isEmpty())
            assertTrue(state.results[1].votes.isEmpty())
            assertFalse(state.results[0].isWinner)
            assertFalse(state.results[1].isWinner)
        }
    }

    @Test
    fun `when initialized, should set poll name correctly`() = runTest {
        val pollName = "Test Poll Name"
        val poll = randomPoll(name = pollName)
        val sut = PollResultsViewController(poll)

        sut.state.test {
            val state = awaitItem()

            assertEquals(pollName, state.pollName)
        }
    }

    @Test
    fun `when events are accessed, should not emit any events`() = runTest {
        val poll = randomPoll()
        val sut = PollResultsViewController(poll)

        sut.events.test {
            expectNoEvents()
        }
    }
}

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

package io.getstream.chat.android.compose.viewmodel.messages

import io.getstream.chat.android.models.VotingVisibility
import io.getstream.chat.android.randomOption
import io.getstream.chat.android.randomPoll
import io.getstream.chat.android.randomPollVote
import io.getstream.chat.android.ui.common.state.messages.poll.PollResultsViewState
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class PollResultsViewModelTest {

    @Test
    fun `state exposes the poll name and one result per option`() {
        val option1 = randomOption()
        val option2 = randomOption()
        val votes = List(3) { randomPollVote(optionId = option1.id) }
        val poll = randomPoll(
            options = listOf(option1, option2),
            votes = votes,
            voteCountsByOption = mapOf(option1.id to 3),
            votingVisibility = VotingVisibility.PUBLIC,
        )

        val viewModel = PollResultsViewModel(poll)

        val state = viewModel.state.value
        state.pollName `should be equal to` poll.name
        state.results `should be equal to` listOf(
            PollResultsViewState.ResultItem(
                option = option1,
                isWinner = true,
                voteCount = 3,
                votes = votes,
                showAllButton = false,
            ),
            PollResultsViewState.ResultItem(
                option = option2,
                isWinner = false,
                voteCount = 0,
                votes = emptyList(),
                showAllButton = false,
            ),
        )
    }

    @Test
    fun `show all button is shown when an option has more votes than the preview limit`() {
        val option = randomOption()
        val votes = List(6) { randomPollVote(optionId = option.id) }
        val poll = randomPoll(
            options = listOf(option),
            votes = votes,
            voteCountsByOption = mapOf(option.id to 6),
            votingVisibility = VotingVisibility.PUBLIC,
        )

        val viewModel = PollResultsViewModel(poll)

        val result = viewModel.state.value.results.single()
        result.votes `should be equal to` votes.take(5)
        result.showAllButton `should be equal to` true
    }

    @Test
    fun `anonymous poll hides the voters but keeps the vote count`() {
        val option = randomOption()
        val poll = randomPoll(
            options = listOf(option),
            votes = List(3) { randomPollVote(optionId = option.id) },
            voteCountsByOption = mapOf(option.id to 3),
            votingVisibility = VotingVisibility.ANONYMOUS,
        )

        val viewModel = PollResultsViewModel(poll)

        val result = viewModel.state.value.results.single()
        result.votes `should be equal to` emptyList()
        result.voteCount `should be equal to` 3
    }
}

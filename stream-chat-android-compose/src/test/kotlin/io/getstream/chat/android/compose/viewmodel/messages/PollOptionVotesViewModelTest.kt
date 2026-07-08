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

import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.QueryPollVotesResult
import io.getstream.chat.android.randomOption
import io.getstream.chat.android.randomPoll
import io.getstream.chat.android.randomPollVote
import io.getstream.chat.android.randomString
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.ui.common.feature.messages.poll.PollOptionVotesViewAction
import io.getstream.chat.android.ui.common.feature.messages.poll.PollOptionVotesViewController
import io.getstream.chat.android.ui.common.feature.messages.poll.PollOptionVotesViewEvent
import io.getstream.result.Error
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doReturnConsecutively
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock

@ExperimentalCoroutinesApi
@ExtendWith(TestCoroutineExtension::class)
internal class PollOptionVotesViewModelTest {

    private val option = randomOption()
    private val poll = randomPoll(
        options = listOf(option),
        voteCountsByOption = mapOf(option.id to 2),
    )

    @Test
    fun `initial votes are loaded for the option`() = runTest {
        val votes = List(2) { randomPollVote(pollId = poll.id, optionId = option.id) }
        val chatClient = mock<ChatClient> {
            on { queryPollVotes(eq(poll.id), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()) } doReturn
                QueryPollVotesResult(votes = votes, next = null).asCall()
        }

        val viewModel = viewModel(chatClient)

        val state = viewModel.state.value
        state.option `should be equal to` option
        state.voteCount `should be equal to` 2
        state.isWinner `should be equal to` true
        state.isLoading `should be equal to` false
        state.results `should be equal to` votes
        state.canLoadMore `should be equal to` false
    }

    @Test
    fun `load more requested appends the next page of votes`() = runTest {
        val firstPage = List(2) { randomPollVote(pollId = poll.id, optionId = option.id) }
        val secondPage = List(2) { randomPollVote(pollId = poll.id, optionId = option.id) }
        val chatClient = mock<ChatClient> {
            on { queryPollVotes(eq(poll.id), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()) } doReturnConsecutively
                listOf(
                    QueryPollVotesResult(votes = firstPage, next = randomString()).asCall(),
                    QueryPollVotesResult(votes = secondPage, next = null).asCall(),
                )
        }
        val viewModel = viewModel(chatClient)

        viewModel.onViewAction(PollOptionVotesViewAction.LoadMoreRequested)

        val state = viewModel.state.value
        state.results `should be equal to` firstPage + secondPage
        state.canLoadMore `should be equal to` false
        state.isLoadingMore `should be equal to` false
    }

    @Test
    fun `load error emits a load error event`() = runTest {
        val firstPage = List(2) { randomPollVote(pollId = poll.id, optionId = option.id) }
        val error = Error.GenericError(randomString())
        val chatClient = mock<ChatClient> {
            on { queryPollVotes(eq(poll.id), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()) } doReturnConsecutively
                listOf(
                    QueryPollVotesResult(votes = firstPage, next = randomString()).asCall(),
                    error.asCall<QueryPollVotesResult>(),
                )
        }
        val viewModel = viewModel(chatClient)

        viewModel.events.test {
            viewModel.onViewAction(PollOptionVotesViewAction.LoadMoreRequested)

            awaitItem() `should be equal to` PollOptionVotesViewEvent.LoadError(error)
        }
        viewModel.state.value.isLoadingMore `should be equal to` false
    }

    private fun viewModel(chatClient: ChatClient) = PollOptionVotesViewModel(
        poll = poll,
        option = option,
        controllerProvider = {
            PollOptionVotesViewController(
                poll = poll,
                option = option,
                chatClient = chatClient,
                scope = viewModelScope,
            )
        },
    )
}

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
import io.getstream.chat.android.models.QueryPollAnswersResult
import io.getstream.chat.android.randomPoll
import io.getstream.chat.android.randomPollAnswer
import io.getstream.chat.android.randomString
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.ui.common.feature.messages.poll.PollAnswersViewAction
import io.getstream.chat.android.ui.common.feature.messages.poll.PollAnswersViewController
import io.getstream.chat.android.ui.common.feature.messages.poll.PollAnswersViewEvent
import io.getstream.result.Error
import io.getstream.result.call.Call
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doReturnConsecutively
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import java.util.Date

@ExperimentalCoroutinesApi
@ExtendWith(TestCoroutineExtension::class)
internal class PollAnswersViewModelTest {

    private val poll = randomPoll()

    @Test
    fun `initial answers are loaded for the poll`() = runTest {
        val answers = answersNewestFirst(2)
        val chatClient = chatClient(QueryPollAnswersResult(answers = answers, next = null).asCall())

        val viewModel = viewModel(chatClient)

        val state = viewModel.state.value
        state.isLoading `should be equal to` false
        state.results `should be equal to` answers
        state.canLoadMore `should be equal to` false
    }

    @Test
    fun `load more requested appends the next page of answers`() = runTest {
        val answers = answersNewestFirst(4)
        val firstPage = answers.take(2)
        val secondPage = answers.drop(2)
        val chatClient = chatClient(
            QueryPollAnswersResult(answers = firstPage, next = randomString()).asCall(),
            QueryPollAnswersResult(answers = secondPage, next = null).asCall(),
        )
        val viewModel = viewModel(chatClient)

        viewModel.onViewAction(PollAnswersViewAction.LoadMoreRequested)

        val state = viewModel.state.value
        state.results `should be equal to` firstPage + secondPage
        state.canLoadMore `should be equal to` false
        state.isLoadingMore `should be equal to` false
    }

    @Test
    fun `load error emits a load error event`() = runTest {
        val error = Error.GenericError(randomString())
        val chatClient = chatClient(
            QueryPollAnswersResult(answers = listOf(randomPollAnswer()), next = randomString()).asCall(),
            error.asCall(),
        )
        val viewModel = viewModel(chatClient)

        viewModel.events.test {
            viewModel.onViewAction(PollAnswersViewAction.LoadMoreRequested)

            awaitItem() `should be equal to` PollAnswersViewEvent.LoadError(error)
        }
        viewModel.state.value.isLoadingMore `should be equal to` false
    }

    // The server lists answers newest first
    private fun answersNewestFirst(count: Int) = List(count) {
        randomPollAnswer(pollId = poll.id, createdAt = Date(1_000_000L - it * 1_000L))
    }

    private fun chatClient(vararg results: Call<QueryPollAnswersResult>) = mock<ChatClient> {
        on {
            queryPollAnswers(eq(poll.id), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
        } doReturnConsecutively results.toList()
        on { subscribe(any()) } doReturn mock()
    }

    private fun viewModel(chatClient: ChatClient) = PollAnswersViewModel(
        poll = poll,
        controllerProvider = {
            PollAnswersViewController(
                pollId = poll.id,
                chatClient = chatClient,
                scope = viewModelScope,
            )
        },
    )
}

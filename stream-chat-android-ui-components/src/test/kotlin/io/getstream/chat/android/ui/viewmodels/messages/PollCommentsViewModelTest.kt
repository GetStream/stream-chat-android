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

package io.getstream.chat.android.ui.viewmodels.messages

import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.state.StateRegistry
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.QueryPollAnswersResult
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomPoll
import io.getstream.chat.android.randomPollAnswer
import io.getstream.chat.android.test.InstantTaskExecutorExtension
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.test.observeAll
import io.getstream.chat.android.ui.common.feature.messages.poll.PollAnswersViewAction
import io.getstream.chat.android.ui.common.feature.messages.poll.PollAnswersViewController
import io.getstream.chat.android.ui.viewmodel.messages.PollCommentsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doReturnConsecutively
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
internal class PollCommentsViewModelTest {

    @Test
    fun `Given a message hosting a poll When observing Should emit the poll`() = runTest {
        val poll = randomPoll()
        val targetMessage = randomMessage(id = TARGET_MESSAGE_ID, poll = poll)
        val otherMessage = randomMessage(id = "other-message-id", poll = null)

        val viewModel = Fixture()
            .givenChannelMessages(listOf(otherMessage, targetMessage))
            .get()

        val emissions = viewModel.poll.observeAll()
        advanceUntilIdle()

        emissions shouldBeEqualTo listOf(poll)
    }

    @Test
    fun `Given the hosting message has no poll When observing Should not emit`() = runTest {
        val targetMessage = randomMessage(id = TARGET_MESSAGE_ID, poll = null)

        val viewModel = Fixture()
            .givenChannelMessages(listOf(targetMessage))
            .get()

        val emissions = viewModel.poll.observeAll()
        advanceUntilIdle()

        emissions.shouldBeEmpty()
    }

    @Test
    fun `Given the poll on the hosting message changes When observing Should emit each distinct poll`() = runTest {
        val firstPoll = randomPoll()
        val secondPoll = randomPoll()
        val fixture = Fixture()
            .givenChannelMessages(listOf(randomMessage(id = TARGET_MESSAGE_ID, poll = firstPoll)))
        val viewModel = fixture.get()

        val emissions = viewModel.poll.observeAll()
        advanceUntilIdle()

        fixture.givenChannelMessages(listOf(randomMessage(id = TARGET_MESSAGE_ID, poll = firstPoll)))
        advanceUntilIdle()

        fixture.givenChannelMessages(listOf(randomMessage(id = TARGET_MESSAGE_ID, poll = secondPoll)))
        advanceUntilIdle()

        emissions shouldBeEqualTo listOf(firstPoll, secondPoll)
    }

    @Test
    fun `Given the poll has answers When observing the answers Should emit the first page`() = runTest {
        val answers = answersNewestFirst(2)
        val viewModel = Fixture()
            .givenAnswerPages(QueryPollAnswersResult(answers = answers, next = null))
            .get()

        val emissions = viewModel.answersState.observeAll()
        advanceUntilIdle()

        emissions.last().results shouldBeEqualTo answers
        emissions.last().canLoadMore shouldBeEqualTo false
    }

    @Test
    fun `Given more answers When load more is requested Should append the next page`() = runTest {
        val answers = answersNewestFirst(4)
        val firstPage = answers.take(2)
        val secondPage = answers.drop(2)
        val viewModel = Fixture()
            .givenAnswerPages(
                QueryPollAnswersResult(answers = firstPage, next = "next"),
                QueryPollAnswersResult(answers = secondPage, next = null),
            )
            .get()
        val emissions = viewModel.answersState.observeAll()
        advanceUntilIdle()

        viewModel.onViewAction(PollAnswersViewAction.LoadMoreRequested)
        advanceUntilIdle()

        emissions.last().results shouldBeEqualTo firstPage + secondPage
    }

    // The server lists answers newest first
    private fun answersNewestFirst(count: Int) = List(count) {
        randomPollAnswer(pollId = POLL_ID, createdAt = Date(1_000_000L - it * 1_000L))
    }

    private class Fixture {
        private val messages = MutableStateFlow<List<Message>>(emptyList())
        private val channelState: ChannelState = mock {
            on { messages } doReturn this@Fixture.messages
        }
        private val state: StateRegistry = mock {
            on { channel(any(), any()) } doReturn channelState
        }
        private val chatClient: ChatClient = mock {
            on { subscribe(any()) } doReturn mock()
        }

        fun givenChannelMessages(messages: List<Message>) = apply {
            this.messages.value = messages
        }

        fun givenAnswerPages(vararg pages: QueryPollAnswersResult) = apply {
            whenever(
                chatClient.queryPollAnswers(eq(POLL_ID), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()),
            ) doReturnConsecutively pages.map { it.asCall() }
        }

        fun get(): PollCommentsViewModel = PollCommentsViewModel(
            cid = CID,
            messageId = TARGET_MESSAGE_ID,
            pollId = POLL_ID,
            state = state,
            controllerProvider = {
                PollAnswersViewController(pollId = POLL_ID, chatClient = chatClient, scope = viewModelScope)
            },
        )
    }

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()

        @JvmField
        @RegisterExtension
        val instantExecutor = InstantTaskExecutorExtension()

        private const val CID = "messaging:123"
        private const val TARGET_MESSAGE_ID = "target-message-id"
        private const val POLL_ID = "poll-id"
    }
}

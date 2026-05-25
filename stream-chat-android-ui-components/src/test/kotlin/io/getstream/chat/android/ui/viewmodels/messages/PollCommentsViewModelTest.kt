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

import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomPoll
import io.getstream.chat.android.client.api.state.StateRegistry
import io.getstream.chat.android.test.InstantTaskExecutorExtension
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.observeAll
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
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

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

    private class Fixture {
        private val messages = MutableStateFlow<List<Message>>(emptyList())
        private val channelState: ChannelState = mock {
            on { messages } doReturn this@Fixture.messages
        }
        private val state: StateRegistry = mock {
            on { channel(any(), any()) } doReturn channelState
        }

        fun givenChannelMessages(messages: List<Message>) = apply {
            this.messages.value = messages
        }

        fun get(): PollCommentsViewModel = PollCommentsViewModel(
            cid = CID,
            messageId = TARGET_MESSAGE_ID,
            state = state,
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
    }
}

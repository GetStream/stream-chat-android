/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.state.messages.list.MessageItemState
import io.getstream.chat.android.offline.model.channel.ChannelData
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.android.offline.plugin.state.channel.MessagesState
import io.getstream.chat.android.offline.plugin.state.global.internal.GlobalMutableState
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

internal class MessageListViewModelTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    @Test
    fun `Given message list When showing the message list Should update the messages state`() = runTest {
        val messageState = MessagesState.Result(
            messages = listOf(
                Message(id = "message-id-1", createdAt = Date()),
                Message(id = "message-id-2", createdAt = Date()),
            )
        )
        val viewModel = Fixture()
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState(messageState = messageState)
            .get()

        val messageItemCount = viewModel.currentMessagesState
            .messageItems
            .count { it is MessageItemState }
        messageItemCount `should be equal to` 2
    }

    private class Fixture(
        private val chatClient: ChatClient = mock(),
        private val channelId: String = "messaging:123",
    ) {
        private val globalState: GlobalMutableState = mock()
        private val stateRegistry: StateRegistry = mock()

        init {
            StateRegistry.instance = stateRegistry
            GlobalMutableState.instance = globalState
        }

        fun givenCurrentUser(currentUser: User = User(id = "Jc")) = apply {
            whenever(globalState.user) doReturn MutableStateFlow(currentUser)
        }

        fun givenChannelQuery(channel: Channel = Channel()) = apply {
            whenever(chatClient.queryChannel(any(), any(), any())) doReturn channel.asCall()
        }

        fun givenChannelState(
            channelData: ChannelData = ChannelData(
                type = "messaging",
                channelId = "123",
            ),
            messageState: MessagesState = MessagesState.Result(
                messages = emptyList()
            ),
        ) = apply {
            val channelState: ChannelState = mock {
                whenever(it.channelData) doReturn MutableStateFlow(channelData)
                whenever(it.channelConfig) doReturn MutableStateFlow(Config())
                whenever(it.members) doReturn MutableStateFlow(listOf())
                whenever(it.messagesState) doReturn MutableStateFlow(messageState)
                whenever(it.typing) doReturn MutableStateFlow(TypingEvent(channelId, emptyList()))
                whenever(it.reads) doReturn MutableStateFlow(listOf())
                whenever(it.endOfOlderMessages) doReturn MutableStateFlow(false)
            }
            whenever(stateRegistry.channel(any(), any())) doReturn channelState
        }

        fun get(): MessageListViewModel {
            return MessageListViewModel(
                chatClient = chatClient,
                channelId = channelId,
                clipboardHandler = mock(),
            )
        }
    }
}

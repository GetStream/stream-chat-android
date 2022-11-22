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
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessagesState
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.TypingEvent
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.state.plugin.state.global.internal.GlobalMutableState
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.ui.common.feature.messages.list.MessageListController
import io.getstream.chat.android.ui.common.state.messages.React
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not be`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

@ExperimentalCoroutinesApi
internal class MessageListViewModelTest {

    @Test
    fun `Given message list When showing the message list Should update the messages state`() = runTest {
        val messageState = MessagesState.Result(listOf(message1, message2))
        val viewModel = Fixture()
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState(messageState = messageState)
            .givenNotifications()
            .get()

        // Avoid counting date separators
        val messageItemCount = viewModel.currentMessagesState
            .messageItems
            .count { it is MessageItemState }
        messageItemCount `should be equal to` 2
    }

    @Test
    fun `Given message list When selecting a message Should show the selected message menu`() = runTest {
        val messageState = MessagesState.Result(listOf(message1, message2))
        val viewModel = Fixture()
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState(messageState = messageState)
            .givenNotifications()
            .get()

        viewModel.selectMessage(message1)

        val selectedMessageState = viewModel.currentMessagesState.selectedMessageState
        selectedMessageState `should not be` null
        selectedMessageState?.message `should be equal to` message1
        viewModel.isShowingOverlay `should be equal to` true
    }

    @Test
    fun `Given message list When sending a reaction Should send the reaction`() = runTest {
        val chatClient: ChatClient = mock()
        val messageState = MessagesState.Result(listOf(message1, message2))
        val viewModel = Fixture(chatClient)
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState(messageState = messageState)
            .givenNotifications()
            .givenSendReaction()
            .get()

        // Avoid counting date separators
        val messageItemCount = viewModel.currentMessagesState
            .messageItems
            .count { it is MessageItemState }
        messageItemCount `should be equal to` 2

        viewModel.performMessageAction(React(reaction1, message1))

        verify(chatClient).sendReaction(eq(reaction1), eq(true), eq(CID))
    }

    private class Fixture(
        private val chatClient: ChatClient = mock(),
        private val channelId: String = CID,
    ) {
        private val globalState: GlobalMutableState = mock()
        private val clientState: ClientState = mock()
        private val stateRegistry: StateRegistry = mock()

        init {
            StateRegistry.instance = stateRegistry
            GlobalMutableState.instance = globalState

            whenever(chatClient.clientState) doReturn clientState
        }

        fun givenCurrentUser(currentUser: User = user1) = apply {
            whenever(globalState.user) doReturn MutableStateFlow(currentUser)
        }

        fun givenChannelQuery(channel: Channel = Channel()) = apply {
            whenever(chatClient.queryChannel(any(), any(), any(), any())) doReturn channel.asCall()
        }

        fun givenNotifications() = apply {
            whenever(chatClient.notifications) doReturn mock()
        }

        fun givenSendReaction() = apply {
            whenever(chatClient.sendReaction(any(), any(), any())) doReturn Reaction().asCall()
        }

        fun givenChannelState(
            channelData: ChannelData = ChannelData(
                type = CHANNEL_TYPE,
                id = CHANNEL_ID,
            ),
            messageState: MessagesState = MessagesState.Result(
                messages = emptyList()
            ),
        ) = apply {
            val channelState: ChannelState = mock {
                whenever(it.cid) doReturn CID
                whenever(it.channelData) doReturn MutableStateFlow(channelData)
                whenever(it.channelConfig) doReturn MutableStateFlow(Config())
                whenever(it.members) doReturn MutableStateFlow(listOf())
                whenever(it.messagesState) doReturn MutableStateFlow(messageState)
                whenever(it.typing) doReturn MutableStateFlow(TypingEvent(channelId, emptyList()))
                whenever(it.reads) doReturn MutableStateFlow(listOf())
                whenever(it.endOfOlderMessages) doReturn MutableStateFlow(false)
                whenever(it.endOfNewerMessages) doReturn MutableStateFlow(true)
                whenever(it.toChannel()) doReturn Channel(type = CHANNEL_TYPE, id = CHANNEL_ID)
                whenever(it.unreadCount) doReturn MutableStateFlow(0)
                whenever(it.insideSearch) doReturn MutableStateFlow(false)
                whenever(it.loadingNewerMessages) doReturn MutableStateFlow(false)
                whenever(it.loadingOlderMessages) doReturn MutableStateFlow(false)
            }
            whenever(stateRegistry.channel(any(), any())) doReturn channelState
            whenever(stateRegistry.scope) doReturn testCoroutines.scope
        }

        fun get(): MessageListViewModel {
            return MessageListViewModel(
                MessageListController(
                    chatClient = chatClient,
                    cid = channelId,
                    clipboardHandler = mock(),
                )

            )
        }
    }

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()

        private const val CHANNEL_TYPE = "messaging"
        private const val CHANNEL_ID = "123"
        private const val CID = "messaging:123"

        private val user1 = User(id = "Jc", name = "Jc Miñarro")
        private val message1 = Message(id = "message-id-1", createdAt = Date())
        private val message2 = Message(id = "message-id-2", createdAt = Date())
        private val reaction1 = Reaction("message-id-1", "like", 1).apply { user = user1 }
    }
}

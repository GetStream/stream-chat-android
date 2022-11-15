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

package io.getstream.chat.android.ui.viewmodels.messages

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelData
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.MessagesState
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.state.plugin.state.global.internal.GlobalMutableState
import io.getstream.chat.android.test.InstantTaskExecutorExtension
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.test.observeAll
import io.getstream.chat.android.ui.MockChatClientBuilder
import io.getstream.chat.android.ui.common.feature.messages.list.MessageListController
import io.getstream.chat.android.ui.common.state.messages.list.MessagePosition
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.android.ui.model.MessageListItemWrapper
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
internal class MessageListViewModelTest {

    @JvmField
    @RegisterExtension
    val instantExecutorExtension: InstantTaskExecutorExtension = InstantTaskExecutorExtension()

    @Test
    fun `Given initial state remains unchanged Should be in loading state`() = runTest {
        val viewModel = Fixture()
            .givenCurrentUser()
            .givenChannelQuery()
            .givenNotifications()
            .get()

        val state = viewModel.state.observeAll()

        state.last() shouldBeEqualTo MessageListViewModel.State.Loading
    }

    @Test
    fun `Given message list When showing the message list Should update the messages state`() = runTest {
        val messageStartDate = Date()
        val messages = createMessageBatch(amount = 2, firstMessageDate = messageStartDate)
        val messageItems = messages.toMessageItemList()
        val messageState = MessagesState.Result(messages)

        val viewModel = Fixture()
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState(messageState = messageState, messages = messages)
            .givenNotifications()
            .get()

        val state = viewModel.state.observeAll()

        val expectedState = MessageListViewModel.State.Result(
            messageListItem = MessageListItemWrapper(
                items = listOf(
                    MessageListItem.DateSeparatorItem(messageStartDate),
                ) + messageItems,
                hasNewMessages = true,
                isTyping = false,
                isThread = false,
                areNewestMessagesLoaded = true,
            )
        )

        state.last() shouldBeEqualTo expectedState
    }

    @Test
    fun `Given populated message list When hard deleting a message Should hard delete message`() = runTest {
        val messageToDelete = message1
        val messages = listOf(messageToDelete, message2)
        val messageState = MessagesState.Result(messages)
        val chatClient = MockChatClientBuilder().build()

        val viewModel = Fixture(chatClient = chatClient)
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState(messageState = messageState, messages = messages)
            .givenDeleteMessage()
            .givenNotifications()
            .get()

        viewModel.onEvent(MessageListViewModel.Event.DeleteMessage(message = messageToDelete, hard = true))

        verify(chatClient).deleteMessage(messageId = messageToDelete.id, hard = true)
    }

    @Test
    fun `Given populated message list When deleting a message Should soft delete message`() = runTest {
        val messageToDelete = message1
        val messages = listOf(messageToDelete, message2)
        val messageState = MessagesState.Result(messages)
        val chatClient = MockChatClientBuilder().build()

        val viewModel = Fixture(chatClient = chatClient)
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState(messageState = messageState, messages = messages)
            .givenDeleteMessage()
            .givenNotifications()
            .get()

        viewModel.onEvent(MessageListViewModel.Event.DeleteMessage(message = messageToDelete))

        verify(chatClient).deleteMessage(messageId = messageToDelete.id, hard = false)
    }

    @Test
    fun `Given populated message list When flagging a message Should flag message`() = runTest {
        val messageToFlag = message1
        val messages = listOf(messageToFlag, message2)
        val messageState = MessagesState.Result(messages)
        val chatClient = MockChatClientBuilder().build()

        val viewModel = Fixture(chatClient = chatClient)
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState(messageState = messageState, messages = messages)
            .givenFlagMessage(message = messageToFlag)
            .givenNotifications()
            .get()

        viewModel.onEvent(MessageListViewModel.Event.FlagMessage(messageToFlag))

        verify(chatClient).flagMessage(messageId = messageToFlag.id)
    }

    @Test
    fun `Given no previous own reactions on a message When leaving a reaction Should leave reaction`() = runTest {
        val messages = listOf(message1, message2)
        val messageState = MessagesState.Result(messages)
        val chatClient = MockChatClientBuilder().build()

        val viewModel = Fixture(chatClient = chatClient)
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState(messageState = messageState, messages = messages)
            .givenSendReaction()
            .givenNotifications()
            .get()

        viewModel.onEvent(
            MessageListViewModel.Event.MessageReaction(
                message = message1,
                reactionType = reaction1.type,
            )
        )

        verify(chatClient).sendReaction(reaction = reaction1, enforceUnique = true, CID)
    }

    @Test
    fun `Given identical previous own reaction on a message When choosing the same one again Should delete reaction`() =
        runTest {
            val messageWithOwnReaction = message1.copy().apply { ownReactions = mutableListOf(reaction1) }
            val messages = listOf(messageWithOwnReaction, message2)
            val messageState = MessagesState.Result(messages)
            val chatClient = MockChatClientBuilder().build()

            val viewModel = Fixture(chatClient = chatClient)
                .givenCurrentUser()
                .givenChannelQuery()
                .givenChannelState(messageState = messageState, messages = messages)
                .givenDeleteReaction()
                .givenNotifications()
                .get()

            viewModel.onEvent(
                MessageListViewModel.Event.MessageReaction(
                    message = messageWithOwnReaction,
                    reactionType = reaction1.type,
                )
            )

            verify(chatClient).deleteReaction(
                messageId = messageWithOwnReaction.id,
                reactionType = reaction1.type,
                cid = CID
            )
        }

    private class Fixture(
        private val chatClient: ChatClient = MockChatClientBuilder().build(),
        private val channelId: String = CID,
    ) {
        private val globalState: GlobalMutableState = mock()
        private val stateRegistry: StateRegistry = mock()
        private val clientState: ClientState = mock()

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

        fun givenFlagMessage(message: Message) = apply {
            whenever(chatClient.flagMessage(message.id)) doReturn mock()
        }

        fun givenDeleteMessage() = apply {
            whenever(chatClient.deleteMessage(any(), any())) doReturn Message().asCall()
        }

        fun givenSendReaction() = apply {
            whenever(chatClient.sendReaction(any(), any(), any())) doReturn Reaction().asCall()
        }

        fun givenDeleteReaction() = apply {
            whenever(chatClient.deleteReaction(any(), any(), any())) doReturn Message().asCall()
        }

        fun givenChannelState(
            channelData: ChannelData = ChannelData(
                type = CHANNEL_TYPE,
                id = CHANNEL_ID,
            ),
            messageState: MessagesState = MessagesState.Result(
                messages = emptyList()
            ),
            messages: List<Message> = listOf(),
        ) = apply {
            val channelState: ChannelState = mock {
                whenever(it.cid) doReturn CID
                whenever(it.channelId) doReturn CHANNEL_ID
                whenever(it.channelType) doReturn CHANNEL_TYPE
                whenever(it.messages) doReturn MutableStateFlow(messages)
                whenever(it.channelData) doReturn MutableStateFlow(channelData)
                whenever(it.channelConfig) doReturn MutableStateFlow(Config())
                whenever(it.members) doReturn MutableStateFlow(listOf())
                whenever(it.messagesState) doReturn MutableStateFlow(messageState)
                whenever(it.typing) doReturn MutableStateFlow(TypingEvent(channelId, emptyList()))
                whenever(it.reads) doReturn MutableStateFlow(listOf())
                whenever(it.insideSearch) doReturn MutableStateFlow(false)
                whenever(it.endOfOlderMessages) doReturn MutableStateFlow(false)
                whenever(it.loadingOlderMessages) doReturn MutableStateFlow(false)
                whenever(it.toChannel()) doReturn Channel(type = CHANNEL_TYPE, id = CHANNEL_ID)
                whenever(it.endOfNewerMessages) doReturn MutableStateFlow(true)
                whenever(it.loading) doReturn MutableStateFlow(true)
                whenever(it.unreadCount) doReturn MutableStateFlow(0)
                whenever(it.loadingNewerMessages) doReturn MutableStateFlow(false)
            }
            whenever(stateRegistry.channel(any(), any())) doReturn channelState
            whenever(stateRegistry.scope) doReturn testCoroutines.scope
        }

        fun get(): MessageListViewModel {
            return MessageListViewModel(
                MessageListController(
                    chatClient = chatClient,
                    cid = channelId,
                    clipboardHandler = mock()
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
        private val reaction1 = Reaction("message-id-1", "like", 1)

        /**
         * Useful for creating message batches with a known
         * start date and interval making the date separator
         * placement predictable and testable.
         */
        private fun createMessageBatch(
            amount: Int,
            firstMessageDate: Date = Date(),
            periodBetweenMessagesInSeconds: Int = 1,
        ): List<Message> {
            val messages = mutableListOf<Message>()

            for (i in 0 until amount) {
                val date = Date(firstMessageDate.time + periodBetweenMessagesInSeconds * i * 1000)

                val message = Message(id = "message-id-$i", createdAt = date)

                messages.add(message)
            }

            return messages
        }

        /**
         * An approximation of the private grouping logic inside
         *
         * Works in simple situations where all the messages are from the
         * same user but should not be used without modification in situations
         * where more complex scenarios are present.
         */
        private fun List<Message>.toMessageItemList(): List<MessageListItem.MessageItem> {
            return this.mapIndexed { index, message ->
                val position = when {
                    index == 0 -> MessagePosition.TOP
                    this.size > 2 && index != this.size -> MessagePosition.MIDDLE
                    else -> MessagePosition.BOTTOM
                }

                MessageListItem.MessageItem(
                    message = message,
                    showMessageFooter = index == this.size - 1,
                    positions = listOf(position),
                    isMessageRead = false
                )
            }
        }
    }
}

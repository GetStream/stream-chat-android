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

package io.getstream.chat.android.ui.viewmodels

import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.offline.model.channel.ChannelData
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.android.offline.plugin.state.channel.MessagesState
import io.getstream.chat.android.offline.plugin.state.global.internal.GlobalMutableState
import io.getstream.chat.android.test.InstantTaskExecutorExtension
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.ui.MockChatClientBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
internal class MessageInputViewModelTest {

    @JvmField
    @RegisterExtension
    val instantExecutorExtension: InstantTaskExecutorExtension = InstantTaskExecutorExtension()

    @Test
    fun `Given active thread mode When sending a message the message Should contained parentId`() = runTest {
        val chatClient = MockChatClientBuilder().build()

        val originalThreadMessage = message1
        val messageList = listOf(originalThreadMessage, message2)

        val newMessage = Message(text = "Hey.", cid = CID)

        val viewModel = Fixture(chatClient = chatClient)
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState(messageState = MessagesState.Result(messageList))
            .givenStopTyping()
            .givenSendMessage()
            .get()

        viewModel.setActiveThread(originalThreadMessage)
        viewModel.sendMessage(messageText = newMessage.text)

        verify(chatClient).sendMessage(
            channelType = CHANNEL_TYPE,
            channelId = CHANNEL_ID,
            message = newMessage.copy(
                parentId = originalThreadMessage.id
            )
        )
    }

    @Test
    fun `Given message text contains @ followed by a text that is not a selected mention When sending the message Should not contain mentions`() =
        runTest {
            val chatClient = MockChatClientBuilder().build()
            val message = Message(cid = CID, text = "Text with an @sign that should not be treated as a mention")

            val viewModel = Fixture(chatClient = chatClient)
                .givenCurrentUser()
                .givenChannelQuery()
                .givenChannelState()
                .givenSendMessage()
                .givenStopTyping()
                .get()

            viewModel.sendMessage(messageText = message.text)

            verify(chatClient).sendMessage(
                message = message,
                channelType = CHANNEL_TYPE,
                channelId = CHANNEL_ID
            )
        }

    @Test
    fun `Given a selected mention When sending the message Should not contain mention`() =
        runTest {
            val chatClient = MockChatClientBuilder().build()
            val mentionedUser = User(id = "user")
            val message = Message(cid = CID, text = "Hey @user", mentionedUsersIds = mutableListOf("user"))

            val viewModel = Fixture(chatClient = chatClient)
                .givenCurrentUser()
                .givenChannelQuery()
                .givenChannelState()
                .givenSendMessage()
                .givenStopTyping()
                .get()

            viewModel.selectMention(mentionedUser)
            viewModel.sendMessage(messageText = message.text)

            verify(chatClient).sendMessage(
                message = message,
                channelType = CHANNEL_TYPE,
                channelId = CHANNEL_ID
            )
        }

    @Test
    fun `Given attachments have been selected When sending the message Should contain attachments`() = runTest {
        val chatClient = MockChatClientBuilder().build()
        val file = File("testPathname")
        val mimeType = "testMimeType"

        val attachment = Attachment(mimeType = mimeType, upload = file)
        val newMessage = Message(text = "Hey.", cid = CID, attachments = mutableListOf(attachment))

        val viewModel = Fixture(chatClient = chatClient)
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState()
            .givenStopTyping()
            .givenSendMessage()
            .get()

        viewModel.sendMessageWithAttachments(
            messageText = newMessage.text,
            attachmentsWithMimeTypes = listOf(
                Pair(file, mimeType)
            )
        )

        verify(chatClient).sendMessage(
            channelType = CHANNEL_TYPE,
            channelId = CHANNEL_ID,
            message = newMessage
        )
    }

    private class Fixture(
        private val chatClient: ChatClient = MockChatClientBuilder().build(),
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
            whenever(clientState.user) doReturn MutableStateFlow(currentUser)
        }

        fun givenChannelQuery(channel: Channel = Channel()) = apply {
            whenever(chatClient.queryChannel(any(), any(), any())) doReturn channel.asCall()
            whenever(chatClient.queryChannelInternal(any(), any(), any())) doReturn channel.asCall()
        }

        fun givenSendMessage() = apply {
            whenever(
                chatClient.sendMessage(
                    channelType = any(),
                    channelId = any(),
                    message = any(),
                    isRetrying = any(),
                    skipPush = any(),
                    skipEnrichUrl = any(),
                )
            ) doReturn Message().asCall()
        }

        fun givenStopTyping() = apply {
            whenever(chatClient.stopTyping(any(), any(), anyOrNull())) doReturn mock()
        }

        fun givenChannelState(
            channelData: ChannelData = ChannelData(
                type = CHANNEL_TYPE,
                channelId = CHANNEL_ID,
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
                whenever(it.toChannel()) doReturn Channel(
                    type = CHANNEL_TYPE,
                    id = CHANNEL_ID
                )
            }
            whenever(stateRegistry.channel(any(), any())) doReturn channelState
            whenever(stateRegistry.scope) doReturn testCoroutines.scope
        }

        fun get(): MessageInputViewModel {
            return MessageInputViewModel(
                chatClient = chatClient,
                cid = channelId,
            )
        }
    }

    internal companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()

        private const val CHANNEL_TYPE = "messaging"
        private const val CHANNEL_ID = "123"
        private const val CID = "messaging:123"

        private val user1 = User(id = "Jc", name = "Jc Miñarro")

        private val message1 = Message(id = "message-id-1", createdAt = Date())
        private val message2 = Message(id = "message-id-2", createdAt = Date())
    }
}

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

import com.getstream.sdk.chat.utils.AttachmentConstants
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.common.composer.MessageComposerController
import io.getstream.chat.android.common.state.Edit
import io.getstream.chat.android.common.state.MessageMode
import io.getstream.chat.android.common.state.Reply
import io.getstream.chat.android.common.state.ThreadReply
import io.getstream.chat.android.offline.model.channel.ChannelData
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.android.offline.plugin.state.global.internal.GlobalMutableState
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be instance of`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
@ExtendWith(TestCoroutineExtension::class)
internal class MessageComposerViewModelTest {

    @Test
    fun `Given message composer When typing a message Should display the message`() = runTest {
        val viewModel = Fixture()
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState()
            .get()

        viewModel.setMessageInput("Message text")

        val messageComposerState = viewModel.messageComposerState.value
        messageComposerState.inputValue `should be equal to` "Message text"
        viewModel.input.value `should be equal to` "Message text"
    }

    @Test
    fun `Given message composer When typing and sending a message Should send the message and clear the input`() =
        runTest {
            val chatClient: ChatClient = mock()
            val viewModel = Fixture(chatClient = chatClient)
                .givenCurrentUser()
                .givenChannelQuery()
                .givenChannelState()
                .givenSendMessage()
                .get()

            viewModel.setMessageInput("Message text")
            val state = viewModel.messageComposerState.value
            viewModel.sendMessage(
                viewModel.buildNewMessage(
                    message = state.inputValue,
                    attachments = state.attachments
                )
            )

            val captor = argumentCaptor<Message>()
            verify(chatClient).sendMessage(
                channelType = eq("messaging"),
                channelId = eq("123"),
                message = captor.capture(),
                isRetrying = eq(false)
            )
            captor.firstValue.text `should be equal to` "Message text"
            viewModel.input.value `should be equal to` ""
        }

    @Test
    fun `Given message composer When selecting attachments and sending the message Should send the message with attachments and clear the input`() =
        runTest {
            val chatClient: ChatClient = mock()
            val viewModel = Fixture(chatClient = chatClient)
                .givenCurrentUser()
                .givenChannelQuery()
                .givenChannelState()
                .givenSendMessage()
                .get()

            viewModel.addSelectedAttachments(
                listOf(
                    Attachment(imageUrl = "url1"),
                    Attachment(imageUrl = "url2")
                )
            )
            val state = viewModel.messageComposerState.value
            viewModel.sendMessage(
                viewModel.buildNewMessage(
                    message = state.inputValue,
                    attachments = state.attachments
                )
            )

            val captor = argumentCaptor<Message>()
            verify(chatClient).sendMessage(
                channelType = eq("messaging"),
                channelId = eq("123"),
                message = captor.capture(),
                isRetrying = eq(false)
            )
            captor.firstValue.attachments.size `should be equal to` 2
            viewModel.selectedAttachments.value.size `should be equal to` 0
        }

    @Test
    fun `Given message composer When selecting attachments and deselecting them Should display the correct number of selected attachments`() =
        runTest {
            val chatClient: ChatClient = mock()
            val viewModel = Fixture(chatClient = chatClient)
                .givenCurrentUser()
                .givenChannelQuery()
                .givenChannelState()
                .get()

            viewModel.addSelectedAttachments(
                listOf(
                    Attachment(imageUrl = "url1"),
                    Attachment(imageUrl = "url2")
                )
            )
            viewModel.removeSelectedAttachment(
                Attachment(imageUrl = "url1")
            )

            viewModel.selectedAttachments.value.size `should be equal to` 1
        }

    @Test
    fun `Given message composer When checking the also send to channel checkbox Should update the checkbox state`() =
        runTest {
            val viewModel = Fixture()
                .givenCurrentUser()
                .givenChannelQuery()
                .givenChannelState()
                .get()

            viewModel.setMessageMode(MessageMode.MessageThread(Message()))
            viewModel.setAlsoSendToChannel(true)

            val messageComposerState = viewModel.messageComposerState.value
            messageComposerState.alsoSendToChannel `should be equal to` true
            viewModel.alsoSendToChannel.value `should be equal to` true
        }

    @Test
    fun `Given message composer When starting a thread Should enter thread mode`() {
        val viewModel = Fixture()
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState()
            .get()

        viewModel.performMessageAction(ThreadReply(Message()))

        val messageComposerState = viewModel.messageComposerState.value
        messageComposerState.messageMode `should be instance of` MessageMode.MessageThread::class
        viewModel.messageMode.value `should be instance of` MessageMode.MessageThread::class
    }

    @Test
    fun `Given message composer When starting a thread and leaving it Should display the list in normal mode`() {
        val viewModel = Fixture()
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState()
            .get()

        viewModel.performMessageAction(ThreadReply(Message()))
        viewModel.leaveThread()

        val messageComposerState = viewModel.messageComposerState.value
        messageComposerState.messageMode `should be instance of` MessageMode.Normal::class
        viewModel.messageMode.value `should be instance of` MessageMode.Normal::class
    }

    @Test
    fun `Given message composer When replying to a message Should update the action state`() {
        val viewModel = Fixture()
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState()
            .get()

        viewModel.performMessageAction(Reply(Message()))

        val messageComposerState = viewModel.messageComposerState.value
        messageComposerState.action `should be instance of` Reply::class
    }

    @Test
    fun `Given message composer When editing a message Should update the action state`() {
        val viewModel = Fixture()
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState()
            .get()

        viewModel.performMessageAction(Edit(Message()))

        val messageComposerState = viewModel.messageComposerState.value
        messageComposerState.action `should be instance of` Edit::class
    }

    @Test
    fun `Given channel state with own capabilities When observing the state Should return the state with own capabilities`() {
        val channelData = ChannelData(
            type = "messaging",
            channelId = "123",
            ownCapabilities = setOf(
                "send-message",
                "send-reaction",
                "send-reply"
            )
        )
        val viewModel = Fixture()
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState(channelData)
            .get()

        val ownCapabilities = viewModel.ownCapabilities.value
        ownCapabilities.size `should be equal to` 3
    }

    @Test
    fun `Given message composer When typing slash Should show giphy command suggestion`() = runTest {
        val viewModel = Fixture()
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState(config = Config(commands = listOf(giphyCommand)))
            .get()

        viewModel.setMessageInput("/")

        viewModel.messageComposerState.value.commandSuggestions.size `should be equal to` 1
        viewModel.commandSuggestions.value.size `should be equal to` 1
    }

    @Test
    fun `Given message composer When typing slash and removing it Should not show command suggestions`() = runTest {
        val viewModel = Fixture()
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState(config = Config(commands = listOf(giphyCommand)))
            .get()

        viewModel.setMessageInput("/")
        viewModel.setMessageInput("")

        viewModel.messageComposerState.value.commandSuggestions.size `should be equal to` 0
        viewModel.commandSuggestions.value.size `should be equal to` 0
    }

    @Test
    fun `Given message composer When toggling commands and selecting giphy command Should populate the input with the command`() =
        runTest {
            val viewModel = Fixture()
                .givenCurrentUser()
                .givenChannelQuery()
                .givenChannelState(config = Config(commands = listOf(giphyCommand)))
                .get()

            viewModel.toggleCommandsVisibility()
            viewModel.selectCommand(viewModel.commandSuggestions.value.first())

            viewModel.messageComposerState.value.commandSuggestions.size `should be equal to` 0
            viewModel.commandSuggestions.value.size `should be equal to` 0
            viewModel.messageComposerState.value.inputValue `should be equal to` "/giphy "
            viewModel.input.value `should be equal to` "/giphy "
        }

    @Test
    fun `Given message composer When typing member autocomplete suggestion symbol Should show a list of available members`() =
        runTest {
            val viewModel = Fixture()
                .givenCurrentUser()
                .givenChannelQuery()
                .givenChannelState(members = listOf(Member(user = user1), Member(user = user2)))
                .get()

            viewModel.setMessageInput("@")

            viewModel.messageComposerState.value.mentionSuggestions.size `should be equal to` 2
            viewModel.mentionSuggestions.value.size `should be equal to` 2
        }

    @Test
    fun `Given message composer When selecting a mention suggestion Should populate the input with the user`() =
        runTest {
            val viewModel = Fixture()
                .givenCurrentUser()
                .givenChannelQuery()
                .givenChannelState(members = listOf(Member(user = user1), Member(user = user2)))
                .get()

            viewModel.setMessageInput("@")
            viewModel.selectMention(viewModel.mentionSuggestions.value.first())

            viewModel.messageComposerState.value.mentionSuggestions.size `should be equal to` 0
            viewModel.mentionSuggestions.value.size `should be equal to` 0
            viewModel.input.value `should be equal to` "@Jc Miñarro "
        }

    private class Fixture(
        private val chatClient: ChatClient = mock(),
        private val channelId: String = "messaging:123",
        private val maxAttachmentCount: Int = AttachmentConstants.MAX_ATTACHMENTS_COUNT,
        private val maxAttachmentSize: Long = AttachmentConstants.MAX_UPLOAD_FILE_SIZE,
    ) {
        private val globalState: GlobalMutableState = mock()
        private val clientState: ClientState = mock()
        private val stateRegistry: StateRegistry = mock()

        init {
            StateRegistry.instance = stateRegistry
            GlobalMutableState.instance = globalState
        }

        fun givenCurrentUser(currentUser: User = user1) = apply {
            whenever(clientState.user) doReturn MutableStateFlow(currentUser)
            whenever(chatClient.clientState) doReturn clientState
        }

        fun givenChannelQuery(channel: Channel = Channel()) = apply {
            whenever(chatClient.queryChannel(any(), any(), any())) doReturn channel.asCall()
        }

        fun givenChannelState(
            channelData: ChannelData = ChannelData(
                type = "messaging",
                channelId = "123",
            ),
            config: Config = Config(),
            members: List<Member> = emptyList(),
        ) = apply {
            val channelState: ChannelState = mock {
                whenever(it.channelData) doReturn MutableStateFlow(channelData)
                whenever(it.channelConfig) doReturn MutableStateFlow(config)
                whenever(it.members) doReturn MutableStateFlow(members)
            }
            whenever(stateRegistry.channel(any(), any())) doReturn channelState
        }

        fun givenSendMessage(message: Message = Message()) = apply {
            whenever(chatClient.sendMessage(any(), any(), any(), any())) doReturn message.asCall()
        }

        fun get(): MessageComposerViewModel {
            return MessageComposerViewModel(
                MessageComposerController(
                    chatClient = chatClient,
                    channelId = channelId,
                    maxAttachmentCount = maxAttachmentCount,
                    maxAttachmentSize = maxAttachmentSize,
                )
            )
        }
    }

    companion object {

        val user1 = User(
            id = "Jc",
            name = "Jc Miñarro"
        )
        val user2 = User(
            id = "amit",
            name = "Amit Kumar"
        )
        val giphyCommand = Command(
            name = "giphy",
            description = "Post a random gif to the channel",
            args = "[text]",
            set = "fun_set"
        )
    }
}

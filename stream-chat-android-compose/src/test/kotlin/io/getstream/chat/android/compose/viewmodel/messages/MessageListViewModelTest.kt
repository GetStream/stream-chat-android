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
import io.getstream.chat.android.client.audio.AudioPlayer
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessagesState
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.TypingEvent
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomChannelUserRead
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory
import io.getstream.chat.android.state.plugin.internal.StatePlugin
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.state.plugin.state.global.GlobalState
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
            .get()

        // Avoid counting date separators
        val messageItemCount = viewModel.currentMessagesState.value
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
            .get()

        viewModel.selectMessage(message1)

        val selectedMessageState = viewModel.currentMessagesState.value.selectedMessageState
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
            .givenSendReaction()
            .get()

        // Avoid counting date separators
        val messageItemCount = viewModel.currentMessagesState.value
            .messageItems
            .count { it is MessageItemState }
        messageItemCount `should be equal to` 2

        viewModel.performMessageAction(React(reaction1, message1))

        verify(chatClient).sendReaction(eq(reaction1), eq(true), eq(CID))
    }

    @Test
    fun `When calling pauseAudioRecordingAttachments, audioPlayer is invoked`() = runTest {
        val audioPlayer = mock<AudioPlayer>()
        val viewModel = Fixture()
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState()
            .givenAudioPlayer(audioPlayer)
            .get()
        viewModel.pauseAudioRecordingAttachments()
        verify(audioPlayer).pause()
    }

    private class Fixture(
        private val chatClient: ChatClient = mock(),
        private val channelId: String = CID,
        statePluginConfig: StatePluginConfig = StatePluginConfig(),
    ) {
        private val clientState: ClientState = mock()
        private val stateRegistry: StateRegistry = mock()
        private val globalState: GlobalState = mock()
        private val channelState: ChannelState = mock()

        init {
            val statePlugin: StatePlugin = mock()
            val statePluginFactory: StreamStatePluginFactory = mock()
            whenever(statePlugin.resolveDependency(eq(StateRegistry::class))) doReturn stateRegistry
            whenever(statePlugin.resolveDependency(eq(GlobalState::class))) doReturn globalState
            whenever(statePluginFactory.resolveDependency(eq(StatePluginConfig::class))) doReturn statePluginConfig
            whenever(chatClient.plugins) doReturn listOf(statePlugin)
            whenever(chatClient.pluginFactories) doReturn listOf(statePluginFactory)
            whenever(chatClient.clientState) doReturn clientState
        }

        fun givenCurrentUser(currentUser: User = user1) = apply {
            whenever(clientState.user) doReturn MutableStateFlow(currentUser)
            whenever(clientState.initializationState) doReturn MutableStateFlow(InitializationState.COMPLETE)
        }

        fun givenChannelQuery(channel: Channel = Channel()) = apply {
            whenever(chatClient.queryChannel(any(), any(), any(), any())) doReturn channel.asCall()
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
                messages = emptyList(),
            ),
        ) = apply {
            whenever(channelState.cid) doReturn CID
            whenever(channelState.channelData) doReturn MutableStateFlow(channelData)
            whenever(channelState.channelConfig) doReturn MutableStateFlow(Config())
            whenever(channelState.members) doReturn MutableStateFlow(listOf())
            whenever(channelState.membersCount) doReturn MutableStateFlow(randomInt())
            whenever(channelState.watcherCount) doReturn MutableStateFlow(randomInt())
            whenever(channelState.messagesState) doReturn MutableStateFlow(messageState)
            whenever(channelState.pinnedMessages) doReturn MutableStateFlow(emptyList())
            whenever(channelState.typing) doReturn MutableStateFlow(TypingEvent(channelId, emptyList()))
            whenever(channelState.reads) doReturn MutableStateFlow(listOf())
            whenever(channelState.read) doReturn MutableStateFlow(randomChannelUserRead(lastReadMessageId = null))
            whenever(channelState.endOfOlderMessages) doReturn MutableStateFlow(false)
            whenever(channelState.endOfNewerMessages) doReturn MutableStateFlow(true)
            whenever(channelState.toChannel()) doReturn Channel(type = CHANNEL_TYPE, id = CHANNEL_ID)
            whenever(channelState.unreadCount) doReturn MutableStateFlow(0)
            whenever(channelState.insideSearch) doReturn MutableStateFlow(false)
            whenever(channelState.loadingNewerMessages) doReturn MutableStateFlow(false)
            whenever(channelState.loadingOlderMessages) doReturn MutableStateFlow(false)
            whenever(stateRegistry.channel(any(), any())) doReturn channelState
        }

        fun givenAudioPlayer(audioPlayer: AudioPlayer) = apply {
            whenever(chatClient.audioPlayer) doReturn audioPlayer
        }

        fun get(): MessageListViewModel {
            return MessageListViewModel(
                MessageListController(
                    chatClient = chatClient,
                    cid = channelId,
                    clipboardHandler = mock(),
                    threadLoadOrderOlderToNewer = false,
                    channelState = MutableStateFlow(channelState),
                ),

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
        private val reaction1 = Reaction(
            messageId = "message-id-1",
            type = "like",
            score = 1,
            user = user1,
        )
    }
}

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

package io.getstream.chat.android.ui.common.feature.messages.list

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.createDate
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.models.MessagesState
import io.getstream.chat.android.models.TypingEvent
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomMessageList
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.internal.StatePlugin
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.state.plugin.state.global.GlobalState
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.ui.common.state.messages.list.DateSeparatorItemState
import io.getstream.chat.android.ui.common.state.messages.list.DeletedMessageVisibility
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import io.getstream.chat.android.ui.common.state.messages.list.MessageListState
import io.getstream.chat.android.ui.common.state.messages.list.MessagePosition
import io.getstream.chat.android.ui.common.state.messages.list.TypingItemState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
internal class MessageListControllerTests {

    @Test
    fun `Given no messages When no one is Typing Should return an empty message list`() = runTest {
        val messageState = MessagesState.Result(emptyList())
        val controller = Fixture()
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState(messageState = messageState)
            .get()

        val expectedResult = MessageListState(
            currentUser = user1,
            endOfNewMessagesReached = true,
        )

        controller.messageListState.value `should be equal to` expectedResult
    }

    // test typing indicator logic
    @Test
    fun `Given other users are typing When there are no messages Should return only the typing indicator`() = runTest {
        val controller = Fixture()
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState(
                typingUsers = listOf(user2),
            )
            .get()

        val expectedResult = MessageListState(
            currentUser = user1,
            endOfNewMessagesReached = true,
            messageItems = listOf(
                TypingItemState(listOf(user2)),
            ),
        )

        controller.messageListState.value `should be equal to` expectedResult
    }

    @Test
    fun `Given other users are typing When there are messages Should add typing indicator to end`() = runTest {
        val messageState = MessagesState.Result(randomMessageList())
        val controller = Fixture()
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState(
                messageState = messageState,
                typingUsers = listOf(user2),
            )
            .get(dateSeparatorHandler = { _, _ -> false })

        val expectedLastItem = TypingItemState(listOf(user2))
        val lastItem = controller.messageListState.value.messageItems.last()

        lastItem `should be equal to` expectedLastItem
    }

    // test message grouping
    @Test
    fun `Given regular message followed and preceded by current user message When grouping messages Should add middle position to message`() =
        runTest {
            val messages = randomMessageList(3) { randomMessage(user = user1) }
            val messageState = MessagesState.Result(messages)
            val controller = Fixture()
                .givenCurrentUser()
                .givenChannelQuery()
                .givenChannelState(messageState = messageState)
                .get(dateSeparatorHandler = { _, _ -> false })

            val expectedPosition = listOf(MessagePosition.MIDDLE)
            val messagePosition = (controller.messageListState.value.messageItems[1] as MessageItemState).groupPosition

            messagePosition `should be equal to` expectedPosition
        }

    @Test
    fun `Given regular message followed and preceded by other user message When grouping messages Should add top and bottom positions to messages`() =
        runTest {
            var message = 0
            val messages = randomMessageList(3) {
                message++
                randomMessage(user = if (message % 2 == 0) user1 else user2)
            }
            val messageState = MessagesState.Result(messages)
            val controller = Fixture()
                .givenCurrentUser()
                .givenChannelQuery()
                .givenChannelState(messageState = messageState)
                .get(dateSeparatorHandler = { _, _ -> false })

            val expectedPosition = listOf(MessagePosition.TOP, MessagePosition.BOTTOM)
            val messagePosition = (controller.messageListState.value.messageItems[1] as MessageItemState).groupPosition

            messagePosition `should be equal to` expectedPosition
        }

    @Test
    fun `Given regular message followed by system message When grouping messages Should add bottom position to the regular message`() =
        runTest {
            var message = 0
            val messages = randomMessageList(3) {
                message++
                randomMessage(user = if (message % 2 == 0) user1 else user2)
            }
            val messageState = MessagesState.Result(messages)
            val controller = Fixture()
                .givenCurrentUser()
                .givenChannelQuery()
                .givenChannelState(messageState = messageState)
                .get(dateSeparatorHandler = { _, _ -> false })

            val expectedPosition = listOf(MessagePosition.TOP, MessagePosition.BOTTOM)
            val messagePosition = (controller.messageListState.value.messageItems[1] as MessageItemState).groupPosition

            messagePosition `should be equal to` expectedPosition
        }

    // test date separators
    @Test
    fun `Given date separators with time difference Should add 3 date separators`() = runTest {
        var message = 0
        val messages = randomMessageList(3) {
            message++
            randomMessage(createdAt = createDate(2022, 5, message))
        }
        val messageState = MessagesState.Result(messages)
        val controller = Fixture()
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState(messageState = messageState)
            .get()

        val dateSeparatorCount = controller.messageListState.value.messageItems.count { it is DateSeparatorItemState }

        dateSeparatorCount `should be equal to` 3
    }

    @Test
    fun `Given handler returns no date separators Should not add date separators`() = runTest {
        var message = 0
        val messages = randomMessageList(3) {
            message++
            randomMessage(createdAt = createDate(2022, 5, message))
        }
        val messageState = MessagesState.Result(messages)
        val controller = Fixture()
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState(messageState = messageState)
            .get(dateSeparatorHandler = { _, _ -> false })

        val dateSeparatorCount = controller.messageListState.value.messageItems.count { it is DateSeparatorItemState }

        dateSeparatorCount `should be equal to` 0
    }

    // deleted visibility
    @Test
    fun `When deleted visibility is never When grouping messages Should not add any deleted messages`() = runTest {
        var message = 0
        val messages = randomMessageList {
            message++
            randomMessage(deletedAt = if (message % 2 == 0) randomDate() else null)
        }
        val messageState = MessagesState.Result(messages)
        val controller = Fixture()
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState(messageState = messageState)
            .get(deletedMessageVisibility = DeletedMessageVisibility.ALWAYS_HIDDEN)

        val deletedMessageCount =
            controller.messageListState.value.messageItems.count { it is MessageItemState && it.message.isDeleted() }
        deletedMessageCount `should be equal to` 0
    }

    @Test
    fun `When deleted visibility is always When grouping messages Should add all deleted messages`() = runTest {
        var message = 0
        val messages = randomMessageList {
            message++
            randomMessage(deletedAt = if (message % 2 == 0) randomDate() else null)
        }
        val messageState = MessagesState.Result(messages)
        val controller = Fixture()
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState(messageState = messageState)
            .get()

        val messagesCount = controller.messageListState.value.messageItems.count { it is MessageItemState }
        messagesCount `should be equal to` 10
    }

    @Test
    fun `When deleted visibility is current user When grouping messages Should not see other users deleted messages`() =
        runTest {
            var message = 0
            val messages = randomMessageList {
                message++
                randomMessage(deletedAt = if (message % 2 == 0) randomDate() else null)
            }
            val messageState = MessagesState.Result(messages)
            val controller = Fixture()
                .givenCurrentUser()
                .givenChannelQuery()
                .givenChannelState(messageState = messageState)
                .get(deletedMessageVisibility = DeletedMessageVisibility.VISIBLE_FOR_CURRENT_USER)

            val deletedMessageCount =
                controller.messageListState.value.messageItems.count { it is MessageItemState && it.message.isDeleted() }
            deletedMessageCount `should be equal to` 0
        }

    // footer visibility
    @Test
    fun `When footer visibility is with time difference When message is after specified time Show message footer`() =
        runTest {
            var message = 0
            val messages = randomMessageList(3) {
                message++
                randomMessage(createdAt = createDate(2022, 5, message))
            }
            val messageState = MessagesState.Result(messages)
            val controller = Fixture()
                .givenCurrentUser()
                .givenChannelQuery()
                .givenChannelState(messageState = messageState)
                .get(dateSeparatorHandler = { _, _ -> false })

            val dateSeparatorCount =
                controller.messageListState.value.messageItems.count { it is MessageItemState && it.showMessageFooter }

            dateSeparatorCount `should be equal to` 3
        }

    @Test
    fun `When repetitive markLastMessageRead calls appear only single API call should be sent`() = runTest {
        val chatClient: ChatClient = mock()
        val messages = arrayListOf(
            randomMessage(id = "1"),
            randomMessage(id = "2"),
            randomMessage(id = "3"),
        )
        val messageState = MessagesState.Result(messages)
        val controller = Fixture(chatClient = chatClient)
            .givenCurrentUser()
            .givenChannelQuery()
            .givenMarkRead()
            .givenChannelState(messageState = messageState)
            .get()

        controller.markLastMessageRead(); delay(10)
        controller.markLastMessageRead(); delay(10)
        controller.markLastMessageRead(); delay(10)
        controller.markLastMessageRead(); delay(10)
        controller.markLastMessageRead(); delay(1000)

        verify(chatClient, times(1)).markRead(any(), any())
    }

    private class Fixture(
        private val chatClient: ChatClient = mock(),
        private val channelId: String = CID,
        statePluginConfig: StatePluginConfig = StatePluginConfig(),
    ) {
        private val clientState: ClientState = mock()
        private val stateRegistry: StateRegistry = mock()
        private val globalState: GlobalState = mock()

        init {
            val statePlugin: StatePlugin = mock()
            whenever(statePlugin.resolveDependency(eq(StateRegistry::class))) doReturn stateRegistry
            whenever(statePlugin.resolveDependency(eq(StatePluginConfig::class))) doReturn statePluginConfig
            whenever(chatClient.plugins) doReturn listOf(statePlugin)
            whenever(chatClient.clientState) doReturn clientState
            whenever(statePlugin.resolveDependency(eq(GlobalState::class))) doReturn globalState
        }

        fun givenCurrentUser(currentUser: User = user1) = apply {
            whenever(clientState.user) doReturn MutableStateFlow(currentUser)
            whenever(clientState.initializationState) doReturn MutableStateFlow(InitializationState.COMPLETE)
        }

        fun givenChannelQuery(channel: Channel = Channel()) = apply {
            whenever(chatClient.queryChannel(any(), any(), any(), any())) doReturn channel.asCall()
        }

        fun givenMarkRead() = apply {
            whenever(chatClient.markRead(any(), any())) doReturn Unit.asCall()
        }

        fun givenMarkMessageRead() = apply {
            whenever(chatClient.markMessageRead(any(), any(), any())) doReturn Unit.asCall()
        }

        fun givenChannelState(
            channelData: ChannelData = ChannelData(
                type = CHANNEL_TYPE,
                id = CHANNEL_ID,
            ),
            messageState: MessagesState = MessagesState.Result(
                messages = emptyList(),
            ),
            typingUsers: List<User> = listOf(),
        ) = apply {
            val channelState: ChannelState = mock {
                whenever(it.cid) doReturn CID
                whenever(it.channelData) doReturn MutableStateFlow(channelData)
                whenever(it.channelConfig) doReturn MutableStateFlow(Config())
                whenever(it.members) doReturn MutableStateFlow(listOf())
                whenever(it.messagesState) doReturn MutableStateFlow(messageState)
                whenever(it.typing) doReturn MutableStateFlow(TypingEvent(channelId, typingUsers))
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
        }

        fun get(
            dateSeparatorHandler: DateSeparatorHandler = DateSeparatorHandler.getDefaultDateSeparatorHandler(),
            deletedMessageVisibility: DeletedMessageVisibility = DeletedMessageVisibility.ALWAYS_VISIBLE,
        ): MessageListController {
            return MessageListController(
                cid = channelId,
                chatClient = chatClient,
                clipboardHandler = mock(),
                dateSeparatorHandler = dateSeparatorHandler,
                deletedMessageVisibility = deletedMessageVisibility,
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
        private val user2 = User(id = "NotJc", name = "Not Jc Miñarro")
    }
}

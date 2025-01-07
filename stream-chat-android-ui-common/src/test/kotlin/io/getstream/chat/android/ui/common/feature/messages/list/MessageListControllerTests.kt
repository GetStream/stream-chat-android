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
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageType
import io.getstream.chat.android.models.MessagesState
import io.getstream.chat.android.models.TypingEvent
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomChannelUserRead
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMembers
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomMessageList
import io.getstream.chat.android.randomString
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory
import io.getstream.chat.android.state.plugin.internal.StatePlugin
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.state.plugin.state.global.GlobalState
import io.getstream.chat.android.suspendableRandomMessageList
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.ui.common.state.messages.list.DateSeparatorItemState
import io.getstream.chat.android.ui.common.state.messages.list.DeletedMessageVisibility
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import io.getstream.chat.android.ui.common.state.messages.list.MessageListState
import io.getstream.chat.android.ui.common.state.messages.list.MessagePosition
import io.getstream.chat.android.ui.common.state.messages.list.Other
import io.getstream.chat.android.ui.common.state.messages.list.SystemMessageItemState
import io.getstream.chat.android.ui.common.state.messages.list.TypingItemState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

@ExperimentalCoroutinesApi
internal class MessageListControllerTests {

    @Test
    fun `Given no messages When no one is Typing Should return an empty message list`() = runTest {
        val messagesState = MutableStateFlow(emptyList<Message>())
        val controller = Fixture()
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState(messagesState = messagesState)
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
        val messagesState = MutableStateFlow(randomMessageList())
        val controller = Fixture()
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState(
                messagesState = messagesState,
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
            val messagesState = MutableStateFlow(messages)
            val controller = Fixture()
                .givenCurrentUser()
                .givenChannelQuery()
                .givenChannelState(messagesState = messagesState)
                .get(dateSeparatorHandler = { _, _ -> false })

            val expectedPosition = listOf(MessagePosition.MIDDLE)
            val messagePosition = (controller.messageListState.value.messageItems[1] as MessageItemState).groupPosition

            messagePosition `should be equal to` expectedPosition
        }

    @Test
    fun `Given regular message followed and preceded by other user message When grouping messages Should add none position to the regular message`() =
        runTest {
            val messages = listOf(
                randomMessage(user = user1), // First message from user1
                randomMessage(user = user2), // Second message from user2
                randomMessage(user = user1), // Third message from user1
            )
            val messagesState = MutableStateFlow(messages)
            val controller = Fixture()
                .givenCurrentUser()
                .givenChannelQuery()
                .givenChannelState(messagesState = messagesState)
                .get(dateSeparatorHandler = { _, _ -> false })

            val expectedPosition = listOf(MessagePosition.NONE)
            val messagePosition = (controller.messageListState.value.messageItems[1] as MessageItemState).groupPosition

            messagePosition `should be equal to` expectedPosition
        }

    @Test
    fun `Given regular message followed by system message When grouping messages Should add none position to the regular message`() =
        runTest {
            val messages = listOf(
                randomMessage(user = user1, type = MessageType.REGULAR),
                randomMessage(user = user2, type = MessageType.REGULAR), // Regular message from user2
                randomMessage(user = user1, type = MessageType.SYSTEM), // System message from user1
            )
            val messagesState = MutableStateFlow(messages)
            val controller = Fixture()
                .givenCurrentUser()
                .givenChannelQuery()
                .givenChannelState(messagesState = messagesState)
                .get(dateSeparatorHandler = { _, _ -> false })

            val expectedPosition = listOf(MessagePosition.NONE)
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
        val messagesState = MutableStateFlow(messages)
        val controller = Fixture()
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState(messagesState = messagesState)
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
        val messagesState = MutableStateFlow(messages)
        val controller = Fixture()
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState(messagesState = messagesState)
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
        val messagesState = MutableStateFlow(messages)
        val controller = Fixture()
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState(messagesState = messagesState)
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
        val messagesState = MutableStateFlow(messages)
        val controller = Fixture()
            .givenCurrentUser()
            .givenChannelQuery()
            .givenChannelState(messagesState = messagesState)
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
            val messagesState = MutableStateFlow(messages)
            val controller = Fixture()
                .givenCurrentUser()
                .givenChannelQuery()
                .givenChannelState(messagesState = messagesState)
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
            val messagesState = MutableStateFlow(messages)
            val controller = Fixture()
                .givenCurrentUser()
                .givenChannelQuery()
                .givenChannelState(messagesState = messagesState)
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
        val messagesState = MutableStateFlow(messages)
        val controller = Fixture(chatClient = chatClient)
            .givenCurrentUser()
            .givenChannelQuery()
            .givenMarkRead()
            .givenChannelState(messagesState = messagesState)
            .get()

        controller.markLastMessageRead()
        delay(10)
        controller.markLastMessageRead()
        delay(10)
        controller.markLastMessageRead()
        delay(10)
        controller.markLastMessageRead()
        delay(10)
        controller.markLastMessageRead()
        delay(1000)

        verify(chatClient, times(1)).markRead(any(), any())
    }

    @Test
    fun `When channelData changes the updated Channel instance must be emitted`() = runTest {
        val chatClient: ChatClient = mock()
        val channelData = ChannelData(
            type = CHANNEL_TYPE,
            id = CHANNEL_ID,
        )
        val channelDataState = MutableStateFlow(channelData)
        val controller = Fixture(chatClient = chatClient)
            .givenCurrentUser()
            .givenChannelQuery()
            .givenMarkRead()
            .givenChannelState(channelDataState = channelDataState)
            .get()

        delay(1000)

        channelDataState.value = channelData.copy(
            name = "channel_name",
            image = "http://new.image.jpg",
        )

        delay(1000)

        val channel = controller.channel.value
        channel.id `should be equal to` CHANNEL_ID
        channel.type `should be equal to` CHANNEL_TYPE
        channel.name `should be equal to` "channel_name"
        channel.image `should be equal to` "http://new.image.jpg"
    }

    @Test
    fun `When watcherCount changes the updated Channel instance must be emitted`() = runTest {
        val chatClient: ChatClient = mock()
        val watchersCountState = MutableStateFlow(2)
        val controller = Fixture(chatClient = chatClient)
            .givenCurrentUser()
            .givenChannelQuery()
            .givenMarkRead()
            .givenChannelState(watchersCountState = watchersCountState)
            .get()

        delay(1000)

        watchersCountState.value = 4

        delay(1000)

        val channel = controller.channel.value
        channel.watcherCount `should be equal to` watchersCountState.value
    }

    @Test
    fun `When memberCount changes the updated Channel instance must be emitted`() = runTest {
        val chatClient: ChatClient = mock()
        val channelData = ChannelData(
            type = CHANNEL_TYPE,
            id = CHANNEL_ID,
            memberCount = 2,
        )
        val membersState = MutableStateFlow(listOf(randomMember(), randomMember()))
        val channelDataState = MutableStateFlow(channelData)
        val membersCountState = MutableStateFlow(2)
        val controller = Fixture(chatClient = chatClient)
            .givenCurrentUser()
            .givenChannelQuery()
            .givenMarkRead()
            .givenChannelState(
                channelDataState = channelDataState,
                membersState = membersState,
                membersCountState = membersCountState,
            )
            .get()

        delay(1000)

        controller.channel.value.members.size `should be equal to` 2

        delay(1000)

        membersState.value = listOf(randomMember(), randomMember(), randomMember(), randomMember())

        delay(1000)

        controller.channel.value.members.size `should be equal to` 4

        membersCountState.value = 4

        delay(1000)

        controller.channel.value.members.size `should be equal to` 4
    }

    @Test
    fun `When member gets banned the updated Channel instance must be emitted`() = runTest {
        val chatClient: ChatClient = mock()
        val members = randomMembers(size = 2) {
            randomMember(banned = false, banExpires = null)
        }
        val channelData = ChannelData(
            type = CHANNEL_TYPE,
            id = CHANNEL_ID,
            memberCount = members.size,
        )

        val membersState = MutableStateFlow(members)
        val channelDataState = MutableStateFlow(channelData)
        val membersCountState = MutableStateFlow(members.size)
        val controller = Fixture(chatClient = chatClient)
            .givenCurrentUser()
            .givenChannelQuery()
            .givenMarkRead()
            .givenChannelState(
                channelDataState = channelDataState,
                membersState = membersState,
                membersCountState = membersCountState,
            )
            .get()

        delay(1000)

        controller.channel.value.members.size `should be equal to` members.size
        controller.channel.value.members.forEach {
            it.banned.shouldBeFalse()
            it.banExpires.shouldBeNull()
        }

        delay(1000)

        membersState.value = members.map {
            it.copy(banned = true, banExpires = randomDate())
        }

        delay(1000)

        controller.channel.value.members.size `should be equal to` members.size
        controller.channel.value.members.forEach {
            it.banned.shouldBeTrue()
            it.banExpires.shouldNotBeNull()
        }
    }

    @Test
    fun `When system message arrives, markRead should be invoked for that channel`() = runTest {
        /* Given */
        val chatClient: ChatClient = mock()
        val members = randomMembers(size = 2) {
            randomMember(user = if (it % 2 == 0) user1 else user2)
        }
        val channelData = ChannelData(
            type = CHANNEL_TYPE,
            id = CHANNEL_ID,
            memberCount = members.size,
            createdBy = user1,
        )

        val messages = suspendableRandomMessageList(2) {
            nowMessage(author = user1, type = "regular", text = "regular_$it").also {
                delay(100L)
            }
        }
        val membersState = MutableStateFlow(members)
        val channelDataState = MutableStateFlow(channelData)
        val membersCountState = MutableStateFlow(members.size)
        val messagesState = MutableStateFlow(emptyList<Message>())
        val controller = Fixture(chatClient = chatClient)
            .givenCurrentUser()
            .givenChannelQuery()
            .givenMarkRead()
            .givenChannelState(
                channelDataState = channelDataState,
                membersState = membersState,
                membersCountState = membersCountState,
                messagesState = messagesState,
            )
            .get()

        /* When */

        // 1 ==> simulate channel entering
        messagesState.emit(messages)
        controller.updateLastSeenMessage(messages.last())
        // wait for 1 sec to let controller.debouncer trigger markRead for the last message
        delay(1000)

        // 2 ==> simulate new system message arrival
        val newMessage = nowMessage(author = user1, type = "system", text = "system_${messages.size}")
        messagesState.emit(messages + newMessage)
        controller.updateLastSeenMessage(newMessage)
        // wait for 1 sec to let controller.debouncer trigger markRead for new system message
        delay(1000)

        /* Then */
        verify(chatClient, times(2)).markRead(eq(CHANNEL_TYPE), eq(CHANNEL_ID))
        controller.lastSeenMessageId `should be equal to` newMessage.id
    }

    @Test
    fun `When new message arrives, newMessageState should remain the same`() = runTest {
        /* Given */
        val chatClient: ChatClient = mock()
        val members = randomMembers(size = 2) {
            randomMember(user = if (it % 2 == 0) user1 else user2)
        }
        val channelData = ChannelData(
            type = CHANNEL_TYPE,
            id = CHANNEL_ID,
            memberCount = members.size,
            createdBy = user1,
        )

        val messages = suspendableRandomMessageList(2) {
            nowMessage(author = user1, type = "regular", text = "regular_$it").also {
                delay(100L)
            }
        }
        val membersState = MutableStateFlow(members)
        val channelDataState = MutableStateFlow(channelData)
        val membersCountState = MutableStateFlow(members.size)
        val messagesState = MutableStateFlow(emptyList<Message>())
        val typingState = MutableStateFlow(TypingEvent(CHANNEL_ID, emptyList()))
        val controller = Fixture(chatClient = chatClient)
            .givenCurrentUser()
            .givenChannelQuery()
            .givenMarkRead()
            .givenChannelState(
                channelDataState = channelDataState,
                membersState = membersState,
                membersCountState = membersCountState,
                messagesState = messagesState,
                typingState = typingState,
            )
            .get()

        /* When */

        // 1 ==> simulate channel entering
        messagesState.emit(messages)
        controller.updateLastSeenMessage(messages.last())

        // wait for 1 sec
        delay(1000)

        // 2 ==> simulate typing start event
        typingState.emit(TypingEvent(CHANNEL_ID, listOf(user2)))

        // 3 ==> simulate new system message arrival
        val newMessage = nowMessage(author = user2, type = "regular", text = "Last message")
        messagesState.emit(messages + newMessage)
        controller.updateLastSeenMessage(newMessage)

        // 4 ==> simulate typing stop event
        typingState.emit(TypingEvent(CHANNEL_ID, emptyList()))

        // wait for 1 sec
        delay(1000)

        /* Then */
        controller.messageListState.value.newMessageState `should be equal to` Other(newMessage.createdAt?.time)
    }

    @Test
    fun `When showSystemMessages is true, Then system messages should be shown`() = runTest {
        val messages = listOf(randomMessage(user = user1, type = MessageType.SYSTEM, deletedAt = null))
        val messagesState = MutableStateFlow(messages)
        val controller = Fixture()
            .givenCurrentUser()
            .givenChannelState(messagesState = messagesState)
            .get(dateSeparatorHandler = { _, _ -> false }, showSystemMessages = true)

        val expectedMessageItems = messages.map(::SystemMessageItemState)
        controller.messageListState.value.messageItems `should be equal to` expectedMessageItems
    }

    @Test
    fun `When showSystemMessages is false, Then system messages should be hidden`() = runTest {
        val messages = listOf(randomMessage(user = user1, type = MessageType.SYSTEM, deletedAt = null))
        val messagesState = MutableStateFlow(messages)
        val controller = Fixture()
            .givenCurrentUser()
            .givenChannelState(messagesState = messagesState)
            .get(dateSeparatorHandler = { _, _ -> false }, showSystemMessages = false)

        val expectedMessageItems = emptyList<MessageItemState>()
        controller.messageListState.value.messageItems `should be equal to` expectedMessageItems
    }

    private class Fixture(
        private val chatClient: ChatClient = mock(),
        private val cid: String = CID,
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
            whenever(statePluginFactory.resolveDependency(eq(StatePluginConfig::class))) doReturn statePluginConfig
            whenever(chatClient.plugins) doReturn listOf(statePlugin)
            whenever(chatClient.pluginFactories) doReturn listOf(statePluginFactory)
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
            channelDataState: StateFlow<ChannelData> = MutableStateFlow(
                ChannelData(
                    type = CHANNEL_TYPE,
                    id = CHANNEL_ID,
                ),
            ),
            messagesState: StateFlow<List<Message>> = MutableStateFlow(emptyList()),
            pinnedMessagesState: StateFlow<List<Message>> = MutableStateFlow(emptyList()),
            membersState: StateFlow<List<Member>> = MutableStateFlow(emptyList()),
            membersCountState: StateFlow<Int> = MutableStateFlow(0),
            watchersState: StateFlow<List<User>> = MutableStateFlow(emptyList()),
            watchersCountState: StateFlow<Int> = MutableStateFlow(0),
            typingUsers: List<User> = listOf(),
            typingState: StateFlow<TypingEvent> = MutableStateFlow(TypingEvent(cid, typingUsers)),
        ) = apply {
            whenever(channelState.cid) doReturn CID
            whenever(channelState.channelData) doReturn channelDataState
            whenever(channelState.channelConfig) doReturn MutableStateFlow(Config())
            whenever(channelState.members) doReturn membersState
            whenever(channelState.membersCount) doReturn membersCountState
            whenever(channelState.watchers) doReturn watchersState
            whenever(channelState.watcherCount) doReturn watchersCountState
            whenever(channelState.messages) doReturn messagesState
            whenever(channelState.pinnedMessages) doReturn pinnedMessagesState
            whenever(channelState.messagesState) doReturn messagesState.map { messages ->
                MessagesState.Result(messages)
            }.stateIn(testCoroutines.scope, SharingStarted.Eagerly, MessagesState.Result(emptyList()))
            whenever(channelState.typing) doReturn typingState
            whenever(channelState.reads) doReturn MutableStateFlow(listOf())
            whenever(channelState.read) doReturn MutableStateFlow(randomChannelUserRead(lastReadMessageId = null))
            whenever(channelState.endOfOlderMessages) doReturn MutableStateFlow(false)
            whenever(channelState.endOfNewerMessages) doReturn MutableStateFlow(true)
            whenever(channelState.unreadCount) doReturn MutableStateFlow(0)
            whenever(channelState.insideSearch) doReturn MutableStateFlow(false)
            whenever(channelState.loadingNewerMessages) doReturn MutableStateFlow(false)
            whenever(channelState.loadingOlderMessages) doReturn MutableStateFlow(false)
            whenever(channelState.hidden) doReturn MutableStateFlow(false)
            whenever(channelState.toChannel()) doAnswer {
                channelState.convertToChannel()
            }
            whenever(stateRegistry.channel(any(), any())) doReturn channelState
        }

        fun get(
            dateSeparatorHandler: DateSeparatorHandler = DateSeparatorHandler.getDefaultDateSeparatorHandler(),
            deletedMessageVisibility: DeletedMessageVisibility = DeletedMessageVisibility.ALWAYS_VISIBLE,
            showSystemMessages: Boolean = true,
        ): MessageListController {
            return MessageListController(
                cid = cid,
                chatClient = chatClient,
                clipboardHandler = mock(),
                dateSeparatorHandler = dateSeparatorHandler,
                deletedMessageVisibility = deletedMessageVisibility,
                showSystemMessages = showSystemMessages,
                threadLoadOrderOlderToNewer = false,
                channelState = MutableStateFlow(channelState),
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

        private fun nowDate() = Date(testCoroutines.dispatcher.scheduler.currentTime)

        private fun nowMessage(author: User, type: String, text: String = randomString()): Message {
            val nowDate = nowDate()
            return randomMessage(
                user = author,
                type = type,
                text = text,
                createdAt = nowDate,
                updatedAt = nowDate,
                deletedAt = null,
                createdLocallyAt = null,
                updatedLocallyAt = null,
            )
        }

        private fun ChannelState.convertToChannel(): Channel {
            val channelData = channelData.value

            val messages = messages.value
            val members = members.value
            val watchers = watchers.value
            val reads = reads.value
            val watcherCount = watcherCount.value
            val insideSearch = insideSearch.value

            val channel = channelData.toChannel(
                messages,
                emptyList(),
                members,
                reads,
                watchers,
                watcherCount,
                insideSearch,
                null,
            )
            return channel.copy(
                config = channelConfig.value,
                hidden = hidden.value,
                isInsideSearch = insideSearch,
            )
        }
    }
}

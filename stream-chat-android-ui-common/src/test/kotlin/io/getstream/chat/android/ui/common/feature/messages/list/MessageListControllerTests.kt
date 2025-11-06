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

import app.cash.turbine.test
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.audio.AudioPlayer
import io.getstream.chat.android.client.audio.AudioState
import io.getstream.chat.android.client.audio.audioHash
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.createDate
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageType
import io.getstream.chat.android.models.MessagesState
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.TypingEvent
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomChannelUserRead
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMembers
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomMessageList
import io.getstream.chat.android.randomPollOption
import io.getstream.chat.android.randomPollVote
import io.getstream.chat.android.randomReaction
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory
import io.getstream.chat.android.state.plugin.internal.StatePlugin
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.state.plugin.state.global.GlobalState
import io.getstream.chat.android.suspendableRandomMessageList
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.test.callFrom
import io.getstream.chat.android.ui.common.state.messages.list.DateSeparatorItemState
import io.getstream.chat.android.ui.common.state.messages.list.DeletedMessageVisibility
import io.getstream.chat.android.ui.common.state.messages.list.MessageFocused
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import io.getstream.chat.android.ui.common.state.messages.list.MessageListState
import io.getstream.chat.android.ui.common.state.messages.list.MessagePosition
import io.getstream.chat.android.ui.common.state.messages.list.Other
import io.getstream.chat.android.ui.common.state.messages.list.SystemMessageItemState
import io.getstream.chat.android.ui.common.state.messages.list.Typing
import io.getstream.chat.android.ui.common.state.messages.list.TypingItemState
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.call.Call
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be false`
import org.amshove.kluent.`should be true`
import org.amshove.kluent.`should not be null`
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Assertions
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

@Suppress("LargeClass")
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
            newMessageState = Typing,
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
        val messages = listOf(
            randomMessage(user = user1, type = MessageType.SYSTEM, deletedAt = null, deletedForMe = false),
        )
        val messagesState = MutableStateFlow(messages)
        val controller = Fixture()
            .givenCurrentUser()
            .givenChannelState(messagesState = messagesState)
            .get(dateSeparatorHandler = { _, _ -> false }, showSystemMessages = false)

        val expectedMessageItems = emptyList<MessageItemState>()
        controller.messageListState.value.messageItems `should be equal to` expectedMessageItems
    }

    @Test
    fun `When scroll to first unread message is called, and message is already loaded, Then message is focused`() =
        runTest {
            val user = randomUser()
            val messages = listOf(
                randomMessage(id = "last_read_message_id"),
                randomMessage(id = "first_unread_message_id"),
            )
            val channelRead = MutableStateFlow(
                randomChannelUserRead(
                    user = user,
                    lastReadMessageId = "last_read_message_id",
                ),
            )
            val messagesState = MutableStateFlow(messages)
            val controller = Fixture()
                .givenCurrentUser(user)
                .givenChannelState(
                    messagesState = messagesState,
                    read = channelRead,
                )
                .get(dateSeparatorHandler = { _, _ -> false })
            controller.scrollToFirstUnreadMessage()
            val items = controller.messageListState.value.messageItems
            val lastReadMessage = items.first() as MessageItemState
            val firstUnreadMessage = items.last() as MessageItemState
            lastReadMessage.message.id `should be equal to` "last_read_message_id"
            lastReadMessage.focusState `should be equal to` null
            firstUnreadMessage.message.id `should be equal to` "first_unread_message_id"
            firstUnreadMessage.focusState `should be equal to` MessageFocused
        }

    @Test
    fun `Show unread label, when unread message is loaded`() =
        runTest {
            val user = randomUser()
            val firstMessage = randomMessage(id = "last_read_message_id", deletedAt = null, deletedForMe = false)
            val messages = listOf(
                firstMessage,
                randomMessage(id = "first_unread_message_id", deletedAt = null, deletedForMe = false),
            )
            val channelRead = MutableStateFlow(
                randomChannelUserRead(
                    user = user,
                    lastReadMessageId = firstMessage.id,
                    unreadMessages = 0,
                ),
            )
            val messagesState = MutableStateFlow(messages)
            val controller = Fixture()
                .givenCurrentUser(user)
                .givenChannelState(
                    messagesState = messagesState,
                    read = channelRead,
                )
                .get()

            val unreadLabel = controller.unreadLabelState.value
            unreadLabel.`should not be null`()
            unreadLabel.lastReadMessageId `should be equal to` firstMessage.id
            unreadLabel.buttonVisibility.`should be true`()
        }

    @Test
    fun `Show unread label, when message is marked as unread`() =
        runTest {
            val user = randomUser()
            val lastReadMessage = randomMessage(id = "last_read_message_id")
            val messages = listOf(
                lastReadMessage,
                randomMessage(id = "first_unread_message_id"),
            )
            val channelUserRead = MutableStateFlow<ChannelUserRead?>(null)
            val messagesState = MutableStateFlow(messages)
            val controller = Fixture()
                .givenCurrentUser(user)
                .givenChannelState(
                    messagesState = messagesState,
                    read = channelUserRead,
                )
                .givenMarkMessageUnread()
                .get()

            controller.markUnread(lastReadMessage)
            channelUserRead.emit(
                randomChannelUserRead(
                    user = user,
                    lastReadMessageId = lastReadMessage.id,
                    unreadMessages = 0,
                ),
            )

            val unreadLabel = controller.unreadLabelState.value
            unreadLabel.`should not be null`()
            unreadLabel.lastReadMessageId `should be equal to` lastReadMessage.id
            unreadLabel.buttonVisibility.`should be false`()
        }

    @Test
    fun `When deleting message with playing audio, audio is stopped before deletion`() = runTest {
        val messageId = randomString()
        val audioRecording = randomAttachment(type = AttachmentType.AUDIO_RECORDING)
        val messages = listOf(
            randomMessage(id = messageId, attachments = listOf(audioRecording)),
        )
        val messagesState = MutableStateFlow(messages)
        val audioPlayer = mock<AudioPlayer>().apply {
            whenever(currentState) doReturn AudioState.PLAYING
            whenever(currentPlayingId) doReturn audioRecording.audioHash
        }
        val controller = Fixture()
            .givenCurrentUser()
            .givenChannelState(messagesState = messagesState)
            .givenAudioPlayer(audioPlayer)
            .givenDeleteMessage(callFrom { messages.first() })
            .get()
        controller.deleteMessage(messages.first())
        verify(audioPlayer).pause()
    }

    @Test
    fun `When deleting message with not playing audio, audio is not stopped before deletion`() = runTest {
        val messageId = randomString()
        val audioRecording = randomAttachment(type = AttachmentType.AUDIO_RECORDING)
        val messages = listOf(randomMessage(id = messageId, attachments = listOf(audioRecording)))
        val messagesState = MutableStateFlow(messages)

        val audioPlayer = mock<AudioPlayer>().apply {
            whenever(currentState) doReturn AudioState.IDLE
            whenever(currentPlayingId) doReturn audioRecording.audioHash
        }

        val controller = Fixture()
            .givenCurrentUser()
            .givenChannelState(messagesState = messagesState)
            .givenAudioPlayer(audioPlayer)
            .givenDeleteMessage(callFrom { messages.first() })
            .get()
        controller.deleteMessage(messages.first())
        verify(audioPlayer, times(0)).pause()
    }

    @Test
    fun `When deleting message with audio attachment, and different audio is playing, audio is not stopped before deletion`() = runTest {
        val messageId = randomString()
        val audioRecording = randomAttachment(type = AttachmentType.AUDIO_RECORDING)
        val messages = listOf(randomMessage(id = messageId, attachments = listOf(audioRecording)))
        val messagesState = MutableStateFlow(messages)

        val audioPlayer = mock<AudioPlayer>().apply {
            whenever(currentState) doReturn AudioState.PLAYING
            whenever(currentPlayingId) doReturn randomAttachment().audioHash
        }

        val controller = Fixture()
            .givenCurrentUser()
            .givenChannelState(messagesState = messagesState)
            .givenAudioPlayer(audioPlayer)
            .givenDeleteMessage(callFrom { messages.first() })
            .get()
        controller.deleteMessage(messages.first())
        verify(audioPlayer, times(0)).pause()
    }

    @Test
    fun `When deleting message without attachments, audio is not stopped before deletion`() = runTest {
        val messageId = randomString()
        val messages = listOf(randomMessage(id = messageId))
        val messagesState = MutableStateFlow(messages)
        val audioPlayer = mock<AudioPlayer>().apply {
            whenever(currentState) doReturn AudioState.PLAYING
            whenever(currentPlayingId) doReturn randomInt()
        }
        val controller = Fixture()
            .givenCurrentUser()
            .givenChannelState(messagesState = messagesState)
            .givenAudioPlayer(audioPlayer)
            .givenDeleteMessage(callFrom { messages.first() })
            .get()
        controller.deleteMessage(messages.first())
        verify(audioPlayer, times(0)).pause()
    }

    @Test
    fun `When calling pauseAudioRecordingAttachments, audioPlayer is invoked`() = runTest {
        val audioPlayer = mock<AudioPlayer>()
        val messagesState = MutableStateFlow(emptyList<Message>())
        val controller = Fixture()
            .givenCurrentUser()
            .givenChannelState(messagesState = messagesState)
            .givenAudioPlayer(audioPlayer)
            .get()
        controller.pauseAudioRecordingAttachments()
        verify(audioPlayer).pause()
    }

    @Test
    fun `When calling castVote, ChannelClient castPollVote is invoked`() = runTest {
        val messageId = randomString()
        val pollId = randomString()
        val option = randomPollOption()
        val messagesState = MutableStateFlow(emptyList<Message>())
        val chatClient = mock<ChatClient>()
        val controller = Fixture(chatClient = chatClient)
            .givenCurrentUser()
            .givenChannelState(messagesState = messagesState)
            .givenCastVote(callFrom { randomPollVote() })
            .get()
        controller.castVote(messageId, pollId, option)

        verify(chatClient).castPollVote(messageId, pollId, option)
    }

    @Test
    fun `When castVote fails, errorEvent is emitted`() = runTest {
        val messageId = randomString()
        val pollId = randomString()
        val option = randomPollOption()
        val messagesState = MutableStateFlow(emptyList<Message>())
        val error = Error.GenericError("error")
        val chatClient = mock<ChatClient>()
        val controller = Fixture(chatClient = chatClient)
            .givenCurrentUser()
            .givenChannelState(messagesState = messagesState)
            .givenCastVote(TestCall(Result.Failure(error)))
            .get()
        controller.castVote(messageId, pollId, option)

        val expectedEvent = MessageListController.ErrorEvent.PollCastingVoteError(error)
        controller.errorEvents.value `should be equal to` expectedEvent
    }

    @Test
    fun `When calling removeVote, ChannelClient removePollVote is invoked`() = runTest {
        val messageId = randomString()
        val pollId = randomString()
        val vote = randomPollVote()
        val messagesState = MutableStateFlow(emptyList<Message>())
        val chatClient = mock<ChatClient>()
        val controller = Fixture(chatClient = chatClient)
            .givenCurrentUser()
            .givenChannelState(messagesState = messagesState)
            .givenRemoveVote(callFrom { randomPollVote() })
            .get()
        controller.removeVote(messageId, pollId, vote)

        verify(chatClient).removePollVote(messageId, pollId, vote.id)
    }

    @Test
    fun `When removeVote fails, errorEvent is emitted`() = runTest {
        val messageId = randomString()
        val pollId = randomString()
        val vote = randomPollVote()
        val messagesState = MutableStateFlow(emptyList<Message>())
        val error = Error.GenericError("error")
        val chatClient = mock<ChatClient>()
        val controller = Fixture(chatClient = chatClient)
            .givenCurrentUser()
            .givenChannelState(messagesState = messagesState)
            .givenRemoveVote(TestCall(Result.Failure(error)))
            .get()
        controller.removeVote(messageId, pollId, vote)

        val expectedEvent = MessageListController.ErrorEvent.PollRemovingVoteError(error)
        controller.errorEvents.value `should be equal to` expectedEvent
    }

    @Test
    fun `When toggleOriginalText, the message translation is toggled`() = runTest {
        val messageId = randomString()
        val message = randomMessage(
            id = messageId,
            text = "Original text",
            i18n = mapOf("fr" to "Texte original"),
        )
        val user = randomUser(language = "fr")
        val controller = Fixture()
            .givenCurrentUser(user)
            .givenChannelState(messagesState = MutableStateFlow(listOf(message)))
            .get(dateSeparatorHandler = { _, _ -> false })
        controller.messageListState.test {
            // Verify messageItem.showOriginalText is false initially
            val initialState = awaitItem()
            val initialMessageItem = initialState.messageItems.first() as MessageItemState
            Assertions.assertFalse(initialMessageItem.showOriginalText)
            // Toggle original text
            controller.toggleOriginalText(messageId)
            // Verify messageItem.showOriginalText is true after toggling
            val toggledState = awaitItem()
            val toggledMessageItem = toggledState.messageItems.first() as MessageItemState
            Assertions.assertTrue(toggledMessageItem.showOriginalText)
            // Toggle original text again
            controller.toggleOriginalText(messageId)
            // Verify messageItem.showOriginalText is false after toggling again
            val toggledBackState = awaitItem()
            val toggledBackMessageItem = toggledBackState.messageItems.first() as MessageItemState
            Assertions.assertFalse(toggledBackMessageItem.showOriginalText)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `When reactToMessage is called with skipPush set to true, sendReaction is invoked with skipPush true`() = runTest {
        val messageId = randomString()
        val reactionType = "love"
        val reaction = randomReaction(messageId = messageId, type = reactionType)
        val message = randomMessage(id = messageId, ownReactions = emptyList())
        val messagesState = MutableStateFlow(listOf(message))
        val chatClient = mock<ChatClient>()
        val controller = Fixture(chatClient = chatClient)
            .givenCurrentUser()
            .givenChannelState(messagesState = messagesState)
            .givenSendReaction(callFrom { reaction })
            .get()

        controller.reactToMessage(reaction, message, skipPush = true)

        verify(chatClient).sendReaction(
            enforceUnique = true,
            reaction = reaction,
            cid = CID,
            skipPush = true,
        )
    }

    @Test
    fun `When reactToMessage is called with default skipPush value, sendReaction is invoked with skipPush false`() = runTest {
        val messageId = randomString()
        val reactionType = "love"
        val reaction = randomReaction(messageId = messageId, type = reactionType)
        val message = randomMessage(id = messageId, ownReactions = emptyList())
        val messagesState = MutableStateFlow(listOf(message))
        val chatClient = mock<ChatClient>()
        val controller = Fixture(chatClient = chatClient)
            .givenCurrentUser()
            .givenChannelState(messagesState = messagesState)
            .givenSendReaction(callFrom { reaction })
            .get()

        controller.reactToMessage(reaction, message)

        verify(chatClient).sendReaction(
            enforceUnique = true,
            reaction = reaction,
            cid = CID,
            skipPush = false,
        )
    }

    @Test
    fun `When reactToMessage is called for existing reaction, deleteReaction is invoked`() = runTest {
        val messageId = randomString()
        val reactionType = "love"
        val reaction = randomReaction(messageId = messageId, type = reactionType)
        val existingReaction = randomReaction(messageId = messageId, type = reactionType)
        val message = randomMessage(id = messageId, ownReactions = listOf(existingReaction))
        val messagesState = MutableStateFlow(listOf(message))
        val chatClient = mock<ChatClient>()
        val controller = Fixture(chatClient = chatClient)
            .givenCurrentUser()
            .givenChannelState(messagesState = messagesState)
            .givenDeleteReaction(callFrom { message })
            .get()

        controller.reactToMessage(reaction, message)

        verify(chatClient).deleteReaction(
            messageId = messageId,
            reactionType = reactionType,
            cid = CID,
        )
    }

    @Test
    fun `When reactToMessage fails to send reaction, error is logged but no exception is thrown`() = runTest {
        val messageId = randomString()
        val reactionType = "love"
        val reaction = randomReaction(messageId = messageId, type = reactionType)
        val message = randomMessage(id = messageId, ownReactions = emptyList())
        val messagesState = MutableStateFlow(listOf(message))
        val error = Error.GenericError("Failed to send reaction")
        val chatClient = mock<ChatClient>()
        val controller = Fixture(chatClient = chatClient)
            .givenCurrentUser()
            .givenChannelState(messagesState = messagesState)
            .givenSendReaction(TestCall(Result.Failure(error)))
            .get()

        // Should not throw exception
        controller.reactToMessage(reaction, message)

        verify(chatClient).sendReaction(
            enforceUnique = true,
            reaction = reaction,
            cid = CID,
            skipPush = false,
        )
    }

    @Test
    fun `When reactToMessage fails to delete reaction, error is logged but no exception is thrown`() = runTest {
        val messageId = randomString()
        val reactionType = "love"
        val reaction = randomReaction(messageId = messageId, type = reactionType)
        val existingReaction = randomReaction(messageId = messageId, type = reactionType)
        val message = randomMessage(id = messageId, ownReactions = listOf(existingReaction))
        val messagesState = MutableStateFlow(listOf(message))
        val error = Error.GenericError("Failed to delete reaction")
        val chatClient = mock<ChatClient>()
        val controller = Fixture(chatClient = chatClient)
            .givenCurrentUser()
            .givenChannelState(messagesState = messagesState)
            .givenDeleteReaction(TestCall(Result.Failure(error)))
            .get()

        // Should not throw exception
        controller.reactToMessage(reaction, message)

        verify(chatClient).deleteReaction(
            messageId = messageId,
            reactionType = reactionType,
            cid = CID,
        )
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

        fun givenMarkMessageUnread() = apply {
            whenever(chatClient.markUnread(any(), any(), any())) doReturn Unit.asCall()
        }

        fun givenDeleteMessage(message: Call<Message>) = apply {
            whenever(chatClient.deleteMessage(any(), any())) doReturn message
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
            read: StateFlow<ChannelUserRead?> = MutableStateFlow(randomChannelUserRead(lastReadMessageId = null)),
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
            whenever(channelState.read) doReturn read
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

        fun givenAudioPlayer(audioPlayer: AudioPlayer) = apply {
            whenever(chatClient.audioPlayer) doReturn audioPlayer
        }

        fun givenCastVote(vote: Call<Vote>) = apply {
            whenever(chatClient.castPollVote(any(), any(), any())) doReturn vote
        }

        fun givenRemoveVote(vote: Call<Vote>) = apply {
            whenever(chatClient.removePollVote(any(), any(), voteId = any())) doReturn vote
        }

        fun givenSendReaction(reaction: Call<Reaction>) = apply {
            whenever(chatClient.sendReaction(any(), any(), any(), any())) doReturn reaction
        }

        fun givenDeleteReaction(message: Call<Message>) = apply {
            whenever(chatClient.deleteReaction(any(), any(), any())) doReturn message
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
            )
            return channel.copy(
                config = channelConfig.value,
                hidden = hidden.value,
                isInsideSearch = insideSearch,
            )
        }
    }
}

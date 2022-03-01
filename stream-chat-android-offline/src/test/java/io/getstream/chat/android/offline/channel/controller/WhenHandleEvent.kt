package io.getstream.chat.android.offline.channel.controller

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.SynchronizedCoroutineTest
import io.getstream.chat.android.offline.experimental.channel.logic.ChannelLogic
import io.getstream.chat.android.offline.experimental.channel.state.ChannelMutableState
import io.getstream.chat.android.offline.message.attachment.AttachmentUrlValidator
import io.getstream.chat.android.offline.randomChannel
import io.getstream.chat.android.offline.randomChannelDeletedEvent
import io.getstream.chat.android.offline.randomChannelUpdatedEvent
import io.getstream.chat.android.offline.randomMember
import io.getstream.chat.android.offline.randomMemberAddedEvent
import io.getstream.chat.android.offline.randomMessage
import io.getstream.chat.android.offline.randomMessageReadEvent
import io.getstream.chat.android.offline.randomMessageUpdateEvent
import io.getstream.chat.android.offline.randomNewMessageEvent
import io.getstream.chat.android.offline.randomNotificationMarkReadEvent
import io.getstream.chat.android.offline.randomReactionNewEvent
import io.getstream.chat.android.offline.randomTypingStartEvent
import io.getstream.chat.android.offline.randomTypingStopEvent
import io.getstream.chat.android.offline.randomUser
import io.getstream.chat.android.test.TestCoroutineRule
import io.getstream.chat.android.test.randomDate
import io.getstream.chat.android.test.randomDateAfter
import io.getstream.chat.android.test.randomDateBefore
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestCoroutineScope
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeEqualTo
import org.junit.Rule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Date

@ExperimentalCoroutinesApi
internal class WhenHandleEvent : SynchronizedCoroutineTest {

    @get:Rule
    val testCoroutines = TestCoroutineRule()
    private val channelId = randomString()
    private val currentUser = User(id = CURRENT_USER_ID)
    private val userFlow = MutableStateFlow(currentUser)

    override fun getTestScope(): TestCoroutineScope = testCoroutines.scope

    private val chatClient: ChatClient = mock {
        on(it.channel(any())) doReturn mock()
    }
    private val chatDomain: ChatDomainImpl = mock {
        on(it.appContext) doReturn mock()
        on(it.scope) doReturn testCoroutines.scope
        on(it.user) doReturn userFlow
        on(it.getChannelConfig(any())) doReturn Config(connectEventsEnabled = true, muteEnabled = true)
    }
    private val attachmentUrlValidator: AttachmentUrlValidator = mock()

    private lateinit var channelLogic: ChannelLogic
    private lateinit var channelMutableState: ChannelMutableState

    @BeforeEach
    fun setUp() {
        whenever(attachmentUrlValidator.updateValidAttachmentsUrl(any(), any())) doAnswer { invocation ->
            invocation.arguments[0] as List<Message>
        }

        channelMutableState = ChannelMutableState(
            "type1", channelId, testCoroutines.scope, userFlow,
            MutableStateFlow(
                mapOf(currentUser.id to currentUser)
            )
        )

        channelLogic = ChannelLogic(
            channelMutableState,
            chatDomain,
            attachmentUrlValidator
        )
    }

    // User watching event
    @Test
    fun `when user watching event arrives, last message should be updated`() {
        val user = User()
        val newDate = Date(Long.MAX_VALUE)
        val newMessage = randomMessage(
            id = "thisId",
            createdAt = newDate,
            silent = false,
            showInChannel = true
        )

        whenever(attachmentUrlValidator.updateValidAttachmentsUrl(any(), any())) doReturn listOf(newMessage)

        val userStartWatchingEvent = randomNewMessageEvent(user = user, createdAt = newDate, message = newMessage)

        channelLogic.handleEvent(userStartWatchingEvent)

        channelMutableState.toChannel().lastMessageAt shouldBeEqualTo newDate
    }

    // New message event
    @Test
    fun `when new message event arrives, messages should be propagated correctly`(): Unit = coroutineTest {
        val user = User(id = CURRENT_USER_ID)
        val message = randomMessage(
            createdAt = Date(1000L),
            user = user,
            silent = false,
            showInChannel = true
        )
        val newMessageEvent = randomNewMessageEvent(user = user, message = message)
        whenever(attachmentUrlValidator.updateValidAttachmentsUrl(any(), any())) doReturn listOf(message)

        channelLogic.handleEvent(newMessageEvent)

        // Message is propagated
        channelMutableState.messages.value shouldBeEqualTo listOf(message)
        // Unread count should not be propagated, because it is a message form the same user
        channelMutableState.unreadCount.value shouldBeEqualTo 0
        // Last message is updated
        channelMutableState.toChannel().lastMessageAt shouldBeEqualTo Date(1000L)
    }

    @Test
    fun `when new message event arrives from other user, unread number should be updated`() = coroutineTest {
        val createdAt = Date()
        val message = randomMessage(
            createdAt = createdAt,
            user = User(id = "otherUserId"),
            silent = false,
            showInChannel = true
        )

        val newMessageEvent = randomNewMessageEvent(message = message)

        channelLogic.handleEvent(newMessageEvent)

        // Message is propagated
        channelMutableState.messages.value shouldBeEqualTo listOf(message)

        // Unread count should be propagated, because it is a message form another user
        channelMutableState.unreadCount.value shouldBeEqualTo 1

        // Last message is updated
        channelMutableState.toChannel().lastMessageAt shouldBeEqualTo createdAt
    }

    // Message update
    @Test
    fun `when a message update for a non existing message arrives, it is added`() = coroutineTest {
        val messageId = randomString()
        val message = randomMessage(
            id = messageId,
            user = User(id = "otherUserId"),
            silent = false,
            showInChannel = true
        )

        val messageUpdateEvent = randomMessageUpdateEvent(message = message)

        channelLogic.handleEvent(messageUpdateEvent)

        channelMutableState.messages.value.first() shouldBeEqualTo message
    }

    @Test
    fun `when a message update event is outdated, it should be ignored`() = coroutineTest {
        val messageId = randomString()
        val createdAt = randomDate()
        val createdLocallyAt = randomDateBefore(createdAt.time)
        val updatedAt = randomDateAfter(createdAt.time)
        val oldUpdatedAt = randomDateBefore(updatedAt.time)
        val recentMessage = randomMessage(
            id = messageId,
            user = User(id = "otherUserId"),
            createdAt = createdAt,
            createdLocallyAt = createdLocallyAt,
            updatedAt = updatedAt,
            updatedLocallyAt = updatedAt,
            deletedAt = null,
            silent = false,
            showInChannel = true
        )
        val oldMessage = randomMessage(
            id = messageId,
            user = User(id = "otherUserId"),
            createdAt = createdAt,
            createdLocallyAt = createdLocallyAt,
            updatedAt = oldUpdatedAt,
            updatedLocallyAt = oldUpdatedAt,
            deletedAt = null,
            silent = false,
            showInChannel = true
        )
        channelLogic.upsertMessages(listOf(recentMessage))
        val messageUpdateEvent = randomMessageUpdateEvent(message = oldMessage)

        channelLogic.handleEvent(messageUpdateEvent)

        channelMutableState.messages.value shouldBeEqualTo listOf(recentMessage)
        channelMutableState.messages.value shouldNotBeEqualTo listOf(oldMessage)
    }

    // Member added event
    @Test
    fun `when member is added, it should be propagated`(): Unit = coroutineTest {
        val user = randomUser()
        val member = randomMember(user = user)
        val memberAddedEvent = randomMemberAddedEvent(user = user, member = member)

        channelLogic.handleEvent(memberAddedEvent)

        channelMutableState.members.value shouldBeEqualTo listOf(member)
    }

    // Typing events
    @Test
    fun `when events of start and stop tying arrive, it should be correctly propagated`() {
        val user1 = randomUser()
        val user2 = randomUser()

        val typingStartEvent1 = randomTypingStartEvent(user = user1, channelId = channelId)
        val typingStartEvent2 = randomTypingStartEvent(user = user2, channelId = channelId)
        val typingStopEvent = randomTypingStopEvent(user = user2, channelId = channelId)

        channelLogic.run {
            handleEvent(typingStartEvent1)
            channelMutableState.typing.value.users shouldBeEqualTo listOf(user1)

            handleEvent(typingStartEvent2)
            channelMutableState.typing.value.users shouldBeEqualTo listOf(user1, user2)

            handleEvent(typingStopEvent)
            channelMutableState.typing.value.users shouldBeEqualTo listOf(user1)
        }
    }

    // Read event
    @Test
    fun `when read notification event arrives, it should be correctly propagated`() {
        val readEvent = randomNotificationMarkReadEvent(user = currentUser)

        channelLogic.handleEvent(readEvent)

        channelMutableState.reads.value shouldBeEqualTo listOf(ChannelUserRead(readEvent.user, readEvent.createdAt))
    }

    // Read event notification
    @Test
    fun `when read event arrives, it should be correctly propagated`() {
        val readEvent = randomMessageReadEvent(user = currentUser)

        channelLogic.handleEvent(readEvent)

        channelMutableState.reads.value shouldBeEqualTo listOf(ChannelUserRead(readEvent.user, readEvent.createdAt))
    }

    // Reaction event
    @Test
    fun `when reaction event arrives, the message of the event should be upsert`(): Unit = coroutineTest {
        val message = randomMessage(
            showInChannel = true,
            silent = false,
        )
        val reactionEvent = randomReactionNewEvent(user = randomUser(), message = message)
        whenever(attachmentUrlValidator.updateValidAttachmentsUrl(any(), any())) doReturn listOf(message)

        channelLogic.handleEvent(reactionEvent)

        // Message is propagated
        channelMutableState.messages.value shouldBeEqualTo listOf(message)
    }

    // Channel deleted event
    @Test
    fun `when channel is deleted, messages are deleted too`() {
        val deleteChannelEvent = randomChannelDeletedEvent()

        channelLogic.handleEvent(deleteChannelEvent)

        channelMutableState.messages.value.shouldBeEmpty()
    }

    @Test
    fun `when channel is deleted, the status is updated`() = coroutineTest {
        val channel = randomChannel()
        val deleteChannelEvent = randomChannelDeletedEvent(channel = channel)
        val updateChannelEvent = randomChannelUpdatedEvent(channel = channel)

        channelLogic.handleEvent(updateChannelEvent)
        channelLogic.handleEvent(deleteChannelEvent)

        val channelFlowValue = channelMutableState.channelData.value
        channelFlowValue.channelId shouldBeEqualTo channel.id
        channelFlowValue.deletedAt shouldBeEqualTo deleteChannelEvent.createdAt
    }

    private companion object {
        private const val CURRENT_USER_ID = "currentUserId"
    }
}

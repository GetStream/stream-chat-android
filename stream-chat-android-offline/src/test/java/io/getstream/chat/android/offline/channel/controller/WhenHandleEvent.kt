package io.getstream.chat.android.offline.channel.controller

import com.google.common.truth.Truth
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
import io.getstream.chat.android.offline.channel.ChannelController
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
import io.getstream.chat.android.test.randomDate
import io.getstream.chat.android.test.randomDateAfter
import io.getstream.chat.android.test.randomDateBefore
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Date

@ExperimentalCoroutinesApi
internal class WhenHandleEvent {

    private val channelId = randomString()
    private val currentUser = User(id = CURRENT_USER_ID)

    private val chatClient: ChatClient = mock()
    private val chatDomain: ChatDomainImpl = mock {
        on(it.scope) doReturn TestCoroutineScope()
        on(it.currentUser) doReturn currentUser
        on(it.getChannelConfig(any())) doReturn Config(isConnectEvents = true, isMutes = true)
    }
    private val attachmentUrlValidator: AttachmentUrlValidator = mock()

    private lateinit var channelController: ChannelController

    @BeforeEach
    fun setUp() {
        whenever(attachmentUrlValidator.updateValidAttachmentsUrl(any(), any())) doAnswer { invocation ->
            invocation.arguments[0] as List<Message>
        }

        channelController = ChannelController(
            channelType = "type1",
            channelId = channelId,
            client = chatClient,
            domainImpl = chatDomain,
            messageHelper = attachmentUrlValidator
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

        channelController.handleEvent(userStartWatchingEvent)

        Truth.assertThat(channelController.toChannel().lastMessageAt).isEqualTo(newDate)
    }

    // New message event
    @Test
    fun `when new message event arrives, messages should be propagated correctly`() {
        val user = User(id = CURRENT_USER_ID)
        val message = randomMessage(
            createdAt = Date(1000L),
            user = user,
            silent = false,
            showInChannel = true
        )
        val newMessageEvent = randomNewMessageEvent(user = user, message = message)
        whenever(attachmentUrlValidator.updateValidAttachmentsUrl(any(), any())) doReturn listOf(message)

        channelController.handleEvent(newMessageEvent)

        // Message is propagated
        Truth.assertThat(channelController.messages.value).isEqualTo(listOf(message))
        // Unread count should not be propagated, because it is a message form the same user
        Truth.assertThat(channelController.unreadCount.value).isEqualTo(0)
        // Last message is updated
        Truth.assertThat(channelController.toChannel().lastMessageAt).isEqualTo(Date(1000L))
    }

    @Test
    fun `when new message event arrives from other user, unread number should be updated`() {
        val createdAt = Date()
        val message = randomMessage(
            createdAt = createdAt,
            user = User(id = "otherUserId"),
            silent = false,
            showInChannel = true
        )

        val newMessageEvent = randomNewMessageEvent(message = message)

        channelController.handleEvent(newMessageEvent)

        // Message is propagated
        Truth.assertThat(channelController.messages.value).isEqualTo(listOf(message))

        // Unread count should be propagated, because it is a message form another user
        Truth.assertThat(channelController.unreadCount.value).isEqualTo(1)

        // Last message is updated
        Truth.assertThat(channelController.toChannel().lastMessageAt).isEqualTo(createdAt)
    }

    // Message update
    @Test
    fun `when a message update for a non existing message arrives, it is added`() {
        val messageId = randomString()
        val message = randomMessage(
            id = messageId,
            user = User(id = "otherUserId"),
            silent = false,
            showInChannel = true
        )

        val messageUpdateEvent = randomMessageUpdateEvent(message = message)

        channelController.handleEvent(messageUpdateEvent)

        Truth.assertThat(channelController.messages.value.first()).isEqualTo(message)
    }

    @Test
    fun `when a message update event is outdated, it should be ignored`() {
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
        channelController.upsertMessage(recentMessage)
        val messageUpdateEvent = randomMessageUpdateEvent(message = oldMessage)

        channelController.handleEvent(messageUpdateEvent)

        Truth.assertThat(channelController.messages.value).isEqualTo(listOf(recentMessage))
        Truth.assertThat(channelController.messages.value).isNotEqualTo(listOf(oldMessage))
    }

    // Member added event
    @Test
    fun `when member is added, it should be propagated`() {
        val user = randomUser()
        val member = randomMember(user = user)
        val memberAddedEvent = randomMemberAddedEvent(user = user, member = member)

        channelController.handleEvent(memberAddedEvent)

        Truth.assertThat(channelController.members.value).isEqualTo(listOf(member))
    }

    // Typing events
    @Test
    fun `when events of start and stop tying arrive, it should be correctly propagated`() {
        val user1 = randomUser()
        val user2 = randomUser()

        val typingStartEvent1 = randomTypingStartEvent(user = user1, channelId = channelId)
        val typingStartEvent2 = randomTypingStartEvent(user = user2, channelId = channelId)
        val typingStopEvent = randomTypingStopEvent(user = user2, channelId = channelId)

        channelController.run {
            handleEvent(typingStartEvent1)
            Truth.assertThat(typing.value.users).isEqualTo(listOf(user1))

            handleEvent(typingStartEvent2)
            Truth.assertThat(typing.value.users).isEqualTo(listOf(user1, user2))

            handleEvent(typingStopEvent)
            Truth.assertThat(typing.value.users).isEqualTo(listOf(user1))
        }
    }

    // Read event
    @Test
    fun `when read notification event arrives, it should be correctly propagated`() {
        val readEvent = randomNotificationMarkReadEvent(user = currentUser)

        channelController.handleEvent(readEvent)

        Truth.assertThat(channelController.reads.value)
            .isEqualTo(listOf(ChannelUserRead(readEvent.user, readEvent.createdAt)))
    }

    // Read event notification
    @Test
    fun `when read event arrives, it should be correctly propagated`() {
        val readEvent = randomMessageReadEvent(user = currentUser)

        channelController.handleEvent(readEvent)

        Truth.assertThat(channelController.reads.value)
            .isEqualTo(listOf(ChannelUserRead(readEvent.user, readEvent.createdAt)))
    }

    // Reaction event
    @Test
    fun `when reaction event arrives, the message of the event should be upsert`() {
        val message = randomMessage(
            showInChannel = true,
            silent = false,
        )
        val reactionEvent = randomReactionNewEvent(user = randomUser(), message = message)
        whenever(attachmentUrlValidator.updateValidAttachmentsUrl(any(), any())) doReturn listOf(message)

        channelController.handleEvent(reactionEvent)

        // Message is propagated
        Truth.assertThat(channelController.messages.value).isEqualTo(listOf(message))
    }

    // Channel deleted event
    @Test
    fun `when channel is deleted, messages are deleted too`() {
        val deleteChannelEvent = randomChannelDeletedEvent()

        channelController.handleEvent(deleteChannelEvent)

        Truth.assertThat(channelController.messages.value).isEmpty()
    }

    @Test
    fun `when channel is deleted, the status is updated`() {
        val channel = randomChannel()
        val deleteChannelEvent = randomChannelDeletedEvent(channel = channel)
        val updateChannelEvent = randomChannelUpdatedEvent(channel = channel)

        channelController.handleEvent(updateChannelEvent)
        channelController.handleEvent(deleteChannelEvent)

        val channelFlowValue = channelController.channelData.value
        Truth.assertThat(channelFlowValue.channelId).isEqualTo(channel.id)
        Truth.assertThat(channelFlowValue.deletedAt).isEqualTo(deleteChannelEvent.createdAt)
    }

    private companion object {
        private const val CURRENT_USER_ID = "currentUserId"
    }
}

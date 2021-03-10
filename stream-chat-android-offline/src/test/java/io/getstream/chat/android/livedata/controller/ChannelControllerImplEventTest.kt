package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.Observer
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.controller.helper.MessageHelper
import io.getstream.chat.android.livedata.randomMemberAddedEvent
import io.getstream.chat.android.livedata.randomMessage
import io.getstream.chat.android.livedata.randomMessageReadEvent
import io.getstream.chat.android.livedata.randomMessageUpdateEvent
import io.getstream.chat.android.livedata.randomNewMessageEvent
import io.getstream.chat.android.livedata.randomNotificationMarkReadEvent
import io.getstream.chat.android.livedata.randomNotificationMessageNewEvent
import io.getstream.chat.android.livedata.randomReactionNewEvent
import io.getstream.chat.android.livedata.randomTypingStartEvent
import io.getstream.chat.android.livedata.randomTypingStopEvent
import io.getstream.chat.android.livedata.randomUser
import io.getstream.chat.android.test.InstantTaskExecutorExtension
import io.getstream.chat.android.test.randomInt
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Date

private const val CURRENT_USER_ID = "currentUserId"

@ExperimentalCoroutinesApi
@ExtendWith(InstantTaskExecutorExtension::class)
internal class ChannelControllerImplEventNewTest {

    private val channelId = randomString()
    private val currentUser = User(id = CURRENT_USER_ID)

    private val chatClient: ChatClient = mock()
    private val chatDomain: ChatDomainImpl = mock {
        on(it.scope) doReturn TestCoroutineScope()
        on(it.currentUser) doReturn currentUser
        on(it.getChannelConfig(any())) doReturn Config(isConnectEvents = true, isMutes = true)
    }
    private val messageHelper: MessageHelper = mock()

    private lateinit var channelControllerImpl: ChannelControllerImpl

    @BeforeEach
    fun setUp() {
        whenever(messageHelper.updateValidAttachmentsUrl(any(), any())) doAnswer { invocation ->
            invocation.arguments[0] as List<Message>
        }

        channelControllerImpl = ChannelControllerImpl(
            channelType = "type1",
            channelId = channelId,
            client = chatClient,
            domainImpl = chatDomain,
            messageHelper = messageHelper
        )
    }

    // User watching event
    @Test
    fun `when user watching event arrives, last message should be updated`() {
        val user = User()
        val newDate = Date(Long.MAX_VALUE)
        val newMessage = randomMessage(id = "thisId", createdAt = newDate)

        whenever(messageHelper.updateValidAttachmentsUrl(any(), any())) doReturn listOf(newMessage)

        val userStartWatchingEvent = randomNewMessageEvent(user = user, createdAt = newDate, message = newMessage)

        channelControllerImpl.handleEvent(userStartWatchingEvent)

        Truth.assertThat(channelControllerImpl.toChannel().lastMessageAt).isEqualTo(newDate)
    }

    // New message event
    @Test
    fun `when new message event arrives, messages should be propagated correctly`() {
        val updatedAt = Date()
        val user = User(id = CURRENT_USER_ID)
        val message = randomMessage(updatedAt = updatedAt, user = user)

        val newMessageEvent = randomNewMessageEvent(user = user, message = message)

        val messageObserver: Observer<List<Message>> = mock()
        val unreadCountObserver: Observer<Int?> = mock()

        channelControllerImpl.messages.observeForever(messageObserver)
        channelControllerImpl.unreadCount.observeForever(unreadCountObserver)

        whenever(messageHelper.updateValidAttachmentsUrl(any(), any())) doReturn listOf(message)

        channelControllerImpl.handleEvent(newMessageEvent)

        // Message is propagated
        verify(messageObserver).onChanged(listOf(message))

        // Unread count should not be propagated, because it is a message form the same user
        verify(unreadCountObserver, never()).onChanged(1)

        // Last message is updated
        channelControllerImpl.toChannel().lastMessageAt = updatedAt
    }

    @Test
    fun `when new message event arrives from other user, unread number should be updated`() {
        val updatedAt = Date()
        val message = randomMessage(updatedAt = updatedAt, user = User(id = "otherUserId"), silent = false)

        val newMessageEvent = randomNewMessageEvent(message = message)

        val messageObserver: Observer<List<Message>> = mock()
        val unreadCountObserver: Observer<Int?> = mock()

        channelControllerImpl.messages.observeForever(messageObserver)
        channelControllerImpl.unreadCount.observeForever(unreadCountObserver)

        channelControllerImpl.handleEvent(newMessageEvent)

        // Message is propagated
        verify(messageObserver).onChanged(listOf(message))

        // Unread count should be propagated, because it is a message form another user
        verify(unreadCountObserver).onChanged(1)

        // Last message is updated
        channelControllerImpl.toChannel().lastMessageAt = updatedAt
    }

    // Message update
    @Test
    fun `when a message update for a non existing message arrives, it is added`() {
        val messageId = randomString()
        val message = randomMessage(id = messageId, user = User(id = "otherUserId"), silent = false)

        val messageUpdateEvent = randomMessageUpdateEvent(message = message)
        val messageObserver: Observer<List<Message>> = mock()

        channelControllerImpl.messages.observeForever(messageObserver)
        channelControllerImpl.handleEvent(messageUpdateEvent)

        verify(messageObserver, atLeastOnce()).onChanged(
            argThat { messageList ->
                if (messageList.isNotEmpty()) {
                    messageList.first().id == messageId
                } else {
                    true
                }
            }
        )
    }

    @Test
    fun `when a message update event is outdated, it should be ignored`() {
        val recentMessage = randomMessage(user = User(id = "otherUserId"), updatedAt = Date(), silent = false)
        val oldMessage = randomMessage(
            user = User(id = "otherUserId"),
            updatedAt = Date(Long.MIN_VALUE),
            silent = false
        )

        val messageUpdateEvent = randomMessageUpdateEvent()

        channelControllerImpl.upsertMessage(recentMessage)

        val messageObserver: Observer<List<Message>> = mock()

        channelControllerImpl.messages.observeForever(messageObserver)
        channelControllerImpl.handleEvent(messageUpdateEvent)

        // No message is added
        verify(messageObserver, never()).onChanged(listOf(oldMessage))
    }

    // New message notification event
    @Test
    fun `when a new message notification event comes, watchers should be incremented accordingly`() {
        val watcherCount = randomInt()

        val notificationEvent = randomNotificationMessageNewEvent(watcherCount = watcherCount)

        val watchersCountObserver: Observer<Int> = mock()

        channelControllerImpl.watcherCount.observeForever(watchersCountObserver)
        channelControllerImpl.handleEvent(notificationEvent)

        verify(watchersCountObserver).onChanged(watcherCount)
    }

    // Member added event
    @Test
    fun `when member is added, it should be propagated`() {
        val user = randomUser()
        val member = Member(user = user)

        val memberAddedEvent = randomMemberAddedEvent(user = user, member = member)

        val membersCountObserver: Observer<List<Member>> = mock()

        channelControllerImpl.members.observeForever(membersCountObserver)
        channelControllerImpl.handleEvent(memberAddedEvent)

        verify(membersCountObserver).onChanged(listOf(member))
    }

    // Typing events
    @Test
    fun `when events of start and stop tying arrive, it should be correctly propagated`() {
        val user1 = randomUser()
        val user2 = randomUser()

        val typingStartEvent1 = randomTypingStartEvent(user = user1, channelId = channelId)
        val typingStartEvent2 = randomTypingStartEvent(user = user2, channelId = channelId)
        val typingStopEvent = randomTypingStopEvent(user = user2, channelId = channelId)

        val typingStartObserver: Observer<TypingEvent> = mock()

        channelControllerImpl.run {
            typing.observeForever(typingStartObserver)
            handleEvent(typingStartEvent1)
            handleEvent(typingStartEvent2)
            handleEvent(typingStopEvent)
        }

        inOrder(typingStartObserver).run {
            verify(typingStartObserver).onChanged(TypingEvent(channelId, listOf(user1)))
            verify(typingStartObserver).onChanged(TypingEvent(channelId, listOf(user1, user2)))
            verify(typingStartObserver).onChanged(TypingEvent(channelId, listOf(user1)))
        }
    }

    // Read event
    @Test
    fun `when read notification event arrives, it should be correctly propagated`() {
        val readEvent = randomNotificationMarkReadEvent(user = currentUser)

        val readObserver: Observer<List<ChannelUserRead>> = mock()

        channelControllerImpl.reads.observeForever(readObserver)
        channelControllerImpl.handleEvent(readEvent)

        verify(readObserver).onChanged(listOf(ChannelUserRead(readEvent.user, readEvent.createdAt)))
    }

    // Read event notification
    @Test
    fun `when read event arrives, it should be correctly propagated`() {
        val readEvent = randomMessageReadEvent(user = currentUser)

        val readObserver: Observer<List<ChannelUserRead>> = mock()

        channelControllerImpl.reads.observeForever(readObserver)
        channelControllerImpl.handleEvent(readEvent)

        verify(readObserver).onChanged(listOf(ChannelUserRead(readEvent.user, readEvent.createdAt)))
    }

    // Reaction event
    @Test
    fun `when reaction event arrives, the message of the event should be upsert`() {
        val message = randomMessage(showInChannel = true)
        val reactionEvent = randomReactionNewEvent(user = randomUser(), message = message)

        val messageObserver: Observer<List<Message>> = mock()

        whenever(messageHelper.updateValidAttachmentsUrl(any(), any())) doReturn listOf(message)

        channelControllerImpl.messages.observeForever(messageObserver)
        channelControllerImpl.handleEvent(reactionEvent)

        // Message is propagated
        verify(messageObserver).onChanged(listOf(message))
    }
}

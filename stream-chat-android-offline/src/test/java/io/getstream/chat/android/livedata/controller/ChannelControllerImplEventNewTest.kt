package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.Observer
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.controller.helper.MessageHelper
import io.getstream.chat.android.livedata.randomMessage
import io.getstream.chat.android.livedata.randomNewMessageEvent
import io.getstream.chat.android.test.InstantTaskExecutorExtension
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

    private val chatClient: ChatClient = mock()
    private val chatDomain: ChatDomainImpl = mock {
        on(it.scope) doReturn TestCoroutineScope()
        on(it.currentUser) doReturn User(id = CURRENT_USER_ID)
        on(it.getChannelConfig(any())) doReturn Config(isConnectEvents = true, isMutes = true)
    }
    private val messageHelper: MessageHelper = mock {
        on(it.updateValidAttachmentsUrl(any(), any())) doAnswer { invocation ->
            invocation.arguments[0] as List<Message>
        }
    }

    private lateinit var channelControllerImpl: ChannelControllerImpl

    @BeforeEach
    fun setUp() {
        channelControllerImpl = ChannelControllerImpl(
            channelType = "type1",
            channelId = "channelId",
            client = chatClient,
            domainImpl = chatDomain,
            messageHelper = messageHelper
        )
    }

    @Test
    fun `when user watching event arrives, last message should be updated`() = runBlockingTest {
        val user = User()
        val newDate = Date(Long.MAX_VALUE)
        val newMessage = randomMessage(id = "thisId", createdAt = newDate)

        val userStartWatchingEvent = randomNewMessageEvent(user = user, createdAt = newDate, message = newMessage)

        channelControllerImpl.handleEvent(userStartWatchingEvent)

        Truth.assertThat(channelControllerImpl.toChannel().lastMessageAt).isEqualTo(newDate)
    }

    @Test
    fun `when new message event arrives, messages should be propagated correctly`() {
        val updatedAt = Date()
        val user = User(id = CURRENT_USER_ID)
        val message = Message(updatedAt = updatedAt, user = user)

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
        val message = Message(updatedAt = updatedAt, user = User(id = "otherUserId"))

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

    @Test
    fun `when a message update for a non existing message arrives, it is added`() {
        val message = Message(user = User(id = "otherUserId"))

        val messageUpdateEvent = MessageUpdatedEvent(
            type = "type",
            createdAt = Date(),
            user = User(),
            cid = "cid",
            channelType = "channelType",
            channelId = "channelId",
            message = message,
            watcherCount = 1,
        )
        
        val messageObserver: Observer<List<Message>> = mock()

        channelControllerImpl.messages.observeForever(messageObserver)
        channelControllerImpl.handleEvent(messageUpdateEvent)

        verify(messageObserver).onChanged(listOf(message))
    }

    @Test
    fun `when a message update event is outdated, it should be ignored`() {
        val recentMessage = Message(user = User(id = "otherUserId"), updatedAt = Date())
        val oldMessage = Message(user = User(id = "otherUserId"), updatedAt = Date(Long.MIN_VALUE))

        val messageUpdateEvent = MessageUpdatedEvent(
            type = "type",
            createdAt = Date(),
            user = User(),
            cid = "cid",
            channelType = "channelType",
            channelId = "channelId",
            message = oldMessage,
            watcherCount = 1,
        )

        channelControllerImpl.upsertMessage(recentMessage)

        val messageObserver: Observer<List<Message>> = mock()

        channelControllerImpl.messages.observeForever(messageObserver)
        channelControllerImpl.handleEvent(messageUpdateEvent)

        // No message is added
        verify(messageObserver, never()).onChanged(listOf(oldMessage))
        Truth.assertThat(channelControllerImpl.messages.value).isEqualTo(listOf(recentMessage))
    }
}

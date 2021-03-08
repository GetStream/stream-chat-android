package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.test.InstantTaskExecutorExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
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

    private lateinit var channelControllerImpl: ChannelControllerImpl

    @BeforeEach
    fun setUp() {
        channelControllerImpl = ChannelControllerImpl(
            channelType = "type1",
            channelId = "channelId",
            client = chatClient,
            domainImpl = chatDomain
        )
    }

    @Test
    fun `when user watching event arrives, watchers should be incremented`() = runBlockingTest {
        val user = User()

        val userStartWatchingEvent = UserStartWatchingEvent(
            type = "type",
            createdAt = Date(),
            cid = "cid",
            watcherCount = 1,
            channelType = "channelType",
            channelId = "channelId",
            user = user
        )

        val observerMock: Observer<List<User>> = mock()
        channelControllerImpl.watchers.observeForever(observerMock)

        channelControllerImpl.handleEvent(userStartWatchingEvent)

        verify(observerMock).onChanged(listOf(user))
    }

    @Test
    fun `when new message event arrives, messages should be propagated correctly`() = runBlockingTest {
        val updatedAt = Date()
        val message = Message(updatedAt = updatedAt)

        val newMessageEvent = NewMessageEvent(
            type = "type",
            createdAt = Date(),
            user = User(),
            cid = "cid",
            channelType = "channelType",
            channelId = "channelId",
            message = message,
            watcherCount = 1,
            totalUnreadCount = 1,
            unreadChannels = 1
        )

        val messageObserver: Observer<List<Message>> = mock()
        val unreadCountObserver: Observer<Int?> = mock()

        channelControllerImpl.messages.observeForever(messageObserver)
        channelControllerImpl.unreadCount.observeForever(unreadCountObserver)

        channelControllerImpl.handleEvent(newMessageEvent)

        // Message is propagated
        verify(messageObserver).onChanged(listOf(message))

        // Unread count should not be propagated, because it is a message form the same user
        verify(unreadCountObserver, never()).onChanged(1)

        // Last message is updated
        channelControllerImpl.toChannel().lastMessageAt = updatedAt
    }

    @Test
    fun `when new message event arrives from other user, unread number should be updated`() = runBlockingTest {
        val updatedAt = Date()
        val message = Message(updatedAt = updatedAt, user = User(id = "otherUserId"))

        val newMessageEvent = NewMessageEvent(
            type = "type",
            createdAt = Date(),
            user = User(),
            cid = "cid",
            channelType = "channelType",
            channelId = "channelId",
            message = message,
            watcherCount = 1,
            totalUnreadCount = 1,
            unreadChannels = 1
        )

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
    fun `when new message event arrives from same user, no unread should be updated`() = runBlockingTest {
        val updatedAt = Date()
        // The current user has the id of
        val message = Message(updatedAt = updatedAt, user = User(id = CURRENT_USER_ID))

        val newMessageEvent = NewMessageEvent(
            type = "type",
            createdAt = Date(),
            user = User(),
            cid = "cid",
            channelType = "channelType",
            channelId = "channelId",
            message = message,
            watcherCount = 1,
            totalUnreadCount = 1,
            unreadChannels = 1
        )

        val messageObserver: Observer<List<Message>> = mock()
        val unreadCountObserver: Observer<Int?> = mock()

        channelControllerImpl.messages.observeForever(messageObserver)
        channelControllerImpl.unreadCount.observeForever(unreadCountObserver)

        channelControllerImpl.handleEvent(newMessageEvent)

        // Message is propagated
        verify(messageObserver).onChanged(listOf(message))

        // Unread count should not be propagated, because it is a message form the current user
        verify(unreadCountObserver, never()).onChanged(1)

        // Last message is updated
        channelControllerImpl.toChannel().lastMessageAt = updatedAt
    }
}

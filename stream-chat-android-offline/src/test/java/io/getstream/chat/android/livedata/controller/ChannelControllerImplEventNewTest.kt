package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.test.InstantTaskExecutorExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Date

@ExperimentalCoroutinesApi
@ExtendWith(InstantTaskExecutorExtension::class)
internal class ChannelControllerImplEventNewTest {

    private val chatClient: ChatClient = mock()
    private val chatDomain: ChatDomainImpl = mock {
        on(it.scope) doReturn TestCoroutineScope()
        on(it.currentUser) doReturn User()
    }

    private val channelControllerImpl =
        ChannelControllerImpl(
            channelType = "type1",
            channelId = "channelId",
            client = chatClient,
            domainImpl = chatDomain
        )

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

        val observerMock : Observer<List<User>> = mock()
        channelControllerImpl.watchers.observeForever(observerMock)

        channelControllerImpl.handleEvent(userStartWatchingEvent)

        verify(observerMock).onChanged(listOf(user))
    }

    @Test
    fun `when new message event arrives, messages should be propagated`() = runBlockingTest {
        val message = Message()

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
        channelControllerImpl.messages.observeForever(messageObserver)

        channelControllerImpl.handleEvent(newMessageEvent)

        verify(messageObserver).onChanged(listOf(message))
    }
}

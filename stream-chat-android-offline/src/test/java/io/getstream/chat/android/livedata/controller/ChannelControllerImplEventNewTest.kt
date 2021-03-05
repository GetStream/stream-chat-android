package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.Observer
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.UserStartWatchingEvent
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

}

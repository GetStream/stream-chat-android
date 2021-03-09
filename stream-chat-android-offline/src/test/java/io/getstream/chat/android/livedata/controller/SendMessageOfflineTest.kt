package io.getstream.chat.android.livedata.controller

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.repository.RepositoryFacade
import io.getstream.chat.android.livedata.utils.TestDataHelper
import io.getstream.chat.android.test.InstantTaskExecutorExtension
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.getOrAwaitValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension

@ExperimentalCoroutinesApi
@ExtendWith(InstantTaskExecutorExtension::class)
internal class SendMessageOfflineTest {

    @JvmField
    @RegisterExtension
    val testCoroutines = TestCoroutineExtension()

    private val data = TestDataHelper()

    @Test
    fun `when calling watch, local messages should not be lost`() = testCoroutines.scope.runBlockingTest {
        val sut = Fixture(testCoroutines.scope, data.user1)
            .givenChannelWithoutMessages(data.channel1)
            .givenMockedRepositories()
            .givenIsOffline()
            .get()

        val message = data.createMessage()
        // the message is only created locally
        sut.sendMessage(message)
        // the message should still show up after invocation
        sut.watch()

        val result = sut.messages.getOrAwaitValue()

        result.size `should be equal to` 1
        result.first().id `should be equal to` message.id

        verify(sut.domainImpl.repos).insertMessage(
            message = argThat { id == message.id },
            cache = eq(false)
        )
    }

    private class Fixture(scope: CoroutineScope, user: User) {
        private val repos: RepositoryFacade = mock()
        private val chatClient: ChatClient = mock()
        private val channelClient: ChannelClient = mock()
        private val chatDomainImpl: ChatDomainImpl = mock()

        init {
            whenever(chatClient.channel(any(), any())) doReturn channelClient
            whenever(chatDomainImpl.currentUser) doReturn user
            whenever(chatDomainImpl.job) doReturn Job()
            whenever(chatDomainImpl.scope) doReturn scope
            whenever(chatDomainImpl.repos) doReturn repos
        }

        fun givenMockedRepositories(): Fixture {
            runBlocking {
                whenever(repos.selectChannels(any())) doReturn emptyList()
            }
            return this
        }

        fun givenChannelWithoutMessages(channel: Channel): Fixture {
            whenever(channelClient.watch(any<WatchChannelRequest>())) doReturn TestCall(Result(channel))
            return this
        }

        fun givenIsOffline(): Fixture {
            whenever(chatDomainImpl.isOnline()) doReturn false
            return this
        }

        fun get(): ChannelControllerImpl {
            return ChannelControllerImpl("channelType", "channelId", chatClient, chatDomainImpl)
        }
    }
}

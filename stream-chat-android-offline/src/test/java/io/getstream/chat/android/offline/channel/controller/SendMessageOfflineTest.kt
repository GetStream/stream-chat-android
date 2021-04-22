package io.getstream.chat.android.offline.channel.controller

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
import io.getstream.chat.android.livedata.randomChannel
import io.getstream.chat.android.livedata.randomMessage
import io.getstream.chat.android.livedata.randomUser
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.repository.RepositoryFacade
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@ExperimentalCoroutinesApi
internal class SendMessageOfflineTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    @Test
    fun `when calling watch, local messages should not be lost`() = testCoroutines.scope.runBlockingTest {
        val sut = Fixture(testCoroutines.scope, randomUser())
            .givenChannelWithoutMessages(randomChannel(cid = "channelType:channelId"))
            .givenMockedRepositories()
            .givenIsOffline()
            .get()

        val message = randomMessage(cid = "channelType:channelId", parentId = null)
        // the message is only created locally
        sut.sendMessage(message)
        // the message should still show up after invocation
        sut.watch()

        val result = sut.messages.value

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

        fun get(): ChannelController {
            return ChannelController("channelType", "channelId", chatClient, chatDomainImpl)
        }
    }
}

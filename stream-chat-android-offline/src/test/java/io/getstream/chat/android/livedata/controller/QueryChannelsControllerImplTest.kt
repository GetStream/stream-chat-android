package io.getstream.chat.android.livedata.controller

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.randomChannel
import io.getstream.chat.android.livedata.utils.InstantTaskExecutorExtension
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension

@ExperimentalCoroutinesApi
@ExtendWith(InstantTaskExecutorExtension::class)
internal class QueryChannelsControllerImplTest {

    @JvmField
    @RegisterExtension
    val testCoroutines = TestCoroutineExtension()

    @Test
    fun `when add channel if filter matches should update LiveData from channel to channel controller`() =
        runBlockingTest {
            val channelController = mock<ChannelControllerImpl>()
            val sut = Fixture(testCoroutines.scope)
                .givenNewChannelController(channelController)
                .setupChatControllersInstantiation()
                .get()
            val newChannel = randomChannel()

            sut.addChannelIfFilterMatches(newChannel)

            verify(channelController).updateLiveDataFromChannel(eq(newChannel))
        }

    @Test
    fun `when add channel if filter matches should post value to liveData with the same channel ID`() =
        runBlockingTest {
            val sut = Fixture(testCoroutines.scope)
                .givenNewChannelController(mock())
                .setupChatControllersInstantiation()
                .get()
            val newChannel = randomChannel(cid = "ChannelType:ChannelID")

            sut.addChannelIfFilterMatches(newChannel)

            val result = sut.channels.getOrAwaitValue()
            result.size shouldBeEqualTo 1
            result.first().cid shouldBeEqualTo "ChannelType:ChannelID"
        }

    @Test
    fun `when add channel twice if filter matches should post value to liveData only one value`() =
        runBlockingTest {
            val sut = Fixture(testCoroutines.scope)
                .givenNewChannelController(mock())
                .setupChatControllersInstantiation()
                .get()
            val newChannel = randomChannel(cid = "ChannelType:ChannelID")

            sut.addChannelIfFilterMatches(newChannel)
            sut.addChannelIfFilterMatches(newChannel)

            val result = sut.channels.getOrAwaitValue()
            result.size shouldBeEqualTo 1
            result.first().cid shouldBeEqualTo "ChannelType:ChannelID"
        }
}

private class Fixture(scope: CoroutineScope) {
    private val chatClient: ChatClient = mock()
    private val chatDomainImpl: ChatDomainImpl = mock()

    init {
        whenever(chatDomainImpl.currentUser) doReturn mock()
        whenever(chatDomainImpl.job) doReturn Job()
        whenever(chatDomainImpl.scope) doReturn scope
    }

    fun givenNewChannelController(channelControllerImpl: ChannelControllerImpl): Fixture {
        whenever(chatDomainImpl.channel(any<Channel>())) doReturn channelControllerImpl
        return this
    }

    fun setupChatControllersInstantiation(): Fixture {
        whenever(chatDomainImpl.channel(any<String>())) doAnswer { invocation ->
            val cid = invocation.arguments[0] as String
            val mockChannelController = mock<ChannelControllerImpl>()
            val mockChannel = mock<Channel>()
            whenever(mockChannel.cid) doReturn cid
            whenever(mockChannelController.toChannel()) doReturn mockChannel
            mockChannelController
        }
        whenever(chatDomainImpl.getChannelConfig(any())) doReturn mock()
        return this
    }

    fun get(): QueryChannelsControllerImpl =
        QueryChannelsControllerImpl(mock(), QuerySort(), chatClient, chatDomainImpl)
}

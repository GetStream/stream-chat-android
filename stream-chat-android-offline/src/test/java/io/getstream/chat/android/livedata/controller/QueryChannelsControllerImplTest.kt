package io.getstream.chat.android.livedata.controller

import androidx.arch.core.executor.testing.InstantExecutorExtension
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
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import kotlinx.coroutines.Job
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class)
internal class QueryChannelsControllerImplTest {
    @Test
    fun `when add channel if filter matches should update LiveData from channel to channel controller`() {
        val channelController = mock<ChannelControllerImpl>()
        val sut = Fixture()
            .givenNewChannelController(channelController)
            .setupChatControllersInstantiation()
            .get()
        val newChannel = randomChannel()

        sut.addChannelIfFilterMatches(newChannel)

        verify(channelController).updateLiveDataFromChannel(eq(newChannel))
    }

    @Test
    fun `when add channel if filter matches should post value to liveData with the same channel ID`() {
        val sut = Fixture()
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
    fun `when add channel twice if filter matches should post value to liveData only one value`() {
        val sut = Fixture()
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

private class Fixture {
    private val chatClient: ChatClient = mock()
    private val chatDomainImpl: ChatDomainImpl = mock()

    init {
        whenever(chatDomainImpl.currentUser) doReturn mock()
        whenever(chatDomainImpl.job) doReturn Job()
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

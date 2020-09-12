package io.getstream.chat.android.livedata.controller

import androidx.arch.core.executor.testing.InstantExecutorExtension
import com.nhaarman.mockitokotlin2.*
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import kotlinx.coroutines.Job
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class)
internal class QueryChannelsControllerImplTest {
    @Test
    fun `when add channel if filter matches should update LiveData from channel to channel controller`() {
        val chatDomainImpl = mock<ChatDomainImpl>().apply {
            whenever(currentUser) doReturn mock()
            whenever(job) doReturn Job()
        }
        val channelController = spy(ChannelControllerImpl("ChannelType", "CID", mock(), chatDomainImpl))
        val sut = Fixture()
            .givenChatDomain(chatDomainImpl)
            .giveNewChannelController(channelController)
            .setupChantControllersInstantiation()
            .get()
        val newChannel = Channel(cid = "CID", id = "ID")

        sut.addChannelIfFilterMatches(newChannel)

        verify(channelController).updateLiveDataFromChannel(eq(newChannel))
    }

    @Test
    fun `when add channel if filter matches should post value to liveData with the same channel ID`() {
        val chatDomainImpl = mock<ChatDomainImpl>().apply {
            whenever(currentUser) doReturn mock()
            whenever(job) doReturn Job()
        }
        val channelController = spy(ChannelControllerImpl("ChannelType", "ChannelID", mock(), chatDomainImpl))
        val sut = Fixture()
            .givenChatDomain(chatDomainImpl)
            .giveNewChannelController(channelController)
            .setupChantControllersInstantiation()
            .get()
        val newChannel = Channel(cid = "ChannelType:ChannelID", id = "ChannelID")

        sut.addChannelIfFilterMatches(newChannel)

        val result = sut.channels.getOrAwaitValue()
        result.size shouldBeEqualTo 1
        result.first().cid shouldBeEqualTo "ChannelType:ChannelID"
    }

    @Test
    fun `when add channel twice if filter matches should post value to liveData only one value`() {
        val chatDomainImpl = mock<ChatDomainImpl>().apply {
            whenever(currentUser) doReturn mock()
            whenever(job) doReturn Job()
        }
        val channelController = spy(ChannelControllerImpl("ChannelType", "ChannelID", mock(), chatDomainImpl))
        val sut = Fixture()
            .givenChatDomain(chatDomainImpl)
            .giveNewChannelController(channelController)
            .setupChantControllersInstantiation()
            .get()
        val newChannel = Channel(cid = "ChannelType:ChannelID", id = "ChannelID")

        sut.addChannelIfFilterMatches(newChannel)
        sut.addChannelIfFilterMatches(newChannel)

        val result = sut.channels.getOrAwaitValue()
        result.size shouldBeEqualTo 1
        result.first().cid shouldBeEqualTo "ChannelType:ChannelID"
    }
}

class Fixture {
    private val chatClient: ChatClient = mock()
    private var chatDomainImpl: ChatDomainImpl? = null

    fun givenChatDomain(chatDomainImpl: ChatDomainImpl): Fixture {
        this.chatDomainImpl = chatDomainImpl
        return this
    }

    fun giveNewChannelController(channelControllerImpl: ChannelControllerImpl): Fixture {
        whenever(chatDomainImpl?.channel(any<Channel>())) doReturn channelControllerImpl
        return this
    }

    fun setupChantControllersInstantiation(): Fixture {
        whenever(chatDomainImpl?.channel(any<String>())) doAnswer { invocation ->
            val cid = invocation.arguments[0] as String
            val mockChannelController = mock<ChannelControllerImpl>()
            val mockChannel = mock<Channel>()
            whenever(mockChannel.cid) doReturn cid
            whenever(mockChannelController.toChannel()) doReturn mockChannel
            mockChannelController
        }
        whenever(chatDomainImpl.shouldNotBeNull().getChannelConfig(any())) doReturn mock()
        return this
    }

    fun get(): QueryChannelsControllerImpl =
        QueryChannelsControllerImpl(mock(), mock(), chatClient, chatDomainImpl.shouldNotBeNull())
}
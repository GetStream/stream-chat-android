package io.getstream.chat.android.livedata.controller

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.randomChannel
import io.getstream.chat.android.livedata.randomUser
import io.getstream.chat.android.test.InstantTaskExecutorExtension
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.getOrAwaitValue
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import java.util.Date

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
            val sut = Fixture(testCoroutines.scope, currentUser = randomUser())
                .givenNewChannelController(channelController)
                .setupChatControllersInstantiation(withCurrentUserAsChannelMember = true)
                .get()
            val newChannel = randomChannel()

            sut.addChannelIfFilterMatches(newChannel)

            verify(channelController).updateLiveDataFromChannel(eq(newChannel))
        }

    @Test
    fun `when add channel if filter matches should post value to liveData with the same channel ID`() =
        runBlockingTest {
            val sut = Fixture(testCoroutines.scope, currentUser = randomUser())
                .givenNewChannelController(mock())
                .setupChatControllersInstantiation(withCurrentUserAsChannelMember = true)
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
            val sut = Fixture(testCoroutines.scope, currentUser = randomUser())
                .givenNewChannelController(mock())
                .setupChatControllersInstantiation(withCurrentUserAsChannelMember = true)
                .get()
            val newChannel = randomChannel(cid = "ChannelType:ChannelID")

            sut.addChannelIfFilterMatches(newChannel)
            sut.addChannelIfFilterMatches(newChannel)

            val result = sut.channels.getOrAwaitValue()
            result.size shouldBeEqualTo 1
            result.first().cid shouldBeEqualTo "ChannelType:ChannelID"
        }

    @Test
    fun `Given messaging channel When refreshing channel which doesn't contain current user as member Should post value to liveData without this channel`() =
        runBlockingTest {
            val sut = Fixture(testCoroutines.scope, currentUser = randomUser())
                .givenNewChannelController(mock())
                .setupChatControllersInstantiation(withCurrentUserAsChannelMember = false, channelType = "messaging")
                .setupChatRepositories()
                .get()
            val channel = randomChannel(type = "messaging")
            sut.addChannelIfFilterMatches(channel)

            sut.refreshChannel(channel.cid)

            val result = sut.channels.getOrAwaitValue()
            result.size shouldBeEqualTo 0
        }

    @Test
    fun `Given not messaging channel When refreshing channel which doesn't contain current user as member Should post value to liveData with this channel`() =
        runBlockingTest {
            val cid = "channelType:channelId"
            val sut = Fixture(testCoroutines.scope, currentUser = randomUser())
                .givenNewChannelController(mock())
                .setupChatControllersInstantiation(withCurrentUserAsChannelMember = false)
                .setupChatRepositories()
                .get()
            val channel = randomChannel(cid = cid)
            sut.addChannelIfFilterMatches(channel)

            sut.refreshChannel(channel.cid)

            val result = sut.channels.getOrAwaitValue()
            result.size shouldBeEqualTo 1
            result.first().cid shouldBeEqualTo cid
        }

    @Test
    fun `when refreshing channel which contain current user as member should post value to liveData with channel`() =
        runBlockingTest {
            val sut = Fixture(testCoroutines.scope, currentUser = randomUser())
                .givenNewChannelController(mock())
                .setupChatControllersInstantiation(withCurrentUserAsChannelMember = true)
                .setupChatRepositories()
                .get()
            val cid = "ChannelType:ChannelID"
            val channel = randomChannel(cid = cid)
            sut.addChannelIfFilterMatches(channel)

            sut.refreshChannel(channel.cid)

            val result = sut.channels.getOrAwaitValue()
            result.size shouldBeEqualTo 1
            result.first().cid shouldBeEqualTo cid
        }

    @Test
    fun `when a new message arrives in a new channel, it should update the channels`() =
        runBlockingTest {
            val channelController: ChannelControllerImpl = mock()

            val queryController = Fixture(testCoroutines.scope)
                .givenNewChannelController(channelController)
                .get()

            queryController.handleEvent(notificationNewMessage())

            verify(channelController).updateLiveDataFromChannel(any())
        }

    @Test
    fun `when a new message arrives in a new channel, it NOT should update the channels when it is already there`() =
        runBlockingTest {
            val cid = randomString()
            val channelController: ChannelControllerImpl = mock()

            val queryController = Fixture(testCoroutines.scope)
                .givenNewChannelController(channelController)
                .get()

            queryController.queryChannelsSpec.cids = listOf(cid)

            queryController.handleEvent(notificationNewMessage(cid))

            verify(channelController, never()).updateLiveDataFromChannel(any())
        }
}

private fun notificationNewMessage(channelCid: String = "cid"): NotificationMessageNewEvent {
    return NotificationMessageNewEvent(
        type = "type",
        createdAt = Date(),
        cid = "cid",
        channelType = "channelType",
        channelId = "channelId",
        channel = Channel(cid = channelCid),
        message = Message(),
        watcherCount = 0,
        totalUnreadCount = 0,
        unreadChannels = 0
    )
}

private class Fixture(scope: CoroutineScope, private val currentUser: User = mock()) {
    private val chatClient: ChatClient = mock()
    private val chatDomainImpl: ChatDomainImpl = mock()

    init {
        whenever(chatDomainImpl.currentUser) doReturn currentUser
        whenever(chatDomainImpl.job) doReturn Job()
        whenever(chatDomainImpl.scope) doReturn scope
    }

    fun givenNewChannelController(channelControllerImpl: ChannelControllerImpl): Fixture {
        whenever(chatDomainImpl.channel(any<Channel>())) doReturn channelControllerImpl
        return this
    }

    fun setupChatControllersInstantiation(
        withCurrentUserAsChannelMember: Boolean = false,
        channelType: String? = null,
    ): Fixture {
        whenever(chatDomainImpl.channel(any<String>())) doAnswer { invocation ->
            val cid = invocation.arguments[0] as String
            val mockChannelController = mock<ChannelControllerImpl>()
            val mockChannel = mock<Channel>()
            whenever(mockChannel.cid) doReturn cid
            channelType?.let { type -> whenever(mockChannel.type) doReturn channelType }
            whenever(mockChannel.members) doReturn if (withCurrentUserAsChannelMember) listOf(Member(currentUser)) else emptyList()
            whenever(mockChannelController.toChannel()) doReturn mockChannel
            mockChannelController
        }
        whenever(chatDomainImpl.getChannelConfig(any())) doReturn mock()
        return this
    }

    fun setupChatRepositories(): Fixture {
        whenever(chatDomainImpl.repos) doReturn mock()

        return this
    }

    fun get(): QueryChannelsControllerImpl =
        QueryChannelsControllerImpl(mock(), QuerySort(), chatClient, chatDomainImpl)
}

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
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.randomChannel
import io.getstream.chat.android.livedata.randomMember
import io.getstream.chat.android.livedata.randomNotificationMessageNewEvent
import io.getstream.chat.android.livedata.randomUser
import io.getstream.chat.android.test.InstantTaskExecutorExtension
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.getOrAwaitValue
import io.getstream.chat.android.test.positiveRandomInt
import io.getstream.chat.android.test.randomCID
import io.getstream.chat.android.test.randomString
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

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    @Test
    fun `when add channel if filter matches should update LiveData from channel to channel controller`() =
        runBlockingTest {
            val currentUser = randomUser()
            val newChannel = randomChannel(
                members = List(positiveRandomInt(10)) { randomMember() } + randomMember(user = currentUser)
            )
            val channelController = mock<ChannelControllerImpl>()
            val sut = Fixture(testCoroutines.scope, currentUser = currentUser)
                .givenNewChannelController(channelController)
                .givenFilterObject(
                    Filters.and(
                        Filters.`in`("members", listOf(currentUser.id)),
                        Filters.eq("type", newChannel.type)
                    )
                )
                .setupChatControllersInstantiation(withCurrentUserAsChannelMember = true, newChannel.type)
                .get()

            sut.addChannelIfFilterMatches(newChannel)

            verify(channelController).updateLiveDataFromChannel(eq(newChannel))
        }

    @Test
    fun `when add channel if filter matches should post value to liveData with the same channel ID`() =
        runBlockingTest {
            val currentUser = randomUser()
            val cid = randomCID()
            val newChannel = randomChannel(
                cid = cid,
                id = cid.substringAfter(":"),
                type = cid.substringBefore(":"),
                members = List(positiveRandomInt(10)) { randomMember() } + randomMember(user = currentUser)
            )
            val sut = Fixture(testCoroutines.scope, currentUser = currentUser)
                .givenNewChannelController(mock())
                .setupChatControllersInstantiation(withCurrentUserAsChannelMember = true, newChannel.type)
                .givenFilterObject(
                    Filters.and(
                        Filters.`in`("members", listOf(currentUser.id)),
                        Filters.eq("type", newChannel.type)
                    )
                )
                .get()

            sut.addChannelIfFilterMatches(newChannel)

            val result = sut.channels.getOrAwaitValue()
            result.size shouldBeEqualTo 1
            result.first().cid shouldBeEqualTo newChannel.cid
        }

    @Test
    fun `when add channel twice if filter matches should post value to liveData only one value`() =
        runBlockingTest {
            val currentUser = randomUser()
            val cid = randomCID()
            val newChannel = randomChannel(
                cid = cid,
                id = cid.substringAfter(":"),
                type = cid.substringBefore(":"),
                members = List(positiveRandomInt(10)) { randomMember() } + randomMember(user = currentUser)
            )
            val sut = Fixture(testCoroutines.scope, currentUser = currentUser)
                .givenNewChannelController(mock())
                .setupChatControllersInstantiation(withCurrentUserAsChannelMember = true, newChannel.type)
                .givenFilterObject(
                    Filters.and(
                        Filters.`in`("members", listOf(currentUser.id)),
                        Filters.eq("type", newChannel.type),
                        Filters.eq("cid", newChannel.cid)
                    )
                )
                .get()

            sut.addChannelIfFilterMatches(newChannel)
            sut.addChannelIfFilterMatches(newChannel)

            val result = sut.channels.getOrAwaitValue()
            result.size shouldBeEqualTo 1
            result.first().cid shouldBeEqualTo newChannel.cid
        }

    @Test
    fun `Given messaging channel When refreshing channel which doesn't contain current user as member Should post value to liveData without this channel`() =
        runBlockingTest {
            val currentUser = randomUser()
            val channelType = randomString()
            val sut = Fixture(testCoroutines.scope, currentUser = currentUser)
                .givenNewChannelController(mock())
                .setupChatControllersInstantiation(withCurrentUserAsChannelMember = false, channelType = channelType)
                .setupChatRepositories()
                .givenFilterObject(
                    Filters.and(
                        Filters.`in`("members", listOf(currentUser.id)),
                        Filters.eq("type", channelType)
                    )
                )
                .get()
            val channel = randomChannel(type = channelType)
            sut.addChannelIfFilterMatches(channel)

            sut.refreshChannel(channel.cid)

            val result = sut.channels.getOrAwaitValue()
            result.size shouldBeEqualTo 0
        }

    @Test
    fun `when a new message arrives in a new channel, it should update the channels`() =
        runBlockingTest {
            val channelController: ChannelControllerImpl = mock()

            val queryController = Fixture(testCoroutines.scope)
                .givenNewChannelController(channelController)
                .get()

            val channel = randomChannel()
            queryController.handleEvent(randomNotificationMessageNewEvent(channel = channel))

            verify(channelController).updateLiveDataFromChannel(eq(channel))
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

            queryController.handleEvent(randomNotificationMessageNewEvent(channel = randomChannel(cid = cid)))

            verify(channelController, never()).updateLiveDataFromChannel(any())
        }
}

private class Fixture(scope: CoroutineScope, private val currentUser: User = mock()) {
    private val chatClient: ChatClient = mock()
    private val chatDomainImpl: ChatDomainImpl = mock()
    private var filterObject: FilterObject = Filters.neutral()
    private var querySort: QuerySort<Channel> = QuerySort()

    init {
        whenever(chatDomainImpl.currentUser) doReturn currentUser
        whenever(chatDomainImpl.job) doReturn Job()
        whenever(chatDomainImpl.scope) doReturn scope
    }

    fun givenNewChannelController(channelControllerImpl: ChannelControllerImpl): Fixture = apply {
        whenever(chatDomainImpl.channel(any<Channel>())) doReturn channelControllerImpl
    }

    fun givenFilterObject(filterObject: FilterObject): Fixture = apply {
        this.filterObject = filterObject
    }

    fun setupChatControllersInstantiation(
        withCurrentUserAsChannelMember: Boolean = false,
        channelType: String? = null,
    ): Fixture = apply {
        whenever(chatDomainImpl.channel(any<String>())) doAnswer { invocation ->
            val cid = invocation.arguments[0] as String
            val mockChannelController = mock<ChannelControllerImpl>()
            val mockChannel = randomChannel(
                cid = cid,
                type = channelType ?: randomString(),
                members = if (withCurrentUserAsChannelMember) listOf(Member(currentUser)) else emptyList()
            )
            whenever(mockChannelController.toChannel()) doReturn mockChannel
            mockChannelController
        }
        whenever(chatDomainImpl.getChannelConfig(any())) doReturn mock()
    }

    fun setupChatRepositories(): Fixture = apply {
        whenever(chatDomainImpl.repos) doReturn mock()
    }

    fun get(): QueryChannelsControllerImpl =
        QueryChannelsControllerImpl(filterObject, querySort, chatClient, chatDomainImpl)
}

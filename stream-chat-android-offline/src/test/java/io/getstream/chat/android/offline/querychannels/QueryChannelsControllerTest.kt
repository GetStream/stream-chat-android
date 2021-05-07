package io.getstream.chat.android.offline.querychannels

import com.google.common.truth.Truth
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
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.randomChannel
import io.getstream.chat.android.offline.randomChannelUpdatedByUserEvent
import io.getstream.chat.android.offline.randomChannelUpdatedEvent
import io.getstream.chat.android.offline.randomMember
import io.getstream.chat.android.offline.randomNotificationMessageNewEvent
import io.getstream.chat.android.offline.randomUser
import io.getstream.chat.android.test.positiveRandomInt
import io.getstream.chat.android.test.randomCID
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class QueryChannelsControllerTest {

    @Test
    fun `when add channel if filter matches should update LiveData from channel to channel controller`() =
        runBlockingTest {
            val currentUser = randomUser()
            val newChannel = randomChannel(
                members = List(positiveRandomInt(10)) { randomMember() } + randomMember(user = currentUser)
            )
            val channelController = mock<ChannelController>()
            val sut = Fixture()
                .givenCurrentUser(randomUser())
                .givenNewChannelControllerForChannel(channelController)
                .givenChannelControllerForCidWithCurrentUser()
                .givenFilterObject(
                    Filters.and(
                        Filters.`in`("members", listOf(currentUser.id)),
                        Filters.eq("type", newChannel.type)
                    )
                )
                .get()

            sut.addChannelIfFilterMatches(newChannel)

            verify(channelController).updateDataFromChannel(eq(newChannel))
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
            val sut = Fixture()
                .givenCurrentUser(currentUser)
                .givenChannelType(newChannel.type)
                .givenNewChannelControllerForChannel()
                .givenChannelControllerForCidWithCurrentUser()
                .givenFilterObject(
                    Filters.and(
                        Filters.`in`("members", listOf(currentUser.id)),
                        Filters.eq("type", newChannel.type)
                    )
                )
                .get()

            sut.addChannelIfFilterMatches(newChannel)

            val result = sut.channels.value
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
            val sut = Fixture()
                .givenCurrentUser(currentUser)
                .givenChannelType(newChannel.type)
                .givenNewChannelControllerForChannel()
                .givenChannelControllerForCidWithCurrentUser()
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

            val result = sut.channels.value
            result.size shouldBeEqualTo 1
            result.first().cid shouldBeEqualTo newChannel.cid
        }

    @Test
    fun `Given messaging channel When refreshing channel which doesn't contain current user as member Should post value to liveData without this channel`() =
        runBlockingTest {
            val currentUser = randomUser()
            val channelType = randomString()
            val sut = Fixture()
                .givenCurrentUser(currentUser)
                .givenNewChannelControllerForChannel()
                .givenChannelType(channelType)
                .givenChannelControllerForCidWithoutCurrentUser()
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

            val result = sut.channels.value
            result.size shouldBeEqualTo 0
        }

    @Test
    fun `Given not messaging channel When refreshing channel which doesn't contain current user as member Should post value to liveData with this channel`() =
        runBlockingTest {
            val cid = "channelType:channelId"
            val sut = Fixture()
                .givenCurrentUser(randomUser())
                .givenNewChannelControllerForChannel()
                .givenChannelControllerForCidWithoutCurrentUser()
                .setupChatRepositories()
                .get()
            val channel = randomChannel(cid = cid)
            sut.addChannelIfFilterMatches(channel)

            sut.refreshChannel(channel.cid)

            val result = sut.channels.value
            result.size shouldBeEqualTo 1
            result.first().cid shouldBeEqualTo cid
        }

    @Test
    fun `when refreshing channel which contain current user as member should post value to liveData with channel`() =
        runBlockingTest {
            val sut = Fixture()
                .givenCurrentUser(randomUser())
                .givenNewChannelControllerForChannel()
                .givenChannelControllerForCidWithCurrentUser()
                .setupChatRepositories()
                .get()
            val cid = "ChannelType:ChannelID"
            val channel = randomChannel(cid = cid)
            sut.addChannelIfFilterMatches(channel)

            sut.refreshChannel(channel.cid)

            val result = sut.channels.value
            result.size shouldBeEqualTo 1
            result.first().cid shouldBeEqualTo cid
        }

    @Test
    fun `When a new message arrives in a new channel Should update the channels`() =
        runBlockingTest {
            val channel = randomChannel()
            val channelController: ChannelController = mock {
                on(mock.toChannel()) doReturn channel
            }
            val queryController = Fixture()
                .givenNewChannelControllerForChannel(channelController)
                .get()

            queryController.handleEvent(randomNotificationMessageNewEvent(channel = channel))

            verify(channelController).updateDataFromChannel(eq(channel))
            Truth.assertThat(queryController.queryChannelsSpec.cids).contains(channel.cid)
        }

    @Test
    fun `When a new message arrives in a new channel Should not update the channels when it is already there`() =
        runBlockingTest {
            val cid = randomString()
            val channelController: ChannelController = mock()
            val queryController = Fixture()
                .givenNewChannelControllerForChannel(channelController)
                .get()
            queryController.queryChannelsSpec.cids = listOf(cid)

            queryController.handleEvent(randomNotificationMessageNewEvent(channel = randomChannel(cid = cid)))

            verify(channelController, never()).updateDataFromChannel(any())
        }

    @Test
    fun `When a channel updated arrives Should add the channel when filter matches and it wasn't added yet`() =
        runBlockingTest {
            val channel = randomChannel()
            val channelController: ChannelController = mock {
                on(mock.toChannel()) doReturn channel
            }
            val queryController = Fixture()
                .givenNewChannelControllerForChannel(channelController)
                .get()

            queryController.handleEvent(randomChannelUpdatedEvent(channel = channel))

            Truth.assertThat(queryController.queryChannelsSpec.cids).contains(channel.cid)
        }

    @Test
    fun `When a channel updated by user arrives Should add the channel when filter matches and it wasn't added yet`() =
        runBlockingTest {
            val channel = randomChannel()
            val channelController: ChannelController = mock {
                on(mock.toChannel()) doReturn channel
            }
            val queryController = Fixture()
                .givenNewChannelControllerForChannel(channelController)
                .get()

            queryController.handleEvent(randomChannelUpdatedByUserEvent(channel = channel))

            Truth.assertThat(queryController.queryChannelsSpec.cids).contains(channel.cid)
        }
}

private class Fixture {
    private val chatClient: ChatClient = mock()
    private val chatDomainImpl: ChatDomainImpl = mock()
    private var filterObject: FilterObject = Filters.neutral()
    private var querySort: QuerySort<Channel> = QuerySort()
    private val testCoroutineScope = TestCoroutineScope()

    private var currentUser: User? = null
    private var channelType: String = ""

    init {
        whenever(chatDomainImpl.job) doReturn Job()
        whenever(chatDomainImpl.scope) doReturn testCoroutineScope
    }

    fun givenCurrentUser(user: User) = apply {
        currentUser = user
        whenever(chatDomainImpl.currentUser) doReturn currentUser!!
    }

    fun givenNewChannelControllerForChannel(channelController: ChannelController = mock()): Fixture = apply {
        whenever(chatDomainImpl.channel(any<Channel>())) doReturn channelController
        whenever(chatDomainImpl.channel(any<String>())) doReturn channelController
    }

    fun givenFilterObject(filterObject: FilterObject): Fixture = apply {
        this.filterObject = filterObject
    }

    fun givenChannelType(channelType: String) = apply {
        this.channelType = channelType
    }

    fun givenChannelControllerForCidWithCurrentUser() = setupChatControllersInstantiation(true)

    fun givenChannelControllerForCidWithoutCurrentUser() = setupChatControllersInstantiation(false)

    private fun setupChatControllersInstantiation(
        withCurrentUserAsChannelMember: Boolean = false,
    ): Fixture = apply {
        whenever(chatDomainImpl.channel(any<String>())) doAnswer { invocation ->
            mock<ChannelController> {
                on { toChannel() } doReturn randomChannel(
                    cid = invocation.arguments[0] as String,
                    type = channelType,
                    members = if (withCurrentUserAsChannelMember) listOf(Member(currentUser!!)) else emptyList()
                )
            }
        }
        whenever(chatDomainImpl.getChannelConfig(any())) doReturn mock()
    }

    fun setupChatRepositories(): Fixture = apply {
        whenever(chatDomainImpl.repos) doReturn mock()
    }

    fun get(): QueryChannelsController = QueryChannelsController(filterObject, querySort, chatClient, chatDomainImpl)
}

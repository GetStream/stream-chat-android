package io.getstream.chat.android.offline.querychannels

import com.google.common.truth.Truth
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
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.experimental.querychannels.logic.QueryChannelsLogic
import io.getstream.chat.android.offline.experimental.querychannels.state.QueryChannelsMutableState
import io.getstream.chat.android.offline.randomChannel
import io.getstream.chat.android.offline.randomChannelDeletedEvent
import io.getstream.chat.android.offline.randomChannelUpdatedByUserEvent
import io.getstream.chat.android.offline.randomChannelUpdatedEvent
import io.getstream.chat.android.offline.randomMember
import io.getstream.chat.android.offline.randomNotificationAddedToChannelEvent
import io.getstream.chat.android.offline.randomNotificationChannelDeletedEvent
import io.getstream.chat.android.offline.randomUser
import io.getstream.chat.android.test.positiveRandomInt
import io.getstream.chat.android.test.randomCID
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
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
                .givenCurrentUser(currentUser)
                .givenNewChannelControllerForChannel(channelController)
                .get()

            sut.updateQueryChannelSpec(newChannel)

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
                .get()

            sut.updateQueryChannelSpec(newChannel)

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
                .get()

            sut.updateQueryChannelSpec(newChannel)
            sut.updateQueryChannelSpec(newChannel)

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
                .setupChatRepositories()
                .get()
            val channel = randomChannel(type = channelType, members = emptyList())
            sut.updateQueryChannelSpec(channel)

            sut.refreshChannel(channel.cid)

            val result = sut.channels.value
            result.size shouldBeEqualTo 0
        }

    @Test
    fun `Given channel without current user as member When refresh channel Should not change flow value`() =
        runBlockingTest {
            val cid = "channelType:channelId"
            val sut = Fixture()
                .givenCurrentUser(randomUser())
                .givenNewChannelControllerForChannel()
                .setupChatRepositories()
                .get()
            val channel = randomChannel(cid = cid, members = emptyList())
            sut.updateQueryChannelSpec(channel)

            sut.refreshChannel(channel.cid)

            val result = sut.channels.value
            result.size shouldBeEqualTo 0
        }

    @Test
    fun `when refreshing channel which contain current user as member should set value to flow`() =
        runBlockingTest {
            val user = randomUser()
            val sut = Fixture()
                .givenCurrentUser(user)
                .setupChatRepositories()
                .get()
            val channel = randomChannel(cid = "ChannelType:ChannelID", members = listOf(randomMember(user = user)))
            sut.updateQueryChannelSpec(channel)

            sut.refreshChannel("ChannelType:ChannelID")

            val result = sut.channels.value
            result.size shouldBeEqualTo 1
            result.first().cid shouldBeEqualTo "ChannelType:ChannelID"
        }

    @Test
    fun `When a channel updated arrives Shouldn't check if filter matches the channel`() =
        runBlockingTest {
            val channel = randomChannel()
            val channelController: ChannelController = mock {
                on(mock.toChannel()) doReturn channel
            }
            val queryController = Fixture()
                .givenNewChannelControllerForChannel(channelController)
                .get()

            queryController.handleEvent(randomChannelUpdatedEvent(channel = channel))

            Truth.assertThat(queryController.queryChannelsSpec.cids).doesNotContain(channel.cid)
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
                .enableFilterOnChannelUpdatedEvent()
                .get()

            queryController.handleEvent(randomChannelUpdatedEvent(channel = channel))

            Truth.assertThat(queryController.queryChannelsSpec.cids).contains(channel.cid)
        }

    @Test
    fun `When a channel updated by user arrives Shouldn't check if filter matches the channel`() =
        runBlockingTest {
            val channel = randomChannel()
            val channelController: ChannelController = mock {
                on(mock.toChannel()) doReturn channel
            }
            val queryController = Fixture()
                .givenNewChannelControllerForChannel(channelController)
                .get()

            queryController.handleEvent(randomChannelUpdatedByUserEvent(channel = channel))

            Truth.assertThat(queryController.queryChannelsSpec.cids).doesNotContain(channel.cid)
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
                .enableFilterOnChannelUpdatedEvent()
                .get()

            queryController.handleEvent(randomChannelUpdatedByUserEvent(channel = channel))

            Truth.assertThat(queryController.queryChannelsSpec.cids).contains(channel.cid)
        }

    @Test
    fun `When a channel updated arrives Shouldn't check if filter matches the channel to remove it`() =
        runBlockingTest {
            val channel = randomChannel()
            val channelController: ChannelController = mock {
                on(mock.toChannel()) doReturn channel
            }
            val queryController = Fixture()
                .addInitialChannel(channel)
                .givenNewChannelControllerForChannel(channelController)
                .get()

            queryController.handleEvent(randomChannelUpdatedEvent(channel = channel))

            Truth.assertThat(queryController.queryChannelsSpec.cids).contains(channel.cid)
        }

    @Test
    fun `When a channel updated arrives Should remove the channel when filter matches and it was added previously`() =
        runBlockingTest {
            val channel = randomChannel()
            val channelController: ChannelController = mock {
                on(mock.toChannel()) doReturn channel
            }
            val queryController = Fixture()
                .givenCurrentUser(randomUser())
                .givenNewChannelControllerForChannel(channelController)
                .addInitialChannel(channel)
                .enableFilterOnChannelUpdatedEvent()
                .get()

            queryController.handleEvent(randomChannelUpdatedEvent(channel = channel))

            Truth.assertThat(queryController.queryChannelsSpec.cids).doesNotContain(channel.cid)
        }

    @Test
    fun `When a channel updated by user arrives Shouldn't check if filter matches the channel to remove it`() =
        runBlockingTest {
            val channel = randomChannel()
            val channelController: ChannelController = mock {
                on(mock.toChannel()) doReturn channel
            }
            val queryController = Fixture()
                .addInitialChannel(channel)
                .givenCurrentUser(randomUser())
                .givenNewChannelControllerForChannel(channelController)
                .get()

            queryController.handleEvent(randomChannelUpdatedByUserEvent(channel = channel))

            Truth.assertThat(queryController.queryChannelsSpec.cids).contains(channel.cid)
        }

    @Test
    fun `When a channel updated by user arrives Should remove the channel when filter matches and it was added previously`() =
        runBlockingTest {
            val channel = randomChannel()
            val channelController: ChannelController = mock {
                on(mock.toChannel()) doReturn channel
            }
            val queryController = Fixture()
                .addInitialChannel(channel)
                .givenCurrentUser(randomUser())
                .givenNewChannelControllerForChannel(channelController)
                .enableFilterOnChannelUpdatedEvent()
                .get()

            queryController.handleEvent(randomChannelUpdatedByUserEvent(channel = channel))

            Truth.assertThat(queryController.queryChannelsSpec.cids).doesNotContain(channel.cid)
        }

    @Test
    fun `When a notification channel deleted arrives Should remove the channel`() =
        runBlockingTest {
            val channel = randomChannel()
            val channelController: ChannelController = mock {
                on(mock.toChannel()) doReturn channel
            }
            val queryController = Fixture()
                .addInitialChannel(channel)
                .givenNewChannelControllerForChannel(channelController)
                .get()

            queryController.handleEvent(randomNotificationChannelDeletedEvent(channel = channel))

            Truth.assertThat(queryController.queryChannelsSpec.cids).doesNotContain(channel.cid)
        }

    @Test
    fun `When a channel deleted arrives Should remove the channel`() =
        runBlockingTest {
            val channel = randomChannel()
            val channelController: ChannelController = mock {
                on(mock.toChannel()) doReturn channel
            }
            val queryController = Fixture()
                .addInitialChannel(channel)
                .givenNewChannelControllerForChannel(channelController)
                .get()

            queryController.handleEvent(randomChannelDeletedEvent(channel = channel))

            Truth.assertThat(queryController.queryChannelsSpec.cids).doesNotContain(channel.cid)
        }

    @Test
    fun `When a notification added to channel arrives Should remove the channel when filter matches and it was added previously`() =
        runBlockingTest {
            val channel = randomChannel()
            val channelController: ChannelController = mock {
                on(mock.toChannel()) doReturn channel
            }
            val queryController = Fixture()
                .addInitialChannel(channel)
                .givenCurrentUser(randomUser())
                .givenNewChannelControllerForChannel(channelController)
                .get()

            queryController.handleEvent(randomNotificationAddedToChannelEvent(channel = channel))

            Truth.assertThat(queryController.queryChannelsSpec.cids).doesNotContain(channel.cid)
        }

    @Test
    fun `When a notification added to channel arrives Should add the channel when filter matches and it wasn't added yet`() =
        runBlockingTest {
            val channel = randomChannel()
            val channelController: ChannelController = mock {
                on(mock.toChannel()) doReturn channel
            }
            val queryController = Fixture()
                .givenNewChannelControllerForChannel(channelController)
                .get()

            queryController.handleEvent(randomNotificationAddedToChannelEvent(channel = channel))

            Truth.assertThat(queryController.queryChannelsSpec.cids).contains(channel.cid)
        }
}

private class Fixture {
    private val chatClient: ChatClient = mock()
    private val chatDomainImpl: ChatDomainImpl = mock()
    private var querySort: QuerySort<Channel> = QuerySort()
    private val testCoroutineScope = TestCoroutineScope()

    private var currentUser: User? = null
    private var channelType: String = ""
    private var checkFilterOnChannelUpdatedEvent = false
    private val initialCids = mutableSetOf<String>()

    init {
        whenever(chatDomainImpl.job) doReturn Job()
        whenever(chatDomainImpl.scope) doReturn testCoroutineScope
        whenever(chatDomainImpl.repos) doReturn mock()
        whenever(chatDomainImpl.channel(any<Channel>())) doReturn mock()
        whenever(chatDomainImpl.channel(any<String>())) doAnswer { invocation ->
            mock<ChannelController> {
                on { toChannel() } doReturn randomChannel(
                    cid = invocation.arguments[0] as String,
                    type = channelType,
                )
            }
        }
    }

    fun givenCurrentUser(user: User) = apply {
        currentUser = user
        whenever(chatDomainImpl.user) doReturn MutableStateFlow(currentUser)
    }

    fun givenNewChannelControllerForChannel(channelController: ChannelController = mock()): Fixture = apply {
        whenever(chatDomainImpl.channel(any<Channel>())) doReturn channelController
        whenever(chatDomainImpl.channel(any<String>())) doReturn channelController
    }

    fun givenChannelType(channelType: String) = apply {
        this.channelType = channelType
    }

    fun setupChatRepositories(): Fixture = apply {
        whenever(chatDomainImpl.repos) doReturn mock()
    }

    fun enableFilterOnChannelUpdatedEvent() = apply {
        checkFilterOnChannelUpdatedEvent = true
    }

    fun addInitialChannel(channel: Channel) = apply {
        initialCids.add(channel.cid)
    }

    @OptIn(ExperimentalStreamChatApi::class)
    fun get(): QueryChannelsController {
        val filter = Filters.neutral()
        val mutableState = QueryChannelsMutableState(filter, querySort, chatDomainImpl.scope)
        return QueryChannelsController(
            filter,
            querySort,
            chatDomainImpl,
            mutableState,
            QueryChannelsLogic(mutableState, chatDomainImpl),
        ).apply {
            newChannelEventFilter = { channel, _ ->
                if (currentUser == null) {
                    true
                } else {
                    channel.members.any { member -> member.user.id == currentUser!!.id }
                }
            }
            checkFilterOnChannelUpdatedEvent = this@Fixture.checkFilterOnChannelUpdatedEvent
            queryChannelsSpec.cids = initialCids
        }
    }
}

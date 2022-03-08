package io.getstream.chat.android.offline.querychannels

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.HasChannel
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.SynchronizedCoroutineTest
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.experimental.channel.logic.ChannelLogic
import io.getstream.chat.android.offline.experimental.global.GlobalMutableState
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
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
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.test.positiveRandomInt
import io.getstream.chat.android.test.randomCID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldNotContain
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
internal class QueryChannelsControllerTest : SynchronizedCoroutineTest {

    private val scope = TestCoroutineScope()

    override fun getTestScope(): TestCoroutineScope = scope

    @Test
    fun `when add channel if filter matches should update LiveData from channel to channel controller`() =
        coroutineTest {
            val currentUser = randomUser()
            val newChannel = randomChannel(
                members = List(positiveRandomInt(10)) { randomMember() } + randomMember(user = currentUser)
            )
            val channelController = mock<ChannelController>()
            val sut = Fixture(scope)
                .givenChannelFilterResponse(listOf(newChannel))
                .givenCurrentUser(currentUser)
                .givenNewChannelControllerForChannel(channelController)
                .get()

            sut.updateQueryChannelCollectionByNewChannel(newChannel)

            verify(channelController).updateDataFromChannel(eq(newChannel))
        }

    @Test
    fun `when add channel if filter matches should post value to liveData with the same channel ID`() =
        coroutineTest {
            val currentUser = randomUser()
            val cid = randomCID()
            val newChannel = randomChannel(
                cid = cid,
                id = cid.substringAfter(":"),
                type = cid.substringBefore(":"),
                members = List(positiveRandomInt(10)) { randomMember() } + randomMember(user = currentUser)
            )
            val sut = Fixture(scope)
                .givenChannelFilterResponse(listOf(newChannel))
                .givenCurrentUser(currentUser)
                .givenChannelType(newChannel.type)
                .givenNewChannelControllerForChannel()
                .get()

            sut.updateQueryChannelCollectionByNewChannel(newChannel)

            val result = sut.channels.value
            result.size shouldBeEqualTo 1
            result.first().cid shouldBeEqualTo newChannel.cid
        }

    @Test
    fun `when add channel twice if filter matches should post value to liveData only one value`() =
        coroutineTest {
            val currentUser = randomUser()
            val cid = randomCID()
            val newChannel = randomChannel(
                cid = cid,
                id = cid.substringAfter(":"),
                type = cid.substringBefore(":"),
                members = List(positiveRandomInt(10)) { randomMember() } + randomMember(user = currentUser)
            )
            val sut = Fixture(scope)
                .givenChannelFilterResponse(listOf(newChannel))
                .givenCurrentUser(currentUser)
                .givenChannelType(newChannel.type)
                .givenNewChannelControllerForChannel()
                .get()

            sut.updateQueryChannelCollectionByNewChannel(newChannel)
            sut.updateQueryChannelCollectionByNewChannel(newChannel)

            val result = sut.channels.value
            result.size shouldBeEqualTo 1
            result.first().cid shouldBeEqualTo newChannel.cid
        }

    @Test
    fun `Given channel without current user as member When refresh channel Should not change flow value`() =
        runBlockingTest {
            val cid = "channelType:channelId"
            val sut = Fixture(scope)
                .givenChannelFilterResponse(emptyList())
                .givenCurrentUser(randomUser())
                .givenNewChannelControllerForChannel()
                .setupChatRepositories()
                .get()
            val channel = randomChannel(cid = cid, members = emptyList())
            sut.updateQueryChannelCollectionByNewChannel(channel)

            sut.refreshChannel(channel.cid)

            val result = sut.channels.value
            result.size shouldBeEqualTo 0
        }

    @Test
    fun `When a channel updated arrives Shouldn't check if filter matches the channel`() =
        runBlockingTest {
            val channel = randomChannel()
            val channelController: ChannelController = mock {
                on(mock.toChannel()) doReturn channel
            }
            val queryController = Fixture(scope)
                .givenNewChannelControllerForChannel(channelController)
                .get()

            queryController.handleEvent(randomChannelUpdatedEvent(channel = channel))

            queryController.queryChannelsSpec.cids shouldNotContain channel.cid
        }

    @Test
    fun `When a channel updated arrives Should add the channel when handle result add and it wasn't added yet`() =
        runBlockingTest {
            val channel = randomChannel()
            val channelController: ChannelController = mock {
                on(mock.toChannel()) doReturn channel
            }
            val channelLogic: ChannelLogic = mock {
                on(it.toChannel()) doReturn channel
            }
            val queryController = Fixture(scope)
                .givenChatEventHandler { event, _, _ ->
                    when (event) {
                        is ChannelUpdatedEvent -> EventHandlingResult.Add(event.channel)
                        else -> EventHandlingResult.Skip
                    }
                }
                .givenNewChannelControllerForChannel(channelController)
                .givenNewChannelLogicForChannel(channelLogic)
                .get()

            queryController.handleEvent(randomChannelUpdatedEvent(channel = channel))

            queryController.queryChannelsSpec.cids shouldContain channel.cid
        }

    @Test
    fun `When a channel updated by user arrives Given handler skip result Shouldn't add the channel`() =
        runBlockingTest {
            val channel = randomChannel()
            val channelController: ChannelController = mock {
                on(mock.toChannel()) doReturn channel
            }
            val queryController = Fixture(scope)
                .givenChatEventHandler { _, _, _ -> EventHandlingResult.Skip }
                .givenNewChannelControllerForChannel(channelController)
                .get()

            queryController.handleEvent(randomChannelUpdatedByUserEvent(channel = channel))

            queryController.queryChannelsSpec.cids shouldNotContain channel.cid
        }

    @Test
    fun `When a channel updated by user arrives Should add the channel when handling result add and it wasn't added yet`() =
        runBlockingTest {
            val channel = randomChannel()
            val channelController: ChannelController = mock {
                on(mock.toChannel()) doReturn channel
            }
            val channelLogic: ChannelLogic = mock {
                on(it.toChannel()) doReturn channel
            }
            val queryController = Fixture(scope)
                .givenChatEventHandler { event, _, _ ->
                    when (event) {
                        is ChannelUpdatedByUserEvent -> EventHandlingResult.Add(event.channel)
                        else -> EventHandlingResult.Skip
                    }
                }
                .givenChannelFilterResponse(listOf(channel))
                .givenNewChannelControllerForChannel(channelController)
                .givenNewChannelLogicForChannel(channelLogic)
                .get()

            queryController.handleEvent(randomChannelUpdatedByUserEvent(channel = channel))

            queryController.queryChannelsSpec.cids shouldContain channel.cid
        }

    @Test
    fun `When a channel updated arrives Given the channel in collection and handling result skip Shouldn't remove it`() =
        runBlockingTest {
            val channel = randomChannel()
            val channelController: ChannelController = mock {
                on(mock.toChannel()) doReturn channel
            }
            val queryController = Fixture(scope)
                .givenChatEventHandler { _, _, _ -> EventHandlingResult.Skip }
                .addInitialChannel(channel)
                .givenNewChannelControllerForChannel(channelController)
                .get()

            queryController.handleEvent(randomChannelUpdatedEvent(channel = channel))

            queryController.queryChannelsSpec.cids shouldContain channel.cid
        }

    @Test
    fun `When a channel updated arrives Should remove the channel when filter matches and it was added previously`() =
        runBlockingTest {
            val channel = randomChannel()
            val channelController: ChannelController = mock {
                on(mock.toChannel()) doReturn channel
            }
            val queryController = Fixture(scope)
                .givenCurrentUser(randomUser())
                .givenNewChannelControllerForChannel(channelController)
                .addInitialChannel(channel)
                .get()

            queryController.handleEvent(randomChannelUpdatedEvent(channel = channel))

            queryController.queryChannelsSpec.cids shouldNotContain channel.cid
        }

    @Test
    fun `When a channel updated by user arrives Given channel in collection and handle result skip Shouldn't remove it`() =
        runBlockingTest {
            val channel = randomChannel()
            val channelController: ChannelController = mock {
                on(mock.toChannel()) doReturn channel
            }
            val queryController = Fixture(scope)
                .addInitialChannel(channel)
                .givenChatEventHandler { _, _, _ -> EventHandlingResult.Skip }
                .givenCurrentUser(randomUser())
                .givenNewChannelControllerForChannel(channelController)
                .get()

            queryController.handleEvent(randomChannelUpdatedByUserEvent(channel = channel))

            queryController.queryChannelsSpec.cids shouldContain channel.cid
        }

    @Test
    fun `When a channel updated by user arrives Should remove the channel when filter matches and it was added previously`() =
        runBlockingTest {
            val channel = randomChannel()
            val channelController: ChannelController = mock {
                on(mock.toChannel()) doReturn channel
            }
            val queryController = Fixture(scope)
                .addInitialChannel(channel)
                .givenCurrentUser(randomUser())
                .givenNewChannelControllerForChannel(channelController)
                .get()

            queryController.handleEvent(randomChannelUpdatedByUserEvent(channel = channel))

            queryController.queryChannelsSpec.cids shouldNotContain channel.cid
        }

    @Test
    fun `When a notification channel deleted arrives Should remove the channel`() =
        runBlockingTest {
            val channel = randomChannel()
            val channelController: ChannelController = mock {
                on(mock.toChannel()) doReturn channel
            }
            val queryController = Fixture(scope)
                .addInitialChannel(channel)
                .givenNewChannelControllerForChannel(channelController)
                .get()

            queryController.handleEvent(randomNotificationChannelDeletedEvent(channel = channel))

            queryController.queryChannelsSpec.cids shouldNotContain channel.cid
        }

    @Test
    fun `When a channel deleted arrives Should remove the channel`() =
        runBlockingTest {
            val channel = randomChannel()
            val channelController: ChannelController = mock {
                on(mock.toChannel()) doReturn channel
            }
            val queryController = Fixture(scope)
                .addInitialChannel(channel)
                .givenNewChannelControllerForChannel(channelController)
                .get()

            queryController.handleEvent(randomChannelDeletedEvent(channel = channel))

            queryController.queryChannelsSpec.cids shouldNotContain channel.cid
        }

    @Test
    fun `When a notification added to channel arrives Should remove the channel when filter matches and it was added previously`() =
        runBlockingTest {
            val channel = randomChannel()
            val channelController: ChannelController = mock {
                on(mock.toChannel()) doReturn channel
            }
            val queryController = Fixture(scope)
                .addInitialChannel(channel)
                .givenCurrentUser(randomUser())
                .givenNewChannelControllerForChannel(channelController)
                .get()

            queryController.handleEvent(randomNotificationAddedToChannelEvent(channel = channel))

            queryController.queryChannelsSpec.cids shouldNotContain channel.cid
        }

    @Test
    fun `When a notification added to channel arrives Given handling result add Should add the channel`() =
        runBlockingTest {
            val channel = randomChannel()
            val channelController: ChannelController = mock {
                on(mock.toChannel()) doReturn channel
            }
            val channelLogic: ChannelLogic = mock {
                on(it.toChannel()) doReturn channel
            }
            val queryController = Fixture(scope)
                .givenChatEventHandler { event, _, _ ->
                    when (event) {
                        is NotificationAddedToChannelEvent -> EventHandlingResult.Add(event.channel)
                        else -> EventHandlingResult.Skip
                    }
                }
                .givenNewChannelControllerForChannel(channelController)
                .givenNewChannelLogicForChannel(channelLogic)
                .get()

            queryController.handleEvent(randomNotificationAddedToChannelEvent(channel = channel))

            queryController.queryChannelsSpec.cids shouldContain channel.cid
        }
}

@OptIn(ExperimentalCoroutinesApi::class)
private class Fixture constructor(testCoroutineScope: TestCoroutineScope) {
    private val chatClient: ChatClient = mock()
    private val chatDomainImpl: ChatDomainImpl = mock()
    private val logicRegistry: LogicRegistry = mock()
    private var querySort: QuerySort<Channel> = QuerySort()

    private var currentUser: User? = null
    private var channelType: String = ""
    private val initialCids = mutableSetOf<String>()
    private var chatEventHandler = ChatEventHandler { chatEvent, _, _ ->
        when (chatEvent) {
            is HasChannel -> {
                if (chatEvent.channel.members.any { member -> member.user.id == currentUser!!.id }) {
                    EventHandlingResult.Add(chatEvent.channel)
                } else {
                    EventHandlingResult.Remove(chatEvent.channel.cid)
                }
            }
            else -> EventHandlingResult.Skip
        }
    }

    init {
        whenever(chatDomainImpl.job) doReturn Job()
        whenever(chatDomainImpl.scope) doReturn testCoroutineScope
        whenever(chatDomainImpl.repos) doReturn mock()
        whenever(chatDomainImpl.channel(any())) doReturn mock()
        whenever(chatDomainImpl.channel(any())) doAnswer { invocation ->
            mock {
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
        whenever(chatDomainImpl.channel(any())) doReturn channelController
    }

    fun givenNewChannelLogicForChannel(channelLogic: ChannelLogic = mock()): Fixture = apply {
        whenever(logicRegistry.channel(any(), any())) doReturn channelLogic
    }

    fun givenChannelType(channelType: String) = apply {
        this.channelType = channelType
    }

    fun setupChatRepositories(): Fixture = apply {
        whenever(chatDomainImpl.repos) doReturn mock()
    }

    fun addInitialChannel(channel: Channel) = apply {
        initialCids.add(channel.cid)
    }

    fun get(): QueryChannelsController {
        val filter = Filters.neutral()
        val mutableState =
            QueryChannelsMutableState(filter, querySort, chatDomainImpl.scope, MutableStateFlow(emptyMap()))

        val queryChannelsLogic = QueryChannelsLogic(
            mutableState,
            chatDomainImpl,
            chatClient,
            chatDomainImpl.repos,
            GlobalMutableState.create(),
            logicRegistry
        )
        return QueryChannelsController(chatDomainImpl, mutableState, queryChannelsLogic)
            .apply {
                chatEventHandler = this@Fixture.chatEventHandler
                queryChannelsSpec.cids = initialCids
            }
    }

    fun givenChannelFilterResponse(channels: List<Channel>): Fixture = apply {
        whenever(chatClient.queryChannelsInternal(any())) doReturn channels.asCall()
    }

    fun givenChatEventHandler(chatEventHandler: ChatEventHandler): Fixture = apply {
        this.chatEventHandler = chatEventHandler
    }
}

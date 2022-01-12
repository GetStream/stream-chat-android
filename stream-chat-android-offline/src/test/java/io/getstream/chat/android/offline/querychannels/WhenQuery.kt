package io.getstream.chat.android.offline.querychannels

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.SynchronizedCoroutineTest
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.experimental.querychannels.logic.QueryChannelsLogic
import io.getstream.chat.android.offline.experimental.querychannels.state.QueryChannelsMutableState
import io.getstream.chat.android.offline.randomChannel
import io.getstream.chat.android.offline.randomUser
import io.getstream.chat.android.offline.repository.RepositoryFacade
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.asCall
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestCoroutineScope
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeTrue
import org.junit.jupiter.api.Test
import java.util.Date

@ExperimentalCoroutinesApi
internal class WhenQuery : SynchronizedCoroutineTest {

    private val scope = TestCoroutineScope()

    override fun getTestScope(): TestCoroutineScope = scope

    @Test
    fun `Should request query channels spec in DB`() = coroutineTest {
        val repositories = mock<RepositoryFacade>()
        val sut = Fixture(scope)
            .givenRepoFacade(repositories)
            .givenFailedNetworkRequest()
            .get()

        sut.query()

        verify(repositories).selectBy(any(), any())
    }

    @Test
    fun `Should request channels to chat client`() = coroutineTest {
        val chatClient = mock<ChatClient>()
        val sut = Fixture(scope)
            .givenChatClient(chatClient)
            .givenFailedNetworkRequest()
            .get()

        sut.query()

        verify(chatClient).queryChannelsInternal(any())
    }

    @Test
    fun `Given channels in DB and failed network request Should return channels from DB`() = coroutineTest {
        val dbChannels = listOf(randomChannel(cid = "cid1"), randomChannel(cid = "cid2"))
        val sut = Fixture(scope)
            .givenFailedNetworkRequest()
            .givenQueryChannelsSpec(QueryChannelsSpec(Filters.neutral(), QuerySort()))
            .givenDBChannels(dbChannels)
            .get()

        val result = sut.query()

        result.isSuccess.shouldBeTrue()
        result.data() shouldBeEqualTo dbChannels
    }

    @Test
    fun `Given channels in DB and successful network request Should return channels from network response`() =
        coroutineTest {
            val dbChannel = randomChannel(cid = "cid", lastMessageAt = Date(1000L))
            val networkChannels = listOf(dbChannel.copy(lastMessageAt = Date(2000L)), randomChannel(cid = "cid2"))
            val sut = Fixture(scope)
                .givenQueryChannelsSpec(QueryChannelsSpec(Filters.neutral(), QuerySort()))
                .givenDBChannels(listOf(dbChannel))
                .givenNetworkChannels(networkChannels)
                .get()

            val result = sut.query()

            result.isSuccess.shouldBeTrue()
            result.data() shouldBeEqualTo networkChannels
        }

    @Test
    fun `Given DB channels and failed network response Should set channels from db to channels flow in properly sorted order`() =
        coroutineTest {
            val dbChannel1 = randomChannel(cid = "cid1", lastMessageAt = Date(1000L))
            val dbChannel2 = randomChannel(cid = "cid2", lastMessageAt = Date(2000L))
            val querySort = QuerySort.desc(Channel::lastMessageAt)
            val sut = Fixture(scope)
                .givenFailedNetworkRequest()
                .givenQuerySort(querySort)
                .givenQueryChannelsSpec(
                    QueryChannelsSpec(Filters.neutral(), QuerySort()).apply { cids = setOf("cid1", "cid2") }
                )
                .givenDBChannels(listOf(dbChannel1, dbChannel2))
                .get()

            sut.query()

            sut.channels.value shouldBeEqualTo listOf(dbChannel2, dbChannel1)
        }

    @Test
    fun `Given DB channels and network channels Should set channels from network to channels flow in properly sorted order`() =
        coroutineTest {
            val dbChannel = randomChannel(cid = "cid1", lastMessageAt = Date(1000L))
            val networkChannel1 = dbChannel.copy(lastMessageAt = Date(2000L))
            val networkChannel2 = randomChannel(cid = "cid2", lastMessageAt = Date(3000L))
            val querySort = QuerySort.desc(Channel::lastMessageAt)
            val sut = Fixture(scope)
                .givenQuerySort(querySort)
                .givenQueryChannelsSpec(
                    QueryChannelsSpec(Filters.neutral(), QuerySort()).apply { cids = setOf("cid1", "cid2") }
                )
                .givenDBChannels(listOf(dbChannel))
                .givenNetworkChannels(listOf(networkChannel1, networkChannel2))
                .get()

            sut.query()

            sut.channels.value shouldBeEqualTo listOf(networkChannel2, networkChannel1)
        }

    private class Fixture(val scope: TestCoroutineScope) {
        private var chatClient: ChatClient = mock()
        private var repositories: RepositoryFacade = mock()
        private var querySort: QuerySort<Channel> = QuerySort()

        private val user: User = randomUser()

        private var chatDomainImpl: ChatDomainImpl = mock {
            on(it.user) doReturn MutableStateFlow(user)
        }

        fun givenQuerySort(querySort: QuerySort<Channel>) = apply {
            this.querySort = querySort
        }

        fun givenRepoFacade(repositoryFacade: RepositoryFacade) = apply {
            repositories = repositoryFacade
        }

        fun givenChatDomain(chatDomainImpl: ChatDomainImpl) = apply {
            this.chatDomainImpl = chatDomainImpl
        }

        fun givenChatClient(chatClient: ChatClient) = apply {
            this.chatClient = chatClient
        }

        fun givenFailedNetworkRequest() = apply {
            whenever(chatClient.queryChannelsInternal(any())) doReturn TestCall(Result(mock()))
        }

        suspend fun givenQueryChannelsSpec(queryChannelsSpec: QueryChannelsSpec) = apply {
            whenever(repositories.selectBy(any(), any())) doReturn queryChannelsSpec
            whenever(chatDomainImpl.selectAndEnrichChannels(any(), any())) doReturn emptyList()
        }

        suspend fun givenDBChannels(dbChannels: List<Channel>) = apply {
            whenever(chatDomainImpl.channel(any<String>())) doAnswer { invocationOnMock ->
                val cid = invocationOnMock.arguments[0] as String
                mock<ChannelController> {
                    on { toChannel() } doReturn dbChannels.first { it.cid == cid }
                }
            }
            whenever(chatDomainImpl.channel(any<Channel>())) doAnswer { invocationOnMock ->
                val channel = invocationOnMock.arguments[0] as Channel
                mock<ChannelController> {
                    on { toChannel() } doReturn channel
                }
            }
            whenever(repositories.selectChannels(any(), any(), any())) doReturn dbChannels
        }

        fun givenNetworkChannels(channels: List<Channel>) = apply {
            whenever(chatClient.queryChannelsInternal(any())) doReturn channels.asCall()

            whenever(chatDomainImpl.channel(any<String>())) doAnswer { invocationOnMock ->
                val cid = invocationOnMock.arguments[0] as String
                mock<ChannelController> {
                    on { toChannel() } doReturn channels.first { it.cid == cid }
                }
            }
            whenever(chatDomainImpl.channel(any<Channel>())) doAnswer { invocationOnMock ->
                val channel = invocationOnMock.arguments[0] as Channel
                mock<ChannelController> {
                    on { toChannel() } doReturn channel
                }
            }
        }

        fun get(): QueryChannelsController {
            whenever(chatDomainImpl.scope) doReturn scope
            whenever(chatDomainImpl.repos) doReturn repositories
            whenever(chatDomainImpl.client) doReturn chatClient
            val filter = Filters.neutral()
            val mutableState = QueryChannelsMutableState(
                filter, querySort,
                chatDomainImpl.scope,
                MutableStateFlow(
                    mapOf(user.id to user)
                )
            )

            return QueryChannelsController(
                chatDomainImpl,
                mutableState,
                QueryChannelsLogic(mutableState, chatDomainImpl, chatClient),
            )
        }
    }
}

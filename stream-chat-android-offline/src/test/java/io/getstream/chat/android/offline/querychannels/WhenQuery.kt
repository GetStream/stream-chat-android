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
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.controller.QueryChannelsSpec
import io.getstream.chat.android.livedata.randomChannel
import io.getstream.chat.android.livedata.randomUser
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.repository.RepositoryFacade
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.asCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import java.util.Date

@ExperimentalCoroutinesApi
internal class WhenQuery {

    @Test
    fun `Should request query channels spec in DB`() = runBlockingTest {
        val repositories = mock<RepositoryFacade>()
        val sut = Fixture()
            .givenRepoFacade(repositories)
            .givenFailedNetworkRequest()
            .get()

        sut.query()

        verify(repositories).selectById(any())
    }

    @Test
    fun `Given DB with query channels Should invoke selectAndEnrichChannels in ChatDomain`() = runBlockingTest {
        val user: User = randomUser()

        val chatDomainImpl: ChatDomainImpl = mock {
            on(it.currentUser) doReturn user
        }
        val sut = Fixture()
            .givenChatDomain(chatDomainImpl)
            .givenFailedNetworkRequest()
            .givenQueryChannelsSpec(
                QueryChannelsSpec(
                    Filters.neutral(),
                    QuerySort.desc(Channel::lastMessageAt),
                    cids = listOf("cid1", "cid2")
                )
            )
            .get()

        sut.query()

        verify(chatDomainImpl).selectAndEnrichChannels(eq(listOf("cid1", "cid2")), any())
    }

    @Test
    fun `Should request channels to chat client`() = runBlockingTest {
        val chatClient = mock<ChatClient>()
        val sut = Fixture()
            .givenChatClient(chatClient)
            .givenFailedNetworkRequest()
            .get()

        sut.query()

        verify(chatClient).queryChannels(any())
    }

    @Test
    fun `Given channels in DB and failed network request Should return channels from DB`() = runBlockingTest {
        val dbChannels = listOf(randomChannel(cid = "cid1"), randomChannel(cid = "cid2"))
        val sut = Fixture()
            .givenFailedNetworkRequest()
            .givenQueryChannelsSpec(
                QueryChannelsSpec(
                    Filters.neutral(),
                    QuerySort.Companion.desc(Channel::lastMessageAt), cids = listOf("cid1", "cid2")
                )
            )
            .givenDBChannels(dbChannels)
            .get()

        val result = sut.query()

        Truth.assertThat(result.isSuccess).isTrue()
        Truth.assertThat(result.data()).isEqualTo(dbChannels)
    }

    @Test
    fun `Given channels in DB and successful network request Should return channels from network response`() =
        runBlockingTest {
            val dbChannel = randomChannel(cid = "cid", lastMessageAt = Date(1000L))
            val networkChannels = listOf(dbChannel.copy(lastMessageAt = Date(2000L)), randomChannel(cid = "cid2"))
            val sut = Fixture()
                .givenQueryChannelsSpec(
                    QueryChannelsSpec(
                        Filters.neutral(),
                        QuerySort.Companion.desc(Channel::lastMessageAt), cids = listOf("cid1", "cid2")
                    )
                )
                .givenDBChannels(listOf(dbChannel))
                .givenNetworkChannels(networkChannels)
                .get()

            val result = sut.query()

            Truth.assertThat(result.isSuccess).isTrue()
            Truth.assertThat(result.data()).isEqualTo(networkChannels)
        }

    @Test
    fun `Given DB channels and failed network response Should set channels from db to channels flow in properly sorted order`() =
        runBlockingTest {
            val dbChannel1 = randomChannel(cid = "cid1", lastMessageAt = Date(1000L))
            val dbChannel2 = randomChannel(cid = "cid2", lastMessageAt = Date(2000L))
            val querySort = QuerySort.desc(Channel::lastMessageAt)
            val sut = Fixture()
                .givenFailedNetworkRequest()
                .givenQuerySort(querySort)
                .givenQueryChannelsSpec(
                    QueryChannelsSpec(
                        Filters.neutral(),
                        querySort,
                        cids = listOf("cid1", "cid2")
                    )
                )
                .givenDBChannels(listOf(dbChannel1, dbChannel2))
                .get()

            sut.query()

            Truth.assertThat(sut.channels.value).isEqualTo(listOf(dbChannel2, dbChannel1))
        }

    @Test
    fun `Given DB channels and network channels Should set channels from network to channels flow in properly sorted order`() =
        runBlockingTest {
            val dbChannel = randomChannel(cid = "cid1", lastMessageAt = Date(1000L))
            val networkChannel1 = dbChannel.copy(lastMessageAt = Date(2000L))
            val networkChannel2 = randomChannel(cid = "cid2", lastMessageAt = Date(3000L))
            val querySort = QuerySort.desc(Channel::lastMessageAt)
            val sut = Fixture()
                .givenQuerySort(querySort)
                .givenQueryChannelsSpec(
                    QueryChannelsSpec(
                        Filters.neutral(),
                        querySort,
                        cids = listOf("cid1", "cid2")
                    )
                )
                .givenDBChannels(listOf(dbChannel))
                .givenNetworkChannels(listOf(networkChannel1, networkChannel2))
                .get()

            sut.query()

            Truth.assertThat(sut.channels.value).isEqualTo(listOf(networkChannel2, networkChannel1))
        }

    private class Fixture {
        private var chatClient: ChatClient = mock()
        private var repositories: RepositoryFacade = mock()
        private var scope: CoroutineScope = TestCoroutineScope()
        private var querySort: QuerySort<Channel> = QuerySort()

        private val user: User = randomUser()

        private var chatDomainImpl: ChatDomainImpl = mock {
            on(it.currentUser) doReturn user
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
            whenever(chatClient.queryChannels(any())) doReturn TestCall(Result(mock()))
        }

        suspend fun givenQueryChannelsSpec(queryChannelsSpec: QueryChannelsSpec) = apply {
            whenever(repositories.selectById(any())) doReturn queryChannelsSpec
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
            whenever(chatDomainImpl.selectAndEnrichChannels(any(), any())) doReturn dbChannels
        }

        fun givenNetworkChannels(channels: List<Channel>) = apply {
            whenever(chatClient.queryChannels(any())) doReturn channels.asCall()

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

            return QueryChannelsController(Filters.neutral(), querySort, chatClient, chatDomainImpl)
        }
    }
}

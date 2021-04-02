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
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.controller.ChannelControllerImpl
import io.getstream.chat.android.livedata.controller.QueryChannelsSpec
import io.getstream.chat.android.livedata.randomChannel
import io.getstream.chat.android.livedata.repository.RepositoryFacade
import io.getstream.chat.android.test.TestCall
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class WhenQuery {
    private lateinit var sut: QueryChannelsController
    private lateinit var chatClient: ChatClient
    private lateinit var repositories: RepositoryFacade
    private lateinit var scope: TestCoroutineScope
    private lateinit var chatDomainImpl: ChatDomainImpl
    private val channelId: String = "channelId"

    @BeforeEach
    fun before() {
        scope = TestCoroutineScope()
        repositories = mock()
        chatDomainImpl = mock {
            on { scope } doReturn scope
            on { repos } doReturn repositories
        }
        chatClient = mock()
        sut = QueryChannelsController(Filters.neutral(), mock(), chatClient, chatDomainImpl)

        whenever(chatClient.queryChannels(any())) doReturn TestCall(Result(mock()))
    }

    @Test
    fun `Should request query channels spec in DB`() = runBlockingTest {
        sut.query()

        verify(repositories).selectById(any())
    }

    @Test
    fun `Given DB with query channels Should invoke selectAndEnrichChannels in ChatDomain`() = runBlockingTest {
        whenever(repositories.selectById(any())) doReturn QueryChannelsSpec(
            Filters.neutral(),
            QuerySort.Companion.desc(Channel::lastMessageAt),
            cids = listOf("cid1", "cid2")
        )
        whenever(chatDomainImpl.selectAndEnrichChannels(any(), any())) doReturn emptyList()

        sut.query()

        verify(chatDomainImpl).selectAndEnrichChannels(eq(listOf("cid1", "cid2")), any())
    }

    @Test
    fun `Should request channels to chat client`() = runBlockingTest {
        sut.query()

        verify(chatClient).queryChannels(any())
    }

    @Test
    fun `Given channels in DB and failed network request Should return channels from DB`() = runBlockingTest {
        whenever(repositories.selectById(any())) doReturn QueryChannelsSpec(
            Filters.neutral(),
            QuerySort.Companion.desc(Channel::lastMessageAt),
            cids = listOf("cid1", "cid2")
        )
        val dbChannel1 = randomChannel(cid = "cid1")
        val dbChannel2 = randomChannel(cid = "cid2")
        whenever(chatDomainImpl.channel(any<String>())) doAnswer { invocationOnMock ->
            val cid = invocationOnMock.arguments[0] as String
            mock<ChannelControllerImpl> {
                on { toChannel() } doReturn if (cid == "cid1") dbChannel1 else dbChannel2
            }
        }
        whenever(chatDomainImpl.channel(any<Channel>())) doAnswer { invocationOnMock ->
            val channel = invocationOnMock.arguments[0] as Channel
            mock<ChannelControllerImpl> {
                on { toChannel() } doReturn channel
            }
        }
        whenever(chatDomainImpl.selectAndEnrichChannels(any(), any())) doReturn listOf(dbChannel1, dbChannel2)

        val result = sut.query()

        Truth.assertThat(result.isSuccess).isTrue()
        Truth.assertThat(result.data()).isEqualTo(listOf(dbChannel1, dbChannel2))
    }
}

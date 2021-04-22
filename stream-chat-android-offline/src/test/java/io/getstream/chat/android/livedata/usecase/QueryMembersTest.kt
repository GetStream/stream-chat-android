package io.getstream.chat.android.livedata.usecase

import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.NeutralFilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.randomUser
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.repository.RepositoryFacade
import io.getstream.chat.android.offline.usecase.QueryMembers
import io.getstream.chat.android.test.TestCall
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class QueryMembersTest {

    private lateinit var mockDomain: ChatDomainImpl
    private lateinit var mockClient: ChatClient
    private lateinit var mockChannelClient: ChannelClient
    private lateinit var mockRepo: RepositoryFacade
    private lateinit var queryMembers: QueryMembers

    private val channelType = "test"
    private val channelId = "channel"
    private val channel = Channel(cid = "$channelType:$channelId", type = channelType, id = channelId)
    private val filter: FilterObject = NeutralFilterObject
    private val sort = QuerySort.desc(Member::createdAt)
    private var onlineUsers = listOf(
        randomUser(),
        randomUser(),
        randomUser(),
        randomUser(),
        randomUser(),
        randomUser(),
        randomUser(),
        randomUser()
    )
    private var offlineUsers = listOf(
        randomUser(),
        randomUser(),
        randomUser(),
        randomUser(),
        randomUser(),
        randomUser(),
        randomUser(),
        randomUser()
    )
    private val onlineMembers = onlineUsers.map(::Member)
    private val offlineMembers = offlineUsers.map(::Member)

    @BeforeEach
    fun setup() = runBlocking {

        mockClient = mock()
        mockChannelClient = mock()

        // call through to mock client
        whenever(mockChannelClient.queryMembers(any(), any(), any(), any(), any())).thenAnswer {
            mockClient.queryMembers(
                channel.type,
                channel.id,
                0,
                0,
                filter,
                sort,
                emptyList()
            )
        }

        whenever(mockClient.channel(channel.cid)) doReturn mockChannelClient

        whenever(mockClient.queryMembers(any(), any(), any(), any(), any(), any(), any())) doReturn TestCall(
            Result(
                onlineMembers
            )
        )

        mockRepo = mock { repo ->
            on(repo.selectMembersForChannel(channel.cid)) doReturn offlineMembers
        }

        mockDomain = mock { domain ->
            on(domain.client) doReturn mockClient
            on(domain.repos) doReturn mockRepo
            on(domain.scope) doReturn TestCoroutineScope()
        }

        queryMembers = QueryMembers(mockDomain)
    }

    @Test
    fun `GIVEN default arguments, and ONLINE, SHOULD make a remote call via client`() = runBlockingTest {
        whenever(mockDomain.isOffline()).doReturn(false)

        val result = queryMembers(channel.cid, 0, 0, filter, sort).execute()

        verify(mockClient).queryMembers(
            channel.type,
            channel.id,
            0,
            0,
            filter,
            sort,
            emptyList()
        )

        Truth.assertThat(result.data()).isEqualTo(onlineMembers)
    }

    @Test
    fun `GIVEN default arguments, and ONLINE, SHOULD persist results to DB`() = runBlockingTest {
        whenever(mockDomain.isOffline()).doReturn(false)

        val result = queryMembers(channel.cid, 0, 0, filter, sort).execute()

        verify(mockRepo).updateMembersForChannel(channel.cid, onlineMembers)
        Truth.assertThat(result.data()).isEqualTo(onlineMembers)
    }

    @Test
    fun `GIVEN default arguments, and OFFLINE, SHOULD pull results from DB, not client`() = runBlockingTest {
        whenever(mockDomain.isOffline()).doReturn(true)

        val result = queryMembers(channel.cid, 0, 0, filter, sort).execute()

        verify(mockClient, never()).queryMembers(any(), any(), any(), any(), any(), any(), any())
        verify(mockRepo).selectMembersForChannel(channel.cid)
        Truth.assertThat(result.data()).isEqualTo(offlineMembers)
    }

    @Test
    fun `GIVEN limit, and OFFLINE, SHOULD receive results respecting limit`() = runBlockingTest {
        whenever(mockDomain.isOffline()).doReturn(true)

        val limit = 2
        val result = queryMembers(channel.cid, 0, limit, filter, sort).execute()

        Truth.assertThat(result.data().size).isEqualTo(limit)
    }

    @Test
    fun `GIVEN offset, and OFFLINE, SHOULD receive results respecting offset`() = runBlockingTest {
        whenever(mockDomain.isOffline()).doReturn(true)

        val offset = 2
        val result = queryMembers(channel.cid, offset, 0, filter, sort).execute()

        val data = result.data()
        Truth.assertThat(data.size).isEqualTo(offlineMembers.size - offset)
        Truth.assertThat(data).isEqualTo(offlineMembers.drop(offset))
    }

    @Test
    fun `GIVEN negative limit and offset, and OFFLINE, SHOULD ignore limit and offset`() = runBlockingTest {
        whenever(mockDomain.isOffline()).doReturn(true)

        val offset = -1
        val limit = -1
        val result = queryMembers(channel.cid, offset, limit, filter, sort).execute()

        val data = result.data()
        Truth.assertThat(data.size).isEqualTo(offlineMembers.size)
    }

    @Test
    fun `GIVEN offset greater than size of data set, and OFFLINE, SHOULD receive empty results`() = runBlockingTest {
        whenever(mockDomain.isOffline()).doReturn(true)

        val offset = 50
        val limit = -1
        val result = queryMembers(channel.cid, offset, limit, filter, sort).execute()

        val data = result.data()
        val expectedSize = (offlineMembers.size - offset).coerceAtLeast(0)
        Truth.assertThat(data.size).isEqualTo(expectedSize)
    }

    @Test
    fun `GIVEN limit greater than size of data set, and OFFLINE, SHOULD receive all results`() = runBlockingTest {
        whenever(mockDomain.isOffline()).doReturn(true)

        val offset = -1
        val limit = 50
        val result = queryMembers(channel.cid, offset, limit, filter, sort).execute()

        val data = result.data()
        Truth.assertThat(data.size).isEqualTo(offlineMembers.size)
    }

    @Test
    fun `GIVEN offset less than size of data set & limit less than offset data, and OFFLINE, SHOULD receive correct data set`() =
        runBlockingTest {
            whenever(mockDomain.isOffline()).doReturn(true)

            val offset = 2
            val limit = 2
            val result = queryMembers(channel.cid, offset, limit, filter, sort).execute()

            val data = result.data()
            Truth.assertThat(data.size).isEqualTo(limit)
        }
}

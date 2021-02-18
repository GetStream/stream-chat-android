package io.getstream.chat.android.livedata.usecase

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.randomUser
import io.getstream.chat.android.livedata.repository.RepositoryFacade
import io.getstream.chat.android.test.TestCall
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.When
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.any
import org.amshove.kluent.calling
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class QueryMembersTest {

    private lateinit var mockDomain: ChatDomainImpl
    private lateinit var mockClient: ChatClient
    private lateinit var mockRepo: RepositoryFacade
    private lateinit var queryMembers: QueryMembers

    private val channelType = "test"
    private val channelId = "channel"
    private val channel = Channel(cid = "$channelType:$channelId", type = channelType, id = channelId)
    private val filter = FilterObject()
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
        mockClient = mock { client ->
            on(client.queryMembers(any(), any(), any(), any(), any(), any(), any())) doReturn TestCall(
                Result(
                    onlineMembers
                )
            )
        }

        mockRepo = mock { repo ->
            on(repo.selectMembersForChannel(channel.cid)) doReturn offlineMembers
        }

        mockDomain = mock { domain ->
            on(domain.client) doReturn mockClient
            on(domain.repos) doReturn mockRepo
            on(domain.scope) doReturn TestCoroutineScope()
            on(domain.offlineEnabled) doReturn true
        }

        queryMembers = QueryMembers(mockDomain)
    }

    @Test
    fun `Given default arguments, and ONLINE, SHOULD make a remote call via client`() = runBlockingTest {
        When calling mockDomain.isOffline() doReturn false

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

        result.data() `should be equal to` onlineMembers
    }

    @Test
    fun `Given default arguments, and ONLINE, SHOULD persist results to DB`() = runBlockingTest {
        When calling mockDomain.isOffline() doReturn false

        val result = queryMembers(channel.cid, 0, 0, filter, sort).execute()

        verify(mockRepo).updateMembersForChannel(channel.cid, onlineMembers)
        result.data() `should be equal to` onlineMembers
    }

    @Test
    fun `GIVEN default arguments, and OFFLINE, SHOULD pull results from DB, not client`() = runBlockingTest {
        When calling mockDomain.isOffline() doReturn true

        val result = queryMembers(channel.cid, 0, 0, filter, sort).execute()

        verify(mockClient, never()).queryMembers(any(), any(), any(), any(), any(), any(), any())
        verify(mockRepo).selectMembersForChannel(channel.cid)

        result.data() `should be equal to` offlineMembers
    }

    @Test
    fun `GIVEN limit, and OFFLINE, SHOULD receive results respecting limit`() = runBlockingTest {
        When calling mockDomain.isOffline() doReturn true

        val limit = 2
        val result = queryMembers(channel.cid, 0, limit, filter, sort).execute()

        result.data().size `should be equal to` limit
    }

    @Test
    fun `GIVEN offset, and OFFLINE, SHOULD receive results respecting offset`() = runBlockingTest {
        When calling mockDomain.isOffline() doReturn true

        val offset = 2
        val result = queryMembers(channel.cid, offset, 0, filter, sort).execute()

        val data = result.data()
        data.size `should be equal to` offlineMembers.size - offset
        data `should be equal to` offlineMembers.drop(offset)
    }

    @Test
    fun `GIVEN limit AND offset, and OFFLINE, SHOULD receive results respecting both`() = runBlockingTest {
        When calling mockDomain.isOffline() doReturn true

        val offset = 2
        val limit = 3
        val result = queryMembers(channel.cid, offset, limit, filter, sort).execute()

        val data = result.data()
        data.size `should be equal to` limit
        data `should be equal to` offlineMembers.drop(offset).take(limit)
    }
}

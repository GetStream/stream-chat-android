package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.querysort.QuerySortByField
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.client.test.randomMember
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.test.randomCID
import io.getstream.chat.android.test.randomInt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

@OptIn(ExperimentalCoroutinesApi::class)
internal class QueryMembersListenerDatabaseTest {

    private val userRepository: UserRepository = mock()
    private val channelRepository: ChannelRepository = mock()
    private val queryMembersListenerDatabase = QueryMembersListenerDatabase(userRepository, channelRepository)

    @Test
    fun `when query members is successful database should be updated`() = runTest {
        val memberList = randomMember().let(::listOf)
        val cid = randomCID()
        val (type, id) = cid.cidToTypeAndId()

        queryMembersListenerDatabase.onQueryMembersResult(
            result = Result.success(memberList),
            channelType = type,
            channelId = id,
            offset = randomInt(),
            limit = randomInt(),
            filter = Filters.neutral(),
            sort = QuerySortByField.descByName("name"),
            members = memberList,
        )

        verify(userRepository).insertUsers(memberList.map(Member::user))
        verify(channelRepository).updateMembersForChannel(cid, memberList)
    }

    @Test
    fun `when query members fails database should not be updated`() = runTest {
        val memberList = randomMember().let(::listOf)
        val cid = randomCID()
        val (type, id) = cid.cidToTypeAndId()

        queryMembersListenerDatabase.onQueryMembersResult(
            result = Result.error(ChatError()),
            channelType = type,
            channelId = id,
            offset = randomInt(),
            limit = randomInt(),
            filter = Filters.neutral(),
            sort = QuerySortByField.descByName("name"),
            members = memberList,
        )

        verifyNoInteractions(userRepository, channelRepository)
    }
}

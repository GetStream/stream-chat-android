package io.getstream.chat.android.livedata.usecase

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.randomUser
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.repository.RepositoryFacade
import io.getstream.chat.android.offline.usecase.SearchUsersByName
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.randomBoolean
import io.getstream.chat.android.test.randomInt
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class SearchUsersByNameTests {
    private lateinit var chatDomainImpl: ChatDomainImpl
    private lateinit var chatClient: ChatClient
    private lateinit var repositoryFacade: RepositoryFacade
    private lateinit var sut: SearchUsersByName

    @BeforeEach
    fun setUp() {
        chatClient = mock()
        repositoryFacade = mock()
        chatDomainImpl = mock {
            on(it.client) doReturn chatClient
            on(it.repos) doReturn repositoryFacade
            on(it.scope) doReturn TestCoroutineScope()
            on(it.currentUser) doReturn randomUser()
        }
        sut = SearchUsersByName(chatDomainImpl)
    }

    @Test
    fun `Given empty search string and online state Should perform search query with default filter`() {
        whenever(chatClient.queryUsers(any())) doReturn TestCall(mock())
        whenever(chatDomainImpl.isOnline()) doReturn true

        sut(querySearch = "", offset = randomInt(), userLimit = randomInt(), userPresence = randomBoolean()).execute()

        verify(chatClient).queryUsers(
            argThat {
                filter == sut.defaultUsersQueryFilter
            }
        )
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `Given nonempty search string and online state Should perform search query with autocomplete filter`() {
        whenever(chatClient.queryUsers(any())) doReturn TestCall(mock())
        whenever(chatDomainImpl.isOnline()) doReturn true
        val querySearch = randomString()

        sut(querySearch = querySearch, offset = randomInt(), userLimit = randomInt(), userPresence = true).execute()
        verify(chatClient).queryUsers(
            com.nhaarman.mockitokotlin2.check {
                it.presence `should be equal to` true
                it.filter `should be equal to` Filters.and(
                    Filters.ne("id", chatDomainImpl.currentUser.id),
                    Filters.autocomplete("name", querySearch),
                )
            }
        )
    }

    @Test
    fun `Given nonempty search result and online state Should save result list to DB`() = runBlockingTest {
        whenever(chatClient.queryUsers(any())) doReturn TestCall(Result(listOf(randomUser(), randomUser())))
        whenever(chatDomainImpl.isOnline()) doReturn true

        sut(
            querySearch = randomString(),
            offset = randomInt(),
            userLimit = randomInt(),
            userPresence = randomBoolean()
        ).execute()

        verify(repositoryFacade).insertUsers(argThat { size == 2 })
    }

    @Test
    fun `Given empty search result and online state Should not save to DB`() = runBlockingTest {
        whenever(chatClient.queryUsers(any())) doReturn TestCall(Result(emptyList()))
        whenever(chatDomainImpl.isOnline()) doReturn true

        sut(
            querySearch = randomString(),
            offset = randomInt(),
            userLimit = randomInt(),
            userPresence = randomBoolean()
        ).execute()

        verify(chatClient).queryUsers(any())
        verify(repositoryFacade, never()).insertUsers(any())
    }

    @Test
    fun `Given empty search result and offline state Should fetch all users from DB`() = runBlockingTest {
        whenever(chatDomainImpl.isOnline()) doReturn false
        val dbUsers = listOf(randomUser(), randomUser())
        whenever(repositoryFacade.selectAllUsers(any(), any())) doReturn dbUsers

        val result = sut(
            querySearch = "",
            offset = randomInt(),
            userLimit = randomInt(),
            userPresence = randomBoolean()
        ).execute()

        verify(repositoryFacade).selectAllUsers(any(), any())
        result.data() `should be equal to` dbUsers
    }

    @Test
    fun `Given nonempty search result and offline state Should fetch all users from DB`() = runBlockingTest {
        whenever(chatDomainImpl.isOnline()) doReturn false
        val dbUsers = listOf(randomUser(), randomUser())
        whenever(repositoryFacade.selectUsersLikeName(any(), any(), any())) doReturn dbUsers
        val querySearch = randomString()

        val result = sut(
            querySearch = querySearch,
            offset = randomInt(),
            userLimit = randomInt(),
            userPresence = randomBoolean()
        ).execute()

        verify(repositoryFacade).selectUsersLikeName(eq(querySearch), any(), any())
        result.data() `should be equal to` dbUsers
    }
}

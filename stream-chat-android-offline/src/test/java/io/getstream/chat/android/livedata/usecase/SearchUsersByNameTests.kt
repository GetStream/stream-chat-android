package io.getstream.chat.android.livedata.usecase

import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.randomUser
import io.getstream.chat.android.livedata.repository.RepositoryFacade
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.randomInt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.When
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.any
import org.amshove.kluent.calling
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
        When calling chatClient.queryUsers(any()) doReturn TestCall(mock())
        When calling chatDomainImpl.isOnline() doReturn true

        sut(querySearch = "", randomInt(), randomInt()).execute()

        verify(chatClient).queryUsers(
            argThat {
                filter == sut.defaultUsersQueryFilter
            }
        )
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `Given nonempty search string and online state Should perform search query with autocomplete filter`() {
        When calling chatClient.queryUsers(any()) doReturn TestCall(mock())
        When calling chatDomainImpl.isOnline() doReturn true

        sut(querySearch = "searchString", randomInt(), randomInt()).execute()

        verify(chatClient).queryUsers(
            argThat {
                val filterMap = filter.data[Filters.KEY_AND] as ArrayList<Map<String, Map<String, String>>>
                filterMap.first()[SearchUsersByName.FIELD_NAME]!![Filters.KEY_AUTOCOMPLETE] == "searchString"
            }
        )
    }

    @Test
    fun `Given nonempty search result and online state Should save result list to DB`() = runBlockingTest {
        When calling chatClient.queryUsers(any()) doReturn TestCall(Result(listOf(randomUser(), randomUser())))
        When calling chatDomainImpl.isOnline() doReturn true

        sut(querySearch = "searchString", randomInt(), randomInt()).execute()

        verify(repositoryFacade).insertUsers(argThat { size == 2 })
    }

    @Test
    fun `Given empty search result and online state Should not save to DB`() = runBlockingTest {
        When calling chatClient.queryUsers(any()) doReturn TestCall(Result(emptyList()))
        When calling chatDomainImpl.isOnline() doReturn true

        sut(querySearch = "searchString", randomInt(), randomInt()).execute()

        verify(chatClient).queryUsers(any())
        verify(repositoryFacade, never()).insertUsers(any())
    }

    @Test
    fun `Given empty search result and offline state Should fetch all users from DB`() = runBlockingTest {
        When calling chatDomainImpl.isOnline() doReturn false
        val dbUsers = listOf(randomUser(), randomUser())
        When calling repositoryFacade.selectAllUsers(any(), any()) doReturn dbUsers

        val result = sut(querySearch = "", randomInt(), randomInt()).execute()

        verify(repositoryFacade).selectAllUsers(any(), any())
        result.data() `should be equal to` dbUsers
    }

    @Test
    fun `Given nonempty search result and offline state Should fetch all users from DB`() = runBlockingTest {
        When calling chatDomainImpl.isOnline() doReturn false
        val dbUsers = listOf(randomUser(), randomUser())
        When calling repositoryFacade.selectUsersLikeName(any(), any(), any()) doReturn dbUsers

        val result = sut(querySearch = "querySearch", randomInt(), randomInt()).execute()

        verify(repositoryFacade).selectUsersLikeName(eq("querySearch"), any(), any())
        result.data() `should be equal to` dbUsers
    }
}

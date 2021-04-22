package io.getstream.chat.android.offline.event

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.repository.RepositoryFacade
import io.getstream.chat.android.livedata.utils.TestDataHelper
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.test.InstantTaskExecutorExtension
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension

@ExperimentalCoroutinesApi
@ExtendWith(InstantTaskExecutorExtension::class)
internal class TotalUnreadCountTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private val data = TestDataHelper()

    @Test
    fun `when new message event is received, unread counts in the domain instance should be updated`() =
        testCoroutines.scope.runBlockingTest {
            val chatDomain: ChatDomainImpl = mock()
            val sut = Fixture(chatDomain, testCoroutines.scope, data.user1)
                .givenMockedRepositories()
                .get()

            val newMessageEventWithUnread = data.newMessageEvent.copy(
                totalUnreadCount = 5,
                unreadChannels = 2
            )
            sut.handleEvent(newMessageEventWithUnread)

            verify(chatDomain).setTotalUnreadCount(5)
            verify(chatDomain).setChannelUnreadCount(2)
        }

    @Test
    fun `when mark read event is received, unread counts in the domain instance should be updated`() =
        testCoroutines.scope.runBlockingTest {
            val chatDomain: ChatDomainImpl = mock()
            val sut = Fixture(chatDomain, testCoroutines.scope, data.user1)
                .givenMockedRepositories()
                .get()

            val markReadEventWithUnread = data.user1ReadNotification.copy(
                totalUnreadCount = 0,
                unreadChannels = 0
            )
            sut.handleEvent(markReadEventWithUnread)

            verify(chatDomain).setTotalUnreadCount(0)
            verify(chatDomain).setChannelUnreadCount(0)
        }

    @Test
    fun `when connected event is received, current user in the domain instance should be updated`() =
        testCoroutines.scope.runBlockingTest {
            val chatDomain: ChatDomainImpl = mock()
            val sut = Fixture(chatDomain, testCoroutines.scope, data.user1)
                .givenMockedRepositories()
                .get()

            val userWithUnread = data.user1.copy(totalUnreadCount = 5, unreadChannels = 2)
            val connectedEvent = data.connectedEvent.copy(me = userWithUnread)

            sut.handleEvent(connectedEvent)

            // unread count are updated internally when a user is updated
            verify(chatDomain).updateCurrentUser(userWithUnread)
        }

    private class Fixture(
        chatDomainImpl: ChatDomainImpl,
        scope: CoroutineScope,
        currentUser: User = mock(),
    ) {
        private val repos: RepositoryFacade = mock()
        private val eventHandlerImpl = EventHandlerImpl(chatDomainImpl)

        init {
            whenever(chatDomainImpl.currentUser) doReturn currentUser
            whenever(chatDomainImpl.job) doReturn Job()
            whenever(chatDomainImpl.scope) doReturn scope
            whenever(chatDomainImpl.repos) doReturn repos
        }

        fun givenMockedRepositories(): Fixture {
            runBlocking {
                whenever(repos.selectMessages(any())) doReturn emptyList()
                whenever(repos.selectChannels(any())) doReturn emptyList()
            }
            return this
        }

        fun get(): EventHandlerImpl = eventHandlerImpl
    }
}

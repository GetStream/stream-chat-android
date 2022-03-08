package io.getstream.chat.android.offline.event

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelCapabilities
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.experimental.global.GlobalMutableState
import io.getstream.chat.android.offline.repository.RepositoryFacade
import io.getstream.chat.android.offline.utils.TestDataHelper
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
internal class TotalUnreadCountTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private val data = TestDataHelper()
    private val globalMutableState = GlobalMutableState.create()

    @Test
    fun `When new message event is received for channel with read capability Should properly update total unread counts`() =
        testCoroutines.scope.runBlockingTest {
            val chatDomain: ChatDomainImpl = mock()
            val channelWithReadCapability = data.channel1.copy(ownCapabilities = setOf(ChannelCapabilities.READ_EVENTS))
            val sut = Fixture(chatDomain, testCoroutines.scope, data.user1, globalMutableState)
                .givenMockedRepositories()
                .givenChannel(channelWithReadCapability)
                .get()

            val newMessageEventWithUnread = data.newMessageEvent.copy(
                cid = channelWithReadCapability.cid,
                totalUnreadCount = 5,
                unreadChannels = 2
            )

            sut.handleEvent(newMessageEventWithUnread)

            globalMutableState._totalUnreadCount.value `should be equal to` 5
            globalMutableState._channelUnreadCount.value `should be equal to` 2
        }

    @Test
    fun `When mark read event is received for channel with read capability Should properly update total unread counts`() =
        testCoroutines.scope.runBlockingTest {
            val chatDomain: ChatDomainImpl = mock()
            val channelWithReadCapability = data.channel1.copy(ownCapabilities = setOf(ChannelCapabilities.READ_EVENTS))
            val sut = Fixture(chatDomain, testCoroutines.scope, data.user1, globalMutableState)
                .givenMockedRepositories()
                .givenChannel(channelWithReadCapability)
                .get()

            val markReadEventWithUnread = data.user1ReadNotification.copy(
                cid = channelWithReadCapability.cid,
                totalUnreadCount = 0,
                unreadChannels = 0
            )
            sut.handleEvent(markReadEventWithUnread)

            globalMutableState._totalUnreadCount.value `should be equal to` 0
            globalMutableState._channelUnreadCount.value `should be equal to` 0
        }

    @Test
    fun `when connected event is received, current user in the domain instance should be updated`() =
        testCoroutines.scope.runBlockingTest {
            globalMutableState._user.value = data.user1

            val chatDomain: ChatDomainImpl = mock()
            val sut = Fixture(chatDomain, testCoroutines.scope, data.user1, globalMutableState)
                .givenMockedRepositories()
                .get()

            val userWithUnread = data.user1.copy(totalUnreadCount = 5, unreadChannels = 2)
            val connectedEvent = data.connectedEvent.copy(me = userWithUnread)

            sut.handleEvent(connectedEvent)

            // unread count are updated internally when a user is updated

            globalMutableState._user.value `should be equal to` userWithUnread
        }

    private class Fixture(
        chatDomainImpl: ChatDomainImpl,
        scope: CoroutineScope,
        currentUser: User = mock(),
        globalMutableState: GlobalMutableState
    ) {
        private val repos: RepositoryFacade = mock()
        private val eventHandlerImpl =
            EventHandlerImpl(
                recoveryEnabled = true,
                client = mock(),
                state = mock(),
                logic = mock(),
                mutableGlobalState = globalMutableState,
                repos = repos,
                syncManager = mock(),
            )

        init {
            whenever(chatDomainImpl.user) doReturn MutableStateFlow(currentUser)
            whenever(chatDomainImpl.job) doReturn Job()
            whenever(chatDomainImpl.scope) doReturn scope
            whenever(chatDomainImpl.repos) doReturn repos
        }

        fun givenMockedRepositories(): Fixture {
            runBlocking {
                whenever(repos.selectMessages(any(), any())) doReturn emptyList()
                whenever(repos.selectChannels(any(), any<Boolean>())) doReturn emptyList()
            }
            return this
        }

        fun givenChannel(channel: Channel) = apply {
            runBlockingTest {
                whenever(repos.selectChannels(eq(listOf(channel.cid)), any<Boolean>())) doReturn listOf(channel)
            }
        }

        fun get(): EventHandlerImpl = eventHandlerImpl
    }
}

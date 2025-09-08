/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.state.event

import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.test.utils.TestDataHelper
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.event.handler.internal.EventHandler
import io.getstream.chat.android.state.event.handler.internal.EventHandlerSequential
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class TotalUnreadCountTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private lateinit var userStateFlow: StateFlow<User>

    private lateinit var data: TestDataHelper
    private lateinit var clientMutableState: ClientState

    @BeforeEach
    fun setUp() {
        data = TestDataHelper()
        userStateFlow = MutableStateFlow(data.user1)
        clientMutableState = mock {
            on(it.user) doReturn userStateFlow
        }
    }

    @Test
    fun `When new message event is received for channel with read capability Should properly update total unread counts`() = runTest {
        val channelWithReadCapability = data.channel1.copy(ownCapabilities = setOf(ChannelCapabilities.READ_EVENTS))
        val mutableGlobalState = MutableGlobalState(data.user1.id)
        val sut = Fixture(data.user1, clientMutableState, mutableGlobalState)
            .givenMockedRepositories()
            .givenChannel(channelWithReadCapability)
            .get()

        val newMessageEventWithUnread = data.newMessageEvent.copy(
            cid = channelWithReadCapability.cid,
            totalUnreadCount = 5,
            unreadChannels = 2,
        )

        sut.handleEvents(newMessageEventWithUnread)

        mutableGlobalState.totalUnreadCount.value `should be equal to` 5
        mutableGlobalState.channelUnreadCount.value `should be equal to` 2
    }

    @Test
    fun `When mark read event is received for channel with read capability Should properly update total unread counts`() = runTest {
        val channelWithReadCapability = data.channel1.copy(ownCapabilities = setOf(ChannelCapabilities.READ_EVENTS))
        val mutableGlobalState = MutableGlobalState(data.user1.id).apply {
            setTotalUnreadCount(5)
            setChannelUnreadCount(2)
        }
        val sut = Fixture(data.user1, clientMutableState, mutableGlobalState)
            .givenMockedRepositories()
            .givenChannel(channelWithReadCapability)
            .get()

        val markReadEventWithUnread = data.user1ReadNotification.copy(
            cid = channelWithReadCapability.cid,
            totalUnreadCount = 0,
            unreadChannels = 0,
        )
        sut.handleEvents(markReadEventWithUnread)

        mutableGlobalState.totalUnreadCount.value `should be equal to` 0
        mutableGlobalState.channelUnreadCount.value `should be equal to` 0
    }

    // @Test
    fun `when connected event is received, current user should be updated`() = runTest {
        val mutableGlobalState = MutableGlobalState(data.user1.id)
        val sut = Fixture(data.user1, clientMutableState, mutableGlobalState)
            .givenMockedRepositories()
            .get()

        val userWithUnread = data.user1.copy(totalUnreadCount = 5, unreadChannels = 2)
        val connectedEvent = data.connectedEvent.copy(me = userWithUnread)

        sut.handleEvents(connectedEvent)

        // unread count are updated internally when a user is updated
        userStateFlow.value `should be equal to` userWithUnread
    }

    private class Fixture(
        currentUser: User,
        clientState: ClientState,
        mutableGlobalState: MutableGlobalState,
        sideEffect: suspend () -> Unit = {},
        syncedEvents: Flow<List<ChatEvent>> = MutableStateFlow(emptyList()),
    ) {
        private val repos: RepositoryFacade = mock()
        private val eventHandler: EventHandler = EventHandlerSequential(
            currentUserId = currentUser.id,
            scope = testCoroutines.scope,
            subscribeForEvents = { mock() },
            logicRegistry = mock(),
            stateRegistry = mock(),
            clientState = clientState,
            mutableGlobalState = mutableGlobalState,
            repos = repos,
            sideEffect = sideEffect,
            syncedEvents = syncedEvents,
        )

        fun givenMockedRepositories(): Fixture {
            runTest {
                whenever(repos.selectMessages(any())) doReturn emptyList()
                whenever(repos.selectChannels(any())) doReturn emptyList()
            }
            return this
        }

        fun givenChannel(channel: Channel) = apply {
            runTest {
                whenever(repos.selectChannel(eq(channel.cid))) doReturn channel
            }
        }

        fun get(): EventHandler = eventHandler
    }
}

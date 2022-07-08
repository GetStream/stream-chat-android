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

package io.getstream.chat.android.offline.event

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelCapabilities
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.test.utils.TestDataHelper
import io.getstream.chat.android.offline.event.handler.internal.EventHandler
import io.getstream.chat.android.offline.event.handler.internal.EventHandlerImpl
import io.getstream.chat.android.offline.event.handler.internal.EventHandlerSequential
import io.getstream.chat.android.offline.event.model.EventHandlerType
import io.getstream.chat.android.offline.plugin.state.global.internal.GlobalMutableState
import io.getstream.chat.android.offline.repository.builder.internal.RepositoryFacade
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.logging.StreamLog
import io.getstream.logging.kotlin.KotlinStreamLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
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

    private lateinit var data: TestDataHelper
    private lateinit var globalMutableState: GlobalMutableState
    private lateinit var clientMutableState: ClientState

    @BeforeAll
    fun beforeAll() {
        StreamLog.setValidator { _, _ -> true }
        StreamLog.setLogger(KotlinStreamLogger())
    }

    private lateinit var userStateFlow: StateFlow<User>

    @BeforeEach
    fun setUp() {
        data = TestDataHelper()
        userStateFlow = MutableStateFlow(data.user1)
        clientMutableState = mock {
            on(it.user) doReturn userStateFlow
        }
        globalMutableState = GlobalMutableState.getOrCreate()
    }

    @ParameterizedTest
    @EnumSource(EventHandlerType::class)
    fun `When new message event is received for channel with read capability Should properly update total unread counts`(
        eventHandlerType: EventHandlerType,
    ) = runTest {
        val channelWithReadCapability = data.channel1.copy(ownCapabilities = setOf(ChannelCapabilities.READ_EVENTS))
        val sut = Fixture(globalMutableState, clientMutableState, eventHandlerType, data.user1)
            .givenMockedRepositories()
            .givenChannel(channelWithReadCapability)
            .get()

        val newMessageEventWithUnread = data.newMessageEvent.copy(
            cid = channelWithReadCapability.cid,
            totalUnreadCount = 5,
            unreadChannels = 2
        )

        sut.handleEvents(newMessageEventWithUnread)

        globalMutableState.totalUnreadCount.value `should be equal to` 5
        globalMutableState.channelUnreadCount.value `should be equal to` 2
    }

    @ParameterizedTest
    @EnumSource(EventHandlerType::class)
    fun `When mark read event is received for channel with read capability Should properly update total unread counts`(
        eventHandlerType: EventHandlerType,
    ) = runTest {
        val channelWithReadCapability = data.channel1.copy(ownCapabilities = setOf(ChannelCapabilities.READ_EVENTS))
        val sut = Fixture(globalMutableState, clientMutableState, eventHandlerType, data.user1)
            .givenMockedRepositories()
            .givenChannel(channelWithReadCapability)
            .get()

        val markReadEventWithUnread = data.user1ReadNotification.copy(
            cid = channelWithReadCapability.cid,
            totalUnreadCount = 0,
            unreadChannels = 0
        )
        sut.handleEvents(markReadEventWithUnread)

        globalMutableState.totalUnreadCount.value `should be equal to` 0
        globalMutableState.channelUnreadCount.value `should be equal to` 0
    }

    // @ParameterizedTest
    // @EnumSource(EventHandlerType::class)
    fun `when connected event is received, current user should be updated`(
        eventHandlerType: EventHandlerType,
    ) = runTest {
        val sut = Fixture(globalMutableState, clientMutableState, eventHandlerType, data.user1)
            .givenMockedRepositories()
            .get()

        val userWithUnread = data.user1.copy(totalUnreadCount = 5, unreadChannels = 2)
        val connectedEvent = data.connectedEvent.copy(me = userWithUnread)

        sut.handleEvents(connectedEvent)

        // unread count are updated internally when a user is updated
        userStateFlow.value `should be equal to` userWithUnread
    }

    private class Fixture(
        globalMutableState: GlobalMutableState,
        clientMutableState: ClientState,
        eventHandlerType: EventHandlerType,
        currentUser: User,
    ) {
        private val repos: RepositoryFacade = mock()
        private val eventHandler = when (eventHandlerType) {
            EventHandlerType.SEQUENTIAL -> EventHandlerSequential(
                scope = testCoroutines.scope,
                recoveryEnabled = true,
                subscribeForEvents = { _ -> mock() },
                logicRegistry = mock(),
                stateRegistry = mock(),
                mutableGlobalState = globalMutableState,
                repos = repos,
                syncManager = mock(),
                currentUserId = currentUser.id
            )
            EventHandlerType.DEFAULT -> EventHandlerImpl(
                scope = testCoroutines.scope,
                recoveryEnabled = true,
                client = mock(),
                logic = mock(),
                state = mock(),
                mutableGlobalState = globalMutableState,
                clientMutableState = clientMutableState,
                repos = repos,
                syncManager = mock(),
            )
        }

        fun givenMockedRepositories(): Fixture {
            runTest {
                whenever(repos.selectMessages(any(), any())) doReturn emptyList()
                whenever(repos.selectChannels(any(), any<Boolean>())) doReturn emptyList()
            }
            return this
        }

        fun givenChannel(channel: Channel) = apply {
            runTest {
                whenever(repos.selectChannels(eq(listOf(channel.cid)), any<Boolean>())) doReturn listOf(channel)
            }
        }

        fun get(): EventHandler = eventHandler
    }
}

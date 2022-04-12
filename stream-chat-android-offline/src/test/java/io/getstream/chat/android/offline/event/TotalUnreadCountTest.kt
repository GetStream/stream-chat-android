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
import io.getstream.chat.android.offline.event.handler.internal.EventHandlerImpl
import io.getstream.chat.android.offline.plugin.state.global.internal.GlobalMutableState
import io.getstream.chat.android.offline.repository.builder.internal.RepositoryFacade
import io.getstream.chat.android.offline.utils.TestDataHelper
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.TestCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.Rule
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
internal class TotalUnreadCountTest {
    @get:Rule
    val testCoroutines: TestCoroutineRule = TestCoroutineRule()

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private val data = TestDataHelper()
    private val globalMutableState = GlobalMutableState.create().apply {
        _user.value = data.user1
    }

    @Test
    fun `When new message event is received for channel with read capability Should properly update total unread counts`() =
        runTest {
            val channelWithReadCapability = data.channel1.copy(ownCapabilities = setOf(ChannelCapabilities.READ_EVENTS))
            val sut = Fixture(globalMutableState)
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
        runTest {
            val channelWithReadCapability = data.channel1.copy(ownCapabilities = setOf(ChannelCapabilities.READ_EVENTS))
            val sut = Fixture(globalMutableState)
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
    fun `when connected event is received, current user should be updated`() = runTest {
        val sut = Fixture(globalMutableState)
            .givenMockedRepositories()
            .get()

        val userWithUnread = data.user1.copy(totalUnreadCount = 5, unreadChannels = 2)
        val connectedEvent = data.connectedEvent.copy(me = userWithUnread)

        sut.handleEvent(connectedEvent)

        // unread count are updated internally when a user is updated
        globalMutableState._user.value `should be equal to` userWithUnread
    }

    private class Fixture(globalMutableState: GlobalMutableState) {
        private val repos: RepositoryFacade = mock()
        private val eventHandlerImpl =
            EventHandlerImpl(
                recoveryEnabled = true,
                client = mock(),
                logic = mock(),
                state = mock(),
                mutableGlobalState = globalMutableState,
                repos = repos,
                syncManager = mock()
            )

        fun givenMockedRepositories(): Fixture {
            runBlocking {
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

        fun get(): EventHandlerImpl = eventHandlerImpl
    }
}

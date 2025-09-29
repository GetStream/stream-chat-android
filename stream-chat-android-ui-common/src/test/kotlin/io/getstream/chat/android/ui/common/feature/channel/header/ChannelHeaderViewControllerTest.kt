/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.common.feature.channel.header

import app.cash.turbine.test
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomConnectionState
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.ui.common.state.messages.list.ChannelHeaderViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class ChannelHeaderViewControllerTest {

    @Test
    fun `initial state is correct`() = runTest {
        val sut = Fixture().get(backgroundScope)

        sut.state.value.let { state ->
            assertInstanceOf<ChannelHeaderViewState.Loading>(state)
        }
    }

    @Test
    fun `initial loaded state is correct`() = runTest {
        val sut = Fixture().get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state
            val state = awaitItem()

            assertInstanceOf<ChannelHeaderViewState.Content>(state)
            assertEquals(Fixture.User, state.currentUser)
            assertEquals(Fixture.ConnectionState, state.connectionState)
            assertEquals(Fixture.Channel, state.channel)
        }
    }

    private class Fixture {

        companion object {
            val User = randomUser()
            val ConnectionState = randomConnectionState()
            val Channel = randomChannel()
        }

        private val cid = randomCID()

        private val mockClientState: ClientState = mock {
            on { connectionState } doReturn MutableStateFlow(ConnectionState)
        }

        private val mockChatClient: ChatClient = mock {
            on { clientState } doReturn mockClientState
            on { getCurrentUser() } doReturn User
        }
        private val mockChannelData = MutableStateFlow(ChannelData(id = randomString(), type = randomString()))
        private val mockMembersCount = MutableStateFlow(0)
        private val mockWatcherCount = MutableStateFlow(0)

        private val mockChannelState: ChannelState = mock {
            on { channelData } doReturn mockChannelData
            on { membersCount } doReturn mockMembersCount
            on { watcherCount } doReturn mockWatcherCount
            on { toChannel() } doReturn Channel
        }

        fun get(scope: CoroutineScope) = ChannelHeaderViewController(
            cid = cid,
            scope = scope,
            chatClient = mockChatClient,
            channelState = MutableStateFlow(mockChannelState),
        )
    }
}

/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.internal.state.plugin.state.channel.internal

import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomChannelUserRead
import io.getstream.chat.android.randomConfig
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

/**
 * Tests for on-device unread tracking on the legacy channel state implementation.
 */
internal class ChannelStateLegacyImplLocalUnreadCountTest {

    private val userFlow = MutableStateFlow(currentUser)

    @Test
    fun `markChannelAsRead resets the count locally when local tracking is enabled and read events are disabled`() =
        runTest {
            val state = localTrackingState()
            state.setMessages(listOf(randomMessage(parentId = null, shadowed = false)))
            state.upsertReads(listOf(randomChannelUserRead(user = currentUser, unreadMessages = 3)))
            // when
            val result = state.markChannelAsRead()
            // then
            assertEquals(MarkReadResult.HandledLocally, result)
            assertEquals(0, state.read.value?.unreadMessages)
        }

    @Test
    fun `markChannelAsRead is ignored when local tracking is disabled and read events are disabled`() = runTest {
        val state = localTrackingState(isLocalUnreadCountEnabled = false)
        state.setMessages(listOf(randomMessage(parentId = null, shadowed = false)))
        state.upsertReads(listOf(randomChannelUserRead(user = currentUser, unreadMessages = 3)))
        // when
        val result = state.markChannelAsRead()
        // then: not handled and the count is left untouched
        assertEquals(MarkReadResult.NotNeeded, result)
        assertEquals(3, state.read.value?.unreadMessages)
    }

    private fun localTrackingState(
        isLocalUnreadCountEnabled: Boolean = true,
        readEventsEnabled: Boolean = false,
    ) = ChannelStateLegacyImpl(
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        userFlow = userFlow,
        latestUsers = MutableStateFlow(mapOf(currentUser.id to currentUser)),
        activeLiveLocations = MutableStateFlow(emptyList()),
        baseMessageLimit = null,
        isLocalUnreadCountEnabled = isLocalUnreadCountEnabled,
        now = ::currentTime,
    ).apply {
        setChannelConfig(randomConfig(readEventsEnabled = readEventsEnabled))
    }

    private companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()

        const val CHANNEL_TYPE = "messaging"
        const val CHANNEL_ID = "123"

        val currentUser = User(id = "tom", name = "Tom")

        @OptIn(ExperimentalCoroutinesApi::class)
        fun currentTime() = testCoroutines.dispatcher.scheduler.currentTime
    }
}

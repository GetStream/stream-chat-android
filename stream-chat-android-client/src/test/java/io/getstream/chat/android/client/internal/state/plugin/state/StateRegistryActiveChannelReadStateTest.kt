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

package io.getstream.chat.android.client.internal.state.plugin.state

import io.getstream.chat.android.client.api.MessageLimitConfig
import io.getstream.chat.android.client.api.state.StateRegistry
import io.getstream.chat.android.client.utils.internal.ChannelId
import io.getstream.chat.android.randomChannelUserRead
import io.getstream.chat.android.randomConfig
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class StateRegistryActiveChannelReadStateTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private val channelId = ChannelId.fromTypeAndId("messaging", "123")!!

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `returns the config and reads of the channel's state`(useLegacyChannelState: Boolean) {
        val registry = stateRegistry(useLegacyChannelState)
        val config = randomConfig()
        val reads = listOf(randomChannelUserRead())
        if (useLegacyChannelState) {
            registry.legacyChannelState(channelId).apply {
                setChannelConfig(config)
                upsertReads(reads)
            }
        } else {
            registry.channelState(channelId).apply {
                setChannelConfig(config)
                updateReads(reads)
            }
        }

        val channel = registry.activeChannelReadState(channelId.cid)!!

        assertEquals(channelId.cid, channel.cid)
        assertEquals(config, channel.config)
        assertEquals(reads, channel.read)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `returns null without creating the channel's state`(useLegacyChannelState: Boolean) {
        val registry = stateRegistry(useLegacyChannelState)

        assertNull(registry.activeChannelReadState(channelId.cid))
        assertFalse(registry.isActiveChannel(channelId))
    }

    private fun stateRegistry(useLegacyChannelState: Boolean) = StateRegistry(
        userStateFlow = MutableStateFlow(null),
        latestUsers = MutableStateFlow(emptyMap()),
        activeLiveLocations = MutableStateFlow(emptyList()),
        job = Job(),
        now = { System.currentTimeMillis() },
        scope = testCoroutines.scope,
        messageLimitConfig = MessageLimitConfig(),
        mutedUsers = MutableStateFlow(emptyList()),
        useLegacyChannelState = useLegacyChannelState,
    )
}

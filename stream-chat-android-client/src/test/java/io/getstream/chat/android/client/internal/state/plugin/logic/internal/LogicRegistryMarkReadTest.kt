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

package io.getstream.chat.android.client.internal.state.plugin.logic.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.MessageLimitConfig
import io.getstream.chat.android.client.api.state.StateRegistry
import io.getstream.chat.android.client.internal.state.plugin.state.channel.internal.MarkReadResult
import io.getstream.chat.android.client.internal.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.utils.internal.ChannelId
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

/**
 * Marking a channel as read must not create a state for a channel that has none, since channel lists would copy it as a
 * channel with no members. Each case runs against both channel state implementations.
 */
internal class LogicRegistryMarkReadTest {

    @ParameterizedTest(name = "legacy channel state: {0}")
    @ValueSource(booleans = [false, true])
    fun `a channel with no state gets the answer of an empty state without creating one`(legacy: Boolean) {
        val fixture = Fixture(legacy)
        // An empty state's answer, from another registry that creates one.
        val emptyStateAnswer = Fixture(legacy).logicRegistry.channel(CHANNEL_TYPE, CHANNEL_ID).markRead()

        val result = fixture.logicRegistry.markRead(CHANNEL_TYPE, CHANNEL_ID)

        assertEquals(emptyStateAnswer, result)
        assertFalse(fixture.stateRegistry.isActiveChannel(CHANNEL))
        assertFalse(fixture.logicRegistry.isActiveChannel(CHANNEL_TYPE, CHANNEL_ID))
    }

    @Test
    fun `a channel with a legacy state is marked through it`() {
        val fixture = Fixture(legacy = true)
        // Unlike an empty legacy state, one with a message and no read for the current user asks the server.
        fixture.stateRegistry.legacyChannelState(CHANNEL).upsertMessages(listOf(message()))

        assertEquals(MarkReadResult.RemoteRequired, fixture.logicRegistry.markRead(CHANNEL_TYPE, CHANNEL_ID))
    }

    @Test
    fun `a channel with a state is marked through it`() {
        val fixture = Fixture(legacy = false)
        // Unlike an empty state, one with read events disabled has nothing to send.
        fixture.stateRegistry.channelState(CHANNEL).setChannelConfig(Config(readEventsEnabled = false))

        assertEquals(MarkReadResult.NotNeeded, fixture.logicRegistry.markRead(CHANNEL_TYPE, CHANNEL_ID))
    }

    private fun message() = randomMessage(
        cid = CHANNEL.cid,
        type = "regular",
        parentId = null,
        deletedAt = null,
        shadowed = false,
    )

    private class Fixture(legacy: Boolean) {
        private val currentUser = randomUser()
        private val userFlow = MutableStateFlow<User?>(currentUser)
        val stateRegistry = StateRegistry(
            userStateFlow = userFlow,
            latestUsers = MutableStateFlow(mapOf(currentUser.id to currentUser)),
            activeLiveLocations = MutableStateFlow(emptyList<Location>()),
            job = Job(),
            now = { 0L },
            scope = testCoroutines.scope,
            messageLimitConfig = MessageLimitConfig(),
            useLegacyChannelState = legacy,
        )
        val logicRegistry = LogicRegistry(
            stateRegistry = stateRegistry,
            clientState = mock<ClientState> { on { user } doReturn userFlow },
            mutableGlobalState = MutableGlobalState(currentUser.id),
            userPresence = false,
            repos = mock(),
            client = mock<ChatClient>(),
            coroutineScope = testCoroutines.scope,
            now = { 0L },
            useLegacyChannelLogic = legacy,
            isLocalUnreadCountEnabled = false,
        )
    }

    companion object {

        private const val CHANNEL_TYPE = "messaging"
        private const val CHANNEL_ID = "123"
        private val CHANNEL = ChannelId.fromTypeAndId(CHANNEL_TYPE, CHANNEL_ID)!!

        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }
}

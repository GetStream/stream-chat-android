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

package io.getstream.chat.android.client.internal.state.plugin.logic.channel.internal.legacy

import io.getstream.chat.android.client.internal.state.plugin.logic.channel.internal.SearchLogic
import io.getstream.chat.android.client.internal.state.plugin.state.channel.internal.ChannelStateLegacyImpl
import io.getstream.chat.android.client.internal.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.test.randomChannelUpdatedEvent
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomString
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.result.Error
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

/**
 * The channel state an app can rely on to tell whether a channel was loaded: `loading` also ends when the first load
 * fails, so only a non-null `createdAt` together with the members means the channel data came from the server.
 */
internal class ChannelLoadedStateLegacyTest {

    private lateinit var state: ChannelStateLegacyImpl
    private lateinit var sut: ChannelStateLogic

    @BeforeEach
    fun setUp() {
        val userFlow = MutableStateFlow<User?>(User(id = CURRENT_USER_ID))
        state = ChannelStateLegacyImpl(
            channelType = CHANNEL_TYPE,
            channelId = CHANNEL_ID,
            userFlow = userFlow,
            latestUsers = MutableStateFlow(emptyMap()),
            activeLiveLocations = MutableStateFlow(emptyList()),
            baseMessageLimit = null,
            now = { System.currentTimeMillis() },
        )
        val clientState = mock<ClientState> {
            on(it.user) doReturn userFlow
        }
        sut = ChannelStateLogic(
            clientState = clientState,
            mutableState = state,
            globalMutableState = MutableGlobalState(CURRENT_USER_ID),
            searchLogic = SearchLogic(state),
            coroutineScope = testCoroutines.scope,
        )
    }

    @Test
    fun `given the channel is not loaded, the channel data should be a placeholder without createdAt`() {
        val data = state.channelData.value

        assertEquals(CHANNEL_TYPE, data.type)
        assertEquals(CHANNEL_ID, data.id)
        assertNull(data.createdAt)
        assertTrue(state.members.value.isEmpty())
    }

    @Test
    fun `given updateDataForChannel runs, the channel data should have createdAt and the members should be set`() {
        val channel = randomChannel(
            type = CHANNEL_TYPE,
            id = CHANNEL_ID,
            createdAt = randomDate(),
            members = listOf(randomMember(), randomMember()),
        )

        sut.updateDataForChannel(channel, messageLimit = 0)

        assertLoaded(channel)
    }

    @Test
    fun `given the first load fails, the channel data should stay a placeholder without createdAt`() {
        state.setLoadingIfEmpty()

        sut.propagateQueryError(Error.GenericError(randomString()))

        assertFalse(state.loading.value)
        assertNull(state.channelData.value.createdAt)
        assertTrue(state.members.value.isEmpty())
    }

    @Test
    fun `given a channel event arrives before the load, the channel data should have createdAt without members`() {
        val event = randomChannelUpdatedEvent(
            channel = randomChannel(type = CHANNEL_TYPE, id = CHANNEL_ID, createdAt = randomDate()),
        )

        sut.updateChannelData(event)

        assertNotNull(state.channelData.value.createdAt)
        assertTrue(state.members.value.isEmpty())
    }

    private fun assertLoaded(channel: Channel) {
        assertNotNull(state.channelData.value.createdAt)
        assertEquals(channel.createdAt, state.channelData.value.createdAt)
        assertEquals(channel.members.map { it.getUserId() }.toSet(), state.members.value.map { it.getUserId() }.toSet())
    }

    companion object {

        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()

        private const val CHANNEL_TYPE = "messaging"
        private const val CHANNEL_ID = "channelId"
        private const val CURRENT_USER_ID = "currentUser"
    }
}

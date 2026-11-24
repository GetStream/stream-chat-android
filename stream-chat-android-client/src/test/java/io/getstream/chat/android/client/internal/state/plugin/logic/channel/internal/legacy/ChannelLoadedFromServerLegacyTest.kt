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

import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.internal.state.plugin.logic.channel.internal.SearchLogic
import io.getstream.chat.android.client.internal.state.plugin.state.channel.internal.ChannelStateLegacyImpl
import io.getstream.chat.android.client.internal.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomString
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.result.Error
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.util.Date

internal class ChannelLoadedFromServerLegacyTest {

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
    fun `given the channel is not loaded, it should not be loaded from the server`() {
        assertFalse(state.loadedFromServer.value)
    }

    @Test
    fun `given the database fills the state, it should not be loaded from the server`() {
        sut.updateDataForChannel(channelWithUnreadMessages(0), messageLimit = 30)

        assertFalse(state.loadedFromServer.value)
    }

    @Test
    fun `given the query fails, it should not be loaded from the server`() {
        sut.propagateQueryError(Error.GenericError(randomString()))

        assertFalse(state.loadedFromServer.value)
    }

    @Test
    fun `given the query succeeds, it should be loaded once the read state of the response is applied`() = runTest {
        var unreadWhenLoaded: Int? = null
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            state.loadedFromServer.collect { loaded ->
                if (loaded) unreadWhenLoaded = state.read.value?.unreadMessages
            }
        }

        sut.propagateChannelQuery(channelWithUnreadMessages(2), QueryChannelRequest().withMessages(30))

        assertTrue(state.loadedFromServer.value)
        assertEquals(2, unreadWhenLoaded)
    }

    private fun channelWithUnreadMessages(unreadMessages: Int) = randomChannel(
        type = CHANNEL_TYPE,
        id = CHANNEL_ID,
        messages = emptyList(),
        read = listOf(
            ChannelUserRead(
                user = User(id = CURRENT_USER_ID),
                lastReceivedEventDate = Date(2_000),
                unreadMessages = unreadMessages,
                lastRead = Date(1_000),
                lastReadMessageId = "last_read_message_id",
            ),
        ),
    )

    companion object {

        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()

        private const val CHANNEL_TYPE = "messaging"
        private const val CHANNEL_ID = "channelId"
        private const val CURRENT_USER_ID = "currentUser"
    }
}

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

package io.getstream.chat.android.client.internal.state.plugin.logic.channel.internal

import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.internal.state.plugin.state.channel.internal.ChannelStateImpl
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomString
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.util.Date

internal class ChannelLoadedFromServerTest {

    private lateinit var state: ChannelStateImpl
    private lateinit var sut: ChannelLogicImpl

    @BeforeEach
    fun setUp() {
        val channelLogic = ChannelLogicImplWithRealState(
            CHANNEL_TYPE,
            CHANNEL_ID,
            CURRENT_USER_ID,
            testCoroutines.scope,
        )
        state = channelLogic.state
        sut = channelLogic.logic
    }

    @Test
    fun `given the channel is not loaded, it should not be loaded from the server`() {
        assertFalse(state.loadedFromServer.value)
    }

    @Test
    fun `given the database fills the state, it should not be loaded from the server`() = runTest {
        sut.updateDataForChannel(channel = channelWithUnreadMessages(0), messageLimit = 30)

        assertFalse(state.loadedFromServer.value)
    }

    @Test
    fun `given the query fails, it should not be loaded from the server`() {
        sut.onQueryChannelResult(
            QueryChannelRequest().withMessages(30),
            Result.Failure(Error.GenericError(randomString())),
        )

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

        sut.onQueryChannelResult(QueryChannelRequest().withMessages(30), Result.Success(channelWithUnreadMessages(2)))

        assertTrue(state.loadedFromServer.value)
        assertEquals(2, unreadWhenLoaded)
    }

    @Test
    fun `given a server refresh fills the state, it should be loaded once its read state is applied`() = runTest {
        var unreadWhenLoaded: Int? = null
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            state.loadedFromServer.collect { loaded ->
                if (loaded) unreadWhenLoaded = state.read.value?.unreadMessages
            }
        }

        sut.updateDataForChannel(channel = channelWithUnreadMessages(2), messageLimit = 30, fromServer = true)

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

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
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomLocation
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.util.Date

internal class ChannelLogicImplActiveLiveLocationsTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()

        private const val CURRENT_USER_ID = "currentUser"
        private const val OTHER_USER_ID = "otherUser"
        private const val CID = "messaging:123"
    }

    private lateinit var channelState: ChannelStateImpl
    private lateinit var sut: ChannelLogicImpl

    private val otherMemberLocation = randomLocation(
        cid = CID,
        userId = OTHER_USER_ID,
        endAt = Date(System.currentTimeMillis() + 60_000),
    )
    private val channel = randomChannel(
        id = "123",
        type = "messaging",
        messages = emptyList(),
        activeLiveLocations = listOf(otherMemberLocation),
    )

    @BeforeEach
    fun setUp() {
        val channelLogic = ChannelLogicImplWithRealState("messaging", "123", CURRENT_USER_ID, testCoroutines.scope)
        channelState = channelLogic.state
        sut = channelLogic.logic
    }

    @Test
    fun `query channel result exposes another member's active live location`() {
        sut.onQueryChannelResult(QueryChannelRequest().withMessages(30), Result.Success(channel))

        assertEquals(listOf(otherMemberLocation), channelState.activeLiveLocations.value)
    }

    @Test
    fun `channel list update exposes another member's active live location`() = runTest {
        sut.updateDataForChannel(channel = channel, messageLimit = 30)

        assertEquals(listOf(otherMemberLocation), channelState.activeLiveLocations.value)
    }
}

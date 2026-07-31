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

package io.getstream.chat.android.client.internal.state.plugin.listener.internal

import io.getstream.chat.android.client.internal.state.plugin.logic.channel.internal.ChannelLogic
import io.getstream.chat.android.client.internal.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.client.internal.state.plugin.logic.querychannels.internal.QueryChannelsLogic
import io.getstream.chat.android.client.internal.state.plugin.state.channel.internal.MarkReadResult
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

internal class ChannelMarkReadListenerStateTest {

    private val channelType = "messaging"
    private val channelId = "123"

    @ParameterizedTest(name = "{0}")
    @MethodSource("markReadResults")
    fun `onChannelMarkReadPrecondition maps the mark-read result to a precondition and a channel-list refresh`(
        testName: String,
        markReadResult: MarkReadResult,
        succeeds: Boolean,
        refreshes: Boolean,
    ) = runTest {
        // A remote mark-read succeeds (the network request follows) and relies on the server read
        // event for the refresh; a local one fails (no request) and must refresh the channel-list
        // queries itself; a not-needed one fails and refreshes nothing.
        val queryChannelsLogic: QueryChannelsLogic = mock()
        val listener = listenerWith(markReadResult, listOf(queryChannelsLogic))
        // when
        val result = listener.onChannelMarkReadPrecondition(channelType, channelId)
        // then
        assertInstanceOf(
            if (succeeds) Result.Success::class.java else Result.Failure::class.java,
            result,
        )
        verify(queryChannelsLogic, if (refreshes) times(1) else never())
            .refreshChannelState("$channelType:$channelId")
    }

    private fun listenerWith(
        markReadResult: MarkReadResult,
        activeQueryChannelsLogic: List<QueryChannelsLogic> = emptyList(),
    ): ChannelMarkReadListenerState {
        val channelLogic: ChannelLogic = mock {
            on { it.markRead() } doReturn markReadResult
        }
        val logic: LogicRegistry = mock {
            on { it.channel(channelType, channelId) } doReturn channelLogic
            on { it.getActiveQueryChannelsLogic() } doReturn activeQueryChannelsLogic
        }
        return ChannelMarkReadListenerState(logic)
    }

    companion object {

        @JvmStatic
        fun markReadResults() = listOf(
            // (test name, markReadResult, succeeds, refreshes)
            Arguments.of("remote required", MarkReadResult.RemoteRequired, true, false),
            Arguments.of("handled locally", MarkReadResult.HandledLocally, false, true),
            Arguments.of("not needed", MarkReadResult.NotNeeded, false, false),
        )
    }
}

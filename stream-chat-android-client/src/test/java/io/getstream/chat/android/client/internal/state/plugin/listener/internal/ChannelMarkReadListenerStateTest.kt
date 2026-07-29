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
import io.getstream.chat.android.client.internal.state.plugin.state.channel.internal.MarkReadResult
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class ChannelMarkReadListenerStateTest {

    private val channelType = "messaging"
    private val channelId = "123"

    @Test
    fun `Remote result makes the precondition succeed so the network request proceeds`() = runTest {
        val listener = listenerWith(markReadResult = MarkReadResult.RemoteRequired)
        // when
        val result = listener.onChannelMarkReadPrecondition(channelType, channelId)
        // then
        assertInstanceOf(Result.Success::class.java, result)
    }

    @Test
    fun `Local result makes the precondition fail so no network request follows`() = runTest {
        val listener = listenerWith(markReadResult = MarkReadResult.HandledLocally)
        // when
        val result = listener.onChannelMarkReadPrecondition(channelType, channelId)
        // then
        assertInstanceOf(Result.Failure::class.java, result)
    }

    @Test
    fun `None result makes the precondition fail`() = runTest {
        val listener = listenerWith(markReadResult = MarkReadResult.NotNeeded)
        // when
        val result = listener.onChannelMarkReadPrecondition(channelType, channelId)
        // then
        assertInstanceOf(Result.Failure::class.java, result)
    }

    private fun listenerWith(markReadResult: MarkReadResult): ChannelMarkReadListenerState {
        val channelLogic: ChannelLogic = mock {
            on { it.markRead() } doReturn markReadResult
        }
        val logic: LogicRegistry = mock {
            on { it.channel(channelType, channelId) } doReturn channelLogic
        }
        return ChannelMarkReadListenerState(logic)
    }
}

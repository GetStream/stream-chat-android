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
import io.getstream.chat.android.client.internal.state.plugin.logic.channel.internal.ChannelStateLogic
import io.getstream.chat.android.client.internal.state.plugin.logic.channel.thread.internal.ThreadLogic
import io.getstream.chat.android.client.internal.state.plugin.logic.channel.thread.internal.ThreadStateLogic
import io.getstream.chat.android.client.internal.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.client.internal.state.plugin.logic.querythreads.internal.QueryThreadsLogic
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomMessage
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class SendGiphyListenerStateTest {

    private val logicRegistry: LogicRegistry = mock()
    private val sendGiphyListenerState = SendGiphyListenerState(logicRegistry)

    @Test
    fun `when sending giphy and request succeeds, message should be upserted to channel, threads, and thread`() =
        runTest {
            val channelStateLogic: ChannelStateLogic = mock()
            val channelLogic: ChannelLogic = mock {
                on(it.stateLogic) doReturn channelStateLogic
            }

            val threadsLogic: QueryThreadsLogic = mock()
            val activeThreadsLogic = listOf(threadsLogic)

            val threadStateLogic: ThreadStateLogic = mock()
            val threadLogic: ThreadLogic = mock {
                on(it.stateLogic()) doReturn threadStateLogic
            }

            whenever(logicRegistry.channelFromMessage(any())) doReturn channelLogic
            whenever(logicRegistry.getActiveQueryThreadsLogic()) doReturn activeThreadsLogic
            whenever(logicRegistry.threadFromMessage(any())) doReturn threadLogic

            val testMessage = randomMessage()

            sendGiphyListenerState.onGiphySendResult(randomCID(), Result.Success(testMessage))

            verify(channelStateLogic).upsertMessage(testMessage)
            verify(threadsLogic).upsertMessage(testMessage)
            verify(threadStateLogic).upsertMessage(testMessage)
        }

    @Test
    fun `when sending giphy and request fails, nothing should be upserted`() = runTest {
        val channelStateLogic: ChannelStateLogic = mock()
        val channelLogic: ChannelLogic = mock {
            on(it.stateLogic) doReturn channelStateLogic
        }

        val threadsLogic: QueryThreadsLogic = mock()
        val activeThreadsLogic = listOf(threadsLogic)

        val threadStateLogic: ThreadStateLogic = mock()
        val threadLogic: ThreadLogic = mock {
            on(it.stateLogic()) doReturn threadStateLogic
        }

        whenever(logicRegistry.channelFromMessage(any())) doReturn channelLogic
        whenever(logicRegistry.getActiveQueryThreadsLogic()) doReturn activeThreadsLogic
        whenever(logicRegistry.threadFromMessage(any())) doReturn threadLogic

        sendGiphyListenerState.onGiphySendResult(randomCID(), Result.Failure(Error.GenericError("")))

        verify(channelStateLogic, never()).upsertMessage(any())
        verify(threadsLogic, never()).upsertMessage(any())
        verify(threadStateLogic, never()).upsertMessage(any())
    }
}

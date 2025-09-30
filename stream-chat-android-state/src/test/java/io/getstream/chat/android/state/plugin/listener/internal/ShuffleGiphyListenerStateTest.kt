/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.state.plugin.listener.internal

import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.state.plugin.logic.channel.internal.ChannelLogic
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.state.plugin.logic.querythreads.internal.QueryThreadsLogic
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

internal class ShuffleGiphyListenerStateTest {

    private val channelLogic: ChannelLogic = mock()
    private val threadsLogic: QueryThreadsLogic = mock()
    private val activeThreadsLogic = listOf(threadsLogic)
    private val logic: LogicRegistry = mock {
        on(it.channelFromMessage(any())) doReturn channelLogic
        on(it.getActiveQueryThreadsLogic()) doReturn activeThreadsLogic
    }
    private val shuffleGiphyListenerState = ShuffleGiphyListenerState(logic)

    @Test
    fun `when shuffling giphys and request succeeds, it should be upserted`() = runTest {
        val testMessage = randomMessage()

        shuffleGiphyListenerState.onShuffleGiphyResult(randomCID(), Result.Success(testMessage))

        verify(channelLogic).upsertMessage(testMessage.copy(syncStatus = SyncStatus.COMPLETED))
        verify(threadsLogic).upsertMessage(testMessage.copy(syncStatus = SyncStatus.COMPLETED))
    }

    @Test
    fun `when shuffling giphys and request fails, it should NOT be upserted`() = runTest {
        shuffleGiphyListenerState.onShuffleGiphyResult(randomCID(), Result.Failure(Error.GenericError("")))

        verify(channelLogic, never()).upsertMessage(any())
        verify(threadsLogic, never()).upsertMessage(any())
    }
}

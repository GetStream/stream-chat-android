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

package io.getstream.chat.android.state.plugin.listener.internal

import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomString
import io.getstream.chat.android.state.plugin.logic.channel.internal.ChannelLogic
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
internal class HideChannelListenerStateTest {

    private val channelLogic: ChannelLogic = mock()
    private val logicRegistry: LogicRegistry = mock {
        on(it.channel(any(), any())) doReturn channelLogic
    }
    private val hideChannelListenerState = HideChannelListenerState(logicRegistry)

    @Test
    fun `before the request is made, the channel should be set to hidden`() = runTest {
        hideChannelListenerState.onHideChannelRequest(randomString(), randomString(), randomBoolean())

        verify(channelLogic).setHidden(true)
    }

    @Test
    fun `after the request is made and it fails, the channel should be set to NOT hidden`() = runTest {
        hideChannelListenerState.onHideChannelResult(
            Result.Failure(Error.GenericError("")),
            randomString(),
            randomString(),
            randomBoolean(),
        )

        verify(channelLogic).setHidden(false)
    }

    @Test
    fun `after the request successful and clear history is true, history should be clean`() = runTest {
        hideChannelListenerState.onHideChannelResult(
            Result.Success(Unit),
            randomString(),
            randomString(),
            clearHistory = true,
        )

        verify(channelLogic, never()).setHidden(false)
        verify(channelLogic).run {
            hideMessagesBefore(any())
            removeMessagesBefore(any())
        }
    }
}

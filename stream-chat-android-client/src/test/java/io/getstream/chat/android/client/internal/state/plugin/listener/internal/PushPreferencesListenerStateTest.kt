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
import io.getstream.chat.android.client.internal.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.PushPreference
import io.getstream.chat.android.models.PushPreferenceLevel
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

internal class PushPreferencesListenerStateTest {

    private val channelStateLogic: ChannelStateLogic = mock()
    private val channelLogic: ChannelLogic = mock {
        on(it.stateLogic()) doReturn channelStateLogic
    }
    private val logicRegistry: LogicRegistry = mock {
        on(it.channel(any(), any())) doReturn channelLogic
    }
    private val sut: PushPreferencesListenerState = PushPreferencesListenerState(logicRegistry)

    @Test
    fun `onChannelPushPreferenceSet with success calls updateChannelData`() = runTest {
        // Given
        val cid = "messaging:channel123"
        val level = PushPreferenceLevel.all
        val preference = PushPreference(level = level, disabledUntil = null)
        doNothing().whenever(channelStateLogic).updateChannelData(any<(ChannelData?) -> ChannelData?>())

        // When
        sut.onChannelPushPreferenceSet(cid, level, Result.Success(preference))

        // Then
        verify(channelStateLogic, times(1)).updateChannelData(any<(ChannelData?) -> ChannelData?>())
    }

    @Test
    fun `onChannelPushPreferenceSet with failure does not call updateChannelData`() = runTest {
        // Given
        val cid = "messaging:channel123"
        val level = PushPreferenceLevel.all

        // When
        sut.onChannelPushPreferenceSet(cid, level, Result.Failure(Error.GenericError("error")))

        // Then
        verify(channelStateLogic, times(0)).updateChannelData(any<(ChannelData?) -> ChannelData?>())
    }

    @Test
    fun `onChannelPushNotificationsSnoozed with success calls updateChannelData`() = runTest {
        // Given
        val cid = "messaging:channel456"
        val until = Date()
        val preference = PushPreference(level = PushPreferenceLevel.none, disabledUntil = until)
        doNothing().whenever(channelStateLogic).updateChannelData(any<(ChannelData?) -> ChannelData?>())

        // When
        sut.onChannelPushNotificationsSnoozed(cid, until, Result.Success(preference))

        // Then
        verify(channelStateLogic, times(1)).updateChannelData(any<(ChannelData?) -> ChannelData?>())
    }

    @Test
    fun `onChannelPushNotificationsSnoozed with failure does not call updateChannelData`() = runTest {
        // Given
        val cid = "messaging:channel456"
        val until = Date()

        // When
        sut.onChannelPushNotificationsSnoozed(cid, until, Result.Failure(Error.GenericError("error")))

        // Then
        verify(channelStateLogic, times(0)).updateChannelData(any<(ChannelData?) -> ChannelData?>())
    }
}

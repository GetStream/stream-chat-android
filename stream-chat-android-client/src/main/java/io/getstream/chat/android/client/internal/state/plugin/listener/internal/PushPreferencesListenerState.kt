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

import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.internal.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.client.plugin.listeners.PushPreferencesListener
import io.getstream.chat.android.models.PushPreference
import io.getstream.chat.android.models.PushPreferenceLevel
import io.getstream.result.Result
import java.util.Date

/**
 * [PushPreferencesListener] implementation that updates the channel state when push preferences change.
 *
 * @param logic The [LogicRegistry] to access channel logic.
 */
internal class PushPreferencesListenerState(private val logic: LogicRegistry) : PushPreferencesListener {

    override suspend fun onChannelPushPreferenceSet(
        cid: String,
        level: PushPreferenceLevel,
        result: Result<PushPreference>,
    ) {
        result.onSuccess { pushPreference ->
            updateChannelPushPreference(cid, pushPreference)
        }
    }

    override suspend fun onChannelPushNotificationsSnoozed(cid: String, until: Date, result: Result<PushPreference>) {
        result.onSuccess { pushPreference ->
            updateChannelPushPreference(cid, pushPreference)
        }
    }

    private fun updateChannelPushPreference(cid: String, pushPreference: PushPreference) {
        val (type, id) = cid.cidToTypeAndId()
        logic.channel(type, id)
            .stateLogic
            .updateChannelData { data ->
                data?.copy(pushPreference = pushPreference)
            }
    }
}

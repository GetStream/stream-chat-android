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

package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.extensions.internal.toCid
import io.getstream.chat.android.client.plugin.listeners.HideChannelListener
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.internal.validateCidWithResult
import io.getstream.chat.android.client.utils.toUnitResult
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import java.util.Date

internal class HideChannelListenerState(private val logic: LogicRegistry) : HideChannelListener {

    override suspend fun onHideChannelPrecondition(
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ): Result<Unit> = validateCidWithResult(Pair(channelType, channelId).toCid()).toUnitResult()

    override suspend fun onHideChannelRequest(channelType: String, channelId: String, clearHistory: Boolean) {
        logic.channel(channelType, channelId).stateLogic().setHidden(true)
    }

    override suspend fun onHideChannelResult(
        result: Result<Unit>,
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ) {
        val channelStateLogic = logic.channel(channelType, channelId).stateLogic()
        if (result.isSuccess) {
            if (clearHistory) {
                val now = Date()
                channelStateLogic.run {
                    hideMessagesBefore(now)
                    removeMessagesBefore(now)
                }
            }
        } else {
            channelStateLogic.setHidden(false)
        }
    }
}

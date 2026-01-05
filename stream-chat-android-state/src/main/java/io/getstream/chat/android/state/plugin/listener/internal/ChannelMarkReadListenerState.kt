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

import io.getstream.chat.android.client.plugin.listeners.ChannelMarkReadListener
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.result.Error
import io.getstream.result.Result

/**
 * [ChannelMarkReadListener] implementation for [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 * Checks if the channel can be marked as read and marks it locally if needed.
 *
 * @param state [StateRegistry]
 */
internal class ChannelMarkReadListenerState(private val state: StateRegistry) : ChannelMarkReadListener {

    /**
     * Checks if the channel can be marked as read and marks it locally if needed.
     *
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     *
     * @return [Result] with information if channel should be marked as read.
     */
    override suspend fun onChannelMarkReadPrecondition(channelType: String, channelId: String): Result<Unit> {
        val shouldMarkRead = state.markChannelAsRead(
            channelType = channelType,
            channelId = channelId,
        )

        return if (shouldMarkRead) {
            Result.Success(Unit)
        } else {
            Result.Failure(Error.GenericError("Can not mark channel as read with channel id: $channelId"))
        }
    }
}

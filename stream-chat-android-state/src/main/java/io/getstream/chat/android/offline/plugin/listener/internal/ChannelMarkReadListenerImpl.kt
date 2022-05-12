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

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.experimental.plugin.listeners.ChannelMarkReadListener
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.utils.internal.ChannelMarkReadHelper

/**
 * [ChannelMarkReadListener] implementation for [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 * Checks if the channel can be marked as read and marks it locally if needed.
 *
 * @param channelMarkReadHelper [ChannelMarkReadHelper]
 */
internal class ChannelMarkReadListenerImpl(private val channelMarkReadHelper: ChannelMarkReadHelper) :
    ChannelMarkReadListener {

    /**
     * Checks if the channel can be marked as read and marks it locally if needed.
     *
     * @see [ChannelMarkReadHelper.markChannelReadLocallyIfNeeded]
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     *
     * @return [Result] with information if channel should be marked as read.
     */
    override suspend fun onChannelMarkReadPrecondition(channelType: String, channelId: String): Result<Unit> {
        val shouldMarkRead = channelMarkReadHelper.markChannelReadLocallyIfNeeded(
            channelType = channelType,
            channelId = channelId,
        )

        return if (shouldMarkRead) {
            Result.success(Unit)
        } else {
            Result.error(ChatError("Can not mark channel as read with channel id: $channelId"))
        }
    }
}

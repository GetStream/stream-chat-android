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

package io.getstream.chat.android.client.internal.offline.plugin.listener.internal

import io.getstream.chat.android.client.extensions.internal.toCid
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.plugin.listeners.HideChannelListener
import io.getstream.chat.android.client.utils.internal.validateCidWithResult
import io.getstream.result.Result
import java.util.Date

/**
 * Implementation of [HideChannelListener] that deals with database read and write.
 *
 * @param channelRepository [ChannelRepository]
 * @param messageRepository [MessageRepository]
 */
internal class HideChannelListenerDatabase(
    private val channelRepository: ChannelRepository,
    private val messageRepository: MessageRepository,
) : HideChannelListener {

    /**
     * Run precondition for the request. If it returns [Result.Success] then the request is run otherwise it returns
     * [Result.Failure] and no request is made.
     *
     * @param channelType Type of the requested channel.
     * @param channelId Id of the requested channel.
     * @param clearHistory Boolean, if you want to clear the history of this channel or not.
     *
     * @return [Result.Success] if precondition passes otherwise [Result.Failure]
     */
    override suspend fun onHideChannelPrecondition(
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ): Result<Unit> = validateCidWithResult(Pair(channelType, channelId).toCid()).toUnitResult()

    /**
     * Runs side effect before the request is launched.
     *
     * @param channelType Type of the requested channel.
     * @param channelId Id of the requested channel.
     * @param clearHistory Boolean, if you want to clear the history of this channel or not.
     */
    override suspend fun onHideChannelRequest(channelType: String, channelId: String, clearHistory: Boolean) {
        // Nothing to do.
    }

    /**
     * Runs this function on the result of the request.
     *
     * @param result Result of this request.
     * @param channelType Type of the requested channel.
     * @param channelId Id of the requested channel.
     * @param clearHistory Boolean, if you want to clear the history of this channel or not.
     */
    override suspend fun onHideChannelResult(
        result: Result<Unit>,
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ) {
        if (result is Result.Success) {
            val cid = Pair(channelType, channelId).toCid()

            if (clearHistory) {
                val now = Date()
                channelRepository.setHiddenForChannel(cid, true, now)
                messageRepository.deleteChannelMessagesBefore(cid, now)
            } else {
                channelRepository.setHiddenForChannel(cid, true)
            }
        }
    }
}

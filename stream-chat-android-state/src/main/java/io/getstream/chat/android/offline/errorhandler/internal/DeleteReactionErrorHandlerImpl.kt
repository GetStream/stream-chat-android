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

package io.getstream.chat.android.offline.errorhandler.internal

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.ReturnOnErrorCall
import io.getstream.chat.android.client.call.onErrorReturn
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.experimental.errorhandler.DeleteReactionErrorHandler
import io.getstream.chat.android.client.experimental.errorhandler.ErrorHandler
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.offline.plugin.state.global.GlobalState
import kotlinx.coroutines.CoroutineScope

/**
 * [DeleteReactionErrorHandler] implementation for [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 * Checks if the change was done offline and can be synced.
 *
 * @param scope [CoroutineScope]
 * @param logic [LogicRegistry]
 * @param globalState [GlobalState] provided by the [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 */
internal class DeleteReactionErrorHandlerImpl(
    private val scope: CoroutineScope,
    private val logic: LogicRegistry,
    private val globalState: GlobalState,
) : DeleteReactionErrorHandler {

    /**
     * Replaces the original response error if the user is offline, [cid] is specified and the message exists in the cache.
     * This means that the message was updated locally but the API request failed due to lack of connection.
     * The request will be synced once user's connection is recovered.
     *
     * @param originalCall The original call.
     * @param cid The full channel id, i.e. "messaging:123".
     * @param messageId The id of the message to which reaction belongs.
     *
     * @return result The original or offline related result.
     */
    override fun onDeleteReactionError(
        originalCall: Call<Message>,
        cid: String?,
        messageId: String,
    ): ReturnOnErrorCall<Message> {
        return originalCall.onErrorReturn(scope) { originalError ->
            if (cid == null || globalState.isOnline()) {
                Result.error<Message>(originalError)
            }
            val (channelType, channelId) = cid!!.cidToTypeAndId()
            val cachedMessage = logic.channel(channelType = channelType, channelId = channelId).getMessage(messageId)

            if (cachedMessage != null) {
                Result.success(cachedMessage)
            } else {
                Result.error(ChatError(message = "Local message was not found."))
            }
        }
    }

    override val name: String
        get() = "DeleteReactionErrorHandlerImpl"

    override val priority: Int
        get() = ErrorHandler.DEFAULT_PRIORITY
}

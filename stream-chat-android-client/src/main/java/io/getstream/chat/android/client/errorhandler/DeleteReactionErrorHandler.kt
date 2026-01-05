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

package io.getstream.chat.android.client.errorhandler

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Message
import io.getstream.result.Result
import io.getstream.result.call.Call
import io.getstream.result.call.ReturnOnErrorCall

/**
 * Error handler for [io.getstream.chat.android.client.ChatClient.deleteReaction] calls.
 */
@InternalStreamChatApi
public interface DeleteReactionErrorHandler {

    /**
     * Returns a [Result] from this side effect when original request is failed.
     *
     * @param originalCall The original call.
     * @param cid The full channel id, i.e. "messaging:123".
     * @param messageId The id of the message to which reaction belongs.
     *
     * @return result The replacement for the original result.
     */
    public fun onDeleteReactionError(
        originalCall: Call<Message>,
        cid: String?,
        messageId: String,
    ): ReturnOnErrorCall<Message>
}

internal fun Call<Message>.onMessageError(
    errorHandlers: List<DeleteReactionErrorHandler>,
    cid: String?,
    messageId: String,
): Call<Message> {
    return errorHandlers.fold(this) { messageCall, errorHandler ->
        errorHandler.onDeleteReactionError(messageCall, cid, messageId)
    }
}

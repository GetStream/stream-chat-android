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

package io.getstream.chat.android.client.errorhandler

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import io.getstream.result.Result
import io.getstream.result.call.Call
import io.getstream.result.call.ReturnOnErrorCall

/**
 * Error handler for [io.getstream.chat.android.client.ChatClient.sendReaction] calls.
 */
@InternalStreamChatApi
public interface SendReactionErrorHandler {

    /**
     * Returns a [Result] from this side effect when original request is failed.
     *
     * @param originalCall The original call.
     * @param reaction The [Reaction] to send.
     * @param enforceUnique Flag to determine whether the reaction should replace other ones added by the current user.
     * @param currentUser The currently logged in user.
     *
     * @return result The replacement for the original result.
     */
    public fun onSendReactionError(
        originalCall: Call<Reaction>,
        reaction: Reaction,
        enforceUnique: Boolean,
        skipPush: Boolean,
        currentUser: User,
    ): ReturnOnErrorCall<Reaction>
}

internal fun Call<Reaction>.onReactionError(
    errorHandlers: List<SendReactionErrorHandler>,
    reaction: Reaction,
    enforceUnique: Boolean,
    skipPush: Boolean,
    currentUser: User,
): Call<Reaction> {
    return errorHandlers.fold(this) { originalCall, errorHandler ->
        errorHandler.onSendReactionError(originalCall, reaction, enforceUnique, skipPush, currentUser)
    }
}

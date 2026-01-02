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

package io.getstream.chat.android.client.plugin.listeners

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.result.Result

/**
 * Listener for [io.getstream.chat.android.client.ChatClient.deleteReaction] calls.
 */
public interface DeleteReactionListener {

    /**
     * A method called before making an API call to delete the reaction.
     *
     * @param cid The full channel id, i.e. "messaging:123".
     * @param messageId The id of the message to which reaction belongs.
     * @param reactionType The type of reaction.
     * @param currentUser The currently logged in user.
     */
    public suspend fun onDeleteReactionRequest(
        cid: String?,
        messageId: String,
        reactionType: String,
        currentUser: User,
    )

    /**
     * A method called after receiving the response from the delete reaction call.
     *
     * @param cid The full channel id, i.e. "messaging:123".
     * @param messageId The id of the message to which reaction belongs.
     * @param reactionType The type of reaction.
     * @param currentUser The currently logged in user.
     * @param result The API call result.
     */
    public suspend fun onDeleteReactionResult(
        cid: String?,
        messageId: String,
        reactionType: String,
        currentUser: User,
        result: Result<Message>,
    )

    /**
     * Runs precondition check for [ChatClient.deleteReaction].
     * The request will be run if the method returns [Result.Success] and won't be made if it returns [Result.Failure].
     *
     * @param currentUser The currently logged in user.
     *
     * @return [Result.Success] if the precondition is fulfilled, [Result.Failure] otherwise.
     */
    public fun onDeleteReactionPrecondition(currentUser: User?): Result<Unit>
}

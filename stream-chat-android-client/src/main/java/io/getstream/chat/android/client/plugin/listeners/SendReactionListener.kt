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

package io.getstream.chat.android.client.plugin.listeners

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import io.getstream.result.Result

/**
 * Listener for [io.getstream.chat.android.client.ChatClient.sendReaction] calls.
 */
public interface SendReactionListener {

    /**
     * A method called before making an API call to send the reaction.
     *
     * @param cid The full channel id, i.e. "messaging:123".
     * @param reaction The [Reaction] to send.
     * @param enforceUnique Flag to determine whether the reaction should replace other ones added by the current user.
     * @param currentUser The currently logged in user.
     */
    public suspend fun onSendReactionRequest(
        cid: String?,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
    )

    /**
     * A method called after receiving the response from the send reaction call.
     *
     * @param cid The full channel id, i.e. "messaging:123".
     * @param reaction The [Reaction] to send.
     * @param enforceUnique Flag to determine whether the reaction should replace other ones added by the current user.
     * @param currentUser The currently logged in user.
     *
     * @param result The API call result.
     */
    public suspend fun onSendReactionResult(
        cid: String?,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
        result: Result<Reaction>,
    )

    /**
     * Runs precondition check for [ChatClient.sendReaction].
     * The request will be run if the method returns [Result.Success] and won't be made if it returns [Result.Failure].
     *
     * @param currentUser The currently logged in user.
     * @param reaction The [Reaction] to send.
     *
     * @return [Result.Success] if the precondition is fulfilled, [Result.Failure] otherwise.
     */
    public fun onSendReactionPrecondition(currentUser: User?, reaction: Reaction): Result<Unit>
}

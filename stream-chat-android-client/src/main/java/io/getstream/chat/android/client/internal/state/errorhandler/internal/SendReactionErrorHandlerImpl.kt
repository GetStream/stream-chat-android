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

package io.getstream.chat.android.client.internal.state.errorhandler.internal

import io.getstream.chat.android.client.errorhandler.SendReactionErrorHandler
import io.getstream.chat.android.client.extensions.internal.enrichWithDataBeforeSending
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import io.getstream.result.Result
import io.getstream.result.call.Call
import io.getstream.result.call.ReturnOnErrorCall
import io.getstream.result.call.onErrorReturn
import kotlinx.coroutines.CoroutineScope

/**
 * [SendReactionErrorHandler] implementation for
 * [io.getstream.chat.android.client.internal.state.plugin.internal.StatePlugin].
 * Returns a [Reaction] instance enriched with user [Reaction.syncStatus] if reaction was send offline and can be
 * synced.
 *
 * @param scope [CoroutineScope]
 * @param clientState [ClientState] provided by the
 * [io.getstream.chat.android.client.internal.state.plugin.internal.StatePlugin].
 */
internal class SendReactionErrorHandlerImpl(
    private val scope: CoroutineScope,
    private val clientState: ClientState,
) : SendReactionErrorHandler {

    /**
     * Replaces the original response error if the user is offline.
     * This means that the reaction was added locally but the API request failed due to lack of connection.
     * The request will be synced once user's connection is recovered.
     *
     * @param originalCall The original call.
     * @param reaction The [Reaction] to send.
     * @param enforceUnique Flag to determine whether the reaction should replace other ones added by the current user.
     * @param skipPush Flag to determine if push notification should be sent for this reaction.
     * @param currentUser The currently logged in user.
     *
     * @return result The original or offline related result.
     */
    override fun onSendReactionError(
        originalCall: Call<Reaction>,
        reaction: Reaction,
        enforceUnique: Boolean,
        skipPush: Boolean,
        currentUser: User,
    ): ReturnOnErrorCall<Reaction> {
        return originalCall.onErrorReturn(scope) { originalError ->
            if (clientState.isOnline) {
                Result.Failure(originalError)
            } else {
                Result.Success(
                    reaction.enrichWithDataBeforeSending(
                        currentUser = currentUser,
                        isOnline = clientState.isOnline,
                        enforceUnique = enforceUnique,
                        skipPush = skipPush,
                    ),
                )
            }
        }
    }
}

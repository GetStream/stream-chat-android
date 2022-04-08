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
import io.getstream.chat.android.client.experimental.errorhandler.ErrorHandler
import io.getstream.chat.android.client.experimental.errorhandler.SendReactionErrorHandler
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.extensions.internal.enrichWithDataBeforeSending
import io.getstream.chat.android.offline.plugin.state.global.GlobalState
import kotlinx.coroutines.CoroutineScope

/**
 * [SendReactionErrorHandler] implementation for [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 * Returns a [Reaction] instance enriched with user [Reaction.syncStatus] if reaction was send offline and can be synced.
 *
 * @param scope [CoroutineScope]
 * @param globalState [GlobalState] provided by the [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 */
internal class SendReactionErrorHandlerImpl(private val scope: CoroutineScope, private val globalState: GlobalState) :
    SendReactionErrorHandler {

    /**
     * Replaces the original response error if the user is offline.
     * This means that the reaction was added locally but the API request failed due to lack of connection.
     * The request will be synced once user's connection is recovered.
     *
     * @param originalCall The original call.
     * @param reaction The [Reaction] to send.
     * @param enforceUnique Flag to determine whether the reaction should replace other ones added by the current user.
     * @param currentUser The currently logged in user.
     *
     * @return result The original or offline related result.
     */
    override fun onSendReactionError(
        originalCall: Call<Reaction>,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
    ): ReturnOnErrorCall<Reaction> {
        return originalCall.onErrorReturn(scope) { originalError ->
            if (globalState.isOnline()) {
                Result.error(originalError)
            } else {
                Result.success(
                    reaction.enrichWithDataBeforeSending(
                        currentUser = currentUser,
                        isOnline = globalState.isOnline(),
                        enforceUnique = enforceUnique,
                    ),
                )
            }
        }
    }

    override val name: String
        get() = "SendReactionErrorHandlerImpl"

    override val priority: Int
        get() = ErrorHandler.DEFAULT_PRIORITY
}

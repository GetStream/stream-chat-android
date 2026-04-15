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

package io.getstream.chat.android.client.internal.state.plugin.listener.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.internal.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.client.plugin.listeners.DeleteChannelListener
import io.getstream.chat.android.client.plugin.listeners.DeleteReactionListener
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.User
import io.getstream.result.Error
import io.getstream.result.Result

/**
 * [DeleteReactionListener] implementation for [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 * Handles adding reaction to the state of the SDK.
 *
 * @param logic [LogicRegistry]
 * @param clientState [ClientState]
 */
internal class DeleteChannelListenerState(
    private val logic: LogicRegistry,
    private val clientState: ClientState,
) : DeleteChannelListener {

    /**
     * A method called before making an API call to delete a channel.
     *
     * @param currentUser The currently logged in user.
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     */
    override suspend fun onDeleteChannelRequest(
        currentUser: User?,
        channelType: String,
        channelId: String,
    ) {
        /* No-op */
    }

    /**
     * A method called after receiving the response from the delete channel call.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     * @param result The API call result.
     */
    override suspend fun onDeleteChannelResult(
        channelType: String,
        channelId: String,
        result: Result<Channel>,
    ) {
        // Nothing to be done. The Reaction is deleted optimistically.
    }

    /**
     * Runs precondition check for [ChatClient.deleteChannel].
     * The request will be run if the method returns [Result.Success] and won't be made if it returns [Result.Failure].
     *
     * @param currentUser The currently logged in user.
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     *
     * @return [Result.Success] if the precondition is fulfilled, [Result.Failure] otherwise.
     */
    override suspend fun onDeleteChannelPrecondition(
        currentUser: User?,
        channelType: String,
        channelId: String,
    ): Result<Unit> {
        return if (currentUser != null) {
            Result.Success(Unit)
        } else {
            Result.Failure(Error.GenericError(message = "Current user is null!"))
        }
    }
}

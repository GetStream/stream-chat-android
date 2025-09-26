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

package io.getstream.chat.android.state.plugin.listener.internal

import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.internal.removeMyReaction
import io.getstream.chat.android.client.plugin.listeners.DeleteReactionListener
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.result.Error
import io.getstream.result.Result
import java.util.Date

/**
 * [DeleteReactionListener] implementation for [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 * Handles adding reaction to the state of the SDK.
 *
 * @param logic [LogicRegistry]
 * @param clientState [ClientState]
 */
internal class DeleteReactionListenerState(
    private val logic: LogicRegistry,
    private val clientState: ClientState,
) : DeleteReactionListener {

    /**
     * A method called before making an API call to delete the reaction.
     * Creates the reaction based on [messageId] and [reactionType], updates reactions' database
     * and runs optimistic update if [cid] is specified.
     *
     * @param cid The full channel id, i.e. "messaging:123".
     * @param messageId The id of the message to which reaction belongs.
     * @param reactionType The type of reaction.
     * @param currentUser The currently logged in user.
     */
    override suspend fun onDeleteReactionRequest(
        cid: String?,
        messageId: String,
        reactionType: String,
        currentUser: User,
    ) {
        val reaction = Reaction(
            messageId = messageId,
            type = reactionType,
            user = currentUser,
            userId = currentUser.id,
            syncStatus = if (clientState.isNetworkAvailable) SyncStatus.IN_PROGRESS else SyncStatus.SYNC_NEEDED,
            deletedAt = Date(),
        )

        val channelLogic = cid?.cidToTypeAndId()?.let { (type, id) -> logic.channel(type, id) }
            ?: logic.channelFromMessageId(reaction.messageId)
        val cachedChannelMessage = channelLogic?.getMessage(reaction.messageId)
            ?.removeMyReaction(reaction = reaction)
        cachedChannelMessage?.let(channelLogic::upsertMessage)

        logic.getActiveQueryThreadsLogic().forEach { logic ->
            val cachedThreadMessage = logic.getMessage(reaction.messageId)
                ?.removeMyReaction(reaction = reaction)
            cachedThreadMessage?.let(logic::upsertMessage)
        }

        val threadLogic = logic.threadFromMessageId(messageId)
        val cachedThreadMessage = threadLogic?.getMessage(reaction.messageId)
            ?.removeMyReaction(reaction = reaction)
        cachedThreadMessage?.let(threadLogic::upsertMessage)
    }

    /**
     * A method called after receiving the response from the delete reaction call.
     * It doesn't have any behaviour in this implementation, because the reactions were deleted optimistically.
     *
     * @param cid The full channel id, i.e. "messaging:123".
     * @param messageId The id of the message to which reaction belongs.
     * @param reactionType The type of reaction.
     * @param currentUser The currently logged in user.
     * @param result The API call result.
     */
    override suspend fun onDeleteReactionResult(
        cid: String?,
        messageId: String,
        reactionType: String,
        currentUser: User,
        result: Result<Message>,
    ) {
        // Nothing to be done. The Reaction is deleted optimistically.
    }

    /**
     * Checks if current user is set.
     *
     * @param currentUser The currently logged in user.
     */
    override fun onDeleteReactionPrecondition(currentUser: User?): Result<Unit> {
        return if (currentUser != null) {
            Result.Success(Unit)
        } else {
            Result.Failure(Error.GenericError(message = "Current user is null!"))
        }
    }
}

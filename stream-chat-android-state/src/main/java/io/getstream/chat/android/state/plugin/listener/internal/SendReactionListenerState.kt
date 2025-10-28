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

import io.getstream.chat.android.client.errors.isPermanent
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.internal.addMyReaction
import io.getstream.chat.android.client.extensions.internal.enrichWithDataBeforeSending
import io.getstream.chat.android.client.plugin.listeners.SendReactionListener
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.result.Error
import io.getstream.result.Result

/**
 * State implementation for SendReactionListener. It updates the state accordingly and does the optimistic UI update.
 *
 * @param logic [LogicRegistry] Handles the state of channels.
 * @param clientState [ClientState] Check the state of the SDK.
 */
internal class SendReactionListenerState(
    private val logic: LogicRegistry,
    private val clientState: ClientState,
) : SendReactionListener {

    /**
     * A method called before making an API call to send the reaction.
     * runs optimistic update if the message and channel can be found in memory.
     *
     * @param cid The full channel id, i.e. "messaging:123".
     * @param reaction The [Reaction] to send.
     * @param enforceUnique Flag to determine whether the reaction should replace other ones added by the current user.
     * @param currentUser The currently logged in user.
     */
    @Deprecated(
        "This method will be removed in the future. " +
            "Use SendReactionListener#onSendReactionRequest(cid, reaction, enforceUnique, skipPush, currentUser) " +
            "instead. For backwards compatibility, this method is still called internally by the new, non-deprecated " +
            "method.",
    )
    override suspend fun onSendReactionRequest(
        cid: String?,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
    ) {
        saveReactionInState(cid, reaction, enforceUnique, null, currentUser)
    }

    override suspend fun onSendReactionRequest(
        cid: String?,
        reaction: Reaction,
        enforceUnique: Boolean,
        skipPush: Boolean,
        currentUser: User,
    ) {
        saveReactionInState(cid, reaction, enforceUnique, skipPush, currentUser)
    }

    override suspend fun onSendReactionResult(
        cid: String?,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
        result: Result<Reaction>,
    ) {
        val channelLogic = cid?.cidToTypeAndId()?.let { (type, id) -> logic.channel(type, id) }
            ?: logic.channelFromMessageId(reaction.messageId)
        channelLogic?.getMessage(reaction.messageId)?.let { message ->
            channelLogic.upsertMessage(
                message.updateReactionSyncStatus(
                    originReaction = reaction,
                    result = result,
                ),
            )
        }

        logic.getActiveQueryThreadsLogic().forEach { logic ->
            val cachedThreadsMessage = logic.getMessage(reaction.messageId)
                ?.updateReactionSyncStatus(originReaction = reaction, result = result)
            cachedThreadsMessage?.let(logic::upsertMessage)
        }

        val threadLogic = logic.threadFromMessageId(reaction.messageId)
        threadLogic?.getMessage(reaction.messageId)?.let { message ->
            threadLogic.upsertMessage(
                message.updateReactionSyncStatus(
                    originReaction = reaction,
                    result = result,
                ),
            )
        }
    }

    /**
     * Checks if current user is set and reaction contains required data.
     *
     * @param currentUser The currently logged in user.
     * @param reaction The [Reaction] to send.
     */
    @Deprecated(
        "This method will be removed in the future. " +
            "Use SendReactionListener#onSendReactionPrecondition(cid, currentUser, reaction) instead." +
            "For backwards compatibility, this method is still called internally by the new, non-deprecated method.",
    )
    override suspend fun onSendReactionPrecondition(currentUser: User?, reaction: Reaction): Result<Unit> = when {
        currentUser == null -> {
            Result.Failure(Error.GenericError(message = "Current user is null!"))
        }
        reaction.messageId.isBlank() || reaction.type.isBlank() -> {
            Result.Failure(
                Error.GenericError(
                    message = "Reaction::messageId and Reaction::type cannot be empty!",
                ),
            )
        }
        else -> {
            Result.Success(Unit)
        }
    }

    /**
     * Checks if current user is set and reaction contains required data.
     *
     * @param cid The full channel id, i.e. "messaging:123".
     * @param currentUser The currently logged in user.
     * @param reaction The [Reaction] to send.
     */
    override suspend fun onSendReactionPrecondition(
        cid: String?,
        currentUser: User?,
        reaction: Reaction,
    ): Result<Unit> = when {
        currentUser == null -> {
            Result.Failure(Error.GenericError(message = "Current user is null!"))
        }
        reaction.messageId.isBlank() || reaction.type.isBlank() -> {
            Result.Failure(
                Error.GenericError(
                    message = "Reaction::messageId and Reaction::type cannot be empty!",
                ),
            )
        }
        else -> {
            Result.Success(Unit)
        }
    }

    private fun saveReactionInState(
        cid: String?,
        reaction: Reaction,
        enforceUnique: Boolean,
        skipPush: Boolean?,
        currentUser: User,
    ) {
        val reactionToSend = reaction.enrichWithDataBeforeSending(
            currentUser = currentUser,
            isOnline = clientState.isNetworkAvailable,
            enforceUnique = enforceUnique,
            skipPush = skipPush ?: false,
        )

        val channelLogic = cid?.cidToTypeAndId()?.let { (type, id) -> logic.channel(type, id) }
            ?: logic.channelFromMessageId(reaction.messageId)
        val cachedChannelMessage = channelLogic?.getMessage(reaction.messageId)
            ?.addMyReaction(reaction = reactionToSend, enforceUnique = enforceUnique)
        cachedChannelMessage?.let(channelLogic::upsertMessage)

        logic.getActiveQueryThreadsLogic().forEach { logic ->
            val cachedMessage = logic.getMessage(reaction.messageId)
                ?.addMyReaction(reaction = reactionToSend, enforceUnique = enforceUnique)
            cachedMessage?.let(logic::upsertMessage)
        }

        val threadLogic = logic.threadFromMessageId(reaction.messageId)
        val cachedThreadMessage = threadLogic?.getMessage(reaction.messageId)
            ?.addMyReaction(reaction = reactionToSend, enforceUnique = enforceUnique)
        cachedThreadMessage?.let(threadLogic::upsertMessage)
    }

    private fun Message.updateReactionSyncStatus(originReaction: Reaction, result: Result<*>): Message = this.copy(
        ownReactions = ownReactions
            .map { ownReaction ->
                when (ownReaction.id) {
                    originReaction.id -> ownReaction.updateSyncStatus(result)
                    else -> ownReaction
                }
            },
        latestReactions = latestReactions
            .map { latestReaction ->
                when (latestReaction.id) {
                    originReaction.id -> latestReaction.updateSyncStatus(result)
                    else -> latestReaction
                }
            },
    )

    private fun Reaction.updateSyncStatus(result: Result<*>): Reaction = this.copy(
        syncStatus = when (result) {
            is Result.Success -> SyncStatus.COMPLETED
            is Result.Failure -> when {
                result.value.isPermanent() -> SyncStatus.FAILED_PERMANENTLY
                else -> SyncStatus.SYNC_NEEDED
            }
        },
    )
}

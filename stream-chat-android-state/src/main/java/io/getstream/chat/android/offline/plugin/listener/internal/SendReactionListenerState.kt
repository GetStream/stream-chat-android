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

package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.internal.addMyReaction
import io.getstream.chat.android.client.extensions.internal.enrichWithDataBeforeSending
import io.getstream.chat.android.client.extensions.isPermanent
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.plugin.listeners.SendReactionListener
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry

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
    override suspend fun onSendReactionRequest(
        cid: String?,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
    ) {
        val reactionToSend = reaction.enrichWithDataBeforeSending(
            currentUser = currentUser,
            isOnline = clientState.isNetworkAvailable,
            enforceUnique = enforceUnique,
        )

        val channelLogic = cid?.cidToTypeAndId()?.let { (type, id) -> logic.channel(type, id) }
            ?: logic.channelFromMessageId(reaction.messageId)
        val cachedChannelMessage = channelLogic?.getMessage(reaction.messageId)
            ?.apply {
                addMyReaction(reaction = reactionToSend, enforceUnique = enforceUnique)
            }
        cachedChannelMessage?.let(channelLogic::upsertMessage)

        val threadLogic = logic.threadFromMessageId(reaction.messageId)
        val cachedThreadMessage = threadLogic?.getMessage(reaction.messageId)
            ?.apply {
                addMyReaction(reaction = reactionToSend, enforceUnique = enforceUnique)
            }
        cachedThreadMessage?.let(threadLogic::upsertMessage)
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
            message.ownReactions
                .find { ownReaction -> ownReaction == reaction }
                ?.updateSyncStatus(result)

            message.latestReactions
                .find { ownReaction -> ownReaction == reaction }
                ?.updateSyncStatus(result)

            channelLogic.upsertMessage(message)
        }

        val threadLogic = logic.threadFromMessageId(reaction.messageId)
        threadLogic?.getMessage(reaction.messageId)?.let { message ->
            message.ownReactions
                .find { ownReaction -> ownReaction == reaction }
                ?.updateSyncStatus(result)

            message.latestReactions
                .find { ownReaction -> ownReaction == reaction }
                ?.updateSyncStatus(result)

            threadLogic.upsertMessage(message)
        }
    }

    /**
     * Checks if current user is set and reaction contains required data.
     *
     * @param currentUser The currently logged in user.
     * @param reaction The [Reaction] to send.
     */
    override fun onSendReactionPrecondition(currentUser: User?, reaction: Reaction): Result<Unit> {
        return when {
            currentUser == null -> {
                Result.error(ChatError(message = "Current user is null!"))
            }
            reaction.messageId.isBlank() || reaction.type.isBlank() -> {
                Result.error(ChatError(message = "Reaction::messageId and Reaction::type cannot be empty!"))
            }
            else -> {
                Result.success(Unit)
            }
        }
    }

    private fun Reaction.updateSyncStatus(result: Result<*>) {
        if (result.isSuccess) {
            syncStatus = SyncStatus.COMPLETED
        } else {
            updateFailedReactionSyncStatus(result.error())
        }
    }

    private fun Reaction.updateFailedReactionSyncStatus(chatError: ChatError) {
        syncStatus = if (chatError.isPermanent()) {
            SyncStatus.FAILED_PERMANENTLY
        } else {
            SyncStatus.SYNC_NEEDED
        }
    }
}

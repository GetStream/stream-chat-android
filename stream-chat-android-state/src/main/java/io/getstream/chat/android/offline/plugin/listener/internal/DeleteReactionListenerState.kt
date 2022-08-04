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
import io.getstream.chat.android.client.extensions.internal.removeMyReaction
import io.getstream.chat.android.client.extensions.isPermanent
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.plugin.listeners.DeleteReactionListener
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
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

        val channelLogic = logic.channelFromMessageId(reaction.messageId)
        val cachedMessage = channelLogic?.getMessage(reaction.messageId)
            ?.apply {
                removeMyReaction(reaction = reaction)
            }

        if (cid != null && cachedMessage != null) {
            doOptimisticMessageUpdate(cid = cid, message = cachedMessage)
        }
    }

    /**
     * Updates [io.getstream.chat.android.offline.plugin.state.channel.internal.ChannelMutableState.messages].
     *
     * @param cid The full channel id, i.e. "messaging:123".
     * @param message The [Message] to update.
     */
    private fun doOptimisticMessageUpdate(cid: String, message: Message) {
        val (channelType, channelId) = cid.cidToTypeAndId()
        logic.channel(channelType = channelType, channelId = channelId).upsertMessage(message)
    }

    /**
     * A method called after receiving the response from the delete reaction call.
     * Updates reaction's sync status stored in the database based on API result.
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
        val channelLogic = logic.channelFromMessageId(messageId)
        channelLogic?.getMessage(messageId)?.let { message ->
            message.ownReactions
                .find { ownReaction ->
                    ownReaction.run {
                        this.type == reactionType && this.messageId == messageId && this.user == currentUser
                    }
                }?.updateSyncStatus(result)

            message.latestReactions
                .find { latestReaction ->
                    latestReaction.run {
                        this.type == reactionType && this.messageId == messageId && this.user == currentUser
                    }
                }?.updateSyncStatus(result)

            channelLogic.upsertMessage(message)
        }
    }

    /**
     * Checks if current user is set.
     *
     * @param currentUser The currently logged in user.
     */
    override fun onDeleteReactionPrecondition(currentUser: User?): Result<Unit> {
        return if (currentUser != null) {
            Result.success(Unit)
        } else {
            Result.error(ChatError(message = "Current user is null!"))
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

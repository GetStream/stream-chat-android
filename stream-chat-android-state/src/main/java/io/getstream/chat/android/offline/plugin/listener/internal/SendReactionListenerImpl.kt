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
import io.getstream.chat.android.client.extensions.internal.addMyReaction
import io.getstream.chat.android.client.extensions.internal.enrichWithDataBeforeSending
import io.getstream.chat.android.client.extensions.internal.updateSyncStatus
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.plugin.listeners.SendReactionListener
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import java.util.Date

/**
 * [SendReactionListener] implementation for [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 * Handles adding reaction offline, updates the database and does the optimistic UI update.
 *
 * @param logic [LogicRegistry]
 * @param clientState [ClientState] provided by the [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 * @param repos [RepositoryFacade] to cache intermediate data and final result.
 */
internal class SendReactionListenerImpl(
    private val logic: LogicRegistry,
    private val clientState: ClientState,
    private val repos: RepositoryFacade,
) : SendReactionListener {

    /**
     * A method called before making an API call to send the reaction.
     * Fills the reaction with necessary data, updates reactions' database
     * and runs optimistic update if [cid] is specified.
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
            isOnline = clientState.isOnline,
            enforceUnique = enforceUnique,
        )

        // Update local storage
        if (enforceUnique) {
            // remove all user's reactions to the message
            repos.updateReactionsForMessageByDeletedDate(
                userId = currentUser.id,
                messageId = reactionToSend.messageId,
                deletedAt = Date(),
            )
        }
        repos.insertReaction(reaction = reactionToSend)

        repos.selectMessage(messageId = reactionToSend.messageId)?.copy()?.let { cachedMessage ->
            cachedMessage.addMyReaction(reaction = reactionToSend, enforceUnique = enforceUnique)
            repos.insertMessage(cachedMessage)

            if (cid != null) {
                doOptimisticMessageUpdate(message = cachedMessage)
            }
        }
    }

    /**
     * Updates [io.getstream.chat.android.offline.plugin.state.channel.internal.ChannelMutableState.messages].
     *
     * @param cid The full channel id, i.e. "messaging:123".
     * @param message The [Message] to update.
     */
    private fun doOptimisticMessageUpdate(message: Message) {
        logic.channelFromMessage(message)?.upsertMessages(listOf(message))
        logic.threadFromMessage(message)?.upsertMessages(listOf(message))
    }

    /**
     * A method called after receiving the response from the send reaction call.
     * Updates reaction's sync status stored in the database based on API result.
     *
     * @param cid The full channel id, i.e. "messaging:123".
     * @param reaction The [Reaction] to send.
     * @param enforceUnique Flag to determine whether the reaction should replace other ones added by the current user.
     * @param currentUser The currently logged in user.
     * @param result The API call result.
     */
    override suspend fun onSendReactionResult(
        cid: String?,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
        result: Result<Reaction>,
    ) {
        repos.selectUserReactionToMessage(
            reactionType = reaction.type,
            messageId = reaction.messageId,
            userId = currentUser.id,
        )
            ?.let { cachedReaction ->
                repos.insertReaction(cachedReaction.updateSyncStatus(result))
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
}

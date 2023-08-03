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

import io.getstream.chat.android.client.extensions.internal.addMyReaction
import io.getstream.chat.android.client.extensions.internal.enrichWithDataBeforeSending
import io.getstream.chat.android.client.extensions.internal.updateSyncStatus
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.persistance.repository.ReactionRepository
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.client.plugin.listeners.SendReactionListener
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import io.getstream.result.Error
import io.getstream.result.Result
import java.util.Date

/**
 * [SendReactionListener] implementation for [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 * Handles adding reaction offline, updates the database.
 *
 * @param clientState [ClientState] provided by the [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 * @param reactionsRepository [ReactionRepository] to cache intermediate data and final result related to reactions.
 * @param messageRepository [MessageRepository] to cache intermediate data and final result related to messages.
 * @param userRepository [UserRepository] to cache intermediate data and final result related to User.
 */
internal class SendReactionListenerDatabase(
    private val clientState: ClientState,
    private val reactionsRepository: ReactionRepository,
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
) : SendReactionListener {

    /**
     * A method called before making an API call to send the reaction.
     * Fills the reaction with necessary data, updates reactions' database
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

        // Update local storage
        if (enforceUnique) {
            // remove all user's reactions to the message
            reactionsRepository.updateReactionsForMessageByDeletedDate(
                userId = currentUser.id,
                messageId = reactionToSend.messageId,
                deletedAt = Date(),
            )
        }

        reaction.user?.let { user -> userRepository.insertUser(user) }
        reactionsRepository.insertReaction(reaction = reactionToSend)

        messageRepository.selectMessage(messageId = reactionToSend.messageId)?.copy()?.let { cachedMessage ->
            messageRepository.insertMessage(
                cachedMessage.addMyReaction(reaction = reactionToSend, enforceUnique = enforceUnique),
            )
        }
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
        reactionsRepository.selectUserReactionToMessage(
            reactionType = reaction.type,
            messageId = reaction.messageId,
            userId = currentUser.id,
        )?.let { cachedReaction ->
            reactionsRepository.insertReaction(cachedReaction.updateSyncStatus(result))
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
                Result.Failure(Error.GenericError(message = "Current user is null!"))
            }
            reaction.messageId.isBlank() || reaction.type.isBlank() -> {
                Result.Failure(Error.GenericError("Reaction::messageId and Reaction::type cannot be empty!"))
            }
            else -> {
                Result.Success(Unit)
            }
        }
    }
}

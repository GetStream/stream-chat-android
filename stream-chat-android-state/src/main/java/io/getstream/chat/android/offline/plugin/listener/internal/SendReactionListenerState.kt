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
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.plugin.listeners.SendReactionListener
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry

internal class SendReactionListenerState(
    private val logic: LogicRegistry,
    private val clientState: ClientState,
) : SendReactionListener {

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

        val channelLogic = logic.channelFromMessageId(reaction.messageId)
        val cachedMessage = channelLogic?.getMessage(reaction.messageId)
            ?.apply {
                addMyReaction(reaction = reactionToSend, enforceUnique = enforceUnique)
            }

        cachedMessage?.let(channelLogic::upsertMessage)
    }

    override suspend fun onSendReactionResult(
        cid: String?,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
        result: Result<Reaction>,
    ) {
        // Nothing to do here.
    }

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
